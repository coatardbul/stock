package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockTradeBuyStrategy;import java.util.List;

public interface StockTradeBuyStrategyMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeBuyStrategy record);

    int insertSelective(StockTradeBuyStrategy record);

    StockTradeBuyStrategy selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeBuyStrategy record);

    int updateByPrimaryKey(StockTradeBuyStrategy record);

    List<StockTradeBuyStrategy> selectByAll(StockTradeBuyStrategy stockTradeBuyStrategy);
}