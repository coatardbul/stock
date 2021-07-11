package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockBase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StockBaseMapper {
    int deleteByPrimaryKey(String code);

    int insert(StockBase record);

    int insertSelective(StockBase record);

    StockBase selectByPrimaryKey(String code);

    int updateByPrimaryKeySelective(StockBase record);

    int updateByPrimaryKey(StockBase record);
}