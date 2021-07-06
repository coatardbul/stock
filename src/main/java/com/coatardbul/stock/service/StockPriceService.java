package com.coatardbul.stock.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.coatardbul.stock.mapper.StockPriceMapper;
import com.coatardbul.stock.model.entity.StockPrice;
@Service
public class StockPriceService{

    @Resource
    private StockPriceMapper stockPriceMapper;

    
    public int deleteByPrimaryKey(String code) {
        return stockPriceMapper.deleteByPrimaryKey(code);
    }

    
    public int insert(StockPrice record) {
        return stockPriceMapper.insert(record);
    }

    
    public int insertSelective(StockPrice record) {
        return stockPriceMapper.insertSelective(record);
    }

    
    public StockPrice selectByPrimaryKey(String code) {
        return stockPriceMapper.selectByPrimaryKey(code);
    }

    
    public int updateByPrimaryKeySelective(StockPrice record) {
        return stockPriceMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(StockPrice record) {
        return stockPriceMapper.updateByPrimaryKey(record);
    }

}
