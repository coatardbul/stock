package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeStrategyMapper;
import com.coatardbul.stock.model.dto.StockTradeStrategyDTO;
import com.coatardbul.stock.model.entity.StockTradeStrategy;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
public class StockTradeStrategyService {

    @Autowired
    StockTradeStrategyMapper stockTradeStrategyMapper;

    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;
    public void add(StockTradeStrategy stockTradeStrategy){
        stockTradeStrategy.setId(baseServerFeign.getSnowflakeId());
        stockTradeStrategyMapper.insert(stockTradeStrategy);
    }

    public List<StockTradeStrategy> findAll(StockTradeStrategyDTO dto) {
     return  stockTradeStrategyMapper.selectAllByTypeAndNameLikeAndExpressExampleLike(dto.getType(), dto.getName(), dto.getExpressExample());
    }

    public void modify(StockTradeStrategy dto) {
        stockTradeStrategyMapper.updateByPrimaryKeySelective(dto);
    }

    public void delete(StockTradeStrategy dto) {
        stockTradeStrategyMapper.deleteByPrimaryKey(dto.getId());
    }
}
