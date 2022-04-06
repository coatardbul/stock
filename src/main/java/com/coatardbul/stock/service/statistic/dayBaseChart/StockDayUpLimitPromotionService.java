package com.coatardbul.stock.service.statistic.dayBaseChart;

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
import com.coatardbul.stock.model.bo.DayUpLimitPromotionStatisticBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class StockDayUpLimitPromotionService extends BaseChartDayAbstractService {
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

            String specialDay = riverRemoteService.getSpecialDay(dto.getDateStr(), -1);

            //获取数组里面的对象
            Map<String, StrategyBO> map = new HashMap<>();
            for (String templateId : templateIdList) {
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
                stockStrategyQueryDTO.setDateStr(specialDay);
                StrategyBO strategy = null;
                try {
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                map.put(templateId, strategy);
            }
            List<DayAxiosBaseBO> rebuild = rebuild(stockStaticTemplate, map);
            addStockDayEmotion.setObjectStaticArray(JsonUtil.toJson(rebuild));
            stockDayEmotionMapper.deleteByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
            stockDayEmotionMapper.insertSelective(addStockDayEmotion);
        }
    }


    private List<DayAxiosBaseBO> rebuild(StockStaticTemplate stockStaticTemplate, Map<String, StrategyBO> map) throws IllegalAccessException {
        Class classBySign = StockStaticModuleUtil.getClassBySign(stockStaticTemplate.getObjectSign());
        DayUpLimitPromotionStatisticBo o = (DayUpLimitPromotionStatisticBo) JsonUtil.readToValue(stockStaticTemplate.getObjectStr(), classBySign);

        List<DayAxiosBaseBO> result = new ArrayList<>();
        for (Map.Entry<String, StrategyBO> entry : map.entrySet()) {
            DayAxiosBaseBO dayAxiosBaseBO = new DayAxiosBaseBO();
            dayAxiosBaseBO.setName(riverRemoteService.getTemplateNameById(entry.getKey()));
            dayAxiosBaseBO.setValue(new BigDecimal(entry.getValue().getTotalNum()));
            result.add(dayAxiosBaseBO);
        }
        DayAxiosBaseBO dayAxiosBaseBO1 = new DayAxiosBaseBO();
        dayAxiosBaseBO1.setName("二板板晋级率");
        try {
            BigDecimal subtract1 = new BigDecimal(map.get(o.getFirstUpLimitTodayId()).getTotalNum()).divide(new BigDecimal(map.get(o.getFirstUpLimitYesterdayId()).getTotalNum()), 4, BigDecimal.ROUND_HALF_UP);
            dayAxiosBaseBO1.setValue(subtract1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        result.add(dayAxiosBaseBO1);


        DayAxiosBaseBO dayAxiosBaseBO2 = new DayAxiosBaseBO();
        dayAxiosBaseBO2.setName("三板晋级率");
        try {
            BigDecimal subtract2 = new BigDecimal(map.get(o.getSecondUpLimitTodayId()).getTotalNum()).divide(new BigDecimal(map.get(o.getSecondUpLimitYesterdayId()).getTotalNum()), 4, BigDecimal.ROUND_HALF_UP);
            dayAxiosBaseBO2.setValue(subtract2);
        } catch (Exception e) {
            log.error(e.getMessage(), e);

        }
        result.add(dayAxiosBaseBO2);


        DayAxiosBaseBO dayAxiosBaseBO3 = new DayAxiosBaseBO();
        dayAxiosBaseBO3.setName("四板晋级率");
        try {
            BigDecimal subtract3 = new BigDecimal(map.get(o.getThirdUpLimitTodayId()).getTotalNum()).divide(new BigDecimal(map.get(o.getThirdUpLimitYesterdayId()).getTotalNum()), 4, BigDecimal.ROUND_HALF_UP);
            dayAxiosBaseBO3.setValue(subtract3);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        result.add(dayAxiosBaseBO3);

        DayAxiosBaseBO dayAxiosBaseBO4 = new DayAxiosBaseBO();
        dayAxiosBaseBO4.setName("四板以上晋级率");
        try {
            BigDecimal subtract4 = new BigDecimal(map.get(o.getFourUpLimitTodayId()).getTotalNum()).divide(new BigDecimal(map.get(o.getFourUpLimitYesterdayId()).getTotalNum()), 4, BigDecimal.ROUND_HALF_UP);
            dayAxiosBaseBO4.setValue(subtract4);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        result.add(dayAxiosBaseBO4);
        return result;
    }


}
