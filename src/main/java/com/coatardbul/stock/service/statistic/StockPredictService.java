package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.mapper.StockTemplatePredictMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockPredictDto;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockTemplatePredict;
import com.coatardbul.stock.model.feign.StockTemplateQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/4
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockPredictService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;
    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockStaticTemplateMapper stockStaticTemplateMapper;
    @Autowired
    StockMinuterEmotionMapper stockMinuterEmotionMapper;
    @Autowired
    StockVerifyService stockVerifyService;
    @Autowired
    StockTemplatePredictMapper stockTemplatePredictMapper;
    @Autowired
    StockParseAndConvertService stockParseAndConvertService;

    public void execute(StockPredictDto dto) {
        if (!StringUtils.isNotBlank(dto.getId())) {
            throw new BusinessException("id不能为空");
        }
        Assert.notNull(dto.getHoleDay(), "天数不不能为空");
        //获取时间区间
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());

        for (String dateStr : dateIntervalList) {
            Constant.emotionByDateRangeThreadPool.submit(() -> {
                //将持有天数转换成脚本
                String saleQueryScript = getSaleQueryScript(dto.getHoleDay());
                //买入的查询语句
                String buyQueryInfo = getBuyQueryInfo(dto, dateStr);
                //卖出的查询语句
                String saleQueryInfo = getSaleQueryInfo(dto, saleQueryScript, dateStr);
                //和id查询到的问句拼接
                String finalQueryInfo = buyQueryInfo + saleQueryInfo;
                //策略查询
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setQueryStr(finalQueryInfo);
                try {
                    StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    //动态结果解析
                    if (strategy != null) {
                        parseStrategyResult(dto, strategy, dateStr);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            });
        }


    }

    /**
     * 需要拼接 2022年03月04日13点30分价格
     * 则结果为：{{afterDay4}}{{time}}截个
     *
     * @param holeDay 持有天数
     * @return
     */
    private String getSaleQueryScript(Integer holeDay) {
        StringBuffer sb = new StringBuffer();
        sb.append("{{afterDay").append(holeDay).append("}}").append("{{time}}").append("价格");
        return sb.toString();
    }

    private String getSaleQueryInfo(StockPredictDto dto, String saleQueryScript, String dateStr) {
        StockTemplateQueryDTO stockTemplateQueryDto = new StockTemplateQueryDTO();
        stockTemplateQueryDto.setDateStr(dateStr);
        stockTemplateQueryDto.setTimeStr(dto.getSaleTime());
        stockTemplateQueryDto.setStockScript(saleQueryScript);
        CommonResult<String> riverServerFeignResult = riverServerFeign.getQuery(stockTemplateQueryDto);
        if (riverServerFeignResult != null) {
            return riverServerFeignResult.getData();
        } else {
            return null;
        }
    }

    private String getBuyQueryInfo(StockPredictDto dto, String dateStr) {
        StockTemplateQueryDTO stockTemplateQueryDto = new StockTemplateQueryDTO();
        stockTemplateQueryDto.setId(dto.getId());
        stockTemplateQueryDto.setDateStr(dateStr);
        stockTemplateQueryDto.setTimeStr(dto.getBuyTime());
        CommonResult<String> riverServerFeignResult = riverServerFeign.getQuery(stockTemplateQueryDto);
        if (riverServerFeignResult != null) {
            return riverServerFeignResult.getData();
        } else {
            return null;
        }
    }


    private void parseStrategyResult(StockPredictDto dto, StrategyBO strategy, String dateStr) {
        JSONArray data = strategy.getData();
        String dateFormat = dateStr.replace("-", "");
        String saleDateFormat = riverRemoteService.getSpecialDay(dateStr, dto.getHoleDay()).replace("-", "");
        stockTemplatePredictMapper.deleteByDateAndTempatedId(dateStr, dto.getId());
        for (Object jo : data) {
            StockTemplatePredict addInfo = new StockTemplatePredict();
            addInfo.setId(baseServerFeign.getSnowflakeId());
            addInfo.setDate(dateStr);
            addInfo.setTemplatedId(dto.getId());
            addInfo.setHoldDay(dto.getHoleDay());
            addInfo.setSaleTime(dto.getSaleTime());
            addInfo.setBuyTime(dto.getBuyTime());
            addInfo.setCode((String) ((JSONObject) jo).get("code"));
            addInfo.setName((String) ((JSONObject) jo).get("股票简称"));
            Set<String> keys = ((JSONObject) jo).keySet();
            for (String key : keys) {
                if (key.contains(saleDateFormat) && key.contains("分时收盘价:不复权")) {
                    if (StringUtils.isNotBlank(dto.getSaleTime())) {
                        if (key.contains(dto.getSaleTime())) {
                            addInfo.setSalePrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
                        }
                    } else {
                        addInfo.setSalePrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
                    }
                }
                if (key.contains("市值")) {
                    addInfo.setMarketValue(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
                }
                if (key.contains(dateFormat) && key.contains("收盘价:不复权")) {
                    addInfo.setBuyPrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
                }
            }
            addInfo.setDetail(jo.toString());
            stockTemplatePredictMapper.insertSelective(addInfo);
        }

    }


    public List<StockTemplatePredict> getAll(StockPredictDto dto) {

        List<StockTemplatePredict> stockTemplatePredicts = stockTemplatePredictMapper.selectAllByDateBetweenEqualAndTemplatedIdAndHoldDay(dto.getBeginDate(), dto.getEndDate(), dto.getId(), dto.getHoleDay());

        if (stockTemplatePredicts != null && stockTemplatePredicts.size() > 0) {
            Map<String, String> templateIdMap = stockTemplatePredicts.stream().collect(Collectors.toMap(StockTemplatePredict::getTemplatedId, StockTemplatePredict::getTemplatedId, (o1, o2) -> o1));
            for (Map.Entry<String, String> entry : templateIdMap.entrySet()) {
                String templateName = riverRemoteService.getTemplateNameById(entry.getKey());
                entry.setValue(templateName);
            }
            stockTemplatePredicts = stockTemplatePredicts.stream().map(o1 -> convert(o1, templateIdMap)).collect(Collectors.toList());
        }
        return stockTemplatePredicts;
    }

    private StockTemplatePredict convert(StockTemplatePredict stockTemplatePredict, Map<String, String> templateIdMap) {
        stockTemplatePredict.setTemplatedName(templateIdMap.get(stockTemplatePredict.getTemplatedId()));
        return stockTemplatePredict;
    }

    public void deleteById(StockPredictDto dto) {
        stockTemplatePredictMapper.deleteByPrimaryKey(dto.getId());
    }
}
