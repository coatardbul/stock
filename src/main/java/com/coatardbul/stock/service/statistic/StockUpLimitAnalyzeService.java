package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockUplimitAnalyzeMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockDefineStaticDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.dto.StockUplimitAnalyzeDTO;
import com.coatardbul.stock.model.entity.StockDefineStatic;
import com.coatardbul.stock.model.entity.StockUplimitAnalyze;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    RiverRemoteService riverRemoteService;

    @Autowired
    StockStrategyService stockStrategyService;

    public void add(Map dto) throws IllegalAccessException {

        stockUplimitAnalyzeMapper.deleteByDateAndCode(
                (String) dto.get("dateStr"),
                (String) dto.get("code"));
        StockUplimitAnalyze stockUplimitAnalyze = new StockUplimitAnalyze();
        stockUplimitAnalyze.setId(baseServerFeign.getSnowflakeId());
        rebuild(stockUplimitAnalyze, dto);
        stockUplimitAnalyzeMapper.insert(stockUplimitAnalyze);
    }

    private void rebuild(StockUplimitAnalyze stockUplimitAnalyze, Map dto) throws IllegalAccessException {
        stockUplimitAnalyze.setCode(String.valueOf(dto.get("code")));
        stockUplimitAnalyze.setDate(String.valueOf(dto.get("dateStr")));
        stockUplimitAnalyze.setObjectSign(String.valueOf(dto.get("objectEnumSign")));
        ReflexUtil.singleSetMaptoObject(dto, stockUplimitAnalyze, stockUplimitAnalyze.getClass());
        stockUplimitAnalyze.setLastTurnOver(String.valueOf(dto.get("lastTurnOverRate")));
        stockUplimitAnalyze.setLastVol(String.valueOf(dto.get("lastVolRate")));
        stockUplimitAnalyze.setAuctionIncrease(String.valueOf(dto.get("auctionIncreaseRate")));
        stockUplimitAnalyze.setNewIncrease(String.valueOf(dto.get("newIncreaseRate")));
        stockUplimitAnalyze.setAuctionTurnOver(String.valueOf(dto.get("auctionTurnOverRate")));
        stockUplimitAnalyze.setMarketValue(String.valueOf(dto.get("marketValue")));
        stockUplimitAnalyze.setCirculationMarketValue(String.valueOf(dto.get("circulationMarketValue")));
        stockUplimitAnalyze.setCurrentPrice(String.valueOf(dto.get("newPrice")));

        stockUplimitAnalyze.setJsonDetail(JsonUtil.toJson(dto));

    }

    public List<Map> getAll(StockUplimitAnalyzeDTO dto) {
        PageHelper.startPage(1, 600);
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

    public void onlyAdd(Map dto) throws IllegalAccessException {
        StockUplimitAnalyze stockUplimitAnalyze = new StockUplimitAnalyze();
        stockUplimitAnalyze.setId(baseServerFeign.getSnowflakeId());
        rebuild(stockUplimitAnalyze, dto);
        stockUplimitAnalyzeMapper.insert(stockUplimitAnalyze);
    }

    /**
     * 模拟添加二板数据的数据
     *
     * @param dto
     */
    public void simulateAdd(StockDefineStaticDTO dto) {
        //查看开始，结束时间之间二板的信息
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDateStr(), dto.getEndDateStr());
        if (dateIntervalList != null && dateIntervalList.size() > 0) {

            for (String dateStr : dateIntervalList) {
                Constant.onceUpLimitThreadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        stockUplimitAnalyzeMapper.deleteByDate(dateStr);
                        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                        stockStrategyQueryDTO.setRiverStockTemplateSign(StockTemplateEnum.FIRST_UP_LIMIT_WATCH_TWO.getSign());
                        stockStrategyQueryDTO.setDateStr(dateStr);
                        try {
                            StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                            Integer totalNum = strategy.getTotalNum();
                            if (totalNum > 0) {
                                JSONArray data = strategy.getData();
                                for (int i = 0; i < data.size(); i++) {
                                    JSONObject jsonObject = data.getJSONObject(i);
                                    Map convert = convert(jsonObject, dateStr);
                                    if(convert.size()>0){
                                        onlyAdd(convert);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


            }

        }
    }

    public Map convert(JSONObject jsonObject, String dateStr) {

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("dateStr", dateStr);
        dateStr=dateStr.replaceAll("-","");
        Set<String> keys = jsonObject.keySet();
        for (String key : keys) {
            if (key.equals("封单范围")) {
                jsonMap.put("upLimitVolRange", jsonObject.get(key));
            }
            if (key.equals("首次涨停时间")) {
                jsonMap.put("firstUplimitTime", jsonObject.get(key));
            }
            if (key.equals("首次封单差值")) {
                jsonMap.put("upLimitMixSubVolRange", jsonObject.get(key));
            }
            if (key.equals("有效封单差值")) {
                jsonMap.put("upLimitMaxSubVolRange", jsonObject.get(key));
            }
            if (key.equals("打开涨停次数")) {
                jsonMap.put("upLimitOpenNum", jsonObject.get(key));
            }
            if (key.equals("涨停强弱概览")) {
                jsonMap.put("upLimitDetail", jsonObject.get(key));
            }
            if (key.equals("code")) {
                jsonMap.put("code", jsonObject.get(key));
            }
            if (key.equals("股票简称")) {
                jsonMap.put("name", jsonObject.get(key));
            }
            if (key.indexOf("竞价涨幅") > -1 && key.indexOf(dateStr) > -1
            ) {
                jsonMap.put("auctionIncreaseRate", jsonObject.get(key));
            }
            if (key.indexOf("1日avl") > -1 && key.indexOf("收盘价:不复权") > -1
            ) {
                jsonMap.put("avgPriceRate", jsonObject.get(key));
            }
            //昨日竞价涨幅
            if (key.indexOf("竞价涨幅") > -1 && key.indexOf(dateStr) == -1
            ) {
                jsonMap.put("lastAuctionIncreaseRate", jsonObject.get(key));
            }
            if (key.indexOf("竞价金额") > -1 && key.indexOf(dateStr) > -1 && key.indexOf("{/}") < 0
            ) {
                jsonMap.put("auctionTradeAmount", jsonObject.get(key));
            }
            if (key.indexOf("总市值") > -1 && key.indexOf(dateStr) > -1
            ) {
                jsonMap.put("marketValue", jsonObject.get(key));
            }
            if (key.indexOf("涨停原因类别") > -1
            ) {
                jsonMap.put("theme", jsonObject.get(key));
            }
            if (key.indexOf("市值") > -1 && key.indexOf("总") < 0
            ) {
                jsonMap.put("circulationMarketValue", jsonObject.get(key));
            }
            if (key.indexOf("股票代码") > -1
            ) {
                String[] split = jsonObject.getString(key).split("\\.");
                jsonMap.put("codeUrl", split[1].toLowerCase() + split[0]);
            }
            if (key.indexOf("换手率") > -1 && key.indexOf("分时") < 0 && key.indexOf(dateStr) < 0 && key.indexOf("{/}") < 0
            ) {
                jsonMap.put("lastTurnOverRate", jsonObject.get(key));
            }
            if (key.indexOf("量比") > -1 && key.indexOf(dateStr) < 0 && key.indexOf("{/}") < 0
            ) {
                jsonMap.put("lastVolRate", jsonObject.get(key));
            }
            if (key.indexOf("成交额") > -1 && key.indexOf(dateStr) < 0 && key.indexOf("{/}") < 0 && key.indexOf("分时") < 0
            ) {
                jsonMap.put("lasttradeAmount", jsonObject.get(key));
            }

            if (key.indexOf("涨跌幅") > -1 && key.indexOf(dateStr) > -1
            ) {
                jsonMap.put("newIncreaseRate", jsonObject.get(key));
            }
            if (key.indexOf("成交额") > -1 && key.indexOf(dateStr) > -1 && key.indexOf("{/}") < 0
            ) {
                jsonMap.put("tradeAmount", jsonObject.get(key));
            }
            if (key.indexOf("分时换手率") > -1 && key.indexOf(dateStr) > -1
            ) {
                jsonMap.put("auctionTurnOverRate", jsonObject.get(key));
            }
            if (key.indexOf("分时量比") > -1 && key.indexOf(dateStr) > -1 && key.indexOf("09:25") > 0
            ) {
                jsonMap.put("auctionVol", jsonObject.get(key));
            }
            if (key.indexOf("换手率") > -1 && key.indexOf(dateStr) > -1 && key.indexOf("分时") < 0
            ) {
                jsonMap.put("turnOverRate", jsonObject.get(key));
            }
            if (key.indexOf("收盘价") > -1 && key.indexOf(dateStr) > -1
            ) {
                jsonMap.put("newPrice", jsonObject.get(key));
            }

        }
        BigDecimal subtract = new BigDecimal(String.valueOf(jsonMap.get("newIncreaseRate")))
                .subtract(new BigDecimal(String.valueOf(jsonMap.get("auctionIncreaseRate"))));
        jsonMap.put("subIncreaseRate", subtract);
        //昨日换手/昨日量比  是否埋伏
        BigDecimal divide = new BigDecimal(String.valueOf(jsonMap.get("lastTurnOverRate")))
                .divide(new BigDecimal(String.valueOf(jsonMap.get("lastVolRate"))), 2, BigDecimal.ROUND_HALF_UP);
        jsonMap.put("compressionDivision", divide);
        //竞价动能比例，今日竞价金额/昨日成交额
        BigDecimal divide1 = new BigDecimal(String.valueOf(jsonMap.get("auctionTradeAmount"))).multiply(new BigDecimal(100))
                .divide(new BigDecimal(String.valueOf(jsonMap.get("lasttradeAmount"))), 2, BigDecimal.ROUND_HALF_UP);
        jsonMap.put("auctionPowerRate", divide1);
        jsonMap.put("objectSign",StockTemplateEnum.FIRST_UP_LIMIT_WATCH_TWO.getSign());
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(jsonMap.get("lastAuctionIncreaseRate")));
        if(bigDecimal.compareTo(new BigDecimal(-5))>0&&bigDecimal.compareTo(new BigDecimal(3))<0){
            return jsonMap;
        }else {
            return new HashMap();
        }
    }


}
