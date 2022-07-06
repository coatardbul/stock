package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;

import com.coatardbul.stock.model.entity.StockTradeSellJob;import java.util.List;

public interface StockTradeSellJobMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeSellJob record);

    int insertSelective(StockTradeSellJob record);

    StockTradeSellJob selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeSellJob record);

    int updateByPrimaryKey(StockTradeSellJob record);

    List<StockTradeSellJob> selectByAll(StockTradeSellJob stockTradeSellJob);

    List<StockTradeSellJob> selectAllByTypeAndStatus(@Param("type")Integer type,@Param("status")Integer status);


}