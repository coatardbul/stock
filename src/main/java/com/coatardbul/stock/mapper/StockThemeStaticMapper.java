package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;

import com.coatardbul.stock.model.entity.StockDayEmotion;
import com.coatardbul.stock.model.entity.StockThemeStatic;

import java.util.List;

public interface StockThemeStaticMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockThemeStatic record);

    int insertSelective(StockThemeStatic record);

    StockThemeStatic selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockThemeStatic record);

    int updateByPrimaryKey(StockThemeStatic record);

    List<StockThemeStatic> selectAllByDateAndObjectSignAndTheme(@Param("date")String date,@Param("objectSign")String objectSign,@Param("theme")String theme);



    List<StockThemeStatic> selectAllByThemeAndObjectSignAndDateBetweenEqual(@Param("theme")String theme,@Param("objectSign")String objectSign,@Param("minDate")String minDate,@Param("maxDate")String maxDate);



    int deleteByDateAndObjectSignAndTheme(@Param("date")String date,@Param("objectSign")String objectSign,@Param("theme")String theme);


}