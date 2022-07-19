package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockTradeSellTask;

public interface StockTradeSellTaskMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeSellTask record);

    int insertSelective(StockTradeSellTask record);

    StockTradeSellTask selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeSellTask record);

    int updateByPrimaryKey(StockTradeSellTask record);

    List<StockTradeSellTask> selectByAll(StockTradeSellTask stockTradeSellTask);


}