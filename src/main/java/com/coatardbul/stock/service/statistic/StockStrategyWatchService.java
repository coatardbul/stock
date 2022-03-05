package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.mapper.StockStrategyWatchMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockStrategyWatch;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.util.List;
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


    //模拟历史扫描数据
    public void simulateHistoryStrategyWatch(StockEmotionDayDTO dto) {
        strategyWatch(dto, false);
    }

    //当前时间实现
    public void strategyNowWatch(StockEmotionDayDTO dto) {
        strategyWatch(dto, true);
    }


    public void strategyWatch(StockEmotionDayDTO dto, boolean isNow) {
        //todo 根据类型，查询出需要扫描的数据
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectAllByType(2);
        //过滤符合要求的信息
        if (stockStrategyWatches == null || stockStrategyWatches.size() == 0) {
            return;
        }
        List<StockStrategyWatch> noExecuteStrategy = stockStrategyWatches.stream().filter(o1 -> filter(o1, dto.getTimeStr())).collect(Collectors.toList());
        if (noExecuteStrategy == null || noExecuteStrategy.size() == 0) {
            return;
        }
        for (StockStrategyWatch executeStrategy : stockStrategyWatches) {
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


    private boolean filter(StockStrategyWatch stockStrategyWatch, String cronTime) {
        if (StringUtils.isNotBlank(stockStrategyWatch.getEndTime())) {
            return stockStrategyWatch.getEndTime().compareTo(cronTime) > 0;
        } else {
            return true;
        }
    }
}
