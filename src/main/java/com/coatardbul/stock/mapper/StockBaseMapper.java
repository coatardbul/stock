package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockBaseMapper {
    int deleteByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int insert(StockBase record);

    int insertSelective(StockBase record);

    StockBase selectByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int updateByPrimaryKeySelective(StockBase record);

    int updateByPrimaryKey(StockBase record);
}