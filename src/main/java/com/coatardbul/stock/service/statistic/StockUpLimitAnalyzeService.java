package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockUplimitAnalyzeMapper;
import com.coatardbul.stock.model.dto.StockUplimitAnalyzeDTO;
import com.coatardbul.stock.model.entity.StockUplimitAnalyze;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/7/3
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockUpLimitAnalyzeService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockUplimitAnalyzeMapper stockUplimitAnalyzeMapper;

    public void add(Map dto) throws IllegalAccessException {

        stockUplimitAnalyzeMapper.deleteByDateAndCode(
                (String) dto.get("dateStr"),
                (String) dto.get("code"));
        StockUplimitAnalyze stockUplimitAnalyze = new StockUplimitAnalyze();
        stockUplimitAnalyze.setId(baseServerFeign.getSnowflakeId());
        stockUplimitAnalyze.setCode(String.valueOf( dto.get("code")));
        stockUplimitAnalyze.setDate(String.valueOf( dto.get("dateStr")));
        stockUplimitAnalyze.setObjectSign(String.valueOf( dto.get("objectEnumSign")));
        stockUplimitAnalyze.setJsonDetail(JsonUtil.toJson(dto));
        ReflexUtil.singleSetMaptoObject(dto, stockUplimitAnalyze, stockUplimitAnalyze.getClass());
        stockUplimitAnalyze.setLastTurnOver(String.valueOf( dto.get("lastTurnOverRate")));
        stockUplimitAnalyze.setLastVol(String.valueOf( dto.get("lastVolRate")));
        stockUplimitAnalyze.setAuctionIncrease(String.valueOf( dto.get("auctionIncreaseRate")));
        stockUplimitAnalyze.setNewIncrease(String.valueOf( dto.get("newIncreaseRate")));

        stockUplimitAnalyze.setAuctionTurnOver(String.valueOf( dto.get("auctionTurnOverRate")));

        stockUplimitAnalyze.setMarketValue(String.valueOf( dto.get("marketValue")));
        stockUplimitAnalyze.setCirculationMarketValue(String.valueOf( dto.get("circulationMarketValue")));
        stockUplimitAnalyze.setCurrentPrice(String.valueOf( dto.get("newPrice")));


        stockUplimitAnalyzeMapper.insert(stockUplimitAnalyze);

    }

    public List<Map> getAll(StockUplimitAnalyzeDTO dto) {
        List<StockUplimitAnalyze> stockUplimitAnalyzes = stockUplimitAnalyzeMapper.selectByCondition(dto);
        if (stockUplimitAnalyzes.size() > 0) {
            return stockUplimitAnalyzes.stream().map(this::convert).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private Map convert(StockUplimitAnalyze dto) {
        String jsonDetail = dto.getJsonDetail();
        if (StringUtils.isNotBlank(jsonDetail)) {
            return JsonUtil.readToValue(jsonDetail, Map.class);
        } else {
            return new HashMap();
        }
    }
}
