package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.ModulePrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ModulePriceMapper {
    int deleteByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int insert(ModulePrice record);

    int insertSelective(ModulePrice record);

    ModulePrice selectByPrimaryKey(@Param("code") String code, @Param("date") String date);

    int updateByPrimaryKeySelective(ModulePrice record);

    int updateByPrimaryKey(ModulePrice record);
}