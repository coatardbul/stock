package com.coatardbul.stock.mapper;
import java.util.List;

import com.coatardbul.stock.model.entity.StockTemplatePredict;import org.apache.ibatis.annotations.Param;

public interface StockTemplatePredictMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockTemplatePredict record);

    int insertSelective(StockTemplatePredict record);

    StockTemplatePredict selectByPrimaryKey(String id);

    List<StockTemplatePredict> selectAllByDateBetweenEqualAndTemplatedIdAndHoldDay(@Param("minDate")String minDate,@Param("maxDate")String maxDate,@Param("templatedId")String templatedId,@Param("holdDay")Integer holdDay);



    int updateByPrimaryKeySelective(StockTemplatePredict record);

    int updateByPrimaryKey(StockTemplatePredict record);

    int deleteByDateAndTempatedId(@Param("date") String date, @Param("tempatedId") String tempatedId);
}