package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockDayEmotionMapper;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockScatterStaticMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.bo.DayAxiosMiddleBaseBO;
import com.coatardbul.stock.model.bo.StockLineInfoBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockScatterStatic;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/1
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockScatterService {
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
    StockDayEmotionMapper stockDayEmotionMapper;
    @Autowired
    StockScatterStaticMapper stockScatterStaticMapper;

    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException {
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(dto.getObjectEnumSign());
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockStaticTemplates.get(0);
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);

        if (templateIdList != null && templateIdList.size() > 0) {
            StockScatterStatic stockScatterStatic = new StockScatterStatic();
            stockScatterStatic.setId(baseServerFeign.getSnowflakeId());
            stockScatterStatic.setDate(dto.getDateStr());
//            stockScatterStatic.setObjectStaticArray();
            stockScatterStatic.setObjectSign(dto.getObjectEnumSign());


            //获取数组里面的对象
            List<DayAxiosMiddleBaseBO> list = new ArrayList<>();
            String templateId = templateIdList.get(0);
            StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
            stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
            stockStrategyQueryDTO.setDateStr(dto.getDateStr());
            StrategyBO strategy = null;
            try {
                strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            List<StockLineInfoBo> objectArray = new ArrayList<>();
            if (strategy != null && strategy.getTotalNum() > 0) {
                strategy.getData().forEach(item -> {
                    Set<Map.Entry<String, Object>> entries = ((JSONObject) item).entrySet();
                    StockLineInfoBo stockLineInfoBo = new StockLineInfoBo();
                    entries.forEach(stockLineInfo -> {
                        if (stockLineInfo.getKey().equals("code")) {
                            stockLineInfoBo.setCode((String) stockLineInfo.getValue());
                        }
                        if (stockLineInfo.getKey().contains("股票简称")) {
                            stockLineInfoBo.setName((String) stockLineInfo.getValue());
                        }
                        if (stockLineInfo.getKey().contains("总市值")) {
                            stockLineInfoBo.setMarketValue(new BigDecimal(String.valueOf(stockLineInfo.getValue())));
                        }
                        if (stockLineInfo.getKey().contains("成交额")) {
                            stockLineInfoBo.setTradeMoney(new BigDecimal(String.valueOf(stockLineInfo.getValue())));
                        }
                        if (stockLineInfo.getKey().contains("换手率")) {
                            stockLineInfoBo.setTurnoverRate(new BigDecimal(String.valueOf(stockLineInfo.getValue())));

                        }
                    });
                    objectArray.add(stockLineInfoBo);
                });

            }
            stockScatterStatic.setObjectStaticArray(JsonUtil.toJson(objectArray));
            stockScatterStaticMapper.deleteByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
            stockScatterStaticMapper.insertSelective(stockScatterStatic);
        }
    }

    public void refreshDayRange(StockEmotionDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            //表中有数据，直接返回，没有再查询
            List<StockScatterStatic> stockScatterStatics = stockScatterStaticMapper.selectAllByDateAndObjectSign(dateStr, dto.getObjectEnumSign());
            if (stockScatterStatics != null && stockScatterStatics.size() > 0) {
                continue;
            }
            StockEmotionDayDTO stockEmotionDayDTO = new StockEmotionDayDTO();
            stockEmotionDayDTO.setDateStr(dateStr);
            stockEmotionDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
            try {
                refreshDay(stockEmotionDayDTO);
            } catch (IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    public void forceRefreshDayRange(StockEmotionDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            StockEmotionDayDTO stockEmotionDayDTO = new StockEmotionDayDTO();
            stockEmotionDayDTO.setDateStr(dateStr);
            stockEmotionDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
            try {
                Thread.sleep(1000);
                refreshDay(stockEmotionDayDTO);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    public List<StockScatterStatic> getRangeStatic(StockEmotionRangeDayDTO dto) {
        List<StockScatterStatic> stockScatterStatics = stockScatterStaticMapper.selectAllByDateBetweenEqualAndObjectSign(dto.getDateBeginStr(), dto.getDateEndStr(), dto.getObjectEnumSign());
        return stockScatterStatics;
    }
}
