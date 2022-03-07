package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockOptionalPoolMapper;
import com.coatardbul.stock.model.entity.StockOptionalPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/6
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockOptionalPoolService {
@Autowired
    BaseServerFeign baseServerFeign;
@Autowired
StockOptionalPoolMapper stockOptionalPoolMapper;

    public void add(StockOptionalPool dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockOptionalPoolMapper.insertSelective(dto);
    }

    public void modify(StockOptionalPool dto) {
        stockOptionalPoolMapper.updateByPrimaryKeySelective(dto);
    }

    public void delete(StockOptionalPool dto) {
        stockOptionalPoolMapper.deleteByPrimaryKey(dto.getId());
    }

    public List<StockOptionalPool> findAll() {
      return   stockOptionalPoolMapper.selectByAll(null);
    }
}
