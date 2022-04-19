package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockOptionalPoolMapper;
import com.coatardbul.stock.model.dto.PlateStockAddDTO;
import com.coatardbul.stock.model.dto.StockOptionalPoolQueryDTO;
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

    public List<StockOptionalPool> findAll(StockOptionalPoolQueryDTO dto) {
        return stockOptionalPoolMapper.selectAllByNameLikeAndPlateIdIn(dto.getName(),dto.getPlateList());
    }

    public void addPlateStock(PlateStockAddDTO dto) {
        List<StockOptionalPool> stockOptionalPoolList = dto.getStockOptionalPoolList();
        for (StockOptionalPool stockOptionalPool : stockOptionalPoolList) {
            try {
                stockOptionalPool.setId(baseServerFeign.getSnowflakeId());
                stockOptionalPool.setPlateId(dto.getStockOptionalPlate().getId());
                stockOptionalPoolMapper.insertSelective(stockOptionalPool);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
    }
}
