package com.coatardbul.stock.service.statistic.dayStatic;

import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockEmotionQueryDTO;
import com.coatardbul.stock.model.dto.StockEmotionRangeDayDTO;
import com.coatardbul.stock.model.entity.StockDayEmotion;

import java.text.ParseException;
import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/8
 *
 * @author Su Xiaolei
 */
public interface DayService {
     void refreshDay(StockEmotionDayDTO dto) throws IllegalAccessException, ParseException;

    /**
     * 表中有数据，不刷新，无数据，增量刷新
     *
     * @param dto
     */
     void refreshDayRange(StockEmotionDayRangeDTO dto) ;

     List getDayStatic(StockEmotionQueryDTO dto);

    public List getRangeStatic(StockEmotionRangeDayDTO dto) ;

     void forceRefreshDayRange(StockEmotionDayRangeDTO dto);

    public void deleteDay(StockEmotionDayDTO dto) ;
}
