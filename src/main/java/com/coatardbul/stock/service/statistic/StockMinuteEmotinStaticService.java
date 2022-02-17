package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.StaticLatitudeEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
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
import java.util.ArrayList;
import java.util.List;

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
     * 刷新某天的分钟情绪数据
     *
     * @param dto
     * @throws IllegalAccessException
     */
    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException {
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(dto.getObjectEnumSign());
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockStaticTemplates.get(0);
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
            if (templateIdList != null && templateIdList.size() > 0) {
                for (String templateId : templateIdList) {
                    Constant.emotionTemplateAndIntervalByDateThreadPool.submit(() -> {
                        inserteEmotionDate(dto, timeIntervalListData, templateId);
                    });
                }
            }
        }
    }

    /**
     *
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
            StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
            stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
            stockStrategyQueryDTO.setDateStr(dto.getDateStr());
            stockStrategyQueryDTO.setTimeStr(timeStr);
            StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            AxiosBaseBo axiosBaseBo = new AxiosBaseBo();
            axiosBaseBo.setDateTimeStr(timeStr);
            axiosBaseBo.setValue(new BigDecimal(strategy.getTotalNum()));
            list.add(axiosBaseBo);
        }
        addStockMinuterEmotion.setObjectStaticArray(JsonUtil.toJson(list));
        stockMinuterEmotionMapper.insert(addStockMinuterEmotion);
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
                } catch (IllegalAccessException e) {
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
