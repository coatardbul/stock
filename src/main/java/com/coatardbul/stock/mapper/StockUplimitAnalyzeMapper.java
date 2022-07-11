package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.dto.StockUplimitAnalyzeDTO;import com.coatardbul.stock.model.entity.StockUplimitAnalyze;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockUplimitAnalyzeMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockUplimitAnalyze record);

    int insertSelective(StockUplimitAnalyze record);

    StockUplimitAnalyze selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockUplimitAnalyze record);

    int updateByPrimaryKey(StockUplimitAnalyze record);

    List<StockUplimitAnalyze> selectByCondition(StockUplimitAnalyzeDTO stockUplimitAnalyze);

    List<StockUplimitAnalyze> selectAllByDateBetweenEqual(@Param("minDate") String minDate, @Param("maxDate") String maxDate);

    int deleteByDateAndCode(@Param("date") String date, @Param("code") String code);
    int deleteByDate(@Param("date")String date);


}