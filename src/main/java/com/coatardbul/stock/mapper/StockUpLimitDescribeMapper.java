package com.coatardbul.stock.mapper;
import java.util.Collection;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockUpLimitDescribe;

public interface StockUpLimitDescribeMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockUpLimitDescribe record);

    int insertSelective(StockUpLimitDescribe record);

    StockUpLimitDescribe selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockUpLimitDescribe record);

    int updateByPrimaryKey(StockUpLimitDescribe record);

    StockUpLimitDescribe selectAllByCodeAndDate(@Param("code")String code,@Param("date")String date);

    List<StockUpLimitDescribe> selectAllByCode(@Param("code")String code);



    List<StockUpLimitDescribe> selectAllByDateLessThan(@Param("maxDate")String maxDate);


    List<StockUpLimitDescribe> selectAllByDateLessThanAndCodeIn(@Param("maxDate")String maxDate,@Param("codeCollection")Collection<String> codeCollection);


}