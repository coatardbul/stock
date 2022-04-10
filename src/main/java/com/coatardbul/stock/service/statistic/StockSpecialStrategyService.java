package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.model.bo.StockUpLimitInfoBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.base.StockStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    public List<StockUpLimitInfoBO> getUpLimitInfo(StockEmotionDayDTO dto) {

        List<StockUpLimitInfoBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);

        for (int i = 2; i < 9; i++) {
            final int num=i;
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
                    if(stockUpLimitInfoBO.getNameList().size()>0){
                        result.add(stockUpLimitInfoBO);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }finally {
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


}
