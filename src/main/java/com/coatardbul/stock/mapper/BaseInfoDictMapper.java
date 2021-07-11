package com.coatardbul.stock.mapper;

import com.coatardbul.stock.model.entity.BaseInfoDict;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BaseInfoDictMapper {
    int deleteByPrimaryKey(String code);

    int insert(BaseInfoDict record);

    int insertSelective(BaseInfoDict record);

    BaseInfoDict selectByPrimaryKey(String code);

    int updateByPrimaryKeySelective(BaseInfoDict record);

    int updateByPrimaryKey(BaseInfoDict record);

    int batchInsert(@Param("list") List<BaseInfoDict> list);

    int deleteByType(@Param("type")Integer type);


}