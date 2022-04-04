package com.coatardbul.stock.mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Date;

import com.coatardbul.stock.model.entity.ProxyIp;

public interface ProxyIpMapper {
    int deleteByPrimaryKey(String id);

    int insert(ProxyIp record);

    int insertSelective(ProxyIp record);

    ProxyIp selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(ProxyIp record);

    int updateByPrimaryKey(ProxyIp record);

    List<ProxyIp> selectAllByCreateTimeGreaterThanEqualAndUseTimeLessThanEqual(@Param("minCreateTime")Date minCreateTime,@Param("maxUseTime")Integer maxUseTime);


    int deleteByCreateTimeLessThanEqual(@Param("maxCreateTime")Date maxCreateTime);


    int deleteByIp(@Param("ip")String ip);


}