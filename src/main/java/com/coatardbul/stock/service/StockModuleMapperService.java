package com.coatardbul.stock.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.coatardbul.stock.mapper.StockModuleMapperMapper;
import com.coatardbul.stock.model.entity.StockModuleMapper;
@Service
public class StockModuleMapperService{

    @Resource
    private StockModuleMapperMapper stockModuleMapperMapper;

    
    public int deleteByPrimaryKey(String stockCode,String moduleCode) {
        return stockModuleMapperMapper.deleteByPrimaryKey(stockCode,moduleCode);
    }

    
    public int insert(StockModuleMapper record) {
        return stockModuleMapperMapper.insert(record);
    }

    
    public int insertSelective(StockModuleMapper record) {
        return stockModuleMapperMapper.insertSelective(record);
    }

}
