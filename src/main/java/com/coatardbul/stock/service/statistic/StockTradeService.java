package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.StockTradeSellStatusEnum;
import com.coatardbul.stock.common.constants.StockTradeSellTypeEnum;
import com.coatardbul.stock.common.constants.StockWatchTypeEnum;
import com.coatardbul.stock.common.constants.TradeSignEnum;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockStrategyWatchMapper;
import com.coatardbul.stock.mapper.StockTradeBuyConfigMapper;
import com.coatardbul.stock.mapper.StockTradeSellJobMapper;
import com.coatardbul.stock.mapper.StockTradeUrlMapper;
import com.coatardbul.stock.model.bo.StockTradeBO;
import com.coatardbul.stock.model.entity.StockStrategyWatch;
import com.coatardbul.stock.model.entity.StockTradeBuyConfig;
import com.coatardbul.stock.model.entity.StockTradeSellJob;
import com.coatardbul.stock.model.entity.StockTradeUrl;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockTradeService {


    @Autowired
    StockTradeUrlMapper stockTradeUrlMapper;
    @Autowired
    StockTradeBaseService stockTradeBaseService;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockTradeSellJobMapper stockTradeSellJobMapper;
    @Autowired
    StockStrategyWatchMapper stockStrategyWatchMapper;

    @Autowired
    StockTradeBuyConfigMapper stockTradeBuyConfigMapper;
    @Autowired
    RiverRemoteService riverRemoteService;

    @Autowired
    StockVerifyService stockVerifyService;
    /**
     * 查询持仓
     *
     * @return
     */
    public String queryAssetAndPosition() {

        List<StockTradeUrl> stockTradeUrls = stockTradeUrlMapper.selectAllBySign(TradeSignEnum.ASSET_POSITION.getSign());
        if (stockTradeUrls == null || stockTradeUrls.size() == 0) {
            return null;
        }
        StockTradeUrl stockTradeUrl = stockTradeUrls.get(0);

        String url = stockTradeUrl.getUrl().replace("${validatekey}", stockTradeUrl.getValidateKey());
        String param = "moneyType=RMB";
        try {
            String result = stockTradeBaseService.tradeByString(url, param);
            JSONObject jsonObject = JSONObject.parseObject(result);
            String status = jsonObject.getString("Status");
            if ("0".equals(status)) {
                return jsonObject.getString("Data");
            }
            return result;
        } catch (ConnectTimeoutException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }



    private String bugSellCommon(StockTradeBO dto){
        List<StockTradeUrl> stockTradeUrls = stockTradeUrlMapper.selectAllBySign(TradeSignEnum.BUY_SELL.getSign());
        if (stockTradeUrls == null || stockTradeUrls.size() == 0) {
            return null;
        }
        StockTradeUrl stockTradeUrl = stockTradeUrls.get(0);

        String url = stockTradeUrl.getUrl().replace("${validatekey}", stockTradeUrl.getValidateKey());

        try {
            String result = stockTradeBaseService.trade(url, dto);
            JSONObject jsonObject = JSONObject.parseObject(result);
            String status = jsonObject.getString("Status");
            if ("0".equals(status)) {
                return jsonObject.getString("Data");
            }
            return result;
        } catch (ConnectTimeoutException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
    public String sell(StockTradeBO dto) {
        dto.setTradeType("S");
        return  bugSellCommon(dto);
    }
    public String buy(StockTradeBO dto) {
        dto.setTradeType("B");
       return  bugSellCommon(dto);
    }

    public void addSellInfo(StockTradeSellJob dto) {
        dto.setId(baseServerFeign.getSnowflakeId());
        dto.setStatus(1);
        stockTradeSellJobMapper.insert(dto);
    }
    public void modifySellInfo(StockTradeSellJob dto) {
        stockTradeSellJobMapper.updateByPrimaryKeySelective(dto);
    }
    public List<StockTradeSellJob> querySellInfo(StockTradeSellJob dto) {
        List<StockTradeSellJob> stockTradeSellJobs = stockTradeSellJobMapper.selectByAll(dto);
        return stockTradeSellJobs;
    }

    public void deleteSellInfo(StockTradeSellJob dto) {
        stockTradeSellJobMapper.deleteByPrimaryKey(dto.getId());
    }

    public void syncBuyInfo() {
        StockStrategyWatch stockStrategyWatch = new StockStrategyWatch();
        stockStrategyWatch.setType(StockWatchTypeEnum.EMAIL.getType());
        List<StockStrategyWatch> stockStrategyWatches = stockStrategyWatchMapper.selectByAll(stockStrategyWatch);
        if (stockStrategyWatches.size() > 0) {
            for (StockStrategyWatch ssw : stockStrategyWatches) {
                StockTradeBuyConfig stbc = stockTradeBuyConfigMapper.selectAllByTemplateId(ssw.getTemplatedId());
                if (stbc == null) {
                    StockTradeBuyConfig stockTradeBuyConfig = new StockTradeBuyConfig();
                    stockTradeBuyConfig.setId(baseServerFeign.getSnowflakeId());
                    stockTradeBuyConfig.setTemplateId(ssw.getTemplatedId());
                    stockTradeBuyConfig.setTemplateName(riverRemoteService.getTemplateNameById(ssw.getTemplatedId()));
                    stockTradeBuyConfigMapper.insertSelective(stockTradeBuyConfig);
                }
            }
        }
    }

    public void modifyBuyInfo(StockTradeBuyConfig dto) {
        stockTradeBuyConfigMapper.updateByPrimaryKeySelective(dto);
    }

    public void deleteBuyInfo(StockTradeBuyConfig dto) {
        stockTradeBuyConfigMapper.deleteByPrimaryKey(dto.getId());
    }

    public List<StockTradeBuyConfig> queryBuyInfo(StockTradeBuyConfig dto) {
        List<StockTradeBuyConfig> stockTradeBuyConfigs = stockTradeBuyConfigMapper.selectByAll(dto);
        return stockTradeBuyConfigs;
    }

    public void initBuyInfo() {
        List<StockTradeBuyConfig> stockTradeBuyConfigs = stockTradeBuyConfigMapper.selectByAll(null);
        if(stockTradeBuyConfigs!=null &&stockTradeBuyConfigs.size()>0){
            //查询持仓可用金额
            String result = queryAssetAndPosition();
            JSONArray jsonArray = JSONArray.parseArray(result);
            String kyzj = jsonArray.getJSONObject(0).getString("Kyzj");
            for(StockTradeBuyConfig stbc:stockTradeBuyConfigs){
                if(stbc.getProportion()!=null){
                    BigDecimal multiply = new BigDecimal(kyzj).multiply(stbc.getProportion());
                    stbc.setAllMoney(multiply);
                    stbc.setSubMoney(multiply);
                    stbc.setSubNum(stbc.getAllNum());
                    stockTradeBuyConfigMapper.updateByPrimaryKeySelective(stbc);
                }
            }

        }




    }

    public void sellStrategyJobHandler() throws ParseException {
        String dateStr= DateTimeUtil.getDateFormat(new Date(),DateTimeUtil.YYYY_MM_DD);
        String timeStr= DateTimeUtil.getDateFormat(new Date(),DateTimeUtil.HH_MM);

        if (stockVerifyService.isIllegalDate(dateStr)) {
            return;
        }
        if (timeStr.compareTo("09:30") < 0) {
            return;
        }
        if (timeStr.compareTo("11:30") > 0 && timeStr.compareTo("13:00") < 0) {
            return;
        }

        //查询未卖出，且状态为定时的
        List<StockTradeSellJob> stockTradeSellJobs = stockTradeSellJobMapper.selectAllByTypeAndStatus(StockTradeSellTypeEnum.TIME_SELL.getType(), StockTradeSellStatusEnum.NO_SELL.getType());
        if(stockTradeSellJobs==null ||stockTradeSellJobs.size()==0){
            return;
        }

        for(StockTradeSellJob stockTradeSellJob:stockTradeSellJobs){
            if(stockTradeSellJob.getSellDate().equals(dateStr)){
                if(timeStr.compareTo(stockTradeSellJob.getSellTime())<=0){
                    return;
                }else {
                    //调用卖出接口
                    StockTradeBO stockTradeBO=new StockTradeBO();
                    stockTradeBO.setStockCode(stockTradeSellJob.getCode());
                    stockTradeBO.setPrice(stockTradeSellJob.getPrice());
                    stockTradeBO.setAmount(stockTradeSellJob.getAmount());
                    stockTradeBO.setZqmc(stockTradeSellJob.getName());
                    String response = sell(stockTradeBO);
                    //调用成功
                    if(StringUtils.isNotBlank(response)){
                        stockTradeSellJob.setStatus(StockTradeSellStatusEnum.HAVE_SELL.getType());
                        stockTradeSellJobMapper.updateByPrimaryKey(stockTradeSellJob);
                    }
                }
            }
        }
    }


}
