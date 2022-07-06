package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockTradeUser;

public interface StockTradeUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeUser record);

    int insertSelective(StockTradeUser record);

    StockTradeUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeUser record);

    int updateByPrimaryKey(StockTradeUser record);
}