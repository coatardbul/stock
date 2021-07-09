package com.coatardbul.stock;

import com.coatardbul.stock.mapper.StockPriceMapper;
import com.coatardbul.stock.model.entity.StockPrice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StockApplicationTests {
@Autowired
private StockPriceMapper stockPriceMapper;
    @Test
    void contextLoads() {
        StockPrice stockPrice=new StockPrice();
        stockPrice.setCode("000234");
        stockPrice.setName("xxxxx");
        stockPrice.setOpenPrice(123L);
        stockPrice.setClosePrice(1111L);
        stockPrice.setMinPrice(2222L);
        stockPrice.setMaxPrice(3333L);
        stockPrice.setLastClosePrice(4444L);
        stockPrice.setTurnOverRate(5555L);
        stockPrice.setQuantityRelativeRatio(6666L);
        stockPrice.setVolumn(4444);
        stockPrice.setDate("20210212");
        stockPriceMapper.insert(stockPrice);


    }

}
