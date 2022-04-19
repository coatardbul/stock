package com.coatardbul.stock.mapper;
import java.util.Collection;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockAnomalousBehaviorDetail;

public interface StockAnomalousBehaviorDetailMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockAnomalousBehaviorDetail record);

    int insertSelective(StockAnomalousBehaviorDetail record);

    StockAnomalousBehaviorDetail selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockAnomalousBehaviorDetail record);

    int updateByPrimaryKey(StockAnomalousBehaviorDetail record);

    StockAnomalousBehaviorDetail selectAllByCodeAndDate(@Param("code")String code, @Param("date")String date);

    List<StockAnomalousBehaviorDetail> selectAllByCode(@Param("code")String code);



    List<StockAnomalousBehaviorDetail> selectAllByDateLessThan(@Param("maxDate")String maxDate);


    List<StockAnomalousBehaviorDetail> selectAllByDateLessThanAndCodeIn(@Param("maxDate")String maxDate, @Param("codeCollection")Collection<String> codeCollection);


 List<StockAnomalousBehaviorDetail> selectAllByCodeInAndDateBetweenEqual(@Param("codeCollection")Collection<String> codeCollection,@Param("minDate")String minDate,@Param("maxDate")String maxDate);



 int deleteByCode(@Param("code")String code);



}