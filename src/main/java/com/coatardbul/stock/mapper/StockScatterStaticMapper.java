package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockDayEmotion;
import org.apache.ibatis.annotations.Param;

import com.coatardbul.stock.model.entity.StockScatterStatic;

import java.util.List;

public interface StockScatterStaticMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockScatterStatic record);

    int insertSelective(StockScatterStatic record);

    StockScatterStatic selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockScatterStatic record);

    int updateByPrimaryKey(StockScatterStatic record);


    int deleteByDateAndObjectSign(@Param("date") String date, @Param("objectSign") String objectSign);


    List<StockScatterStatic> selectAllByDateAndObjectSign(@Param("date") String date, @Param("objectSign") String objectSign);

    List<StockScatterStatic> selectAllByDateBetweenEqualAndObjectSign(@Param("minDate") String minDate, @Param("maxDate") String maxDate, @Param("objectSign") String objectSign);

}