package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockTradeBuyTask;

public interface StockTradeBuyTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeBuyTask record);

    int insertSelective(StockTradeBuyTask record);

    StockTradeBuyTask selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeBuyTask record);

    int updateByPrimaryKey(StockTradeBuyTask record);

    List<StockTradeBuyTask> selectByAll(StockTradeBuyTask stockTradeBuyTask);


}