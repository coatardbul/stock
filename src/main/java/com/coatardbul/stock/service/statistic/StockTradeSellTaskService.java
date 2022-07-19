package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeSellTaskMapper;
import com.coatardbul.stock.model.entity.StockTradeSellTask;
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
public class StockTradeSellTaskService {
    @Autowired
    StockTradeSellTaskMapper stockTradeSellTaskMapper;

    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void add(StockTradeSellTask dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockTradeSellTaskMapper.insert(dto);
    }

    public void modify(StockTradeSellTask dto) {
        stockTradeSellTaskMapper.updateByPrimaryKeySelective(dto);

    }

    public void delete(StockTradeSellTask dto) {
        stockTradeSellTaskMapper.deleteByPrimaryKey(dto.getId());
    }


    public Object findAll() {
        return stockTradeSellTaskMapper.selectByAll(null);
    }
}
