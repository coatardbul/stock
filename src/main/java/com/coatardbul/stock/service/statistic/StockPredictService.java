package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.IsNotEnum;
import com.coatardbul.stock.common.constants.StockWatchTypeEnum;
import com.coatardbul.stock.common.constants.StrategySimulateEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockDayEmotionMapper;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.mapper.StockStrategyWatchMapper;
import com.coatardbul.stock.mapper.StockTemplatePredictMapper;
import com.coatardbul.stock.model.bo.DayAxiosBaseBO;
import com.coatardbul.stock.model.bo.StockUpLimitShortHighBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockPredictDto;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockDayEmotion;
import com.coatardbul.stock.model.entity.StockStrategyWatch;
import com.coatardbul.stock.model.entity.StockTemplatePredict;
import com.coatardbul.stock.model.feign.StockTemplateQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    StockStrategyWatchMapper stockStrategyWatchMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    StockDayEmotionMapper stockDayEmotionMapper;

    public void execute(StockPredictDto dto) {
        if (!StringUtils.isNotBlank(dto.getId())) {
            throw new BusinessException("id不能为空");
        }
        Assert.notNull(dto.getHoleDay(), "天数不不能为空");
        //获取时间区间
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());

        //模拟策略
        StockStrategyWatch stockStrategyWatch = new StockStrategyWatch();
        stockStrategyWatch.setType(StockWatchTypeEnum.STRATEGY_SIMULATE.getType());
        stockStrategyWatch.setTemplatedId(dto.getId());
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectByAll(stockStrategyWatch);

        for (String dateStr : dateIntervalList) {
            if (stockStrategyWatches != null && stockStrategyWatches.size() > 0) {
                StockStrategyWatch stockStrategyWatch1 = stockStrategyWatches.get(0);

                if (StringUtils.isNotBlank(stockStrategyWatch1.getStrategySign())) {
                    Constant.emotionByDateRangeThreadPool.execute(() -> {
                        jointStrategyQueryAndParse(dto, dateStr, "", stockStrategyWatch1);
                    });
                } else {
                    List<String> timeList = null;
                    if (StringUtils.isNotBlank(stockStrategyWatch1.getBeginTime())) {
                        timeList = DateTimeUtil.getRangeMinute(stockStrategyWatch1.getBeginTime(), stockStrategyWatch1.getEndTime(), 1);
                    } else {
                        timeList = DateTimeUtil.getRangeMinute("09:30", stockStrategyWatch1.getEndTime(), 1);
                    }
                    List<String> finalTimeList = timeList;
                    Constant.emotionByDateRangeThreadPool.execute(() -> {
                        for (String timeStr : finalTimeList) {
                            jointStrategyQueryAndParse(dto, dateStr, timeStr);
                        }
                    });
                }
            } else {
                Constant.emotionByDateRangeThreadPool.execute(() -> {
                    jointStrategyQueryAndParse(dto, dateStr);
                });
            }
        }
    }

    private void jointStrategyQueryAndParse(StockPredictDto dto, String dateStr) {
        jointStrategyQueryAndParse(dto, dateStr, "");
    }

    private void jointStrategyQueryAndParse(StockPredictDto dto, String dateStr, String timeStr) {
        jointStrategyQueryAndParse(dto, dateStr, timeStr, new StockStrategyWatch());
    }

    /**
     * @param dto
     * @param dateStr
     * @param timeStr
     */
    private void jointStrategyQueryAndParse(StockPredictDto dto, String dateStr, String timeStr, StockStrategyWatch stockStrategyWatch) {
        //将持有天数转换成脚本
        String saleQueryScript = getSaleQueryScript(dto.getHoleDay());
        //买入的查询语句
        StockPredictDto result = new StockPredictDto();
        BeanUtils.copyProperties(dto, result);
        result.setBuyTime(timeStr);
        String buyQueryInfo = getBuyQueryInfo(result, dateStr);
        //卖出的查询语句
        String saleQueryInfo = getSaleQueryInfo(result, saleQueryScript, dateStr);
        //和id查询到的问句拼接
        String finalQueryInfo = buyQueryInfo + saleQueryInfo;
        //策略查询
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setQueryStr(finalQueryInfo);
        try {
            StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            //动态结果解析
            if (strategy != null&&strategy.getTotalNum()>0) {
                parseStrategyResult(result, strategy, dateStr, stockStrategyWatch);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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


    private void parseStrategyResult(StockPredictDto dto, StrategyBO strategy, String dateStr, StockStrategyWatch stockStrategyWatch) {
        JSONArray data = strategy.getData();
        String dateFormat = dateStr.replace("-", "");
        String saleDateFormat = riverRemoteService.getSpecialDay(dateStr, dto.getHoleDay()).replace("-", "");
        if (StringUtils.isNotBlank(stockStrategyWatch.getStrategySign())) {
            if (StrategySimulateEnum.UPLIMIT_SHORT_HIGH.getSign().equals(stockStrategyWatch.getStrategySign())) {
                List<StockTemplatePredict> result = new ArrayList<>();

                List<StockTemplatePredict> addList = new ArrayList<>();

                for (Object jo : data) {
                    StockTemplatePredict addInfo = getStockTemplatePredictUpLimitShortHigh(dto, dateStr, dateFormat, saleDateFormat, jo);
                    result.add(addInfo);
                }
                result = result.stream().sorted(Comparator.comparing(StockTemplatePredict::getBuyIncreaseRate).reversed()).collect(Collectors.toList());
                if (result.size() == 1) {
                    return;
                } else if (result.size() > 3) {
                    BigDecimal buyIncreaseRate = result.get(2).getBuyIncreaseRate();
                    if (buyIncreaseRate.compareTo(new BigDecimal(9)) > 0) {
                        // 买入前三个
                        addList = result.subList(0, 3);
                    } else {
                        BigDecimal buyIncreaseRate1 = result.get(1).getBuyIncreaseRate();
                        if (buyIncreaseRate1.compareTo(new BigDecimal(9)) > 0) {
                            // 买入前两个
                            addList = result.subList(0, 2);
                        } else {
                            //买入前一个
                            addList = result.subList(0, 1);
                        }

                    }
                } else if (result.size() > 0) {
                    addList = result.subList(0, 1);
                }
                if (StringUtils.isNotBlank(stockStrategyWatch.getBuyCondition())) {

                    StockUpLimitShortHighBo stockUpLimitShortHighBo = JsonUtil.readToValue(stockStrategyWatch.getBuyCondition(), StockUpLimitShortHighBo.class);

                    String lastDay = riverRemoteService.getSpecialDay(dateStr, -1);
                    //买入条件判断
                    DayAxiosBaseBO nowDayAxiosBaseBO = getDayAxiosBaseBO(dateStr);
                    DayAxiosBaseBO lastDayAxiosBaseBO = getDayAxiosBaseBO(lastDay);

                    if(IsNotEnum.YES.getType().equals(stockUpLimitShortHighBo.getIsFilterStatus())&&
                            nowDayAxiosBaseBO.getValue().divide(lastDayAxiosBaseBO.getValue(),2,BigDecimal.ROUND_HALF_UP)
                                    .compareTo(stockUpLimitShortHighBo.getRate())<0){
                        for (StockTemplatePredict addInfo : addList) {
                            stockTemplatePredictMapper.insertSelective(addInfo);
                        }
                    }
                    if(IsNotEnum.NO.getType().equals(stockUpLimitShortHighBo.getIsFilterStatus())){
                        for (StockTemplatePredict addInfo : addList) {
                            stockTemplatePredictMapper.insertSelective(addInfo);
                        }
                    }
                } else {
                    for (StockTemplatePredict addInfo : addList) {
                        stockTemplatePredictMapper.insertSelective(addInfo);
                    }
                }


            }


        } else {
            for (Object jo : data) {
                StockTemplatePredict addInfo = getStockTemplatePredict(dto, dateStr, dateFormat, saleDateFormat, jo);
                String key = addInfo.getDate() + "_" + addInfo.getTemplatedId() + "_" + addInfo.getCode();
                if (StringUtils.isNotBlank(addInfo.getBuyTime())) {
                    //redis判断是否有重复
                    if (redisTemplate.hasKey(key)) {
                        String str = (String) redisTemplate.opsForValue().get(key);
                        StockTemplatePredict stockTemplatePredict = JsonUtil.readToValue(str, StockTemplatePredict.class);
                        if (addInfo.getBuyTime().compareTo(stockTemplatePredict.getBuyTime()) < 0) {
                            stockTemplatePredictMapper.deleteByDateAndTemplatedIdAndCode(addInfo.getDate(), addInfo.getTemplatedId(), addInfo.getCode());
                            redisTemplate.opsForValue().set(key, JsonUtil.toJson(addInfo), 10, TimeUnit.MINUTES);
                            stockTemplatePredictMapper.insertSelective(addInfo);
                        }else {
                            continue;
                        }
                    }
                }
                redisTemplate.opsForValue().set(key, JsonUtil.toJson(addInfo), 10, TimeUnit.MINUTES);
                stockTemplatePredictMapper.insertSelective(addInfo);
            }
        }
    }


    private DayAxiosBaseBO getDayAxiosBaseBO(String dateStr) {
        List<StockDayEmotion> nowDayInfo = stockDayEmotionMapper.selectAllByDateAndObjectSign(dateStr, StockStaticModuleUtil.DAY_TRUMPET_CALC_STATISTIC);
        if (nowDayInfo.size() > 0) {
            StockDayEmotion stockDayEmotion = nowDayInfo.get(0);
            String objectStaticArray = stockDayEmotion.getObjectStaticArray();
            List<DayAxiosBaseBO> nowDayAxiosBase = JsonUtil.readToValue(objectStaticArray, new TypeReference<List<DayAxiosBaseBO>>() {
            });
            return nowDayAxiosBase.get(1);
        }

        return null;
    }


    private StockTemplatePredict getStockTemplatePredictUpLimitShortHigh(StockPredictDto dto, String dateStr, String dateFormat, String saleDateFormat, Object jo) {
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
            if (key.contains(dateFormat) && key.contains("开盘价:不复权") && !key.contains("{-}")) {
                addInfo.setBuyPrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
            if (key.contains(dateFormat) && key.contains("收盘价:不复权") && !key.contains("{-}") && !key.contains("分时")) {
                if (addInfo.getBuyPrice() == null) {
                    addInfo.setBuyPrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
                }
            }
            if (key.contains(dateFormat) && key.contains("涨跌幅") && !key.contains("分时")) {
                addInfo.setCloseIncreaseRate(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
            if (key.contains(dateFormat) && key.contains("竞价涨幅") && !key.contains("{-}")) {
                addInfo.setBuyIncreaseRate(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
            if (key.contains(dateFormat) && key.contains("换手率")) {
                addInfo.setTurnoverRate(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
        }
        addInfo.setDetail(jo.toString());
        return addInfo;
    }


    private StockTemplatePredict getStockTemplatePredict(StockPredictDto dto, String dateStr, String dateFormat, String saleDateFormat, Object jo) {
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
            if (key.contains(dateFormat) && key.contains("分时收盘价:不复权") && !key.contains("{-}")) {
                addInfo.setBuyPrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
            if (key.contains(dateFormat) && key.contains("收盘价:不复权") && !key.contains("{-}") && !key.contains("分时")) {
                if (addInfo.getBuyPrice() == null) {
                    addInfo.setBuyPrice(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
                }
            }
            if (key.contains(dateFormat) && key.contains("分时涨跌幅") && !key.contains("{-}")) {
                addInfo.setBuyIncreaseRate(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
            if (key.contains(dateFormat) && key.contains("涨跌幅") && !key.contains("分时")) {
                addInfo.setCloseIncreaseRate(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
            if (key.contains(dateFormat) && key.contains("换手率")) {
                addInfo.setTurnoverRate(stockParseAndConvertService.convert(((JSONObject) jo).get(key)));
            }
        }
        addInfo.setDetail(jo.toString());
        return addInfo;
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

    public void deleteByQuery(StockPredictDto dto) {
        stockTemplatePredictMapper.deleteByTemplatedIdAndHoldDayAndDateBetweenEqual(dto.getId(), dto.getHoleDay(), dto.getBeginDate(), dto.getEndDate());
    }
}
