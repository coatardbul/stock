package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockUpLimitValPriceMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.UpLimitValPriceBO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.dto.StockValPriceDTO;
import com.coatardbul.stock.model.entity.StockUpLimitValPrice;
import com.coatardbul.stock.model.feign.StockTemplateQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/4
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockUpLimitValPriceService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;
    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockVerifyService stockVerifyService;
    @Autowired
    StockUpLimitValPriceMapper stockUpLimitValPriceMapper;

    public void execute(StockValPriceDTO dto) {
        //获取查询股票的脚本
        String queryScript = getQueryScript();
        //获取查询的问句
        String queryInfo = getTongHuaShunQueryResult(queryScript, dto.getDateStr(), dto.getCode());
        //策略查询
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setStockCode(dto.getCode());
        stockStrategyQueryDTO.setQueryStr(queryInfo);
        try {
            StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            //动态结果解析
            if (strategy != null) {
                parseStrategyResult(strategy);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void parseStrategyResult(StrategyBO strategy) {
        JSONArray data = strategy.getData();
        Object jo = data.get(0);
        //key YYYYMMDD  value 具体信息
        Map<String, UpLimitValPriceBO> allDateMap = new HashMap<>();
        //解析http请求
        rebuildUpLimitValPrice((JSONObject) jo, allDateMap);
        //股票代码和名称
        String code = ((JSONObject) jo).getString("code");
        String stockName = ((JSONObject) jo).getString("股票简称");

        //查询表中的信息,将表中的信息添加到总的信息里面
        addTableUpLimitValPrice(code, allDateMap);

        List<UpLimitValPriceBO> allUpLimitValPriceList = allDateMap.values().stream().collect(Collectors.toList());
        allUpLimitValPriceList = allUpLimitValPriceList.stream().sorted(Comparator.comparing(UpLimitValPriceBO::getDateStr)).collect(Collectors.toList());

        //过滤数据
        List<UpLimitValPriceBO> filterUpLimitValPriceList = filterUpLimitValPrice(allUpLimitValPriceList);
        //将数据更新到表中
        addUpdateTableDate(code, stockName, filterUpLimitValPriceList);
    }

    /**
     * 将数据更新到表中
     *
     * @param code
     * @param stockName
     * @param filterUpLimitValPriceList 时间由小到大排序
     */
    private void addUpdateTableDate(String code, String stockName, List<UpLimitValPriceBO> filterUpLimitValPriceList) {
        StockUpLimitValPrice addInfo = new StockUpLimitValPrice();
        addInfo.setId(baseServerFeign.getSnowflakeId());
        addInfo.setCode(code);
        addInfo.setName(stockName);
        addInfo.setBeginDate(filterUpLimitValPriceList.get(0).getDateStr());
        addInfo.setEndDate(filterUpLimitValPriceList.get(filterUpLimitValPriceList.size() - 1).getDateStr());
        addInfo.setObjectArray(JsonUtil.toJson(filterUpLimitValPriceList));
        stockUpLimitValPriceMapper.deleteByCode(code);
        stockUpLimitValPriceMapper.insertSelective(addInfo);
    }


    /**
     * 将非涨停的数据过滤出去，
     * 1.如果两个涨停中间有一个非涨停，不过滤
     * 2.如果结束时间前一天为涨停，结束时间未涨停，不过滤
     *
     * @param allUpLimitValPriceList
     * @return
     */
    private List<UpLimitValPriceBO> filterUpLimitValPrice(List<UpLimitValPriceBO> allUpLimitValPriceList) {
        int beginIndex = 0;
        int endIndex = 0;
        //开始过滤操作
        for (int i = 0; i < allUpLimitValPriceList.size(); i++) {
            if (new BigDecimal(9.5).compareTo(allUpLimitValPriceList.get(i).getIncreaseRate()) > 0) {
                if (i + 1 < allUpLimitValPriceList.size()) {
                    if (new BigDecimal(9.5).compareTo(allUpLimitValPriceList.get(i + 1).getIncreaseRate()) > 0) {
                        continue;
                    } else if (i - 1 >= 0 && new BigDecimal(9.5).compareTo(allUpLimitValPriceList.get(i - 1).getIncreaseRate()) > 0) {
                        beginIndex = i + 1;
                    }

                } else {
                    endIndex = allUpLimitValPriceList.size();
                }
            } else {
                if (i + 2 < allUpLimitValPriceList.size()) {
                    if (new BigDecimal(9.5).compareTo(allUpLimitValPriceList.get(i + 1).getIncreaseRate()) > 0
                            && new BigDecimal(9.5).compareTo(allUpLimitValPriceList.get(i + 2).getIncreaseRate()) > 0
                    ) {
                        endIndex = i + 1;
                    }
                    continue;

                } else {
                    endIndex = allUpLimitValPriceList.size();
                }
            }
        }
        List<UpLimitValPriceBO> filterUpLimitValPriceList = allUpLimitValPriceList.subList(beginIndex, endIndex);
        return filterUpLimitValPriceList;
    }


    /**
     * 将表中的数据添加到中数据中
     *
     * @param code
     * @param dateMap
     */
    private void addTableUpLimitValPrice(String code, Map<String, UpLimitValPriceBO> dateMap) {
        StockUpLimitValPrice stockUpLimitValPriceTemp = stockUpLimitValPriceMapper.selectAllByCode(code);
        if (stockUpLimitValPriceTemp != null) {
            Map<String, UpLimitValPriceBO> tableMap = new HashMap<>();
            List<UpLimitValPriceBO> upLimitValPriceTableList = JsonUtil.readToValue(stockUpLimitValPriceTemp.getObjectArray(), new TypeReference<List<UpLimitValPriceBO>>() {
            });
            if (upLimitValPriceTableList != null && upLimitValPriceTableList.size() > 0) {
                tableMap = upLimitValPriceTableList.stream().collect(Collectors.toMap(UpLimitValPriceBO::getDateStr, Function.identity(), (o1, o2) -> o1));
            }
            //去除掉表中和http获取到的数据中公共的日期，以http请求的为准
            for (Map.Entry<String, UpLimitValPriceBO> upLimitValPriceMap : dateMap.entrySet()) {
                if (tableMap.containsKey(upLimitValPriceMap.getKey())) {
                    tableMap.remove(upLimitValPriceMap.getKey());
                }
            }
            //将表中剩余残留的加入到所有数据中
            if (tableMap.size() > 0) {
                dateMap.putAll(tableMap);
            }
        }
    }

    /**
     * 将http请求的涨停量价关系放到map存储
     *
     * @param jo
     * @param dateMap
     */
    private void rebuildUpLimitValPrice(JSONObject jo, Map<String, UpLimitValPriceBO> dateMap) {
        Set<String> keys = jo.keySet();
        for (String key : keys) {
            if (key.contains("竞价金额")) {
                String dateInfo = key.substring(5, key.length() - 1);
                dateMap.put(dateInfo, new UpLimitValPriceBO());
            }
        }
        for (String key : keys) {
            if (key.contains("竞价金额")) {
                for (Map.Entry<String, UpLimitValPriceBO> map : dateMap.entrySet()) {
                    if (key.contains(map.getKey())) {
                        map.getValue().setCallAuctionTradeAmount(convert(jo.get(key)));
                    }
                }
            }
            if (key.contains("竞价涨幅")) {
                for (Map.Entry<String, UpLimitValPriceBO> map : dateMap.entrySet()) {
                    if (key.contains(map.getKey())) {
                        map.getValue().setCallAuctionIncreaseRate(convert(jo.get(key)));
                    }
                }
            }
            if (key.contains("分时换手率")) {
                for (Map.Entry<String, UpLimitValPriceBO> map : dateMap.entrySet()) {
                    if (key.contains(map.getKey())) {
                        map.getValue().setCallAuctionTurnOverRate(convert(jo.get(key)));
                    }
                }
            }
            if (key.contains("成交额")) {
                for (Map.Entry<String, UpLimitValPriceBO> map : dateMap.entrySet()) {
                    if (key.contains(map.getKey())) {
                        map.getValue().setTradeAmount(convert(jo.get(key)));
                    }
                }
            }
            if (key.contains("涨跌幅:前复权")) {
                for (Map.Entry<String, UpLimitValPriceBO> map : dateMap.entrySet()) {
                    if (key.contains(map.getKey())) {
                        map.getValue().setIncreaseRate(convert(jo.get(key)));
                    }
                }
            }
            if (key.contains("换手率") && key.length() == 13) {
                for (Map.Entry<String, UpLimitValPriceBO> map : dateMap.entrySet()) {
                    if (key.contains(map.getKey())) {
                        map.getValue().setTurnOverRate(convert(jo.get(key)));
                        map.getValue().setDateStr(map.getKey());
                    }
                }
            }
        }
    }

    /**
     * 数字转换
     *
     * @param value
     * @return
     */
    private BigDecimal convert(Object value) {
        if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        return (BigDecimal) value;
    }

    /**
     * 量价关系的问句脚本
     *
     * @return
     */
    private String getQueryScript() {
        StringBuffer sb = new StringBuffer();
        sb.append("股票代码包含{{stockCode}},");
        //前4天
        for (int i = 4; i > 0; i--) {
            String dateScript = "{{lastDay" + i + "}}";
            sb.append(getIndexQueryScript(dateScript));
        }
        //当日
        sb.append(getIndexQueryScript("{{today}}"));
        return sb.toString();
    }

    /**
     * 遍历中每一小部分的脚本
     *
     * @param dateScript
     * @return
     */
    private String getIndexQueryScript(String dateScript) {
        StringBuffer sb = new StringBuffer();
        sb.append(dateScript).append("成交额，");
        sb.append(dateScript).append("涨幅，");
        sb.append(dateScript).append("换手率，");
        sb.append(dateScript).append("竞价金额，");
        sb.append(dateScript).append("竞价涨幅，");
        sb.append(dateScript).append("竞价换手率，");
        return sb.toString();
    }

    /**
     * 根据脚本，其他参数从river上获取最终的查询语句
     *
     * @param queryScript 脚本
     * @param dateStr     时间
     * @param code        股票代码
     * @return
     */
    private String getTongHuaShunQueryResult(String queryScript, String dateStr, String code) {
        StockTemplateQueryDTO stockTemplateQueryDto = new StockTemplateQueryDTO();
        stockTemplateQueryDto.setDateStr(dateStr);
        stockTemplateQueryDto.setStockCode(code);
        stockTemplateQueryDto.setStockScript(queryScript);
        CommonResult<String> riverServerFeignResult = riverServerFeign.getQuery(stockTemplateQueryDto);
        if (riverServerFeignResult != null) {
            return riverServerFeignResult.getData();
        } else {
            return null;
        }
    }

    public void delete(StockValPriceDTO dto) {
        stockUpLimitValPriceMapper.deleteByCode(dto.getCode());

    }

    public List<StockUpLimitValPrice> getAll(StockValPriceDTO dto) {
        return stockUpLimitValPriceMapper.selectAllByCodeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(dto.getCode(), dto.getDateStr().replace("-", ""));
    }
}
