package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockUpLimitValPrice;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockUpLimitValPriceMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockUpLimitValPrice record);

    int insertSelective(StockUpLimitValPrice record);

    StockUpLimitValPrice selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockUpLimitValPrice record);

    int updateByPrimaryKey(StockUpLimitValPrice record);

    int deleteByCode(@Param("code") String code);

    StockUpLimitValPrice selectAllByCode(@Param("code") String code);

    StockUpLimitValPrice selectAllByName(@Param("name")String name);



    List<StockUpLimitValPrice> selectAllByCodeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(@Param("code") String code, @Param("maxBeginDate") String maxBeginDate);
}