package com.coatardbul.stock.service.statistic.scatter;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockDayEmotionMapper;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockScatterStaticMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.entity.StockScatterStatic;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;

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
public abstract class ScatterDayAbstractService {
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

    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
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
