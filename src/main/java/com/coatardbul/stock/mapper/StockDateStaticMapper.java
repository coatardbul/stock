package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockDateStatic;

public interface StockDateStaticMapper {
    int deleteByPrimaryKey(String date);

    int insert(StockDateStatic record);

    int insertSelective(StockDateStatic record);

    StockDateStatic selectByPrimaryKey(String date);

    int updateByPrimaryKeySelective(StockDateStatic record);

    int updateByPrimaryKey(StockDateStatic record);
}