package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockDayEmotion;
@Mapper
public interface StockDayEmotionMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockDayEmotion record);

    int insertSelective(StockDayEmotion record);

    StockDayEmotion selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockDayEmotion record);

    int updateByPrimaryKey(StockDayEmotion record);

    int deleteByDateAndObjectSign(@Param("date")String date,@Param("objectSign")String objectSign);



    List<StockDayEmotion> selectAllByDateAndObjectSign(@Param("date")String date,@Param("objectSign")String objectSign);


    List<StockDayEmotion> selectAllByDateBetweenEqualAndObjectSign(@Param("minDate")String minDate,@Param("maxDate")String maxDate,@Param("objectSign")String objectSign);


}