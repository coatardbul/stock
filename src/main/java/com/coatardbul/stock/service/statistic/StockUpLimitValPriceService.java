package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockUpLimitValPriceMapper;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.StockUpLimitInfoBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.UpLimitDetailInfo;
import com.coatardbul.stock.model.bo.UpLimitStrongWeakBO;
import com.coatardbul.stock.model.bo.UpLimitValPriceBO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.dto.StockUpLimitNumDTO;
import com.coatardbul.stock.model.dto.StockValPriceDTO;
import com.coatardbul.stock.model.entity.StockUpLimitValPrice;
import com.coatardbul.stock.model.feign.StockTemplateQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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
                strongWeak(dto);
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
        if (StringUtils.isNotBlank(dto.getDateStr())) {
            return stockUpLimitValPriceMapper.selectAllByCodeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(dto.getCode(), dto.getDateStr().replace("-", ""));
        } else {
            return stockUpLimitValPriceMapper.selectAllByCodeAndBeginDateLessThanEqualAndEndDateGreaterThanEqual(dto.getCode(), null);

        }
    }

    /**
     * 股票强弱分析
     *
     * @param dto
     */
    public void strongWeak(StockValPriceDTO dto) throws ParseException {
        StockUpLimitValPrice stockUpLimitValPrice = stockUpLimitValPriceMapper.selectAllByCode(dto.getCode());
        if (!StringUtils.isNotBlank(stockUpLimitValPrice.getBeginDate())) {
            throw new BusinessException("开始时间为空，无法分析");
        }
        if (!StringUtils.isNotBlank(stockUpLimitValPrice.getEndDate())) {
            throw new BusinessException("结束时间为空，无法分析");
        }

        String beginDateStr = DateTimeUtil.getDateFormat(DateTimeUtil.parseDateStr(stockUpLimitValPrice.getBeginDate(), DateTimeUtil.YYYYMMDD), DateTimeUtil.YYYY_MM_DD);
        String endDateStr = DateTimeUtil.getDateFormat(DateTimeUtil.parseDateStr(stockUpLimitValPrice.getEndDate(), DateTimeUtil.YYYYMMDD), DateTimeUtil.YYYY_MM_DD);

        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(beginDateStr, endDateStr);

        CountDownLatch countDownLatch = new CountDownLatch(dateIntervalList.size());

        List<UpLimitStrongWeakBO> strongWeakList = new ArrayList<>();
        for (String dateStr : dateIntervalList) {
            StockStrategyQueryDTO strategyQueryDTO = new StockStrategyQueryDTO();
            strategyQueryDTO.setRiverStockTemplateId("1509349533765730304");
            strategyQueryDTO.setDateStr(dateStr);
            strategyQueryDTO.setStockCode(stockUpLimitValPrice.getCode());
            List<UpLimitStrongWeakBO> finalStrongWeakList = strongWeakList;
            Constant.emotionIntervalByDateThreadPool.submit(() -> {
                try {
                    StrategyBO strategy = stockStrategyService.strategy(strategyQueryDTO);
                    if (strategy.getTotalNum() > 0) {
                        UpLimitStrongWeakBO rebuild = rebuild(strategy, dateStr);
                        finalStrongWeakList.add(rebuild);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        strongWeakList = strongWeakList.stream().sorted(Comparator.comparing(UpLimitStrongWeakBO::getDateStr)).collect(Collectors.toList());
        stockUpLimitValPrice.setStrongWeakArray(JsonUtil.toJson(strongWeakList));
        stockUpLimitValPriceMapper.updateByPrimaryKeySelective(stockUpLimitValPrice);
    }

    private UpLimitStrongWeakBO rebuild(StrategyBO strategy, String dateStr) {
        //取里面的数组信息
        JSONObject jo = (JSONObject) strategy.getData().get(0);
        return rebuild(jo, dateStr);

    }

    private UpLimitStrongWeakBO rebuild(JSONObject jo, String dateStr) {
        //取里面的数组信息
        Set<String> keys = jo.keySet();
        List<UpLimitDetailInfo> upLimitDetailList = new ArrayList<>();
        for (String key : keys) {
            if (key.contains("涨停明细数据")) {
                String upLimitDetailStr = (String) jo.get(key);
                upLimitDetailList = JsonUtil.readToValue(upLimitDetailStr, new TypeReference<List<UpLimitDetailInfo>>() {
                });
            }
        }
        //对单个涨停策略分析
        UpLimitStrongWeakBO upLimitStrongWeakInfo = getUpLimitStrongWeakInfo(upLimitDetailList, dateStr);
        //分析结果
        return upLimitStrongWeakInfo;
    }

    /**
     * 涨停分析
     *
     * @param upLimitDetailList
     * @param dateStr
     * @return
     */
    private UpLimitStrongWeakBO getUpLimitStrongWeakInfo(List<UpLimitDetailInfo> upLimitDetailList, String dateStr) {
        UpLimitStrongWeakBO upLimitStrongWeakBO = new UpLimitStrongWeakBO();
        upLimitStrongWeakBO.setDateStr(dateStr);
        upLimitStrongWeakBO.setFirstUpLimitDate(new Date(upLimitDetailList.get(0).getTime()));
        long sumMinuter = upLimitDetailList.stream().map(UpLimitDetailInfo::getDuration).mapToLong(Long::longValue).sum() / 1000 / 60;
        upLimitStrongWeakBO.setDuration(sumMinuter);
        upLimitStrongWeakBO.setOpenNum(upLimitDetailList.size());
        upLimitStrongWeakBO.setFirstVol(upLimitDetailList.get(0).getFirstVol());
        upLimitStrongWeakBO.setHighestVol(upLimitDetailList.get(0).getHighestVol());

        String firstUpLimitTimeStr = DateTimeUtil.getDateFormat(upLimitStrongWeakBO.getFirstUpLimitDate(), DateTimeUtil.HH_MM);

        //封板时间早
        if (firstUpLimitTimeStr.compareTo("09:30") == 0) {
            if (upLimitDetailList.get(0).getDuration() / 1000 / 10 > 5) {
                if (upLimitStrongWeakBO.getDuration() > 4 * 60 * 0.9) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("T字强回封");
                } else {
                    upLimitStrongWeakBO.setStrongWeakDescribe("T字弱回封");
                }
            }
        } else if (firstUpLimitTimeStr.compareTo("10:00") < 0) {
            if (upLimitDetailList.size() > 3) {
                if (upLimitStrongWeakBO.getDuration() < 4 * 60 * 0.75) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("弱");
                } else if (upLimitStrongWeakBO.getDuration() < 4 * 60 * 0.9) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("弱中带强");
                } else {
                    upLimitStrongWeakBO.setStrongWeakDescribe("强势换手");
                }
            } else if (upLimitDetailList.size() == 2) {
                if (upLimitStrongWeakBO.getDuration() < 3 * 60) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("中偏弱");
                } else {
                    upLimitStrongWeakBO.setStrongWeakDescribe("中");
                }
            } else {
                upLimitStrongWeakBO.setStrongWeakDescribe("强");
            }
        } else if (firstUpLimitTimeStr.compareTo("11:00") < 0) {
            if (upLimitDetailList.size() == 1) {
                upLimitStrongWeakBO.setStrongWeakDescribe("弱中带强");
            }
        } else {
            upLimitStrongWeakBO.setStrongWeakDescribe("未知");

        }
        if (upLimitStrongWeakBO.getFirstVol() > 10 * 100 * 10000) {
            upLimitStrongWeakBO.setStrongWeakDescribe(upLimitStrongWeakBO.getStrongWeakDescribe() + "     封单量大");
        } else {
            upLimitStrongWeakBO.setStrongWeakDescribe(upLimitStrongWeakBO.getStrongWeakDescribe() + "     封单量小");
        }
        return upLimitStrongWeakBO;
    }

    private Object getOnceUpLimitStrongWeakInfo(List<UpLimitDetailInfo> upLimitDetailList) {

        return null;
    }

    public String getDescribe(StockUpLimitNumDTO dto) {
        StockUpLimitValPrice stockUpLimitValPrice = stockUpLimitValPriceMapper.selectAllByName(dto.getName());
        if (stockUpLimitValPrice == null) {
            return null;
        }
        String strongWeakArray = stockUpLimitValPrice.getStrongWeakArray();

        if (StringUtils.isNotBlank(strongWeakArray)) {
            List<UpLimitStrongWeakBO> upLimitStrongWeakBOList = JsonUtil.readToValue(strongWeakArray, new TypeReference<List<UpLimitStrongWeakBO>>() {
            });
            upLimitStrongWeakBOList = upLimitStrongWeakBOList.stream().sorted(Comparator.comparing(UpLimitStrongWeakBO::getDateStr)).collect(Collectors.toList());
            StringBuffer sb = new StringBuffer();
            for (UpLimitStrongWeakBO up : upLimitStrongWeakBOList) {
                sb.append("时间：" + up.getDateStr() + "\n");
                sb.append("首次封单：" + new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_UP).toString() + "万   ");
                sb.append("最高封单：" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_UP).toString() + "万  \n");
                sb.append("封板时长：" + up.getDuration() + "分钟  ");
                sb.append("开板次数：" + (up.getOpenNum() - 1) + "次数\n");
                sb.append("评价描述：" + up.getStrongWeakDescribe() + "\n");
                sb.append("----------------------------------------------\n");
            }
            return sb.toString();
        }
        return null;
    }


    /**
     * 根据id和日期，将code收集起来，进行量价分析
     * @param dto
     */
    public void dayTwoAboveUpLimitVolPriceJobHandler(StockStrategyQueryDTO dto) throws ParseException {
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        List<String> codeList = new ArrayList<>();
        //查询策略
        try {
            StrategyBO strategy = stockStrategyService.strategy(dto);
            //获取所有的code
            codeList=getStrategyCodeInfo(strategy);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        //调用方法
        for (String code : codeList) {
            StockValPriceDTO stockValPriceDTO=new StockValPriceDTO();
            stockValPriceDTO.setCode(code);
            stockValPriceDTO.setDateStr(dto.getDateStr());
            execute(stockValPriceDTO);

        }

    }

    private  List<String> getStrategyCodeInfo(StrategyBO strategy) {
        List<String> codeList = new ArrayList<>();

        JSONArray data = strategy.getData();
        for( Object jo :data){
            //股票代码和名称
            String code = ((JSONObject) jo).getString("code");
            codeList.add(code);
        }
        return codeList;
    }

}
