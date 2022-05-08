package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockFilterMapper;
import com.coatardbul.stock.model.dto.StockFilterDeleteInfoDTO;
import com.coatardbul.stock.model.dto.StockFilterSaveInfoDTO;
import com.coatardbul.stock.model.entity.StockFilter;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/5/8
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockFilterService {
    @Autowired
    StockFilterMapper stockFilterMapper;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;

    public void save(StockFilterSaveInfoDTO dto) {
        String specialDay = riverRemoteService.getSpecialDay(dto.getDate(), 1);
        stockFilterMapper.deleteByDateAndTemplateSign(specialDay,convertSign(dto.getTemplateSign()));
        JSONArray jsonArray = JSON.parseArray(dto.getStockInfoJson());
        for (int i = 0; i < jsonArray.size(); i++) {
            String code = jsonArray.getJSONObject(i).getString("code");
            StockFilter stockFilter = new StockFilter();
            stockFilter.setId(baseServerFeign.getSnowflakeId());
            stockFilter.setDate(specialDay);
            stockFilter.setTemplateSign(convertSign(dto.getTemplateSign()));
            stockFilter.setStockCode(code);
            stockFilter.setExplain("");
            stockFilter.setStatus(0);
            stockFilterMapper.insertSelective(stockFilter);
        }


    }


    private String convertSign(String templateSign) {
        if( StockTemplateEnum.FIRST_UP_LIMIT.getSign().equals(templateSign)){
            return StockTemplateEnum.FIRST_UP_LIMIT_WATCH_TWO.getSign();
        }
        if( StockTemplateEnum.TWO_UP_LIMIT_ABOVE.getSign().equals(templateSign)){
            return StockTemplateEnum.TWO_UP_LIMIT_ABOVE_CALL_AUCTION.getSign();
        }
        return null;
    }

    public void delete(StockFilterDeleteInfoDTO dto) {
        String specialDay = riverRemoteService.getSpecialDay(dto.getDate(), 1);
        stockFilterMapper.deleteByDateAndTemplateSignAndStockCode(specialDay, convertSign(dto.getTemplateSign()), dto.getStockCode());
    }

    public void modify(StockFilter dto) {
        String specialDay = riverRemoteService.getSpecialDay(dto.getDate(), 1);
        StockFilter stockFilter = stockFilterMapper.selectAllByDateAndTemplateSignAndStockCode(specialDay, convertSign(dto.getTemplateSign()), dto.getStockCode());
        stockFilter.setExplain(dto.getExplain());
        if (dto.getStatus() != null) {
            stockFilter.setStatus(1);
        }
        stockFilterMapper.updateByPrimaryKey(stockFilter);
    }

    public List<StockFilter> getFilterInfo(StockFilter dto) {
        String specialDay = riverRemoteService.getSpecialDay(dto.getDate(), 1);
       return stockFilterMapper.selectAllByDateAndTemplateSign(specialDay,convertSign(dto.getTemplateSign()));
    }

    public List<StockFilter> getDirectFilterInfo(StockFilter dto) {
        return stockFilterMapper.selectAllByDateAndTemplateSign(dto.getDate(),dto.getTemplateSign());
    }
}
