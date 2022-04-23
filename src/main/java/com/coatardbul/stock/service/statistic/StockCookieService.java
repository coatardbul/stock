package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockCookieMapper;
import com.coatardbul.stock.model.dto.StockCookieDTO;
import com.coatardbul.stock.model.entity.StockCookie;
import com.coatardbul.stock.service.base.StockStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/1/15
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockCookieService {

    @Autowired
    StockCookieMapper stockCookieMapper;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockStrategyService stockStrategyService;

    public void add(StockCookieDTO dto) {
        StockCookie convert = convert(dto);
        convert.setId(baseServerFeign.getSnowflakeId());
        stockCookieMapper.insert(convert);
    }

    public void modify(StockCookieDTO dto) {
        StockCookie convert = convert(dto);
        stockCookieMapper.updateByPrimaryKeySelective(convert);
    }

    public List<StockCookieDTO> findAll() {
        List<StockCookie> stockCookies = stockCookieMapper.selectAll();
        if (stockCookies != null && stockCookies.size() > 0) {
            return stockCookies.stream().map(this::convert).collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    private StockCookie convert(StockCookieDTO dto) {
        StockCookie result = new StockCookie();
        result.setId(dto.getId());
        result.setTypeKey(dto.getKey());
        result.setCookieValue(dto.getCookie());
        result.setRemark(dto.getRemark());
        return result;
    }

    private StockCookieDTO convert(StockCookie stockCookie) {
        StockCookieDTO dto = new StockCookieDTO();
        dto.setId(stockCookie.getId());
        dto.setKey(stockCookie.getTypeKey());
        dto.setCookie(stockCookie.getCookieValue());
        dto.setRemark(stockCookie.getRemark());
        return dto;

    }

    public void simpleModify(StockCookieDTO dto) {
        stockCookieMapper.updateCookieValue(dto.getCookie());
        stockStrategyService.refreshCookie();
    }
}
