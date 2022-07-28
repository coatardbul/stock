package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.PlateEnum;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.feign.river.BaseServerFeign;

import com.coatardbul.stock.model.bo.StockUpLimitInfoBO;
import com.coatardbul.stock.model.bo.StockUpLimitNameBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.LimitBaseInfoBO;
import com.coatardbul.stock.model.bo.LimitStrongWeakBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
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
    StockParseAndConvertService stockParseAndConvertService;

    /**
     * 两板及两板以上数据，最高到8板
     *
     * @param dto
     * @return
     */
    public List<StockUpLimitNameBO> getTwoAboveUpLimitInfo(StockEmotionDayDTO dto) {

        List<StockUpLimitNameBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);
        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.abThreadPool.execute(() -> {
                //涨停脚本语句
                String upLimitNumScript = getUpLimitNumScript(num);
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                stockStrategyQueryDTO.setStockTemplateScript(upLimitNumScript);
                StrategyBO strategy = null;
                try {
                    //策略查询
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    JSONArray data = strategy.getData();
                    List<String> nameList = new ArrayList<>();
                    for (int j = 0; j < data.size(); j++) {
                        nameList.add(stockParseAndConvertService.getStockName(data.getJSONObject(j)));
                    }
                    StockUpLimitNameBO stockUpLimitNameBO = new StockUpLimitNameBO();
                    stockUpLimitNameBO.setUpLimitNum(num + "板");
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

    public Object getUpLimitSign(StockEmotionDayDTO dto) {
        dto.setDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(),-1));
        List<StockUpLimitNameBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);
        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.abThreadPool.execute(() -> {
                //涨停脚本语句
                String upLimitNumScript = getUpLimitNumScript(num);
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                stockStrategyQueryDTO.setStockTemplateScript(upLimitNumScript);
                StrategyBO strategy = null;
                try {
                    //策略查询
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    JSONArray data = strategy.getData();
                    List<String> codeList = new ArrayList<>();
                    for (int j = 0; j < data.size(); j++) {
                        codeList.add(stockParseAndConvertService.getStockCode(data.getJSONObject(j)));
                    }
                    StockUpLimitNameBO stockUpLimitNameBO = new StockUpLimitNameBO();
                    stockUpLimitNameBO.setUpLimitNum((num+1) + "板");
                    stockUpLimitNameBO.setCodeList(codeList);
                    if (stockUpLimitNameBO.getCodeList().size() > 0) {
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


    /**
     * 涨停题材
     *
     * @param dto
     * @return
     * @throws NoSuchMethodException
     * @throws ScriptException
     * @throws FileNotFoundException
     */
    public List<StockUpLimitInfoBO> getUpLimitTheme(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        StrategyBO strategy = stockStrategyService.strategy(dto);
        if (strategy.getTotalNum() > 0) {
            JSONArray data = strategy.getData();
            return rebuildThemeInfo(data);
        }
        return null;
    }

    /**
     * 题材相关
     *
     * @param data
     * @return
     */
    public List<StockUpLimitInfoBO> rebuildThemeInfo(JSONArray data) {
        //key 为题材名称 value为股票名称
        Map<String, List<String>> themeMap = new HashMap<>();

        //key 为股票名称 value为股票名称
        Map<String, LimitBaseInfoBO> nameMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jo = data.getJSONObject(i);
            //取里面的数组信息
            Set<String> keys = jo.keySet();
            String stockName = stockParseAndConvertService.getStockName(jo);
            LimitStrongWeakBO upLimitStrongWeak = upLimitStrongWeakService.getLimitStrongWeak(jo, "涨停明细数据");
            LimitBaseInfoBO upLimitBaseInfoBO = new LimitBaseInfoBO();
            BeanUtils.copyProperties(upLimitStrongWeak, upLimitBaseInfoBO);
            upLimitBaseInfoBO.setName(stockName);
            upLimitBaseInfoBO.setCode(jo.getString("code"));

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

    private StockUpLimitInfoBO convert(Map.Entry<String, List<String>> map, Map<String, LimitBaseInfoBO> nameMap) {
        StockUpLimitInfoBO stockUpLimitInfoBO = new StockUpLimitInfoBO();
        stockUpLimitInfoBO.setThemeName(map.getKey());
        List<LimitBaseInfoBO> nameList = new ArrayList<>();
        for (String name : map.getValue()) {
            nameList.add(nameMap.get(name));
        }
        stockUpLimitInfoBO.setNameList(nameList);
        stockUpLimitInfoBO.setNum(map.getValue().size());
        return stockUpLimitInfoBO;
    }







    /**
     * 根据策略code数据
     * @param dateStr
     * @param riverStockTemplateId
     * @return
     */
    private List<String> getCodeListByStrategy(String dateStr, String riverStockTemplateId) {
        List<String> codeList = new ArrayList<>();
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


















}
