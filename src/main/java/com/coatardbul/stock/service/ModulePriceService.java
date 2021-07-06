package com.coatardbul.stock.service;

import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import com.coatardbul.stock.model.entity.ModulePrice;
import com.coatardbul.stock.mapper.ModulePriceMapper;
@Service
public class ModulePriceService{

    @Resource
    private ModulePriceMapper modulePriceMapper;

    
    public int deleteByPrimaryKey(String code,String date) {
        return modulePriceMapper.deleteByPrimaryKey(code,date);
    }

    
    public int insert(ModulePrice record) {
        return modulePriceMapper.insert(record);
    }

    
    public int insertSelective(ModulePrice record) {
        return modulePriceMapper.insertSelective(record);
    }

    
    public ModulePrice selectByPrimaryKey(String code,String date) {
        return modulePriceMapper.selectByPrimaryKey(code,date);
    }

    
    public int updateByPrimaryKeySelective(ModulePrice record) {
        return modulePriceMapper.updateByPrimaryKeySelective(record);
    }

    
    public int updateByPrimaryKey(ModulePrice record) {
        return modulePriceMapper.updateByPrimaryKey(record);
    }

}
