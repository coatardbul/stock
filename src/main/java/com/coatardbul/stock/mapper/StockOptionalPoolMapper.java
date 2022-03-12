package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockOptionalPool;

public interface StockOptionalPoolMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockOptionalPool record);

    int insertSelective(StockOptionalPool record);

    StockOptionalPool selectByPrimaryKey(String id);

    List<StockOptionalPool> selectByAll(StockOptionalPool stockOptionalPool);

StockOptionalPool selectAllByCodeAndType(@Param("code")String code,@Param("type")Integer type);



    int updateByPrimaryKeySelective(StockOptionalPool record);

    int updateByPrimaryKey(StockOptionalPool record);
}