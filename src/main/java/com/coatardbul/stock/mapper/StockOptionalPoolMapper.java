package com.coatardbul.stock.mapper;
import java.util.Collection;

import com.coatardbul.stock.model.entity.StockOptionalPool;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockOptionalPoolMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockOptionalPool record);

    int insertSelective(StockOptionalPool record);

    StockOptionalPool selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockOptionalPool record);

    int updateByPrimaryKey(StockOptionalPool record);

int deleteByPlateId(@Param("plateId")String plateId);



    List<StockOptionalPool> selectAllByNameLikeAndPlateIdIn(@Param("likeName")String likeName,@Param("plateIdCollection")Collection<String> plateIdCollection);



}