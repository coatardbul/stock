package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockDefineStaticMapper;
import com.coatardbul.stock.model.dto.StockDefineStaticDTO;
import com.coatardbul.stock.model.entity.StockDefineStatic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/7/5
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockDefineStaticService {


    @Autowired
    StockDefineStaticMapper stockDefineStaticMapper;

    @Autowired
    BaseServerFeign baseServerFeign;

    public void add(StockDefineStatic dto) {

        stockDefineStaticMapper.deleteByDateAndObjectSign(dto.getDate(), dto.getObjectSign());
        dto.setId(baseServerFeign.getSnowflakeId());
        stockDefineStaticMapper.insert(dto);


    }

    public List<StockDefineStatic> getAll(StockDefineStaticDTO dto) {
        List<StockDefineStatic> stockDefineStatics = stockDefineStaticMapper.selectAllByDateBetweenEqualAndObjectSign(dto.getBeginDateStr(),
                dto.getEndDateStr(),
                dto.getObjectSign());
        return stockDefineStatics;
    }
}
