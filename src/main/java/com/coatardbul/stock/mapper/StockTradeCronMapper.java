package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockTradeCron;

public interface StockTradeCronMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeCron record);

    int insertSelective(StockTradeCron record);

    StockTradeCron selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeCron record);

    int updateByPrimaryKey(StockTradeCron record);
}