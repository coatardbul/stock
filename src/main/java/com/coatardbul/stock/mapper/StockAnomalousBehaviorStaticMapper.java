package com.coatardbul.stock.mapper;

import java.util.Collection;

import org.apache.ibatis.annotations.Param;

import java.util.List;

import com.coatardbul.stock.model.entity.StockAnomalousBehaviorStatic;

public interface StockAnomalousBehaviorStaticMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockAnomalousBehaviorStatic record);

    int insertSelective(StockAnomalousBehaviorStatic record);

    StockAnomalousBehaviorStatic selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockAnomalousBehaviorStatic record);

    int updateByPrimaryKey(StockAnomalousBehaviorStatic record);

    StockAnomalousBehaviorStatic selectByCode(@Param("code") String code);

    int deleteByCode(@Param("code") String code);

    List<StockAnomalousBehaviorStatic> selectAll();


    List<StockAnomalousBehaviorStatic> selectAllByCodeIn(@Param("plateIdCollection") Collection<String> plateIdCollection);


}