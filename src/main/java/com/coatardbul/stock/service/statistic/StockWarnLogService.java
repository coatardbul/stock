package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockWarnLogMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockWarnLog;
import com.coatardbul.stock.model.dto.StockWarnLogQueryDto;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
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
public class StockWarnLogService {

    @Autowired
    StockWarnLogMapper stockWarnLogMapper;
    @Autowired
    RiverRemoteService riverRemoteService;
    @Autowired
    BaseServerFeign baseServerFeign;

    public void insertFilterHistory(StrategyBO strategy, StockStrategyQueryDTO query) throws ParseException {
        insertFilter(strategy, query, false);

    }

    public void insertFilterNow(StrategyBO strategy, StockStrategyQueryDTO query) throws ParseException {
        insertFilter(strategy, query, true);
    }


    public void insertFilter(StrategyBO strategy, StockStrategyQueryDTO query, boolean isNow) throws ParseException {

        //获取策略中的股票信息
        List<StockWarnLog> strategyStockInfo = new ArrayList<>();
        strategy.getData().forEach(item -> {
            Set<Map.Entry<String, Object>> entries = ((JSONObject) item).entrySet();
            StockWarnLog stockWarnLog = new StockWarnLog();
            entries.forEach(stockLineInfo -> {
                if (stockLineInfo.getKey().equals("code")) {
                    stockWarnLog.setStockCode((String) stockLineInfo.getValue());
                }
                if (stockLineInfo.getKey().contains("股票简称")) {
                    stockWarnLog.setStockName((String) stockLineInfo.getValue());
                }
            });
            strategyStockInfo.add(stockWarnLog);
        });
        //获取模板名称
        String templateName = riverRemoteService.getTemplateById(query.getRiverStockTemplateId()).getName();

        Map<String, StockWarnLog> stockCodeMap = strategyStockInfo.stream().collect(Collectors.toMap(StockWarnLog::getStockCode, Function.identity()));

        List<StockWarnLog> stockWarnLogs = stockWarnLogMapper.selectAllByTemplateIdAndDate(query.getRiverStockTemplateId(), query.getDateStr());
        //日志表中有数据
        if (stockWarnLogs != null && stockWarnLogs.size() > 0) {

            for (StockWarnLog log : stockWarnLogs) {
                //包含，有则跳过,无则放入数据
                if (stockCodeMap.containsKey(log.getStockCode())) {
                    //清理查询策略中重复的数据
                    stockCodeMap.remove(log.getStockCode());
                }
            }
        }

        if (stockCodeMap.size() > 0) {
            for (Map.Entry<String, StockWarnLog> entry : stockCodeMap.entrySet()) {
                StockWarnLog stockWarnLog = new StockWarnLog();
                stockWarnLog.setId(baseServerFeign.getSnowflakeId());
                stockWarnLog.setStockCode(entry.getKey());
                stockWarnLog.setStockName(entry.getValue().getStockName());
                stockWarnLog.setTemplateId(query.getRiverStockTemplateId());
                stockWarnLog.setTemplateName(templateName);
                stockWarnLog.setDate(query.getDateStr());
                if (isNow) {
                    Date date = new Date();
                    Calendar calendar = DateTimeUtil.getCalendar(date);
                    calendar.set(Calendar.SECOND, new Random().nextInt(59));
                    stockWarnLog.setCreateTime(calendar.getTime());
                } else {
                    Date hisDate = DateTimeUtil.parseDateStr(query.getDateStr() + query.getTimeStr(), DateTimeUtil.YYYY_MM_DD + DateTimeUtil.HH_MM);
                    Calendar calendar = DateTimeUtil.getCalendar(hisDate);
                    calendar.set(Calendar.SECOND, new Random().nextInt(59));
                    stockWarnLog.setCreateTime(calendar.getTime());
                }
                stockWarnLogMapper.insertSelective(stockWarnLog);
            }
        }
    }


    public List<StockWarnLog> findAll(StockWarnLogQueryDto dto) {
        return stockWarnLogMapper.selectAllByDateAndTemplateId(dto.getDateStr(), dto.getTemplateId());
    }

    public void delete(StockWarnLogQueryDto dto) {
        stockWarnLogMapper.deleteByDate(dto.getDateStr());
    }
}
