package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockTradeBuyConfig;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockTradeBuyConfigMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeBuyConfig record);

    int insertSelective(StockTradeBuyConfig record);

    StockTradeBuyConfig selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeBuyConfig record);

    int updateByPrimaryKey(StockTradeBuyConfig record);

    StockTradeBuyConfig selectAllByTemplateId(@Param("templateId") String templateId);

    List<StockTradeBuyConfig> selectByAll(StockTradeBuyConfig stockTradeBuyConfig);
}