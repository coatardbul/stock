package com.coatardbul.stock.service.statistic.dayStatic;

import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.entity.StockDayEmotion;
import com.coatardbul.stock.service.statistic.dayStatic.dayBaseChart.StockDayTrumpetCalcService;
import com.coatardbul.stock.service.statistic.dayStatic.dayBaseChart.StockDayUpLimitPromotionService;
import com.coatardbul.stock.service.statistic.dayStatic.dayBaseChart.StockDayUpLimitStaticService;
import com.coatardbul.stock.service.statistic.dayStatic.scatter.ScatterDayUpLimitCallAuctionService;
import com.coatardbul.stock.service.statistic.dayStatic.scatter.ScatterDayUpLimitMarketValueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.List;

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
public class StockDayStaticService {
    @Autowired
    StockDayTrumpetCalcService stockDayTrumpetCalcService;

    @Autowired
    StockDayUpLimitStaticService stockDayUpLimitStaticService;
    @Autowired
    StockDayUpLimitPromotionService stockDayUpLimitPromotionService;
    @Autowired
    ScatterDayUpLimitMarketValueService scatterDayUpLimitMarketValueService;
    @Autowired
    ScatterDayUpLimitCallAuctionService scatterDayUpLimitCallAuctionService;

    private DayService buildScatterService(String objectSign) {
        if (StockStaticModuleUtil.DAY_UP_DOW_LIMIT_STATISTIC.equals(objectSign)) {
            return stockDayUpLimitStaticService;
        }
        if (StockStaticModuleUtil.DAY_TRUMPET_CALC_STATISTIC.equals(objectSign)) {
            return stockDayTrumpetCalcService;
        }
        if (StockStaticModuleUtil.DAY_UP_LIMIT_PROMOTION_STATISTIC.equals(objectSign)) {
            return stockDayUpLimitPromotionService;
        }
        if (StockStaticModuleUtil.DAY_MARKET_VALUE_STATISTIC.equals(objectSign)) {
            return scatterDayUpLimitMarketValueService;
        }
        if (StockStaticModuleUtil.DAY_CALL_AUCTION_STATISTIC.equals(objectSign)) {
            return scatterDayUpLimitCallAuctionService;
        }
        return null;
    }

    /**
     * 刷新当日数据，全量刷新
     *
     * @param dto
     * @throws IllegalAccessException
     */
    public void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException {
        buildScatterService(dto.getObjectEnumSign()).refreshDay(dto);
    }

    /**
     * 表中有数据，不刷新，无数据，增量刷新
     *
     * @param dto
     */
    public void refreshDayRange(StockEmotionDayRangeDTO dto) {
        buildScatterService(dto.getObjectEnumSign()).refreshDayRange(dto);

    }

    public List<StockDayEmotion> getDayStatic(StockEmotionQueryDTO dto) {
        return buildScatterService(dto.getObjectEnumSign()).getDayStatic(dto);
    }

    public List<StockDayEmotion> getRangeStatic(StockEmotionRangeDayDTO dto) {
        return buildScatterService(dto.getObjectEnumSign()).getRangeStatic(dto);
    }

    public void forceRefreshDayRange(StockEmotionDayRangeDTO dto) {
        buildScatterService(dto.getObjectEnumSign()).forceRefreshDayRange(dto);

    }

    public void deleteDay(StockEmotionDayDTO dto) {
        buildScatterService(dto.getObjectEnumSign()).deleteDay(dto);

    }
}
