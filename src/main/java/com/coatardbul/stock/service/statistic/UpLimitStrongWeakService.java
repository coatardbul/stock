package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockUpLimitValPriceMapper;
import com.coatardbul.stock.model.bo.DetailBaseInfoBO;
import com.coatardbul.stock.model.bo.LimitDetailInfoBO;
import com.coatardbul.stock.model.bo.LimitStrongWeakBO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
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
public class UpLimitStrongWeakService {
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
    @Autowired
    StockPredictService stockPredictService;
    @Autowired
    StockParseAndConvertService stockParseAndConvertService;
    /**
     * 涨停分析
     *
     * @param upLimitDetailList
     * @param dateStr
     * @return
     */
    public LimitStrongWeakBO getUpLimitStrongWeakInfo(List<LimitDetailInfoBO> upLimitDetailList, String dateStr) {
        LimitStrongWeakBO limitStrongWeakBO = new LimitStrongWeakBO();
        limitStrongWeakBO.setDateStr(dateStr);
        limitStrongWeakBO.setFirstUpLimitDate(new Date(upLimitDetailList.get(0).getTime()));
        if (upLimitDetailList.get(upLimitDetailList.size() - 1).getOpenTime() != null) {
            limitStrongWeakBO.setLastUpLimitDate(new Date(upLimitDetailList.get(upLimitDetailList.size() - 1).getOpenTime()));
        } else {
            try {
                Date lastTime = DateTimeUtil.parseDateStr(dateStr + " 15:00:00", DateTimeUtil.YYYY_MM_DD_HH_MM_SS);
                limitStrongWeakBO.setLastUpLimitDate(lastTime);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
        }
        //理想时间
        long subTime = getIdealDuration(limitStrongWeakBO);
        limitStrongWeakBO.setIdealDuration(subTime / 1000 / 60);
        long sumMinuter = upLimitDetailList.stream().map(LimitDetailInfoBO::getDuration).mapToLong(Long::longValue).sum() / 1000 / 60;
        limitStrongWeakBO.setDuration(sumMinuter);
        limitStrongWeakBO.setOpenNum(upLimitDetailList.size());
        limitStrongWeakBO.setFirstVol(upLimitDetailList.get(0).getFirstVol());
        limitStrongWeakBO.setHighestVol(upLimitDetailList.get(0).getHighestVol());

        //最高量
        List<LimitDetailInfoBO> highInfoList = upLimitDetailList.stream().sorted(Comparator.comparing(LimitDetailInfoBO::getHighestVol)).collect(Collectors.toList());
        limitStrongWeakBO.setHighestValidVol(highInfoList.get(highInfoList.size()-1).getHighestVol());
        limitStrongWeakBO.setFirstValidVol(highInfoList.get(highInfoList.size()-1).getFirstVol());

        //描述
        rebuildUpLimitStrongWeakDescribe(upLimitDetailList, limitStrongWeakBO);
        return limitStrongWeakBO;
    }



    private long getIdealDuration(LimitStrongWeakBO limitStrongWeakBO) {
        long subTime = 0L;
        try {
            Date amTime = DateTimeUtil.parseDateStr(limitStrongWeakBO.getDateStr() + " 11:30:00", DateTimeUtil.YYYY_MM_DD_HH_MM_SS);
            Date pmtTime = DateTimeUtil.parseDateStr(limitStrongWeakBO.getDateStr() + " 13:00:00", DateTimeUtil.YYYY_MM_DD_HH_MM_SS);

            if (limitStrongWeakBO.getLastUpLimitDate().compareTo(amTime) <= 0 || limitStrongWeakBO.getFirstUpLimitDate().compareTo(pmtTime) >= 0) {
                subTime = limitStrongWeakBO.getLastUpLimitDate().getTime() - limitStrongWeakBO.getFirstUpLimitDate().getTime();
            }

            if (limitStrongWeakBO.getFirstUpLimitDate().compareTo(amTime) <= 0 && limitStrongWeakBO.getLastUpLimitDate().compareTo(pmtTime) >= 0) {
                long time1 = amTime.getTime() - limitStrongWeakBO.getFirstUpLimitDate().getTime();
                long time2 = limitStrongWeakBO.getLastUpLimitDate().getTime() - pmtTime.getTime();
                subTime = time1 + time2;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return subTime;

    }

    /**
     * 强弱描述重新构建
     *
     * @param upLimitDetailList
     * @param limitStrongWeakBO
     */
    private void rebuildUpLimitStrongWeakDescribe(List<LimitDetailInfoBO> upLimitDetailList, LimitStrongWeakBO limitStrongWeakBO) {
        String firstUpLimitTimeStr = DateTimeUtil.getDateFormat(limitStrongWeakBO.getFirstUpLimitDate(), DateTimeUtil.HH_MM);
        //封板时间早
        if (firstUpLimitTimeStr.compareTo("09:30") == 0) {
            if (upLimitDetailList.get(0).getDuration() / 1000 / 10 > 5) {
                if (limitStrongWeakBO.getDuration() > 4 * 60 * 0.9) {
                    limitStrongWeakBO.setStrongWeakDescribe("T字强回封");
                } else {
                    limitStrongWeakBO.setStrongWeakDescribe("T字弱回封");
                }
            }
        } else if (firstUpLimitTimeStr.compareTo("10:00") < 0) {
            if (upLimitDetailList.size() > 3) {
                if (limitStrongWeakBO.getDuration() < 4 * 60 * 0.75) {
                    limitStrongWeakBO.setStrongWeakDescribe("弱");
                } else if (limitStrongWeakBO.getDuration() < 4 * 60 * 0.9) {
                    limitStrongWeakBO.setStrongWeakDescribe("弱中带强");
                } else {
                    limitStrongWeakBO.setStrongWeakDescribe("强势换手");
                }
            } else if (upLimitDetailList.size() == 2) {
                if (limitStrongWeakBO.getDuration() < 3 * 60) {
                    limitStrongWeakBO.setStrongWeakDescribe("中偏弱");
                } else {
                    limitStrongWeakBO.setStrongWeakDescribe("中");
                }
            } else {
                limitStrongWeakBO.setStrongWeakDescribe("强");
            }
        } else if (firstUpLimitTimeStr.compareTo("11:00") < 0) {
            if (upLimitDetailList.size() == 1) {
                limitStrongWeakBO.setStrongWeakDescribe("弱中带强");
            }
        } else {
            limitStrongWeakBO.setStrongWeakDescribe("未知");

        }
        if (limitStrongWeakBO.getFirstVol() > 10 * 100 * 10000) {
            limitStrongWeakBO.setStrongWeakDescribe(limitStrongWeakBO.getStrongWeakDescribe() + "     封单量大");
        } else {
            limitStrongWeakBO.setStrongWeakDescribe(limitStrongWeakBO.getStrongWeakDescribe() + "     封单量小");
        }
    }


    /**
     * 将涨停信息放到list中，并返回涨停时间
     *
     * @param jo
     * @param upLimitDetailList 涨停的基础信息
     * @param strategyColumnKey 涨停，跌停字段
     * @return YYYY-MM-DD
     */
    private String parseUpLimitDetail(JSONObject jo, List<LimitDetailInfoBO> upLimitDetailList, String strategyColumnKey) {
        String dateStr = null;
        //取里面的数组信息
        Set<String> keys = jo.keySet();
        for (String key : keys) {
            if (key.contains(strategyColumnKey)) {
                try {
                    dateStr = DateTimeUtil.getDateFormat(DateTimeUtil.parseDateStr(key.trim().substring(7, 15), DateTimeUtil.YYYYMMDD), DateTimeUtil.YYYY_MM_DD);
                } catch (ParseException e) {
                    log.error("解析时间异常" + e.getMessage(), e);
                }
                //解析涨停数据
                String upLimitDetailStr = (String) jo.get(key);
                upLimitDetailList.addAll(JsonUtil.readToValue(upLimitDetailStr, new TypeReference<List<LimitDetailInfoBO>>() {
                }));
            }
        }
        return dateStr;
    }

    /**
     * 解析涨停，跌停信息
     *
     * @param jo
     * @param strategyColumnKey
     * @return
     */
    public LimitStrongWeakBO getLimitStrongWeak(JSONObject jo, String strategyColumnKey) {
        LimitStrongWeakBO upLimitStrongWeakInfo = null;
        String dateStr = null;
        //涨停信息
        List<LimitDetailInfoBO> upLimitDetailList = new ArrayList<>();
        //解析涨停信息
        dateStr = parseUpLimitDetail(jo, upLimitDetailList, strategyColumnKey);
        if (upLimitDetailList.size() > 0) {
            //对单个涨停策略分析
            upLimitStrongWeakInfo = getUpLimitStrongWeakInfo(upLimitDetailList, dateStr);
        }
        //分析结果
        return upLimitStrongWeakInfo;
    }

    /**
     * 解析涨停，跌停信息
     *
     * @param jo
     * @return
     */
    public DetailBaseInfoBO getDetailStrongWeak(JSONObject jo) {
        DetailBaseInfoBO result=new DetailBaseInfoBO();
        Set<String> keys = jo.keySet();
        for (String key : keys) {
            if (key.contains("开盘价")) {
                result.setOpenPrice(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("收盘价:不复权")) {
                result.setClosePrice(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("最高价:不复权")) {
                result.setHighPrice(stockParseAndConvertService.convert(jo.get(key)));
            }

            if (key.contains("最低价:不复权")) {
                result.setLowPrice(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("竞价涨幅")) {
                result.setOpenIncreaseRate(stockParseAndConvertService.convert(jo.get(key)));
            }

            if (key.contains("最大涨幅")) {
                result.setHighIncreaseRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("振幅")) {
                result.setDifferenceRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("竞价金额")) {
                result.setCallAuctionTradeAmount(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("成交额")) {
                result.setTradeAmount(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("换手率")) {
                result.setTurnOverRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("a股市值")) {
                result.setMarketValue(stockParseAndConvertService.convert(jo.get(key)));
            }

        }
        //基础价格，即昨日收盘价
        BigDecimal basePrice = result.getHighPrice().divide(result.getHighIncreaseRate().divide(new BigDecimal(100)).add(BigDecimal.ONE),4, BigDecimal.ROUND_HALF_UP);
        result.setCloseIncreaseRate((result.getClosePrice().subtract(basePrice)).multiply(new BigDecimal(100)).divide(basePrice,2,BigDecimal.ROUND_HALF_UP));
        result.setLowIncreaseRate(result.getHighIncreaseRate().subtract(result.getDifferenceRate()));
        //分析结果
        return result;
    }


    public String getUpLimitStrongWeakType(JSONObject jo) {
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "涨停明细数据");

        if (upLimitStrongWeak.getLastUpLimitDate() == null) {
            return "涨停";
        }
        if ("15:00:00".equals(DateTimeUtil.getDateFormat(upLimitStrongWeak.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS))) {
            return "涨停";
        } else {
            return "曾涨停";
        }

    }

    public String getDownLimitStrongWeakType(JSONObject jo) {
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "跌停明细数据");
        if (upLimitStrongWeak.getLastUpLimitDate() == null) {
            return "跌停";
        }
        if ("15:00:00".equals(DateTimeUtil.getDateFormat(upLimitStrongWeak.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS))) {
            return "跌停";
        } else {
            return "曾跌停";
        }

    }

    public String getDetailStrongWeakType(JSONObject jo) {
        StringBuffer sb=new StringBuffer();
        DetailBaseInfoBO detailBaseInfoBO = getDetailStrongWeak(jo);
        if(detailBaseInfoBO.getHighIncreaseRate().compareTo(new BigDecimal(7.5))>0){
            sb.append( "涨幅"+detailBaseInfoBO.getHighIncreaseRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%");
        }
        if(detailBaseInfoBO.getCloseIncreaseRate().compareTo(new BigDecimal(-7.5))<0){
            sb.append( "跌幅"+detailBaseInfoBO.getCloseIncreaseRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%");
        }
        if(detailBaseInfoBO.getDifferenceRate().compareTo(new BigDecimal(15))>0){
            sb.append( "振幅"+detailBaseInfoBO.getDifferenceRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%");
        }
        return sb.toString();

    }

    public String getLimitStrongWeakFirstSubVolDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "涨停明细数据");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(new BigDecimal(upLimitStrongWeak.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                    .subtract(new BigDecimal(upLimitStrongWeak.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "万");
            upLimitStrongWeakDescribe = sb.toString();        }
        //分析结果
        return upLimitStrongWeakDescribe;
    }

    /**
     * 有效封单差值
     * @param jo
     * @return
     */
    public String getLimitStrongWeakValidSubVolDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "涨停明细数据");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(new BigDecimal(upLimitStrongWeak.getHighestValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                    .subtract(new BigDecimal(upLimitStrongWeak.getFirstValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "万");
            upLimitStrongWeakDescribe = sb.toString();
        }
        //分析结果
        return upLimitStrongWeakDescribe;
    }


    public String getLimitStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "涨停明细数据");
        if (upLimitStrongWeak != null) {
            upLimitStrongWeakDescribe = getLimitStrongWeakDescribe(upLimitStrongWeak);
        }
        //分析结果
        return upLimitStrongWeakDescribe;
    }

    public String getDownLimitStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "跌停明细数据");
        if (upLimitStrongWeak != null) {
            upLimitStrongWeakDescribe = getLimitStrongWeakDescribe(upLimitStrongWeak);
        }
        //分析结果
        return upLimitStrongWeakDescribe;
    }

    public String getDetailStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        DetailBaseInfoBO detailBaseInfoBO = getDetailStrongWeak(jo);
        if (detailBaseInfoBO != null) {
            upLimitStrongWeakDescribe = getDetailWeakDescribe(detailBaseInfoBO);
        }
        //分析结果
        return upLimitStrongWeakDescribe;
    }
    public String getDetailWeakDescribe( DetailBaseInfoBO detailBaseInfoBO) {
        StringBuffer sb = new StringBuffer();
        sb.append("开盘涨幅：" + detailBaseInfoBO.getOpenIncreaseRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%------"+detailBaseInfoBO.getOpenPrice() + "\n");
        sb.append("收盘涨幅：" + detailBaseInfoBO.getCloseIncreaseRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%------"+detailBaseInfoBO.getClosePrice()  + "\n");
        sb.append("最高涨幅：" + detailBaseInfoBO.getHighIncreaseRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%------"+detailBaseInfoBO.getHighPrice()  + "\n");
        sb.append("最低涨幅：" + detailBaseInfoBO.getLowIncreaseRate().setScale(2,BigDecimal.ROUND_HALF_UP)+"%------"+detailBaseInfoBO.getLowPrice()  + "\n");
        sb.append("竞价金额：" + stockParseAndConvertService.getMoneyFormat(detailBaseInfoBO.getCallAuctionTradeAmount())  + "\n");
        sb.append("成交额：" + stockParseAndConvertService.getMoneyFormat(detailBaseInfoBO.getTradeAmount())  + "\n");
        sb.append("换手率：" + detailBaseInfoBO.getTurnOverRate().setScale(2,BigDecimal.ROUND_HALF_UP)  + "%\n");
        sb.append("市值：" + stockParseAndConvertService.getMoneyFormat(detailBaseInfoBO.getMarketValue())   + "\n");
        return sb.toString();
    }


    public String getLimitStrongWeakDescribe(LimitStrongWeakBO up) {
        StringBuffer sb = new StringBuffer();
        sb.append("时间：" + up.getDateStr() + "\n");
        sb.append("涨跌停时间：" + DateTimeUtil.getDateFormat(up.getFirstUpLimitDate(), DateTimeUtil.HH_MM_SS) + "--");
        if (up.getLastUpLimitDate() != null) {
            //涨停结束时间
            sb.append( DateTimeUtil.getDateFormat(up.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS) + "\n");
        }
        sb.append("封板时长情况：" + up.getDuration() + "/");
        if (up.getLastUpLimitDate() != null) {
            //理想时间
            sb.append( up.getIdealDuration() + "分钟  ");
        }else {
            sb.append("分钟  ");
        }
        try {
            //开板时长
            sb.append("=" + (up.getIdealDuration() - up.getDuration() - 1) + "分钟  \n");
            if (up.getIdealDuration() != null) {
                sb.append("封板比率：" + new BigDecimal(up.getDuration()).divide(new BigDecimal(up.getIdealDuration()), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "  \n");
            }
        } catch (Exception e) {

        }
        sb.append("开板次数：" + (up.getOpenNum() - 1) + "次数\n");
        sb.append("首次封单：" + new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "万");
        sb.append("/" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "万");
        //封单差值
        sb.append("=" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                .subtract(new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "万  \n");

        sb.append("有效封单：" + new BigDecimal(up.getFirstValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "万");
        sb.append("/" + new BigDecimal(up.getHighestValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "万");
        //封单差值
        sb.append("=" + new BigDecimal(up.getHighestValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                .subtract(new BigDecimal(up.getFirstValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "万  \n");

        sb.append("评价描述：" + up.getStrongWeakDescribe() + "\n");
        return sb.toString();
    }




}
