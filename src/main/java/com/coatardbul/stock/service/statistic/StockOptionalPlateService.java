package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockOptionalPlateMapper;
import com.coatardbul.stock.model.dto.StockOptionalPlateQueryDTO;
import com.coatardbul.stock.model.entity.StockOptionalPlate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/18
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockOptionalPlateService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockOptionalPlateMapper stockOptionalPlateMapper;


    public void add(StockOptionalPlate dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockOptionalPlateMapper.insertSelective(dto);
    }
    public void modify(StockOptionalPlate dto) {
        stockOptionalPlateMapper.updateByPrimaryKeySelective(dto);
    }
    public void delete(StockOptionalPlate dto) {
        stockOptionalPlateMapper.deleteByPrimaryKey(dto.getId());
    }

    public List<StockOptionalPlate> findAll(StockOptionalPlateQueryDTO dto){
       return stockOptionalPlateMapper.selectAllByNameLike(dto.getName());
    }
}
