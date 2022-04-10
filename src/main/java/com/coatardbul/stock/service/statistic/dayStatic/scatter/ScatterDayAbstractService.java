package com.coatardbul.stock.service.statistic.dayStatic.scatter;

import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockDayEmotionMapper;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockScatterStaticMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.entity.StockScatterStatic;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.coatardbul.stock.service.statistic.dayStatic.DayService;
import com.coatardbul.stock.service.statistic.StockVerifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/1
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public abstract class ScatterDayAbstractService implements DayService {
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
    @Autowired
    StockVerifyService stockVerifyService;
    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(dto.getObjectEnumSign());
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("对象标识异常");
        }
        //模型策略数据
        StockStaticTemplate stockStaticTemplate = stockStaticTemplates.get(0);

        refreshDayProcess(dto, stockStaticTemplate);
    }

    /**
     * 子类实现
     *
     * @param dto
     * @param stockStaticTemplate
     * @throws IllegalAccessException
     * @throws ParseException
     */
    public void refreshDayProcess(StockEmotionDayDTO dto, StockStaticTemplate stockStaticTemplate) throws IllegalAccessException, ParseException {

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
            } catch (IllegalAccessException | ParseException e) {
                log.error(e.getMessage(), e);
            }

        }
    }

    public List<StockScatterStatic> getDayStatic(StockEmotionQueryDTO dto) {
        List<StockScatterStatic> stockDayEmotions = stockScatterStaticMapper.selectAllByDateAndObjectSign(dto.getDateStr(), dto.getObjectEnumSign());
        if (stockDayEmotions != null && stockDayEmotions.size() > 0) {
            return stockDayEmotions.stream().sorted(Comparator.comparing(StockScatterStatic::getDate)).collect(Collectors.toList());
        } else {
            return null;
        }
    }


    public void forceRefreshDayRange(StockEmotionDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            StockEmotionDayDTO stockEmotionDayDTO = new StockEmotionDayDTO();
            stockEmotionDayDTO.setDateStr(dateStr);
            stockEmotionDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
            try {
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
    public void deleteDay(StockEmotionDayDTO dto) {
        stockScatterStaticMapper.deleteByDateAndObjectSign(dto.getDateStr(),dto.getObjectEnumSign());
    }
}
