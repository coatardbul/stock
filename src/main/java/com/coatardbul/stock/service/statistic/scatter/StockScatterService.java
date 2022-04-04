package com.coatardbul.stock.service.statistic.scatter;

import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.entity.StockScatterStatic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
@Slf4j
public class StockScatterService {

    @Autowired
    ScatterDayUpLimitService stockScatterUpLimitService;
    @Autowired
    ScatterDayUpLimitMarketValueService stockScatterUpLimitMarketValueService;

    private ScatterDayAbstractService buildScatterService(String objectSign) {
        if (StockStaticModuleUtil.DAY_MARKET_VALUE_STATISTIC.equals(objectSign)) {
            return stockScatterUpLimitMarketValueService;
        }
        if (StockStaticModuleUtil.DAY_CALL_AUCTION_STATISTIC.equals(objectSign)) {
            return stockScatterUpLimitService;
        }
        return null;
    }

    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
        buildScatterService(dto.getObjectEnumSign()).refreshDay(dto);
    }

    public void refreshDayRange(StockEmotionDayRangeDTO dto) {
        buildScatterService(dto.getObjectEnumSign()).refreshDayRange(dto);

    }

    public void forceRefreshDayRange(StockEmotionDayRangeDTO dto) {
        buildScatterService(dto.getObjectEnumSign()).forceRefreshDayRange(dto);
    }

    public List<StockScatterStatic> getRangeStatic(StockEmotionRangeDayDTO dto) {
       return buildScatterService(dto.getObjectEnumSign()).getRangeStatic(dto);

    }
}
