package com.coatardbul.stock.service.statistic.dayBaseChart;

import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockDayEmotionMapper;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.bo.DayAxiosBaseBO;
import com.coatardbul.stock.model.bo.DayAxiosMiddleBaseBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockDayEmotion;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.coatardbul.stock.service.statistic.StockVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/13
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockDayUpLimitStaticService extends BaseChartDayAbstractService{
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
    StockVerifyService stockVerifyService;

    /**
     * 刷新当日数据，全量刷新
     *
     * @param dto
     * @throws IllegalAccessException
     */
    @Override
    public void refreshDayProcess(StockEmotionDayDTO dto, StockStaticTemplate stockStaticTemplate) throws IllegalAccessException, ParseException {
        //获取模型对象中的模板id集合,便于根据模板id查询对应的数据结果
        List<String> templateIdList = stockStrategyService.getTemplateIdList(stockStaticTemplate);

        if (templateIdList != null && templateIdList.size() > 0) {
            StockDayEmotion addStockDayEmotion = new StockDayEmotion();
            addStockDayEmotion.setId(baseServerFeign.getSnowflakeId());
            addStockDayEmotion.setDate(dto.getDateStr());
            addStockDayEmotion.setObjectSign(dto.getObjectEnumSign());

            //获取数组里面的对象
            List<DayAxiosMiddleBaseBO> list = new ArrayList<>();
            for (String templateId : templateIdList) {
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                StrategyBO strategy = null;
                try {
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                DayAxiosMiddleBaseBO axiosBaseBo = new DayAxiosMiddleBaseBO();
                //todo
                axiosBaseBo.setId(templateId);
                axiosBaseBo.setValue(new BigDecimal(strategy.getTotalNum()));
                list.add(axiosBaseBo);
            }
            List<DayAxiosBaseBO> rebuild = rebuild(stockStaticTemplate, list);
            addStockDayEmotion.setObjectStaticArray(JsonUtil.toJson(rebuild));
            stockDayEmotionMapper.deleteByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
            stockDayEmotionMapper.insertSelective(addStockDayEmotion);
        }
    }


    private List<DayAxiosBaseBO> rebuild(StockStaticTemplate stockStaticTemplate, List<DayAxiosMiddleBaseBO> list) throws IllegalAccessException {
        Class classBySign = StockStaticModuleUtil.getClassBySign(stockStaticTemplate.getObjectSign());
        Object o = JsonUtil.readToValue(stockStaticTemplate.getObjectStr(), classBySign);
        List<String> specialIdList = new ArrayList<>();
        Map<String, DayAxiosMiddleBaseBO> specialMap = new HashMap<>();
        //todo 每种模板对应方式不用，目前先不判断
        String o1 = (String) ReflexUtil.readValueByName("riseId", o);
        specialIdList.add(o1);
        String o2 = (String) ReflexUtil.readValueByName("failId", o);
        specialIdList.add(o2);
        List<DayAxiosBaseBO> result = new ArrayList<>();
        for (DayAxiosMiddleBaseBO dayAxiosMiddleBaseBO : list) {
            if (specialIdList.contains(dayAxiosMiddleBaseBO.getId())) {
                specialMap.put(dayAxiosMiddleBaseBO.getId(), dayAxiosMiddleBaseBO);
                continue;
            }
            DayAxiosBaseBO dayAxiosBaseBO = new DayAxiosBaseBO();
            dayAxiosBaseBO.setName(riverRemoteService.getTemplateNameById(dayAxiosMiddleBaseBO.getId()));
            dayAxiosBaseBO.setValue(dayAxiosMiddleBaseBO.getValue());
            result.add(dayAxiosBaseBO);
        }
        DayAxiosBaseBO dayAxiosBaseBO = new DayAxiosBaseBO();
        BigDecimal subtract = specialMap.get(o1).getValue().subtract(specialMap.get(o2).getValue());
        dayAxiosBaseBO.setName("adjs");
        dayAxiosBaseBO.setValue(subtract);
        result.add(dayAxiosBaseBO);
        return result;
    }







}
