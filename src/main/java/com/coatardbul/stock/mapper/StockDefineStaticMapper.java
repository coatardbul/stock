package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockDefineStatic;

public interface StockDefineStaticMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockDefineStatic record);

    int insertSelective(StockDefineStatic record);

    StockDefineStatic selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockDefineStatic record);

    int updateByPrimaryKey(StockDefineStatic record);

    StockDefineStatic selectAllByDateAndObjectSign(@Param("date")String date,@Param("objectSign")String objectSign);


    int deleteByDateAndObjectSign(@Param("date")String date,@Param("objectSign")String objectSign);



    List<StockDefineStatic> selectAllByDateBetweenEqualAndObjectSign(@Param("minDate")String minDate,@Param("maxDate")String maxDate,@Param("objectSign")String objectSign);



}