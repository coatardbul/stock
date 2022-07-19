package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeBuyTaskMapper;
import com.coatardbul.stock.model.entity.StockTradeBuyTask;
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
public class StockTradeBuyTaskService {
    @Autowired
    StockTradeBuyTaskMapper stockTradeBuyTaskMapper;

    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void add(StockTradeBuyTask dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockTradeBuyTaskMapper.insert(dto);
    }

    public void modify(StockTradeBuyTask dto) {
        stockTradeBuyTaskMapper.updateByPrimaryKeySelective(dto);

    }

    public void delete(StockTradeBuyTask dto) {
        stockTradeBuyTaskMapper.deleteByPrimaryKey(dto.getId());
    }


    public Object findAll() {
        return stockTradeBuyTaskMapper.selectByAll(null);
    }
}
