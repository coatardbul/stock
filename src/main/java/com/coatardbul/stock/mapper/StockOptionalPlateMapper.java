package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockOptionalPlate;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockOptionalPlateMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockOptionalPlate record);

    int insertSelective(StockOptionalPlate record);

    StockOptionalPlate selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockOptionalPlate record);

    int updateByPrimaryKey(StockOptionalPlate record);

    List<StockOptionalPlate> selectAllByNameLike(@Param("likeName") String likeName);
}