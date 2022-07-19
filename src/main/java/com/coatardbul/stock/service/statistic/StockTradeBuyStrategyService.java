package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeBuyStrategyMapper;
import com.coatardbul.stock.model.entity.StockTradeBuyStrategy;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/7/19
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockTradeBuyStrategyService {
    @Autowired
    StockTradeBuyStrategyMapper stockTradeBuyStrategyMapper;

    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void add(StockTradeBuyStrategy dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockTradeBuyStrategyMapper.insert(dto);
    }

    public void modify(StockTradeBuyStrategy dto) {
        stockTradeBuyStrategyMapper.updateByPrimaryKeySelective(dto);

    }

    public void delete(StockTradeBuyStrategy dto) {
        stockTradeBuyStrategyMapper.deleteByPrimaryKey(dto.getId());
    }


    public Object findAll() {
        return stockTradeBuyStrategyMapper.selectByAll(null);
    }
}
