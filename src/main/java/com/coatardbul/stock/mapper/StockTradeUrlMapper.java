package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockTradeUrl;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockTradeUrlMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTradeUrl record);

    int insertSelective(StockTradeUrl record);

    StockTradeUrl selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTradeUrl record);

    int updateByPrimaryKey(StockTradeUrl record);

    List<StockTradeUrl> selectByAll(StockTradeUrl stockTradeUrl);

    List<StockTradeUrl> selectAllBySign(@Param("sign") String sign);


    int updateValidateKey(@Param("updatedValidateKey")String updatedValidateKey);


}