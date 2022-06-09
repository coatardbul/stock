package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockTradeUrlMapper;
import com.coatardbul.stock.model.entity.StockTradeUrl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/4
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockTradeUrlService {

    @Autowired
    StockTradeUrlMapper stockTradeUrlMapper;
    @Autowired
    BaseServerFeign baseServerFeign;

    public void add(StockTradeUrl dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockTradeUrlMapper.insert(dto);

    }

    public void modify(StockTradeUrl dto) {
        stockTradeUrlMapper.updateByPrimaryKeySelective(dto);
    }

    public void delete(StockTradeUrl dto) {
        stockTradeUrlMapper.deleteByPrimaryKey(dto.getId());
    }

    public   List<StockTradeUrl> findAll() {

        List<StockTradeUrl> stockTradeUrls = stockTradeUrlMapper.selectByAll(null);
        return  stockTradeUrls;
    }

}
