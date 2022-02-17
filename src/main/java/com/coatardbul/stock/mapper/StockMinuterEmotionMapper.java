package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.dto.StockEmotionStaticDTO;
import com.coatardbul.stock.model.entity.StockMinuterEmotion;import org.apache.ibatis.annotations.Param;import java.util.List;

public interface StockMinuterEmotionMapper {
    int deleteByPrimaryKey(String id);

    int insert(StockMinuterEmotion record);

    int insertSelective(StockMinuterEmotion record);

    StockMinuterEmotion selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(StockMinuterEmotion record);

    int updateByPrimaryKey(StockMinuterEmotion record);


    List<StockMinuterEmotion> selectAllByDateAndObjectSignAndTimeInterval(@Param("date")String date, @Param("objectSign")String objectSign, @Param("timeInterval")Integer timeInterval);

    List<StockEmotionStaticDTO> selectStaticInfoByCondition(@Param("date")String date, @Param("objectSign")String objectSign, @Param("timeInterval")Integer timeInterval);


    List<StockMinuterEmotion> selectAllByDateAndObjectSignAndTemplateId(@Param("date")String date,@Param("objectSign")String objectSign,@Param("templateId")String templateId);



	int deleteByDateAndObjectSignAndTimeInterval(@Param("date")String date,@Param("objectSign")String objectSign,@Param("timeInterval")Integer timeInterval);



}