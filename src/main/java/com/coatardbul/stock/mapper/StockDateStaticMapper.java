package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockDateStatic;
@Mapper
public interface StockDateStaticMapper {
    int deleteByPrimaryKey(String date);

    int insert(StockDateStatic record);

    int insertSelective(StockDateStatic record);

    StockDateStatic selectByPrimaryKey(String date);

    int updateByPrimaryKeySelective(StockDateStatic record);

    int updateByPrimaryKey(StockDateStatic record);


    List<StockDateStatic> selectAllByDateBetweenEqual(@Param("minDate")String minDate,@Param("maxDate")String maxDate);


}