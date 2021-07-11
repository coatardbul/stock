package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.dto.StockPriceRequestDTO;import com.coatardbul.stock.model.entity.StockPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockPriceMapper {
    int deleteByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int insert(StockPrice record);

    int insertSelective(StockPrice record);

    StockPrice selectByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int updateByPrimaryKeySelective(StockPrice record);

    int updateByPrimaryKey(StockPrice record);

    int deleteByPrimaryKey(String code);

    StockPrice selectByPrimaryKey(String code);

    int deleteByCodeAndDateBetweenOrEqualTo(StockPriceRequestDTO dto);
}