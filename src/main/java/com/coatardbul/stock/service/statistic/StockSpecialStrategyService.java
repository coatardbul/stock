package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.bo.OnceUpLimitStrongWeakBO;
import com.coatardbul.stock.model.bo.StockUpLimitInfoBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.UpLimitDetailInfo;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
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

    public List<StockUpLimitInfoBO> getUpLimitInfo(StockEmotionDayDTO dto) {

        List<StockUpLimitInfoBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);

        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.emotionIntervalByDateThreadPool.submit(() -> {
                StockUpLimitInfoBO stockUpLimitInfoBO = new StockUpLimitInfoBO();
                stockUpLimitInfoBO.setUpLimitNum(num + "板");
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
                    stockUpLimitInfoBO.setNameList(nameList);
                    if (stockUpLimitInfoBO.getNameList().size() > 0) {
                        result.add(stockUpLimitInfoBO);
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
        return result.stream().sorted(Comparator.comparing(StockUpLimitInfoBO::getUpLimitNum).reversed()).collect(Collectors.toList());
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
        sb.append("{{lastDay" + (num + 1) + "}}未涨停，");
        for (int i = num; i > 0; i--) {
            sb.append("{{lastDay" + i + "}}涨停，");

        }
        return sb.toString();
    }


    public StrategyBO getOnceUpLimitStrongWeakInfo(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {

        StrategyBO strategy = stockStrategyService.strategy(dto);
        if (strategy.getTotalNum() > 0) {
            JSONArray data = strategy.getData();
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                OnceUpLimitStrongWeakBO strongWeakInfo = stockUpLimitValPriceService.getStrongWeakInfo(jsonObject);
                jsonObject.put("涨停强弱概览", getDescribe(strongWeakInfo));
            }
        }
        return strategy;
    }

    private String getDescribe(OnceUpLimitStrongWeakBO strongWeakInfo) {
        StringBuffer sb = new StringBuffer();
        sb.append("第一次涨停时间：" + DateTimeUtil.getDateFormat(strongWeakInfo.getFirstUpLimitDate(), DateTimeUtil.HH_MM_SS) + "\n");
        sb.append("最后一次涨停时间：" + DateTimeUtil.getDateFormat(strongWeakInfo.getOpenUpLimitDate(), DateTimeUtil.HH_MM_SS) + "\n");
        sb.append("封板时长：" + strongWeakInfo.getDuration() + "分钟  " + "\n");
        sb.append("开板次数：" + (strongWeakInfo.getOpenNum() - 1) + "次数\n");
        sb.append("首次封单：" + new BigDecimal(strongWeakInfo.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_UP).toString() + "万   ");
        sb.append("最高封单：" + new BigDecimal(strongWeakInfo.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_UP).toString() + "万  \n");
        return sb.toString();
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

        for (int i = 0; i < data.size(); i++) {
            JSONObject jo = data.getJSONObject(i);

            //取里面的数组信息
            Set<String> keys = jo.keySet();
            for (String key : keys) {
                if (key.contains("涨停原因类别")) {
                    String themeStr = (String) jo.get(key);
                    String stockName = (String) jo.get("股票简称");
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
        List<StockUpLimitInfoBO> result = themeMap.entrySet().stream().map(this::convert).collect(Collectors.toList());
        result = result.stream().map(this::convert).sorted(Comparator.comparing(StockUpLimitInfoBO::getNum)).collect(Collectors.toList());
        return result;
    }

    private StockUpLimitInfoBO convert(Map.Entry<String, List<String>> map) {
        StockUpLimitInfoBO stockUpLimitInfoBO = new StockUpLimitInfoBO();
        stockUpLimitInfoBO.setUpLimitNum(map.getKey());
        stockUpLimitInfoBO.setNameList(map.getValue());
        return stockUpLimitInfoBO;
    }

    private StockUpLimitInfoBO convert(StockUpLimitInfoBO stockUpLimitInfoBO) {
        stockUpLimitInfoBO.setNum(stockUpLimitInfoBO.getNameList().size());
        return stockUpLimitInfoBO;
    }
}
