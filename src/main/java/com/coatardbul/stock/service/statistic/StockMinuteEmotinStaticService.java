package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.StaticLatitudeEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.bo.AxiosAllDataBo;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.coatardbul.stock.model.bo.AxiosYinfoDataBo;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockMinuterEmotion;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/8
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockMinuteEmotinStaticService {
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


    /**
     * 刷新某天的分钟情绪数据，
     * 1.有时间间隔，根据是否强制刷新，补充数据,不能超过当前时间
     * 2.没有时间间隔，有具体timeStr，固定刷新某一时间点，验证时间。
     *
     * @param dto 参数
     * @throws IllegalAccessException
     * @throws ParseException
     * @throws InterruptedException
     */
    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException, InterruptedException {
        //验证日期
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockVerifyService.verifyObjectSign(dto.getObjectEnumSign());
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);
        //按照天统计
        if (StaticLatitudeEnum.day.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            //todo
        }
        //分钟
        if (StaticLatitudeEnum.minuter.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            //时间间隔为空，必须要有具体HH:mm
            if (StringUtils.isNotBlank(dto.getTimeStr())) {
                //todo
                if (stockVerifyService.isIllegalDateTimeStr(dto.getDateStr(), dto.getTimeStr())) {
                    return;
                }
                timeStrProcess(dto, templateIdList);
                return;
            }
            if (dto.getTimeInterval() != null) {
                timeIntervalProcess(dto, templateIdList);
            }

        }
    }

    /**
     * 刷新某个HH:mm的数据
     *
     * @param dto
     * @param templateIdList
     */
    private void timeStrProcess(StockEmotionDayDTO dto, List<String> templateIdList) {
        if (templateIdList == null || templateIdList.size() == 0) {
            return;
        }
        for (String templateId : templateIdList) {
            timeStrProcess(dto, templateId);
        }
    }


    private void timeStrProcess(StockEmotionDayDTO dto, String templateId) {
        StockMinuterEmotion stockMinuterEmotion = stockMinuterEmotionMapper.selectAllByDateAndObjectSignAndTemplateId(dto.getDateStr(), dto.getObjectEnumSign(), templateId);
        //获取远程数据
        AxiosBaseBo axiosBaseBo = null;
        try {
            axiosBaseBo = getAxiosBaseBo(dto.getDateStr(), templateId, dto.getTimeStr());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        //创建对象
        if (stockMinuterEmotion == null) {
            StockMinuterEmotion defaultAddStockMinuterEmotion = getDefaultAddStockMinuterEmotion(dto.getDateStr(), dto.getObjectEnumSign(), templateId);
            List<AxiosBaseBo> list = new ArrayList<>();
            if (axiosBaseBo != null) {
                list.add(axiosBaseBo);
            }
            defaultAddStockMinuterEmotion.setObjectStaticArray(JsonUtil.toJson(list));
            stockMinuterEmotionMapper.insertSelective(defaultAddStockMinuterEmotion);
        } else {
            //表中有数据，补充刷新
            String objectStaticArray = stockMinuterEmotion.getObjectStaticArray();
            List<AxiosBaseBo> axiosBaseBos = JsonUtil.readToValue(objectStaticArray, new TypeReference<List<AxiosBaseBo>>() {
            });
            //判断时间对象中的数据，是否为空集合
            if (axiosBaseBos == null || axiosBaseBos.size() == 0) {
                List<AxiosBaseBo> list = new ArrayList<>();
                if (axiosBaseBo != null) {
                    list.add(axiosBaseBo);
                }
                stockMinuterEmotion.setObjectStaticArray(JsonUtil.toJson(list));
            } else {
                boolean flag = false;
                //当前时间在表中有数据需要更新
                for (AxiosBaseBo axiosBase : axiosBaseBos) {
                    if (dto.getTimeStr().equals(axiosBase.getDateTimeStr())) {
                        if (axiosBaseBo != null) {
                            axiosBase.setValue(axiosBaseBo.getValue());
                        }
                    } else {
                        flag = true;
                    }
                }
                if (flag) {
                    if (axiosBaseBo != null) {
                        axiosBaseBos.add(axiosBaseBo);
                    }
                }
                stockMinuterEmotion.setObjectStaticArray(JsonUtil.toJson(axiosBaseBos));
            }
            stockMinuterEmotionMapper.updateByPrimaryKeySelective(stockMinuterEmotion);
        }
    }

    /**
     * 有时间间隔的过程
     *
     * @param dto
     * @param templateIdList
     */
    private void timeIntervalProcess(StockEmotionDayDTO dto, List<String> templateIdList) {
        //存入分钟间隔数据
        if (templateIdList == null || templateIdList.size() == 0) {
            return;
        }
        //获取间隔时间字符串
        List<String> timeIntervalListData = stockVerifyService.getRemoteTimeInterval(dto.getTimeInterval());
        for (String timeStr : timeIntervalListData) {
            dto.setTimeStr(timeStr);
            timeStrProcess(dto, templateIdList);
        }
    }

    private StockMinuterEmotion getDefaultAddStockMinuterEmotion(String dateStr, String objectEnumSign, String templatedId) {
        StockMinuterEmotion addStockMinuterEmotion = new StockMinuterEmotion();
        addStockMinuterEmotion.setId(baseServerFeign.getSnowflakeId());
        addStockMinuterEmotion.setDate(dateStr);
        addStockMinuterEmotion.setObjectSign(objectEnumSign);
        addStockMinuterEmotion.setTemplateId(templatedId);
        return addStockMinuterEmotion;
    }

    private AxiosBaseBo getAxiosBaseBo(String dateStr, String templateId, String timeStr) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
        stockStrategyQueryDTO.setDateStr(dateStr);
        stockStrategyQueryDTO.setTimeStr(timeStr);
        StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
        AxiosBaseBo axiosBaseBo = new AxiosBaseBo();
        axiosBaseBo.setDateTimeStr(timeStr);
        if (strategy.getTotalNum() != null) {
            axiosBaseBo.setValue(new BigDecimal(strategy.getTotalNum()));
        }
        return axiosBaseBo;
    }

    /**
     * @param dto
     */
    public void refreshDayRange(StockEmotionDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            Constant.emotionIntervalByDateThreadPool.submit(() -> {
                StockEmotionDayDTO stockEmotionDayDTO = new StockEmotionDayDTO();
                stockEmotionDayDTO.setDateStr(dateStr);
                stockEmotionDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
                stockEmotionDayDTO.setTimeInterval(dto.getTimeInterval());
                try {
                    refreshDay(stockEmotionDayDTO);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });

        }
    }

    /**
     * 获取某日的统计数据
     *
     * @param dto
     */
    public AxiosAllDataBo getDayDetail(StockEmotionDayDTO dto) {
        //根据标识获取间隔
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(dto.getObjectEnumSign());
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }

        List<StockMinuterEmotion> stockMinuterEmotions = stockMinuterEmotionMapper.selectAllByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
        if (stockMinuterEmotions == null || stockMinuterEmotions.size() == 0) {
            return null;
        }
        //获取时间间隔
        List<String> timeIntervalListData = stockVerifyService.getRemoteTimeInterval(dto.getTimeInterval());

        return buildAxiosDate(stockMinuterEmotions, timeIntervalListData);
    }

    /**
     * 获取坐标轴上的数据
     *
     * @param stockMinuterEmotions 表中数据
     * @param timeIntervalListData 间隔数据
     * @return
     */
    private AxiosAllDataBo buildAxiosDate(List<StockMinuterEmotion> stockMinuterEmotions, List<String> timeIntervalListData) {
        AxiosAllDataBo result = new AxiosAllDataBo();
        //x轴数组
        result.setDateTimeStrArray(timeIntervalListData);
        //y轴数组
        List<AxiosYinfoDataBo> yAxiosArray = new ArrayList<>();
        for (StockMinuterEmotion stockMinuterEmotion : stockMinuterEmotions) {
            AxiosYinfoDataBo axiosYinfoDataBo = new AxiosYinfoDataBo();
            //y轴名称
            if (StringUtils.isNotBlank(stockMinuterEmotion.getTemplateId())) {
                axiosYinfoDataBo.setName(riverRemoteService.getTemplateNameById(stockMinuterEmotion.getTemplateId()));

            } else {
                axiosYinfoDataBo.setName(stockMinuterEmotion.getName());
            }
            //y轴数据数组
            axiosYinfoDataBo.setYAxiosInfo(JsonUtil.readToValue(stockMinuterEmotion.getObjectStaticArray(), new TypeReference<List<AxiosBaseBo>>() {
            }));
            yAxiosArray.add(axiosYinfoDataBo);
        }
        result.setYbaseInfo(yAxiosArray);
        return result;
    }


    public void filterDate(StockEmotionDayDTO dto) throws IllegalAccessException {
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockVerifyService.verifyObjectSign(dto.getObjectEnumSign());
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);
        //按照天统计
        if (StaticLatitudeEnum.day.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            //todo
        }
        //分钟
        if (StaticLatitudeEnum.minuter.getCode().equals(stockStaticTemplate.getStaticLatitude())) {

            for (String templateId : templateIdList) {
                StockMinuterEmotion stockMinuterEmotion = stockMinuterEmotionMapper.selectAllByDateAndObjectSignAndTemplateId(dto.getDateStr(), dto.getObjectEnumSign(), templateId);
                if (stockMinuterEmotion != null) {
                    //表中有数据，补充刷新
                    String objectStaticArray = stockMinuterEmotion.getObjectStaticArray();
                    List<AxiosBaseBo> axiosBaseBos = JsonUtil.readToValue(objectStaticArray, new TypeReference<List<AxiosBaseBo>>() {
                    });
                    Map<String, Integer> map = new HashMap<>();
                    //过滤掉值为空的数据
                    List<AxiosBaseBo> newList = new ArrayList<>();
                    for (AxiosBaseBo axiosBaseBo : axiosBaseBos) {
                        if (map.containsKey(axiosBaseBo.getDateTimeStr())) {
                            continue;
                        } else {
                            if (axiosBaseBo.getValue() != null) {
                                newList.add(axiosBaseBo);
                                map.put(axiosBaseBo.getDateTimeStr(), 1);
                            }
                        }
                    }
                    stockMinuterEmotion.setObjectStaticArray(JsonUtil.toJson(newList));
                    stockMinuterEmotionMapper.updateByPrimaryKeySelective(stockMinuterEmotion);
                }
            }
        }
    }

    private Map<String, Integer> convertListToMap(List<String> timeIntervalListData) {
        Map<String, Integer> map = new HashMap<>();
        for (String timeStr : timeIntervalListData) {
            if (!map.containsKey(timeStr)) {
                map.put(timeStr, 1);
            }
        }
        return map;
    }

    public void supplementRefreshDay(StockEmotionDayDTO dto) throws IllegalAccessException {
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockVerifyService.verifyObjectSign(dto.getObjectEnumSign());
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);
        //获取间隔时间字符串
        List<String> timeIntervalListData = stockVerifyService.getRemoteTimeInterval(dto.getTimeInterval());

        for (String templateId : templateIdList) {
            filterTimeInfo(dto, timeIntervalListData, templateId);
        }
    }


    /**
     * 过滤
     *
     * @param dto
     * @param timeIntervalListData 时间数组
     * @param templateId           模板id
     */
    private void filterTimeInfo(StockEmotionDayDTO dto, List<String> timeIntervalListData, String templateId) {
        //当前日期的模板数据随机取一行
        StockMinuterEmotion stockMinuterEmotion = stockMinuterEmotionMapper.selectAllByDateAndObjectSignAndTemplateId(dto.getDateStr(), dto.getObjectEnumSign(), templateId);
        //转换成map
        Map<String, Integer> stringIntegerMap = convertListToMap(timeIntervalListData);
        if (stockMinuterEmotion != null && StringUtils.isNotBlank(stockMinuterEmotion.getObjectStaticArray())) {
            String objectStaticArray = stockMinuterEmotion.getObjectStaticArray();
            List<AxiosBaseBo> axiosBaseBos = JsonUtil.readToValue(objectStaticArray, new TypeReference<List<AxiosBaseBo>>() {
            });
            Map<String, AxiosBaseBo> haveValue = axiosBaseBos.stream().collect(Collectors.toMap(AxiosBaseBo::getDateTimeStr, Function.identity(), (o1, o2) -> o1));
            //移出已经有的数据
            for (Map.Entry<String, AxiosBaseBo> set : haveValue.entrySet()) {
                if (stringIntegerMap.containsKey(set.getKey())) {
                    stringIntegerMap.remove(set.getKey());
                }
            }
        }
        //将剩余的time更新到表中
        for (Map.Entry<String, Integer> set : stringIntegerMap.entrySet()) {
            dto.setTimeStr(set.getKey());
            timeStrProcess(dto, templateId);
        }
    }


    public void forceRefreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, InterruptedException, ParseException {
        stockMinuterEmotionMapper.deleteByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
        refreshDay(dto);
    }

    public void supplementRefreshDayRange(StockEmotionDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            StockEmotionDayDTO stockEmotionDayDTO = new StockEmotionDayDTO();
            stockEmotionDayDTO.setDateStr(dateStr);
            stockEmotionDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
            stockEmotionDayDTO.setTimeInterval(dto.getTimeInterval());
            Constant.emotionIntervalByDateThreadPool.submit(() -> {
                try {
                    filterDate(stockEmotionDayDTO);
                    supplementRefreshDay(stockEmotionDayDTO);
                } catch (IllegalAccessException e) {
                    log.error(e.getLocalizedMessage(), e);
                }
            });

        }
    }
}
