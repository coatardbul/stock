package com.coatardbul.stock.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.coatardbul.stock.mapper.StockFinanceMapper;
import com.coatardbul.stock.model.entity.StockFinance;
@Service
public class StockFinanceService{

    @Resource
    private StockFinanceMapper stockFinanceMapper;

    
    public int deleteByPrimaryKey(String code,String date) {
        return stockFinanceMapper.deleteByPrimaryKey(code,date);
    }

    
    public int insert(StockFinance record) {
        return stockFinanceMapper.insert(record);
    }

    
    public int insertSelective(StockFinance record) {
        return stockFinanceMapper.insertSelective(record);
    }

    
    public StockFinance selectByPrimaryKey(String code,String date) {
        return stockFinanceMapper.selectByPrimaryKey(code,date);
    }

    
    public int updateByPrimaryKeySelective(StockFinance record) {
        return stockFinanceMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(StockFinance record) {
        return stockFinanceMapper.updateByPrimaryKey(record);
    }

}
