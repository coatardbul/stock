package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.BigRoot;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockDayEmotionMapper;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.mapper.StockThemeStaticMapper;
import com.coatardbul.stock.model.bo.DayAxiosBaseBO;
import com.coatardbul.stock.model.bo.StockStaticBaseBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.ThemeBaseInfoBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.dto.StockThemeDayDTO;
import com.coatardbul.stock.model.dto.StockThemeDayRangeDTO;
import com.coatardbul.stock.model.entity.StockDayEmotion;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.entity.StockThemeStatic;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
 * Date: 2022/5/3
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockThemeService {

    @Autowired
    StockThemeStaticMapper stockThemeStaticMapper;
    @Autowired
    StockVerifyService stockVerifyService;
    @Autowired
    StockStaticTemplateMapper stockStaticTemplateMapper;
    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    @Autowired
    StockParseAndConvertService stockParseAndConvertService;

    public void refreshDay(StockThemeDayDTO dto) throws ParseException {
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setThemeStr(dto.getThemeStr());
        stockStrategyQueryDTO.setRiverStockTemplateSign(dto.getObjectEnumSign());
        stockStrategyQueryDTO.setDateStr(dto.getDateStr());
        StrategyBO strategy = null;
        try {
            strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        //对策略进行解析，解析成对象
        List<DayAxiosBaseBO> rebuild = rebuild(strategy, dto.getDateStr());
        if (rebuild == null || rebuild.size() == 0) {
            return;
        }
        //删除主题，日期对应的数据，放到表中
        StockThemeStatic stockThemeStatic = new StockThemeStatic();
        stockThemeStatic.setId(baseServerFeign.getSnowflakeId());
        stockThemeStatic.setDate(dto.getDateStr());
        stockThemeStatic.setTheme(dto.getThemeStr());
        stockThemeStatic.setObjectStaticArray(JsonUtil.toJson(rebuild));
        stockThemeStatic.setObjectSign(dto.getObjectEnumSign());
        stockThemeStaticMapper.deleteByDateAndObjectSignAndTheme(dto.getDateStr(), dto.getObjectEnumSign(), dto.getThemeStr());
        stockThemeStaticMapper.insertSelective(stockThemeStatic);


    }

    public List<DayAxiosBaseBO> rebuild(StrategyBO strategy, String dateStr) {
        String formatDateStr = dateStr.replace("-", "");
        if (strategy == null || strategy.getTotalNum() == 0) {
            return null;
        }
        List<ThemeBaseInfoBO> themeBaseInfoBOS = new ArrayList<>();
        JSONArray data = strategy.getData();
        for (int i = 0; i < data.size(); i++) {
            try {
                ThemeBaseInfoBO themeBaseInfoBO = new ThemeBaseInfoBO();
                themeBaseInfoBO.setDateStr(dateStr);
                JSONObject jsonObject = data.getJSONObject(i);
                for (String key : jsonObject.keySet()) {
                    if (key.contains("竞价涨幅") && key.contains(formatDateStr)) {
                        BigDecimal value = stockParseAndConvertService.convert(jsonObject.get(key));
                        themeBaseInfoBO.setCallAuctionIncreaseRate(value);
                    }
                    if (key.contains("涨跌幅") && key.contains(formatDateStr)) {
                        BigDecimal value = stockParseAndConvertService.convert(jsonObject.get(key));
                        themeBaseInfoBO.setCloseIncreaseRate(value);
                    }
                    if (key.contains("竞价金额") && key.contains(formatDateStr)) {
                        BigDecimal value = stockParseAndConvertService.convert(jsonObject.get(key));
                        themeBaseInfoBO.setCallAuctionTradeAmount(value);
                    }
                    if (key.contains("成交额") && key.contains(formatDateStr)) {
                        BigDecimal value = stockParseAndConvertService.convert(jsonObject.get(key));
                        themeBaseInfoBO.setTradeAmount(value);
                    }
                }
                themeBaseInfoBO.setSubIncreaseRate(themeBaseInfoBO.getCloseIncreaseRate().subtract(themeBaseInfoBO.getCallAuctionIncreaseRate()));
                themeBaseInfoBOS.add(themeBaseInfoBO);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        themeBaseInfoBOS = themeBaseInfoBOS.stream().sorted(Comparator.comparing(ThemeBaseInfoBO::getCallAuctionIncreaseRate)).collect(Collectors.toList());
        return getStaticBase(themeBaseInfoBOS);
    }

    private List<DayAxiosBaseBO> getStaticBase(List<ThemeBaseInfoBO> themeBaseInfoBOS) {
        List<DayAxiosBaseBO> result = new ArrayList<>();
        int medianindex = themeBaseInfoBOS.size() / 2;
        int medianSub = themeBaseInfoBOS.size() % 2;
        if (medianindex != 0) {
            ThemeBaseInfoBO medianStrategy = null;
            if (medianSub == 0) {
                medianStrategy = themeBaseInfoBOS.get(medianindex - 1);
            } else {
                medianStrategy = themeBaseInfoBOS.get(medianindex);
            }
            //标的数
            //中位数
            BigDecimal medianNum = medianStrategy.getCallAuctionIncreaseRate();
            BigDecimal variance = BigDecimal.ZERO;

            //竞价金额
            BigDecimal callAuctionTradeAmount = BigDecimal.ZERO;
            //交易金额
            BigDecimal tradeAmount = BigDecimal.ZERO;
            //涨跌幅
            BigDecimal subIncreaseRate = BigDecimal.ZERO;

            for (int i = 0; i < themeBaseInfoBOS.size(); i++) {
                if (themeBaseInfoBOS.get(i).getCallAuctionIncreaseRate() == null) {
                    continue;
                }
                //方差，标准差  相差的值平方
                BigDecimal b = themeBaseInfoBOS.get(i).getCallAuctionIncreaseRate().subtract(medianNum);
                variance = variance.add(b.multiply(b));
                //竞价金额，交易金额，涨跌幅
                callAuctionTradeAmount = callAuctionTradeAmount.add(themeBaseInfoBOS.get(i).getCallAuctionTradeAmount());
                tradeAmount = tradeAmount.add(themeBaseInfoBOS.get(i).getTradeAmount());
                subIncreaseRate = subIncreaseRate.add(themeBaseInfoBOS.get(i).getSubIncreaseRate());

            }
            DayAxiosBaseBO d1 = new DayAxiosBaseBO();
            d1.setName("中位数");
            d1.setValue(medianNum);
            result.add(d1);
            //方差
            variance = variance.divide(new BigDecimal(themeBaseInfoBOS.size() - 1), 4, BigDecimal.ROUND_HALF_UP);
            //标准差
            DayAxiosBaseBO d2 = new DayAxiosBaseBO();
            d2.setName("标准差");
            d2.setValue(BigRoot.bigRoot(variance, 2, 4, BigDecimal.ROUND_HALF_UP));
            result.add(d2);
            //
            DayAxiosBaseBO d3 = new DayAxiosBaseBO();
            d3.setName("竞价金额平均值");
            d3.setValue(callAuctionTradeAmount.divide(new BigDecimal(themeBaseInfoBOS.size()), 4, BigDecimal.ROUND_HALF_UP));
            result.add(d3);
            //
            DayAxiosBaseBO d4 = new DayAxiosBaseBO();
            d4.setName("成交额平均值");
            d4.setValue(tradeAmount.divide(new BigDecimal(themeBaseInfoBOS.size()), 4, BigDecimal.ROUND_HALF_UP));
            result.add(d4);
            //
            DayAxiosBaseBO d5 = new DayAxiosBaseBO();
            d5.setName("涨跌幅平均值");
            d5.setValue(subIncreaseRate.divide(new BigDecimal(themeBaseInfoBOS.size()), 4, BigDecimal.ROUND_HALF_UP));
            result.add(d5);
            DayAxiosBaseBO d6 = new DayAxiosBaseBO();
            d6.setName("竞价个数");
            d6.setValue(new BigDecimal(themeBaseInfoBOS.size()));
            result.add(d6);
        }
        return result;
    }

    public void refreshDayRange(StockThemeDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            Constant.emotionByDateRangeThreadPool.submit(() -> {
                //表中有数据，直接返回，没有再查询
                List<StockThemeStatic> stockThemeStatics = stockThemeStaticMapper.selectAllByDateAndObjectSignAndTheme(dateStr, dto.getObjectEnumSign(), dto.getThemeStr());
                if (stockThemeStatics != null && stockThemeStatics.size() > 0) {
                    return;
                }
                StockThemeDayDTO stockThemeDayDTO = new StockThemeDayDTO();
                stockThemeDayDTO.setDateStr(dateStr);
                stockThemeDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
                stockThemeDayDTO.setThemeStr(dto.getThemeStr());
                try {
                    refreshDay(stockThemeDayDTO);
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }
            });


        }
    }

    public List<StockThemeStatic> getRangeStatic(StockThemeDayRangeDTO dto) {
        List<StockThemeStatic> stockThemeStatics = stockThemeStaticMapper.selectAllByThemeAndObjectSignAndDateBetweenEqual(dto.getThemeStr(), dto.getObjectEnumSign(), dto.getBeginDate(), dto.getEndDate());
        return stockThemeStatics;
    }

    public void forceRefreshDayRange(StockThemeDayRangeDTO dto) {
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDate(), dto.getEndDate());
        for (String dateStr : dateIntervalList) {
            Constant.emotionByDateRangeThreadPool.submit(() -> {
                StockThemeDayDTO stockThemeDayDTO = new StockThemeDayDTO();
                stockThemeDayDTO.setDateStr(dateStr);
                stockThemeDayDTO.setObjectEnumSign(dto.getObjectEnumSign());
                stockThemeDayDTO.setThemeStr(dto.getThemeStr());
                try {
                    refreshDay(stockThemeDayDTO);
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }
            });


        }
    }
}
