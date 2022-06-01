package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockStrategyWatchMapper;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockStrategyWatch;
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

import java.text.ParseException;
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
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectAllByType(3);
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
                            redisTemplate.opsForValue().set(key, JsonUtil.toJson(jsonObject), 20, TimeUnit.HOURS);
                            //发送邮件
                            emailService.sendProcess("sxl14459048@163.com",
                                    riverRemoteService.getTemplateNameById(executeStrategy.getTemplatedId())+"-"+name,
                                    emailInfo(jsonObject)
                            );
                        }
                    }

                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
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
