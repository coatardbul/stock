package com.coatardbul.stock.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.coatardbul.stock.model.entity.StockBase;
import com.coatardbul.stock.mapper.StockBaseMapper;
@Service
public class StockBaseService{

    @Resource
    private StockBaseMapper stockBaseMapper;

    
    public int deleteByPrimaryKey(String code,String date) {
        return stockBaseMapper.deleteByPrimaryKey(code,date);
    }

    
    public int insert(StockBase record) {
        return stockBaseMapper.insert(record);
    }

    
    public int insertSelective(StockBase record) {
        return stockBaseMapper.insertSelective(record);
    }

    
    public StockBase selectByPrimaryKey(String code,String date) {
        return stockBaseMapper.selectByPrimaryKey(code,date);
    }

    
    public int updateByPrimaryKeySelective(StockBase record) {
        return stockBaseMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(StockBase record) {
        return stockBaseMapper.updateByPrimaryKey(record);
    }

}
