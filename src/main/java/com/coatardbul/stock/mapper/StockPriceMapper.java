package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockPrice;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockPriceMapper {
    int deleteByPrimaryKey(String code);

    int insert(StockPrice record);

    int insertSelective(StockPrice record);

    StockPrice selectByPrimaryKey(String code);

    int updateByPrimaryKeySelective(StockPrice record);

    int updateByPrimaryKey(StockPrice record);
}