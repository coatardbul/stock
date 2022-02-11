package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.StockEmotion;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockEmotionMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockEmotion record);

    int insertSelective(StockEmotion record);

    StockEmotion selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockEmotion record);

    int updateByPrimaryKey(StockEmotion record);

    List<StockEmotion> selectAllByDateAndObjectSign(@Param("date") String date, @Param("objectSign") String objectSign);
}