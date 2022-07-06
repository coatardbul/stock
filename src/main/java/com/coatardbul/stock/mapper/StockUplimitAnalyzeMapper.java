package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;

import com.coatardbul.stock.model.entity.StockUplimitAnalyze;import java.util.List;

public interface StockUplimitAnalyzeMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockUplimitAnalyze record);

    int insertSelective(StockUplimitAnalyze record);

    StockUplimitAnalyze selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockUplimitAnalyze record);

    int updateByPrimaryKey(StockUplimitAnalyze record);

    List<StockUplimitAnalyze> selectByAll();

    List<StockUplimitAnalyze> selectAllByDateAndCode(@Param("date")String date,@Param("code")String code);


}