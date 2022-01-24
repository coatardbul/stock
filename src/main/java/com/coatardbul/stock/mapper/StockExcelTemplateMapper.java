package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

import com.coatardbul.stock.model.entity.StockExcelTemplate;

public interface StockExcelTemplateMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockExcelTemplate record);

    int insertSelective(StockExcelTemplate record);

    StockExcelTemplate selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockExcelTemplate record);

    int updateByPrimaryKey(StockExcelTemplate record);

    List<StockExcelTemplate> selectByAll(StockExcelTemplate stockExcelTemplate);


}