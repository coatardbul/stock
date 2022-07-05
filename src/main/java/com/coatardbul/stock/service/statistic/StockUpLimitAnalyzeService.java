package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockUplimitAnalyzeMapper;
import com.coatardbul.stock.model.entity.StockUplimitAnalyze;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

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

    public void add(Map dto) {

        List<StockUplimitAnalyze> stockUplimitAnalyzes = stockUplimitAnalyzeMapper.selectAllByDateAndCode(
                (String) dto.get("dateStr"),
                (String) dto.get("code"));
        if (stockUplimitAnalyzes == null || stockUplimitAnalyzes.size() == 0) {
            StockUplimitAnalyze stockUplimitAnalyze = new StockUplimitAnalyze();
            stockUplimitAnalyze.setId(baseServerFeign.getSnowflakeId());
            stockUplimitAnalyze.setCode((String) dto.get("code"));
            stockUplimitAnalyze.setDate((String) dto.get("dateStr"));
            stockUplimitAnalyze.setObjectSign((String) dto.get("objectEnumSign"));
            stockUplimitAnalyze.setJsonDetail(JsonUtil.toJson(dto));
            stockUplimitAnalyzeMapper.insert(stockUplimitAnalyze);
        }
    }

    public List<Map> getAll() {
        List<StockUplimitAnalyze> stockUplimitAnalyzes = stockUplimitAnalyzeMapper.selectByAll();
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
