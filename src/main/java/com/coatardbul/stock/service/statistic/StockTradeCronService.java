package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeCronMapper;
import com.coatardbul.stock.model.entity.StockTradeCron;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/7/17
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockTradeCronService {

    @Autowired
    StockTradeCronMapper stockTradeCronMapper;

    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void add(StockTradeCron  stockTradeCron){
        stockTradeCron.setId(baseServerFeign.getSnowflakeId());
        stockTradeCronMapper.insert(stockTradeCron);
    }
    public void modify(StockTradeCron  stockTradeCron){
        stockTradeCron.setId(baseServerFeign.getSnowflakeId());
        stockTradeCronMapper.updateByPrimaryKeySelective(stockTradeCron);
    }
    public void delete(StockTradeCron  stockTradeCron){
        stockTradeCronMapper.deleteByPrimaryKey(stockTradeCron.getId());
    }



}
