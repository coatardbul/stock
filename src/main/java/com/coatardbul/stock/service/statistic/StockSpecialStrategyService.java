package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockUpLimitDescribeMapper;
import com.coatardbul.stock.model.bo.StockUpLimitInfoBO;
import com.coatardbul.stock.model.bo.StockUpLimitNameBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.UpLimitBaseInfoBO;
import com.coatardbul.stock.model.bo.UpLimitStrongWeakBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockUpLimitDescribe;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockSpecialStrategyService {

    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    StockUpLimitValPriceService stockUpLimitValPriceService;
    @Autowired
    UpLimitStrongWeakService upLimitStrongWeakService;
    @Autowired
    RiverRemoteService riverRemoteService;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockUpLimitDescribeMapper stockUpLimitDescribeMapper;

    public List<StockUpLimitNameBO> getTwoAboveUpLimitInfo(StockEmotionDayDTO dto) {

        List<StockUpLimitNameBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);

        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.emotionIntervalByDateThreadPool.submit(() -> {
                StockUpLimitNameBO stockUpLimitNameBO = new StockUpLimitNameBO();
                stockUpLimitNameBO.setUpLimitNum(num + "板");
                String upLimitNumScript = getUpLimitNumScript(num);
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                stockStrategyQueryDTO.setStockTemplateScript(upLimitNumScript);
                StrategyBO strategy = null;
                try {
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    JSONArray data = strategy.getData();
                    List<String> nameList = new ArrayList<>();
                    for (Object jo : data) {
                        nameList.add(((String) ((JSONObject) jo).get("股票简称")));
                    }
                    stockUpLimitNameBO.setNameList(nameList);
                    if (stockUpLimitNameBO.getNameList().size() > 0) {
                        result.add(stockUpLimitNameBO);
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
        return result.stream().sorted(Comparator.comparing(StockUpLimitNameBO::getUpLimitNum).reversed()).collect(Collectors.toList());
    }


    /**
     * 非创业板，非st板块，{{lastDay3}}未涨停，{{lastDay2}}涨停，{{lastDay1}}涨停，{{today}}涨停，
     * 获取连续涨停的脚本
     *
     * @param num
     * @return
     */
    private String getUpLimitNumScript(int num) {
        StringBuffer sb = new StringBuffer();
        sb.append(" 非创业板，非st板块，");
        sb.append("{{lastDay" + (num) + "}}未涨停，");
        for (int i = num - 1; i > 0; i--) {
            sb.append("{{lastDay" + i + "}}涨停，");
        }
        sb.append("{{today}}涨停，");

        return sb.toString();
    }


    public StrategyBO getOnceUpLimitStrongWeakInfo(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        return stockStrategyService.strategy(dto);
    }


    public List<StockUpLimitInfoBO> getUpLimitTheme(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        StrategyBO strategy = stockStrategyService.strategy(dto);
        if (strategy.getTotalNum() > 0) {
            JSONArray data = strategy.getData();
            return rebuildThemeInfo(data);
        }
        return null;
    }

    public List<StockUpLimitInfoBO> rebuildThemeInfo(JSONArray data) {
        //key 为题材名称 value为股票名称
        Map<String, List<String>> themeMap = new HashMap<>();

        //key 为股票名称 value为股票名称
        Map<String, UpLimitBaseInfoBO> nameMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jo = data.getJSONObject(i);
            //取里面的数组信息
            Set<String> keys = jo.keySet();
            String stockName = (String) jo.get("股票简称");
            UpLimitStrongWeakBO upLimitStrongWeak = upLimitStrongWeakService.getUpLimitStrongWeak(jo);
            UpLimitBaseInfoBO upLimitBaseInfoBO = new UpLimitBaseInfoBO();
            BeanUtils.copyProperties(upLimitStrongWeak, upLimitBaseInfoBO);
            upLimitBaseInfoBO.setName(stockName);
            nameMap.put(stockName, upLimitBaseInfoBO);

            for (String key : keys) {
                if (key.contains("涨停原因类别")) {
                    String themeStr = (String) jo.get(key);
                    if (themeStr.contains("+")) {
                        for (String str : themeStr.split("\\+")) {
                            if (themeMap.containsKey(str)) {
                                themeMap.get(str).add(stockName);
                            } else {
                                List<String> name = new ArrayList<>();
                                name.add(stockName);
                                themeMap.put(str, name);
                            }
                        }
                    } else {
                        if (themeMap.containsKey(themeStr)) {
                            themeMap.get(themeStr).add(stockName);
                        } else {
                            List<String> name = new ArrayList<>();
                            name.add(stockName);
                            themeMap.put(themeStr, name);
                        }
                    }
                }
            }
        }
        List<StockUpLimitInfoBO> result = themeMap.entrySet().stream().map(o1 -> convert(o1, nameMap)).collect(Collectors.toList());
        result = result.stream().sorted(Comparator.comparing(StockUpLimitInfoBO::getNum)).collect(Collectors.toList());
        return result;
    }

    private StockUpLimitInfoBO convert(Map.Entry<String, List<String>> map, Map<String, UpLimitBaseInfoBO> nameMap) {
        StockUpLimitInfoBO stockUpLimitInfoBO = new StockUpLimitInfoBO();
        stockUpLimitInfoBO.setThemeName(map.getKey());
        List<UpLimitBaseInfoBO> nameList = new ArrayList<>();
        for (String name : map.getValue()) {
            nameList.add(nameMap.get(name));
        }
        stockUpLimitInfoBO.setNameList(nameList);
        stockUpLimitInfoBO.setNum(map.getValue().size());
        return stockUpLimitInfoBO;
    }

    public List<StockUpLimitDescribe> getOnceUpLimitData(StockLastUpLimitDetailDTO dto) {
        List<String> codeList = getCodeList( dto.getDateStr(),dto.getRiverStockTemplateId());
        if(codeList.size()==0){
            return null;
        }
        List<StockUpLimitDescribe> stockUpLimitDescribes = stockUpLimitDescribeMapper.selectAllByDateLessThanAndCodeIn(dto.getDateStr(),codeList);
        return stockUpLimitDescribes;
    }

    /**
     * 构建过去涨停信息
     * 1.根据templatedId，和date构建
     * 2.根据时间区间和代码构建
     *
     * @param dto date为当日
     * @return
     */
    public void buildLastUpLimitInfo(StockLastUpLimitDetailDTO dto) {
        //获取时间区间
        List<String> codeList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getRiverStockTemplateId()) && StringUtils.isNotBlank(dto.getDateStr())) {
            codeList.addAll(getCodeList(dto.getDateStr(), dto.getRiverStockTemplateId()));
        }
        if (StringUtils.isNotBlank(dto.getStockCode())) {
            codeList.add(dto.getStockCode());
        }
        if (!StringUtils.isNotBlank(dto.getBeginDateStr()) || !StringUtils.isNotBlank(dto.getEndDateStr())) {
            dto.setEndDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(), -1));
            dto.setBeginDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(), -15));
        }

        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDateStr(), dto.getEndDateStr());
        for (String code : codeList) {
            List<String>tableDateStrList=new ArrayList<>();
            //查询表中有的数据
            List<StockUpLimitDescribe> stockUpLimitDescribes = stockUpLimitDescribeMapper.selectAllByCode(code);
            if(stockUpLimitDescribes!=null&&stockUpLimitDescribes.size()>0){
                List<String> collect = stockUpLimitDescribes.stream().map(StockUpLimitDescribe::getDate).sorted().collect(Collectors.toList());
                List<String> dateIntervalList1 = riverRemoteService.getDateIntervalList(collect.get(0), collect.get(collect.size() - 1));
                tableDateStrList.addAll(dateIntervalList1);
            }
            for (String dateStr : dateIntervalList) {
                if(tableDateStrList.contains(dateStr)){
                    return;
                }
                Constant.emotionIntervalByDateThreadPool.submit(() -> {
                    StockUpLimitDescribe stockUpLimitDescribe = stockUpLimitDescribeMapper.selectAllByCodeAndDate(code, dateStr);
                    if (stockUpLimitDescribe == null) {
                        addUpLimitDescribe(code, dateStr);
                    }
                });
            }
        }
    }


    private  List<String> getCodeList(String dateStr,String riverStockTemplateId){
        List<String>codeList=new ArrayList<>();
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setDateStr(dateStr);
        stockStrategyQueryDTO.setRiverStockTemplateId(riverStockTemplateId);
        StrategyBO strategy = null;
        try {
            strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            JSONArray data = strategy.getData();
            for (Object jo : data) {
                codeList.add(((String) ((JSONObject) jo).get("code")));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return codeList;
    }

    private void addUpLimitDescribe(String code, String dateStr) {
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setDateStr(dateStr);
        stockStrategyQueryDTO.setRiverStockTemplateId("1509349533765730304");
        stockStrategyQueryDTO.setStockCode(code);
        try {
            StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            if (strategy == null) {
                return;
            }
            JSONObject jsonObject = strategy.getData().getJSONObject(0);
            String stockName = jsonObject.getString("股票简称");
            String upLimitStrongWeakType = upLimitStrongWeakService.getUpLimitStrongWeakType(jsonObject);
            String upLimitStrongWeakDescribe = upLimitStrongWeakService.getUpLimitStrongWeakDescribe(jsonObject);
            StockUpLimitDescribe stockUpLimitDescribe = new StockUpLimitDescribe();
            stockUpLimitDescribe.setId(baseServerFeign.getSnowflakeId());
            stockUpLimitDescribe.setDate(dateStr);
            stockUpLimitDescribe.setCode(code);
            stockUpLimitDescribe.setName(stockName);
            stockUpLimitDescribe.setUpLimitType(upLimitStrongWeakType);
            stockUpLimitDescribe.setUpLimitInfo(upLimitStrongWeakDescribe);
            stockUpLimitDescribeMapper.insertSelective(stockUpLimitDescribe);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
