package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.common.constants.StockTradeBuyTypeEnum;
import com.coatardbul.stock.common.constants.StockWatchTypeEnum;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockStrategyWatchMapper;
import com.coatardbul.stock.mapper.StockTradeBuyConfigMapper;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.StockTradeBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockStrategyWatch;
import com.coatardbul.stock.model.entity.StockTradeBuyConfig;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.service.base.EmailService;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockStrategyWatchService {

    @Autowired
    StockStrategyWatchMapper stockStrategyWatchMapper;
    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    StockWarnLogService stockWarnLogService;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    RiverRemoteService riverRemoteService;
    @Autowired
    StockVerifyService stockVerifyService;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    EmailService emailService;
    @Autowired
    StockTradeBuyConfigMapper stockTradeBuyConfigMapper;
    @Autowired
    StockParseAndConvertService stockParseAndConvertService;
    @Autowired
    StockTradeService stockTradeService;

    //模拟历史扫描数据
    public void simulateHistoryStrategyWatch(StockEmotionDayDTO dto) throws ParseException {
        strategyWatch(dto, false);
    }

    //当前时间实现
    public void strategyNowWatch(StockEmotionDayDTO dto) throws ParseException {
        strategyWatch(dto, true);
    }

    //邮件扫描数据
    public void emailStrategyWatch(StockEmotionDayDTO dto) throws ParseException {
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        if (dto.getTimeStr().compareTo("09:30") < 0) {
            return;
        }
        if (dto.getTimeStr().compareTo("11:30") > 0 && dto.getTimeStr().compareTo("13:00") < 0) {
            return;
        }
        //需要发送邮件
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectAllByType(StockWatchTypeEnum.EMAIL.getType());
        //过滤符合要求的信息
        if (stockStrategyWatches == null || stockStrategyWatches.size() == 0) {
            return;
        }
        //需要执行的策略监控
        List<StockStrategyWatch> willExecuteStrategy = stockStrategyWatches.stream().filter(o1 -> filter(o1, dto.getTimeStr())).collect(Collectors.toList());
        if (willExecuteStrategy == null || willExecuteStrategy.size() == 0) {
            return;
        }

        for (StockStrategyWatch executeStrategy : willExecuteStrategy) {
            StockStrategyQueryDTO query = new StockStrategyQueryDTO();
            query.setRiverStockTemplateId(executeStrategy.getTemplatedId());
            query.setDateStr(dto.getDateStr());
            query.setTimeStr(dto.getTimeStr());
            try {
                StrategyBO strategy = stockStrategyService.strategy(query);
                //发送邮件
                if (strategy.getTotalNum() > 0) {
                    JSONArray data = strategy.getData();
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        String code = jsonObject.getString("code");
                        String name = jsonObject.getString("股票简称");
                        String key = executeStrategy.getTemplatedId() + "_" + code;
                        if (redisTemplate.opsForValue().get(key) == null) {
                            //执行任务
                            process(executeStrategy.getTemplatedId(), key, jsonObject, name, code);
                        }
                    }

                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 判断走邮件还是直接购买
     *
     * @param templateId
     * @param key
     * @param jsonObject
     * @param name
     * @param code
     */
    private void process(String templateId, String key, JSONObject jsonObject, String name, String code) {
        redisTemplate.opsForValue().set(key, JsonUtil.toJson(jsonObject), 20, TimeUnit.HOURS);

        //判断是发邮件还是直接买
        StockTradeBuyConfig stockTradeBuyConfig = stockTradeBuyConfigMapper.selectAllByTemplateId(templateId);
        //直接买入
        if (StockTradeBuyTypeEnum.DIRECT.getType().equals(stockTradeBuyConfig.getType())) {

            directBuy(stockTradeBuyConfig, code, name);
        }
        //邮件类型
        if (StockTradeBuyTypeEnum.EMAIL.getType().equals(stockTradeBuyConfig.getType())) {
        }

        //发送邮件
        try {
            emailService.sendProcess("sxl14459048@163.com",
                    riverRemoteService.getTemplateNameById(templateId) + "-" + name,
                    emailInfo(jsonObject)
            );
        } catch (Exception e) {

            log.error(e.getMessage(), e);
        }

    }


    /**
     * 直接买入
     *
     * @param stockTradeBuyConfig
     * @param code
     */
    private void directBuy(StockTradeBuyConfig stockTradeBuyConfig, String code, String name) {
        //判断次数
        if (stockTradeBuyConfig.getSubNum() <= 0) {
            return;
        }
        //金额判断,股票详情
        StockStrategyQueryDTO dto = new StockStrategyQueryDTO();
        dto.setRiverStockTemplateSign(StockTemplateEnum.STOCK_DETAIL.getSign());
        dto.setDateStr(DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYY_MM_DD));
        dto.setStockCode(code);
        StrategyBO strategy = null;
        try {
            strategy = stockStrategyService.strategy(dto);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (strategy == null || strategy.getTotalNum() == 0) {
            return;
        }
        JSONObject jsonObject = strategy.getData().getJSONObject(0);

        BigDecimal closePrice = null;
        //当日
        String dateFormat = DateTimeUtil.getDateFormat(new Date(), DateTimeUtil.YYYYMMDD);
        for (String key : jsonObject.keySet()) {
            if (key.contains("收盘价") && !key.contains(dateFormat)) {
                closePrice = stockParseAndConvertService.convert(jsonObject.get(key));
            }
        }
        if (closePrice != null) {
            //涨停价
            BigDecimal upLimit = stockParseAndConvertService.getUpLimit(closePrice);
            //每次可使用的金额
            BigDecimal userMoney = stockTradeBuyConfig.getSubMoney().divide(new BigDecimal(stockTradeBuyConfig.getSubNum()), 2, BigDecimal.ROUND_HALF_UP);
            //可买的多少,1 ,2,
            BigDecimal buyNum = userMoney.divide(upLimit.multiply(new BigDecimal(100)), 0, BigDecimal.ROUND_DOWN);
            if (buyNum.compareTo(BigDecimal.ONE) < 0) {
                //金额不够，无法购买
                return;
            } else {
                //调用购买接口，
                StockTradeBO stockTradeBO=new StockTradeBO();
                stockTradeBO.setStockCode(code);
                stockTradeBO.setPrice(upLimit.toString());
                stockTradeBO.setAmount(buyNum.multiply(new BigDecimal(100)).toString());
                stockTradeBO.setZqmc(name);
                String response = stockTradeService.buy(stockTradeBO);
                //成功
                if(StringUtils.isNotBlank(response)){
                    //剩余次数
                    stockTradeBuyConfig.setSubNum(stockTradeBuyConfig.getSubNum() - 1);
                    //剩余金额
                    BigDecimal subtractMoney = stockTradeBuyConfig.getSubMoney().subtract(upLimit.multiply(buyNum.multiply(new BigDecimal(100))));
                    stockTradeBuyConfig.setSubMoney(subtractMoney);
                    stockTradeBuyConfigMapper.updateByPrimaryKeySelective(stockTradeBuyConfig);
                }
            }
        }
    }


    private String emailInfo(JSONObject jsonObject) {
        StringBuffer sb = new StringBuffer();
        sb.append("股票代码：");
        sb.append(jsonObject.getString("code"));
        sb.append("\n");
        sb.append("股票名称：");
        sb.append(jsonObject.getString("股票简称"));
        sb.append("\n");
        sb.append(JsonUtil.toJson(jsonObject));
        return sb.toString();

    }

    public void strategyWatch(StockEmotionDayDTO dto, List<StockStrategyWatch> stockStrategyWatches) throws ParseException {
        //需要执行的策略监控
        List<StockStrategyWatch> willExecuteStrategy = stockStrategyWatches.stream().filter(o1 -> filter(o1, dto.getTimeStr())).collect(Collectors.toList());
        if (willExecuteStrategy == null || willExecuteStrategy.size() == 0) {
            return;
        }
        for (StockStrategyWatch executeStrategy : willExecuteStrategy) {
            StockStrategyQueryDTO query = new StockStrategyQueryDTO();
            query.setRiverStockTemplateId(executeStrategy.getTemplatedId());
            query.setDateStr(dto.getDateStr());
            query.setTimeStr(dto.getTimeStr());
            try {
                StrategyBO strategy = stockStrategyService.strategy(query);
                //去重存入日志中
                if (strategy.getTotalNum() > 0) {
                    stockWarnLogService.insertFilterHistory(strategy, query);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    public void strategyWatch(StockEmotionDayDTO dto, boolean isNow) throws ParseException {
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        if (dto.getTimeStr().compareTo("09:30") < 0) {
            return;
        }
        if (dto.getTimeStr().compareTo("11:30") > 0 && dto.getTimeStr().compareTo("13:00") < 0) {
            return;
        }
        //todo 根据类型，查询出需要扫描的策略
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectAllByType(2);
        //过滤符合要求的信息
        if (stockStrategyWatches == null || stockStrategyWatches.size() == 0) {
            return;
        }
        //需要执行的策略监控
        List<StockStrategyWatch> willExecuteStrategy = stockStrategyWatches.stream().filter(o1 -> filter(o1, dto.getTimeStr())).collect(Collectors.toList());
        if (willExecuteStrategy == null || willExecuteStrategy.size() == 0) {
            return;
        }
        for (StockStrategyWatch executeStrategy : willExecuteStrategy) {
            StockStrategyQueryDTO query = new StockStrategyQueryDTO();
            query.setRiverStockTemplateId(executeStrategy.getTemplatedId());
            query.setDateStr(dto.getDateStr());
            query.setTimeStr(dto.getTimeStr());
            try {
                StrategyBO strategy = stockStrategyService.strategy(query);
                //去重存入日志中
                if (strategy.getTotalNum() > 0) {
                    if (isNow) {
                        stockWarnLogService.insertFilterNow(strategy, query);
                    } else {
                        stockWarnLogService.insertFilterHistory(strategy, query);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 1.表中的策略时间大于传入的时间，
     * 2.表中结束时间为空，true
     *
     * @param stockStrategyWatch 表中策略数据
     * @param cronTime           定时任务或者传入的当前时间
     * @return
     */
    private boolean filter(StockStrategyWatch stockStrategyWatch, String cronTime) {
        if (StringUtils.isNotBlank(stockStrategyWatch.getEndTime())) {
            return stockStrategyWatch.getEndTime().compareTo(cronTime) >= 0;
        } else {
            return true;
        }
    }

    public void add(StockStrategyWatch dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        stockStrategyWatchMapper.insert(dto);
    }

    public void modify(StockStrategyWatch dto) {
        stockStrategyWatchMapper.updateByPrimaryKeySelective(dto);
    }

    public void delete(StockStrategyWatch dto) {
        stockStrategyWatchMapper.deleteByPrimaryKey(dto.getId());

    }

    public List<StockStrategyWatch> findAll() {
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectByAll(null);
        if (stockStrategyWatches == null || stockStrategyWatches.size() == 0) {
            return stockStrategyWatches;
        }
        return stockStrategyWatches.stream().map(this::setTemplatedName).collect(Collectors.toList());
    }

    private StockStrategyWatch setTemplatedName(StockStrategyWatch dto) {
        dto.setTemplatedName(riverRemoteService.getTemplateNameById(dto.getTemplatedId()));
        return dto;
    }

    public void hisSimulate(StockEmotionDayDTO dto) throws ParseException {
        //验证日期
        if (stockVerifyService.isIllegalDate(dto.getDateStr())) {
            return;
        }
        //todo 根据类型，查询出需要扫描的策略
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectAllByType(2);
        //过滤符合要求的信息
        if (stockStrategyWatches == null || stockStrategyWatches.size() == 0) {
            return;
        }

        //获取间隔时间字符串
        List<String> timeIntervalListData = stockVerifyService.getRemoteTimeInterval(dto.getTimeInterval());


        for (String timeIntervalStr : timeIntervalListData) {
            dto.setTimeStr(timeIntervalStr);
            strategyWatch(dto, stockStrategyWatches);
        }


    }
}
