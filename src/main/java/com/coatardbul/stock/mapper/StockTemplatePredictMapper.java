package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockTemplatePredict;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockTemplatePredictMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTemplatePredict record);

    int insertSelective(StockTemplatePredict record);

    StockTemplatePredict selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockTemplatePredict record);

    int updateByPrimaryKey(StockTemplatePredict record);

    List<StockTemplatePredict> selectAllByDateBetweenEqualAndTemplatedIdAndHoldDay(@Param("minDate") String minDate, @Param("maxDate") String maxDate, @Param("templatedId") String templatedId, @Param("holdDay") Integer holdDay);

    int deleteByDateAndTempatedId(@Param("date") String date, @Param("tempatedId") String tempatedId);
}