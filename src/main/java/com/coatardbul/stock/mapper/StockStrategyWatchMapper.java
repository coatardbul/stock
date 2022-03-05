package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockStrategyWatch;

public interface StockStrategyWatchMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockStrategyWatch record);

    int insertSelective(StockStrategyWatch record);

    StockStrategyWatch selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockStrategyWatch record);

    int updateByPrimaryKey(StockStrategyWatch record);



    List<StockStrategyWatch> selectAllByType(@Param("type")Integer type);


}