package com.coatardbul.stock.service.history;

import com.coatardbul.stock.model.bo.ThsPriceBO;
import com.coatardbul.stock.model.bo.YearDayBO;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.service.history.BaseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.coatardbul.stock.mapper.StockPriceMapper;
import com.coatardbul.stock.model.entity.StockPrice;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockPriceService extends BaseService {
    private static final String SPLIT = ",";
    @Resource
    private StockPriceMapper stockPriceMapper;

    public void refreshStockPrice(StockPriceRequestDTO dto) {
        stockPriceMapper.deleteByCodeAndDateBetweenOrEqualTo(dto);
        stockProcess(dto);
    }



    @Override
    protected String getStockUrl(String code) {
        return "http://d.10jqka.com.cn/v6/line/hs_" +
                code +
                "/01/all.js?hexin-v=A6rEhVkvvr5fKTLSdVxojvC1_RtPGy5zIJ-iGTRjVv2IZ0SNHKt-hfAv8m8H";

    }

    @Override
    protected void parseAndSaveDate(String responseString, StockPriceRequestDTO code) throws IOException {
        //json解析
        ObjectMapper objectMapper = new ObjectMapper();
        int beginIndex = responseString.indexOf("{");
        String jsonString = responseString.substring(beginIndex, responseString.length() - 1);
        ThsPriceBO thsPriceBO = objectMapper.readValue(jsonString, ThsPriceBO.class);
        //将数据存入表中
        parseAndSaveDate(thsPriceBO, code);
    }

    void parseAndSaveDate(ThsPriceBO thsPriceBO, StockPriceRequestDTO code) {
        List<StockPrice> stockPriceList = getStockPriceList(thsPriceBO, code);
        //过滤list 中的时间数据
        filterDate(stockPriceList, code);
        for (StockPrice insertObject : stockPriceList) {
            stockPriceMapper.insert(insertObject);
        }
    }

    /**
     * 过滤list 中的时间数据
     *
     * @param stockPriceList
     * @param code
     */
    private void filterDate(List<StockPrice> stockPriceList, StockPriceRequestDTO code) {
        if (StringUtils.isNotBlank(code.getBeginDate()) && StringUtils.isNotBlank(code.getEndDate())) {
            stockPriceList = stockPriceList.stream().filter(o1 -> Integer.parseInt(code.getEndDate()) - Integer.parseInt(o1.getDate()) >= 0 &&
                    Integer.parseInt(code.getBeginDate()) - Integer.parseInt(o1.getDate()) <= 0).collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(code.getBeginDate())) {
            stockPriceList = stockPriceList.stream().filter(o1 -> Integer.parseInt(code.getBeginDate()) - Integer.parseInt(o1.getDate()) <= 0).collect(Collectors.toList());
        } else if (StringUtils.isNotBlank(code.getEndDate())) {
            stockPriceList = stockPriceList.stream().filter(o1 -> Integer.parseInt(code.getEndDate()) - Integer.parseInt(o1.getDate()) >= 0).collect(Collectors.toList());
        } else {

        }
    }

    /**
     * 将爬去的股票信息结息成值
     *
     * @param thsPriceBO
     * @return
     */
    private List<StockPrice> getStockPriceList(ThsPriceBO thsPriceBO, StockPriceRequestDTO code) {
        List<StockPrice> result = new ArrayList<>();
        if (thsPriceBO == null) {
            return result;
        }
        String name = thsPriceBO.getName();
        List<YearDayBO> dayOfYearCountList = getDayOfYearCount(thsPriceBO.getSortYear());
        //成交量
        String[] volumnArray = thsPriceBO.getVolumn().split(SPLIT);
        //最低价，开盘价，最高价，收盘价，价格需要除以100
        String[] priceArray = thsPriceBO.getPrice().split(SPLIT);
        //MMdd
        String[] dateArray = thsPriceBO.getDates().split(SPLIT);

        result = getStockPriceList(code.getCode(), name, priceArray, dateArray, volumnArray, dayOfYearCountList);
        return result;
    }

    private List<StockPrice> getStockPriceList(String code, String name, String[] priceArray, String[] dateArray, String[] volumnArray, List<YearDayBO> dayOfYearCountList) {
        List<StockPrice> result = new ArrayList<>();
        //将年和月放一块
        int index = 0;
        int count = dayOfYearCountList.get(0).getDayOfYear();

        for (int i = 0; i < dateArray.length - 1; i++) {
            StockPrice stockPrice = new StockPrice();
            stockPrice.setCode(code);
            stockPrice.setName(name);
            stockPrice.setOpenPrice(new BigDecimal(priceArray[4 * i + 1]));
            stockPrice.setClosePrice(new BigDecimal(priceArray[4 * i + 3]));
            stockPrice.setMinPrice(new BigDecimal(priceArray[4 * i]));
            stockPrice.setMaxPrice(new BigDecimal(priceArray[4 * i + 2]));
            //TODO
            stockPrice.setLastClosePrice(BigDecimal.ZERO);
            //TODO
            stockPrice.setTurnOverRate(BigDecimal.ZERO);
            //TODO
            stockPrice.setQuantityRelativeRatio(BigDecimal.ZERO);
            //将年月拼接起来
            if (i == count) {
                index++;
                count += dayOfYearCountList.get(index).getDayOfYear();
            }
            stockPrice.setDate(dayOfYearCountList.get(index).getYear().toString() + dateArray[i]);
            stockPrice.setVolumn(Integer.parseInt(volumnArray[i]));
            //价格渲染
            render(stockPrice);
            result.add(stockPrice);
        }
        return result;
    }

    /**
     * 价格渲染
     *
     * @param stockPrice
     */
    private void render(StockPrice stockPrice) {
        stockPrice.setOpenPrice(stockPrice.getMinPrice() .add( stockPrice.getOpenPrice()));
        stockPrice.setClosePrice(stockPrice.getMinPrice() .add( stockPrice.getClosePrice()));
        stockPrice.setMaxPrice(stockPrice.getMinPrice() .add( stockPrice.getMaxPrice()));

        stockPrice.setOpenPrice(stockPrice.getOpenPrice().divide(new BigDecimal(100)));
        stockPrice.setClosePrice(stockPrice.getClosePrice().divide(new BigDecimal(100)));
        stockPrice.setMaxPrice(stockPrice.getMaxPrice().divide(new BigDecimal(100)));
    }

    /**
     * 哪一年  ,这一年有多少天
     *
     * @param sortYear
     * @return
     */
    private List<YearDayBO> getDayOfYearCount(List<List<Integer>> sortYear) {
        if (sortYear == null || sortYear.isEmpty()) {
            return null;
        }
        //哪一年  ,这一年有多少天
        List<YearDayBO> result = new ArrayList<>();
        for (List<Integer> dayOfYearList : sortYear) {
            YearDayBO yearDayBO = new YearDayBO();
            yearDayBO.setYear(dayOfYearList.get(0));
            yearDayBO.setDayOfYear(dayOfYearList.get(1));
            result.add(yearDayBO);
        }
        return result;
    }

}


