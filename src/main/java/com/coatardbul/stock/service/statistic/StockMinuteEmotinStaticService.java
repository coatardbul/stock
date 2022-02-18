package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.StaticLatitudeEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.bo.AxiosAllDataBo;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.model.dto.StockEmotionStaticDTO;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.coatardbul.stock.model.bo.AxiosYinfoDataBo;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockMinuterEmotion;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.model.feign.StockTimeInterval;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /**
     * 刷新某天的分钟情绪数据，
     * 1.不能刷新当天的，当天请走定时任务
     * 2.当前时间以后的也不能刷新
     *
     * @param dto
     * @throws IllegalAccessException
     */
    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
        //验证时间
        verifyDateStr(dto);
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = verifyObjectSign(dto.getObjectEnumSign());
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);
        //按照天统计
        if (StaticLatitudeEnum.day.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            //todo
        }
        //分钟
        if (StaticLatitudeEnum.minuter.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            if (dto.getTimeInterval() == null) {
                throw new BusinessException("时间间隔不能为空");
            }
            //获取间隔时间字符串
            List<String> timeIntervalListData = getRemoteTimeInterval(dto.getTimeInterval());
            //删除已经有的数据
            stockMinuterEmotionMapper.deleteByDateAndObjectSignAndTimeInterval(dto.getDateStr(), dto.getObjectEnumSign(), dto.getTimeInterval());
            //存入分钟间隔数据
            if (templateIdList == null || templateIdList.size() == 0) {
                return;
            }
            for (String templateId : templateIdList) {
                Constant.emotionTemplateAndIntervalByDateThreadPool.submit(() -> {
                    inserteEmotionDate(dto, timeIntervalListData, templateId);
                });
            }
        }
    }


    public void supplementRefreshDay(StockEmotionDayDTO dto) throws IllegalAccessException {
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = verifyObjectSign(dto.getObjectEnumSign());
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);
        //获取间隔时间字符串
        List<String> timeIntervalListData = getRemoteTimeInterval(dto.getTimeInterval());
        //存入分钟间隔数据
        if (templateIdList == null || templateIdList.size() == 0) {
            return;
        }
        for (String templateId : templateIdList) {
            List<StockMinuterEmotion> stockMinuterEmotions = stockMinuterEmotionMapper.selectAllByDateAndObjectSignAndTemplateId(dto.getDateStr(), dto.getObjectEnumSign(), templateId);
            if (stockMinuterEmotions != null && stockMinuterEmotions.size() > 0) {
                continue;
            }
            Constant.emotionTemplateAndIntervalByDateThreadPool.submit(() -> {
                inserteEmotionDate(dto, timeIntervalListData, templateId);
            });
        }

    }

    /**
     * 验证日期
     * 不能超过当前日期
     * 当天日期走定时任务
     *
     * @param dto YYYY-MM-DD
     * @throws ParseException
     */
    private void verifyDateStr(StockEmotionDayDTO dto) throws ParseException {
        Date date = DateTimeUtil.parseDateStr(dto.getDateStr(), DateTimeUtil.YYYY_MM_DD);
        if (new Date().compareTo(date) < 0) {
            throw new BusinessException("当前日期不合法，不能超过当前时间");
        }
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getDateStr(), dto.getDateStr());
        if (dateIntervalList == null || dateIntervalList.size() == 0) {
            throw new BusinessException("非交易日");
        }
        String nowDateStr = DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD);
        if (nowDateStr.equals(dto.getDateStr())) {
            throw new BusinessException("当天日期请走定时任务");
        }
    }


    private StockStaticTemplate verifyObjectSign(String objectSign) {
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(objectSign);
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }
        //模型策略数据
        return stockStaticTemplates.get(0);
    }


    /**
     * 刷新某天的分钟情绪数据，定时任务
     *
     * @param dto
     * @throws IllegalAccessException
     */
    public void taskRefreshDay(StockEmotionDayDTO dto) throws IllegalAccessException {

        //模型策略数据
        StockStaticTemplate stockStaticTemplate = verifyObjectSign(dto.getObjectEnumSign());
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);
        //按照天统计
        if (StaticLatitudeEnum.day.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            //todo
        }
        //分钟
        if (StaticLatitudeEnum.minuter.getCode().equals(stockStaticTemplate.getStaticLatitude())) {
            if (dto.getTimeInterval() == null) {
                throw new BusinessException("时间间隔不能为空");
            }
            //获取间隔时间字符串
            List<String> timeIntervalListData = getRemoteTimeInterval(dto.getTimeInterval());

            //模板信息
            if (templateIdList == null || templateIdList.size() == 0) {
                return;
            }
            for (String templateId : templateIdList) {
                //查询数据，1时间，2sign ,3 模板id
                List<StockMinuterEmotion> stockMinuterEmotions = stockMinuterEmotionMapper.selectAllByDateAndObjectSignAndTemplateId(dto.getDateStr(), dto.getObjectEnumSign(), templateId);
                //需要遍历的数据
                List<String> timeNeedList = timeIntervalListData.stream().filter(o1 -> o1.compareTo(dto.getTimeStr()) <= 0).collect(Collectors.toList());
                if (stockMinuterEmotions != null && stockMinuterEmotions.size() > 0) {
                    //表中数据，能查到，肯定有一条数据
                    List<AxiosBaseBo> axiosBaseBos = JsonUtil.readToValue(stockMinuterEmotions.get(0).getObjectStaticArray(), new TypeReference<List<AxiosBaseBo>>() {
                    });
                    List<String> timeHaveList = axiosBaseBos.stream().map(AxiosBaseBo::getDateTimeStr).collect(Collectors.toList());
                    //过滤time
                    for (String timeStr : timeNeedList) {
                        if (!timeHaveList.contains(timeStr)) {
                            //查询策略
                            AxiosBaseBo axiosBaseBo = getAxiosBaseBo(dto, templateId, timeStr);
                            axiosBaseBos.add(axiosBaseBo);
                        }
                    }
                    //更新到表中
                    stockMinuterEmotions.get(0).setObjectStaticArray(JsonUtil.toJson(axiosBaseBos));
                    stockMinuterEmotionMapper.updateByPrimaryKeySelective(stockMinuterEmotions.get(0));
                } else {
                    //表中无数据
                    Constant.emotionTemplateAndIntervalByDateThreadPool.submit(() -> {
                        inserteEmotionDate(dto, timeNeedList, templateId);
                    });
                }
            }

        }
    }


    /**
     * 将对应模板id按照某天时间间隔存取表中
     *
     * @param dto                  时间
     * @param timeIntervalListData 时间间隔
     * @param templateId           模板id
     */
    private void inserteEmotionDate(StockEmotionDayDTO dto, List<String> timeIntervalListData, String templateId) {
        StockMinuterEmotion addStockMinuterEmotion = new StockMinuterEmotion();
        addStockMinuterEmotion.setId(baseServerFeign.getSnowflakeId());
        addStockMinuterEmotion.setDate(dto.getDateStr());
        addStockMinuterEmotion.setTimeInterval(dto.getTimeInterval());
//                addStockEmotion.setDateTimeArray();
        addStockMinuterEmotion.setObjectSign(dto.getObjectEnumSign());
        addStockMinuterEmotion.setTemplateId(templateId);
        List<AxiosBaseBo> list = new ArrayList<>();
        for (String timeStr : timeIntervalListData) {
            AxiosBaseBo axiosBaseBo = getAxiosBaseBo(dto, templateId, timeStr);
            list.add(axiosBaseBo);
        }
        addStockMinuterEmotion.setObjectStaticArray(JsonUtil.toJson(list));
        stockMinuterEmotionMapper.insert(addStockMinuterEmotion);
    }

    private AxiosBaseBo getAxiosBaseBo(StockEmotionDayDTO dto, String templateId, String timeStr) {
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
        stockStrategyQueryDTO.setDateStr(dto.getDateStr());
        stockStrategyQueryDTO.setTimeStr(timeStr);
        StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
        AxiosBaseBo axiosBaseBo = new AxiosBaseBo();
        axiosBaseBo.setDateTimeStr(timeStr);
        axiosBaseBo.setValue(new BigDecimal(strategy.getTotalNum()));
        return axiosBaseBo;
    }

    /**
     * 获取远程间隔数据
     *
     * @param timeInterval
     * @return
     */
    private List<String> getRemoteTimeInterval(Integer timeInterval) {
        StockTimeInterval stockTimeInterval = new StockTimeInterval();
        stockTimeInterval.setIntervalType(timeInterval);
        CommonResult<List<String>> timeIntervalList = riverServerFeign.getTimeIntervalList(stockTimeInterval);
        List<String> timeIntervalListData = timeIntervalList.getData();
        if (timeIntervalListData == null || timeIntervalListData.size() == 0) {
            throw new BusinessException("不支持的时间间隔，请更新时间间隔数据");
        }
        return timeIntervalListData;
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

        List<StockMinuterEmotion> stockMinuterEmotions = stockMinuterEmotionMapper.selectAllByDateAndObjectSignAndTimeInterval(dto.getDateStr(), dto.getObjectEnumSign(), dto.getTimeInterval());
        if (stockMinuterEmotions == null || stockMinuterEmotions.size() == 0) {
            return null;
        }
        //获取时间间隔
        List<String> timeIntervalListData = getRemoteTimeInterval(dto.getTimeInterval());

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
                StockTemplateDto templateById = riverRemoteService.getTemplateById(stockMinuterEmotion.getTemplateId());
                axiosYinfoDataBo.setName(templateById.getName());

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

    public void getRangeDetail(StockEmotionDayDTO dto) {

    }

    public List<StockEmotionStaticDTO> getRangeStatic(StockEmotionQueryDTO dto) {
        return stockMinuterEmotionMapper.selectStaticInfoByCondition(dto.getDateStr(), dto.getObjectEnumSign(), dto.getTimeInterval());
    }


}
