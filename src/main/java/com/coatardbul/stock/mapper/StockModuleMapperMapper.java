package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockModuleMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface StockModuleMapperMapper {
    int deleteByPrimaryKey(@Param("stockCode") String stockCode, @Param("moduleCode") String moduleCode);

    int insert(StockModuleMapper record);

    int insertSelective(StockModuleMapper record);

    int deleteByStockCode(@Param("stockCode")String stockCode);


}