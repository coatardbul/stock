package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeAssetPositionMapper;
import com.coatardbul.stock.model.entity.StockTradeAssetPosition;
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
public class StockTradeAssetPositionService {
    @Autowired
    private StockTradeAssetPositionMapper stockTradeAssetPositionMapper;

    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void add(StockTradeAssetPosition dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockTradeAssetPositionMapper.insert(dto);
    }

    public void modify(StockTradeAssetPosition dto) {
        stockTradeAssetPositionMapper.updateByPrimaryKeySelective(dto);

    }

    public void delete(StockTradeAssetPosition dto) {
        stockTradeAssetPositionMapper.deleteByPrimaryKey(dto.getId());
    }



    public Object findAll() {
        return stockTradeAssetPositionMapper.selectByAll(null);
    }
}
