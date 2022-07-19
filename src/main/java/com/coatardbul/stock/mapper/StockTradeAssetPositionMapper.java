package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockTradeAssetPosition;

public interface StockTradeAssetPositionMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeAssetPosition record);

    int insertSelective(StockTradeAssetPosition record);

    StockTradeAssetPosition selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeAssetPosition record);

    int updateByPrimaryKey(StockTradeAssetPosition record);

    List<StockTradeAssetPosition> selectByAll(StockTradeAssetPosition stockTradeAssetPosition);


}