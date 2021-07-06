package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockFinance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockFinanceMapper {
    int deleteByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int insert(StockFinance record);

    int insertSelective(StockFinance record);

    StockFinance selectByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int updateByPrimaryKeySelective(StockFinance record);

    int updateByPrimaryKey(StockFinance record);
}