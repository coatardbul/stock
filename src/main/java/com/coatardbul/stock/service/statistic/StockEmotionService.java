package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.bo.AxiosAllDataBo;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.coatardbul.stock.model.bo.AxiosYinfoDataBo;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockEmotion;
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
public class StockEmotionService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockStaticTemplateMapper stockStaticTemplateMapper;
    @Autowired
    StockEmotionMapper stockEmotionMapper;

    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException {
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(dto.getObjectEnumSign());
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockStaticTemplates.get(0);
        //获取间隔时间字符串
        Integer timeInterval = stockStaticTemplate.getTimeInterval();
        StockTimeInterval stockTimeInterval = new StockTimeInterval();
        stockTimeInterval.setIntervalType(timeInterval);
        CommonResult<List<String>> timeIntervalList = riverServerFeign.getTimeIntervalList(stockTimeInterval);
        List<String> timeIntervalListData = timeIntervalList.getData();
        if (timeIntervalListData == null || timeIntervalListData.size() == 0) {
            throw new BusinessException("不支持的时间间隔，请更新时间间隔数据");
        }
        //
        List<String> templateIdList = getTemplateIdList(stockStaticTemplate);
        if(templateIdList!=null &&templateIdList.size()>0){
            for(String templateId:templateIdList){
                Constant.emotionJobThreadPool.submit(()->{
                    StockEmotion addStockEmotion=new StockEmotion();
                    addStockEmotion.setId(baseServerFeign.getSnowflakeId());
                    addStockEmotion.setDate(dto.getDateStr());
//                addStockEmotion.setDateTimeArray();
                    addStockEmotion.setObjectSign(dto.getObjectEnumSign());
//                addStockEmotion.setTimeInterval();
                    addStockEmotion.setTemplateId(templateId);
                    List<AxiosBaseBo>list=new ArrayList<>();
                    for (String timeStr : timeIntervalListData) {
                        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                        stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
                        stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                        stockStrategyQueryDTO.setTimeStr(timeStr);
                        StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                        AxiosBaseBo axiosBaseBo=new AxiosBaseBo();
                        axiosBaseBo.setDateTimeStr(timeStr);
                        axiosBaseBo.setValue(new BigDecimal(strategy.getTotalNum()));
                        list.add(axiosBaseBo);
                    }
                    addStockEmotion.setObjectStaticArray(JsonUtil.toJson(list));
                    stockEmotionMapper.insert(addStockEmotion);
                });
            }
        }
    }

    private List<String> getTemplateIdList(StockStaticTemplate stockStaticTemplate) throws IllegalAccessException {
        List<String> result = new ArrayList<>();
        //根据标识获取对应的对象解析id数据
        Class classBySign = StockStaticModuleUtil.getClassBySign(stockStaticTemplate.getObjectSign());
        Object o = JsonUtil.readToValue(stockStaticTemplate.getObjectStr(), classBySign);
        //获取类里面的所有属性集合
        ReflexUtil.singleReadAStringAttributeList(o, result);

        return result;
    }


    public void refreshDayRange(StockEmotionDayRangeDTO dto) {

    }

    /**
     * 获取某日的统计数据
     *
     * @param dto
     */
    public AxiosAllDataBo getDayStatic(StockEmotionDayDTO dto) {
        List<StockEmotion> stockEmotions = stockEmotionMapper.selectAllByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
        if (stockEmotions == null || stockEmotions.size() == 0) {
            return null;
        }
        //根据标识获取间隔
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(dto.getObjectEnumSign());
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }
        StockStaticTemplate stockStaticTemplate = stockStaticTemplates.get(0);
        StockTimeInterval stockTimeInterval = new StockTimeInterval();
        stockTimeInterval.setIntervalType(stockStaticTemplate.getTimeInterval());
        CommonResult<List<String>> timeIntervalList = riverServerFeign.getTimeIntervalList(stockTimeInterval);
        List<String> timeIntervalListData = timeIntervalList.getData();
        if (timeIntervalListData == null || timeIntervalListData.size() == 0) {
            throw new BusinessException("不支持的时间间隔，请更新时间间隔数据");
        }


        AxiosAllDataBo result = new AxiosAllDataBo();
        //x轴数组
        result.setDateTimeStrArray(timeIntervalListData);
        //y轴数组
        List<AxiosYinfoDataBo> yAxiosArray = new ArrayList<>();
        for (StockEmotion stockEmotion : stockEmotions) {
            AxiosYinfoDataBo axiosYinfoDataBo = new AxiosYinfoDataBo();
            //y轴名称
            if (StringUtils.isNotBlank(stockEmotion.getTemplateId())) {
                StockTemplateDto stockTemplateDto = new StockTemplateDto();
                stockTemplateDto.setId(stockEmotion.getTemplateId());
                CommonResult<StockTemplateDto> one = riverServerFeign.findOne(stockTemplateDto);
                if (one != null && one.getData() != null) {
                    axiosYinfoDataBo.setName(one.getData().getName());
                }
            } else {
                axiosYinfoDataBo.setName(stockEmotion.getName());
            }
            //y轴数据数组
            axiosYinfoDataBo.setYAxiosInfo(JsonUtil.readToValue(stockEmotion.getObjectStaticArray(), new TypeReference<List<AxiosBaseBo>>() {
            }));
            yAxiosArray.add(axiosYinfoDataBo);
        }
        result.setYbaseInfo(yAxiosArray);
        return result;
    }

    public void getRangeStatic(StockEmotionDayDTO dto) {

    }
}
