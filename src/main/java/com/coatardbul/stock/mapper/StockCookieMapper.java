package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockCookie;

import java.util.List;

public interface StockCookieMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockCookie record);

    int insertSelective(StockCookie record);

    StockCookie selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockCookie record);

    int updateByPrimaryKey(StockCookie record);


    List<StockCookie> selectAll();


}