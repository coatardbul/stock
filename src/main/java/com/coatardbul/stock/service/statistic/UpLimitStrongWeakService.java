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
import com.coatardbul.stock.model.bo.UpLimitValPriceBO;
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
     * æ¶šććæ
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
        //çæłæ¶éŽ
        long subTime = getIdealDuration(limitStrongWeakBO);
        limitStrongWeakBO.setIdealDuration(subTime / 1000 / 60);
        long sumMinuter = upLimitDetailList.stream().map(LimitDetailInfoBO::getDuration).mapToLong(Long::longValue).sum() / 1000 / 60;
        limitStrongWeakBO.setDuration(sumMinuter);
        limitStrongWeakBO.setOpenNum(upLimitDetailList.size());
        limitStrongWeakBO.setFirstVol(upLimitDetailList.get(0).getFirstVol());
        limitStrongWeakBO.setHighestVol(upLimitDetailList.get(0).getHighestVol());

        //æé«é
        List<LimitDetailInfoBO> highInfoList = upLimitDetailList.stream().sorted(Comparator.comparing(LimitDetailInfoBO::getHighestVol)).collect(Collectors.toList());
        limitStrongWeakBO.setHighestValidVol(highInfoList.get(highInfoList.size() - 1).getHighestVol());
        limitStrongWeakBO.setFirstValidVol(highInfoList.get(highInfoList.size() - 1).getFirstVol());

        //æèż°
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
     * ćŒșćŒ±æèż°éæ°æć»ș
     *
     * @param upLimitDetailList
     * @param limitStrongWeakBO
     */
    private void rebuildUpLimitStrongWeakDescribe(List<LimitDetailInfoBO> upLimitDetailList, LimitStrongWeakBO limitStrongWeakBO) {
        String firstUpLimitTimeStr = DateTimeUtil.getDateFormat(limitStrongWeakBO.getFirstUpLimitDate(), DateTimeUtil.HH_MM);
        //ć°æżæ¶éŽæ©
        if (firstUpLimitTimeStr.compareTo("09:30") == 0) {
            if (upLimitDetailList.get(0).getDuration() / 1000 / 10 > 5) {
                if (limitStrongWeakBO.getDuration() > 4 * 60 * 0.9) {
                    limitStrongWeakBO.setStrongWeakDescribe("Tć­ćŒșćć°");
                } else {
                    limitStrongWeakBO.setStrongWeakDescribe("Tć­ćŒ±ćć°");
                }
            }
        } else if (firstUpLimitTimeStr.compareTo("10:00") < 0) {
            if (upLimitDetailList.size() > 3) {
                if (limitStrongWeakBO.getDuration() < 4 * 60 * 0.75) {
                    limitStrongWeakBO.setStrongWeakDescribe("ćŒ±");
                } else if (limitStrongWeakBO.getDuration() < 4 * 60 * 0.9) {
                    limitStrongWeakBO.setStrongWeakDescribe("ćŒ±äž­ćžŠćŒș");
                } else {
                    limitStrongWeakBO.setStrongWeakDescribe("ćŒșćżæąæ");
                }
            } else if (upLimitDetailList.size() == 2) {
                if (limitStrongWeakBO.getDuration() < 3 * 60) {
                    limitStrongWeakBO.setStrongWeakDescribe("äž­ććŒ±");
                } else {
                    limitStrongWeakBO.setStrongWeakDescribe("äž­");
                }
            } else {
                limitStrongWeakBO.setStrongWeakDescribe("ćŒș");
            }
        } else if (firstUpLimitTimeStr.compareTo("11:00") < 0) {
            if (upLimitDetailList.size() == 1) {
                limitStrongWeakBO.setStrongWeakDescribe("ćŒ±äž­ćžŠćŒș");
            }
        } else {
            limitStrongWeakBO.setStrongWeakDescribe("æȘç„");

        }
        if (limitStrongWeakBO.getFirstVol() > 10 * 100 * 10000) {
            limitStrongWeakBO.setStrongWeakDescribe(limitStrongWeakBO.getStrongWeakDescribe() + "     ć°ćéć€§");
        } else {
            limitStrongWeakBO.setStrongWeakDescribe(limitStrongWeakBO.getStrongWeakDescribe() + "     ć°ćéć°");
        }
    }


    /**
     * ć°æ¶šćäżĄæŻæŸć°listäž­ïŒćč¶èżćæ¶šćæ¶éŽ
     *
     * @param jo
     * @param upLimitDetailList æ¶šćçćșçĄäżĄæŻ
     * @param strategyColumnKey æ¶šćïŒè·ćć­æź”
     * @return YYYY-MM-DD
     */
    private String parseUpLimitDetail(JSONObject jo, List<LimitDetailInfoBO> upLimitDetailList, String strategyColumnKey) {
        String dateStr = null;
        //ćééąçæ°ç»äżĄæŻ
        Set<String> keys = jo.keySet();
        for (String key : keys) {
            if (key.contains(strategyColumnKey)) {
                try {
                    dateStr = DateTimeUtil.getDateFormat(DateTimeUtil.parseDateStr(key.trim().substring(7, 15), DateTimeUtil.YYYYMMDD), DateTimeUtil.YYYY_MM_DD);
                } catch (ParseException e) {
                    log.error("è§Łææ¶éŽćŒćžž" + e.getMessage(), e);
                }
                //è§Łææ¶šćæ°æź
                String upLimitDetailStr = (String) jo.get(key);
                upLimitDetailList.addAll(JsonUtil.readToValue(upLimitDetailStr, new TypeReference<List<LimitDetailInfoBO>>() {
                }));
            }
        }
        return dateStr;
    }

    /**
     * è§Łææ¶šćïŒè·ćäżĄæŻ
     *
     * @param jo
     * @param strategyColumnKey
     * @return
     */
    public LimitStrongWeakBO getLimitStrongWeak(JSONObject jo, String strategyColumnKey) {
        LimitStrongWeakBO upLimitStrongWeakInfo = null;
        String dateStr = null;
        //æ¶šćäżĄæŻ
        List<LimitDetailInfoBO> upLimitDetailList = new ArrayList<>();
        //è§Łææ¶šćäżĄæŻ
        dateStr = parseUpLimitDetail(jo, upLimitDetailList, strategyColumnKey);
        if (upLimitDetailList.size() > 0) {
            //ćŻčćäžȘæ¶šćç­ç„ćæ
            upLimitStrongWeakInfo = getUpLimitStrongWeakInfo(upLimitDetailList, dateStr);
        }
        //ćæç»æ
        return upLimitStrongWeakInfo;
    }

    /**
     * è§Łææ¶šćïŒè·ćäżĄæŻ
     *
     * @param jo
     * @return
     */
    public DetailBaseInfoBO getDetailStrongWeak(JSONObject jo) {
        DetailBaseInfoBO result = new DetailBaseInfoBO();
        Set<String> keys = jo.keySet();
        for (String key : keys) {
            if (key.contains("ćŒçä»·")) {
                result.setOpenPrice(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æ¶çä»·:äžć€æ")) {
                result.setClosePrice(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æé«ä»·:äžć€æ")) {
                result.setHighPrice(stockParseAndConvertService.convert(jo.get(key)));
            }

            if (key.contains("æäœä»·:äžć€æ")) {
                result.setLowPrice(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("ç«ä»·æ¶šćč")) {
                result.setOpenIncreaseRate(stockParseAndConvertService.convert(jo.get(key)));
            }

            if (key.contains("æć€§æ¶šćč")) {
                result.setHighIncreaseRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æŻćč")) {
                result.setDifferenceRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("ç«ä»·ééą")) {
                result.setCallAuctionTradeAmount(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æäș€éą")) {
                result.setTradeAmount(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æąæç")) {
                result.setTurnOverRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("aèĄćžćŒ")) {
                result.setMarketValue(stockParseAndConvertService.convert(jo.get(key)));
            }

        }
        //ćșçĄä»·æ ŒïŒćłæšæ„æ¶çä»·
        BigDecimal basePrice = result.getHighPrice().divide(result.getHighIncreaseRate().divide(new BigDecimal(100)).add(BigDecimal.ONE), 4, BigDecimal.ROUND_HALF_UP);
        result.setCloseIncreaseRate((result.getClosePrice().subtract(basePrice)).multiply(new BigDecimal(100)).divide(basePrice, 2, BigDecimal.ROUND_HALF_UP));
        result.setLowIncreaseRate(result.getHighIncreaseRate().subtract(result.getDifferenceRate()));
        //ćæç»æ
        return result;
    }


    public String getUpLimitStrongWeakType(JSONObject jo) {
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");

        if (upLimitStrongWeak.getLastUpLimitDate() == null) {
            return "æ¶šć";
        }
        if ("15:00:00".equals(DateTimeUtil.getDateFormat(upLimitStrongWeak.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS))) {
            return "æ¶šć";
        } else {
            return "æŸæ¶šć";
        }

    }

    public String getDownLimitStrongWeakType(JSONObject jo) {
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "è·ćæç»æ°æź");
        if (upLimitStrongWeak.getLastUpLimitDate() == null) {
            return "è·ć";
        }
        if ("15:00:00".equals(DateTimeUtil.getDateFormat(upLimitStrongWeak.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS))) {
            return "è·ć";
        } else {
            return "æŸè·ć";
        }

    }

    public String getDetailStrongWeakType(JSONObject jo) {
        StringBuffer sb = new StringBuffer();
        DetailBaseInfoBO detailBaseInfoBO = getDetailStrongWeak(jo);
        if (detailBaseInfoBO.getHighIncreaseRate().compareTo(new BigDecimal(7.5)) > 0) {
            sb.append("æ¶šćč" + detailBaseInfoBO.getHighIncreaseRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
        }
        if (detailBaseInfoBO.getCloseIncreaseRate().compareTo(new BigDecimal(-7.5)) < 0) {
            sb.append("è·ćč" + detailBaseInfoBO.getCloseIncreaseRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
        }
        if (detailBaseInfoBO.getDifferenceRate().compareTo(new BigDecimal(15)) > 0) {
            sb.append("æŻćč" + detailBaseInfoBO.getDifferenceRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%");
        }
        return sb.toString();

    }

    public String getLimitStrongWeakFirstSubVolDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(new BigDecimal(upLimitStrongWeak.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                    .subtract(new BigDecimal(upLimitStrongWeak.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "äž");
            upLimitStrongWeakDescribe = sb.toString();
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    public String getLimitStrongWeakRangeVolDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(new BigDecimal(upLimitStrongWeak.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN) + "äž--" +
                    (new BigDecimal(upLimitStrongWeak.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "äž");
            upLimitStrongWeakDescribe = sb.toString();
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    /**
     * ææć°ćć·źćŒ
     *
     * @param jo
     * @return
     */
    public String getLimitStrongWeakValidSubVolDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(new BigDecimal(upLimitStrongWeak.getHighestValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                    .subtract(new BigDecimal(upLimitStrongWeak.getFirstValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "äž");
            upLimitStrongWeakDescribe = sb.toString();
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    /**
     * ć°ćæŻç
     *
     * @param jo
     * @return
     */
    public String getLimitStrongWeakValidTimeRateDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            if (upLimitStrongWeak.getIdealDuration().compareTo(0L) == 0) {
                sb.append("0");
            } else {
                sb.append(new BigDecimal(upLimitStrongWeak.getDuration()).divide(new BigDecimal(upLimitStrongWeak.getIdealDuration()), 2, BigDecimal.ROUND_HALF_DOWN));
            }
            upLimitStrongWeakDescribe = sb.toString();
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    /**
     * éŠæŹĄæ¶šćæ¶éŽ
     *
     * @param jo
     * @return
     */
    public String getLimitStrongWeakFirstUpLimitTimeDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(DateTimeUtil.getDateFormat(upLimitStrongWeak.getFirstUpLimitDate(), DateTimeUtil.HH_MM_SS));
            upLimitStrongWeakDescribe = sb.toString();
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    /**
     * æćŒæ¶šćæŹĄæ°
     *
     * @param jo
     * @return
     */
    public String getLimitStrongWeakOpenNumDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            StringBuffer sb = new StringBuffer();
            sb.append(upLimitStrongWeak.getOpenNum() - 1);
            upLimitStrongWeakDescribe = sb.toString();
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }


    public String getLimitStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "æ¶šćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            upLimitStrongWeakDescribe = getLimitStrongWeakDescribe(upLimitStrongWeak,jo);
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    public String getDownLimitStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        LimitStrongWeakBO upLimitStrongWeak = getLimitStrongWeak(jo, "è·ćæç»æ°æź");
        if (upLimitStrongWeak != null) {
            upLimitStrongWeakDescribe = getLimitStrongWeakDescribe(upLimitStrongWeak,jo);
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    public String getDetailStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        DetailBaseInfoBO detailBaseInfoBO = getDetailStrongWeak(jo);
        if (detailBaseInfoBO != null) {
            upLimitStrongWeakDescribe = getDetailWeakDescribe(detailBaseInfoBO);
        }
        //ćæç»æ
        return upLimitStrongWeakDescribe;
    }

    public String getDetailWeakDescribe(DetailBaseInfoBO detailBaseInfoBO) {
        StringBuffer sb = new StringBuffer();
        sb.append("ćŒçæ¶šćčïŒ" + detailBaseInfoBO.getOpenIncreaseRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%------" + detailBaseInfoBO.getOpenPrice() + "\n");
        sb.append("æ¶çæ¶šćčïŒ" + detailBaseInfoBO.getCloseIncreaseRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%------" + detailBaseInfoBO.getClosePrice() + "\n");
        sb.append("æé«æ¶šćčïŒ" + detailBaseInfoBO.getHighIncreaseRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%------" + detailBaseInfoBO.getHighPrice() + "\n");
        sb.append("æäœæ¶šćčïŒ" + detailBaseInfoBO.getLowIncreaseRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%------" + detailBaseInfoBO.getLowPrice() + "\n");
        sb.append("ç«ä»·ééąïŒ" + stockParseAndConvertService.getMoneyFormat(detailBaseInfoBO.getCallAuctionTradeAmount()) + "\n");
        sb.append("æäș€éąïŒ" + stockParseAndConvertService.getMoneyFormat(detailBaseInfoBO.getTradeAmount()) + "\n");
        sb.append("æąæçïŒ" + detailBaseInfoBO.getTurnOverRate().setScale(2, BigDecimal.ROUND_HALF_UP) + "%\n");
        sb.append("ćžćŒïŒ" + stockParseAndConvertService.getMoneyFormat(detailBaseInfoBO.getMarketValue()) + "\n");
        return sb.toString();
    }


    public String getLimitStrongWeakDescribe(LimitStrongWeakBO up) {
        StringBuffer sb = new StringBuffer();
        sb.append("æ¶éŽïŒ" + up.getDateStr() + "\n");
        sb.append("æ¶šè·ćæ¶éŽïŒ" + DateTimeUtil.getDateFormat(up.getFirstUpLimitDate(), DateTimeUtil.HH_MM_SS) + "--");
        if (up.getLastUpLimitDate() != null) {
            //æ¶šćç»ææ¶éŽ
            sb.append(DateTimeUtil.getDateFormat(up.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS) + "\n");
        }
        sb.append("ć°æżæ¶éżæć”ïŒ" + up.getDuration() + "/");
        if (up.getLastUpLimitDate() != null) {
            //çæłæ¶éŽ
            sb.append(up.getIdealDuration() + "ćé  ");
        } else {
            sb.append("ćé  ");
        }
        try {
            //ćŒæżæ¶éż
            sb.append("=" + (up.getIdealDuration() - up.getDuration() - 1) + "ćé  \n");
            if (up.getIdealDuration() != null) {
                sb.append("ć°æżæŻçïŒ" + new BigDecimal(up.getDuration()).divide(new BigDecimal(up.getIdealDuration()), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "  \n");
            }
        } catch (Exception e) {

        }
        sb.append("ćŒæżæŹĄæ°ïŒ" + (up.getOpenNum() - 1) + "æŹĄæ°\n");
        sb.append("éŠæŹĄć°ćïŒ" + new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "äž");
        sb.append("/" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "äž");
        //ć°ćć·źćŒ
        sb.append("=" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                .subtract(new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "äž  \n");

        sb.append("ææć°ćïŒ" + new BigDecimal(up.getFirstValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "äž");
        sb.append("/" + new BigDecimal(up.getHighestValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "äž");
        //ć°ćć·źćŒ
        sb.append("=" + new BigDecimal(up.getHighestValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                .subtract(new BigDecimal(up.getFirstValidVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "äž  \n");

        sb.append("èŻä»·æèż°ïŒ" + up.getStrongWeakDescribe() + "\n");
        return sb.toString();
    }


    public String getLimitStrongWeakDescribe(LimitStrongWeakBO up, JSONObject jo) {
        String result = getLimitStrongWeakDescribe(up);
        StringBuffer sb = new StringBuffer();
        sb.append(result);
        String dateStr = up.getDateStr();
        String replaceDateStr = dateStr.replace("-", "");

        UpLimitValPriceBO callAuctionUpLimitInfo = getCallAuctionUpLimitInfo(jo, replaceDateStr);
        String callAuctionUpLimitInfoDescribe = getCallAuctionUpLimitInfoDescribe(callAuctionUpLimitInfo);
        sb.append(callAuctionUpLimitInfoDescribe);
        return sb.toString();
    }

    private UpLimitValPriceBO getCallAuctionUpLimitInfo(JSONObject jo, String replaceDateStr) {
        UpLimitValPriceBO upLimitValPriceBO=new UpLimitValPriceBO();
        for (String key : jo.keySet()) {
            if (key.contains("ç«ä»·æ¶šćč") && key.contains(replaceDateStr)) {
                upLimitValPriceBO.setCallAuctionIncreaseRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("ç«ä»·ééą") && key.contains(replaceDateStr)&& !key.contains("{/}")) {
                upLimitValPriceBO.setCallAuctionTradeAmount(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("ćæ¶æąæç") && key.contains(replaceDateStr)) {
                upLimitValPriceBO.setCallAuctionTurnOverRate(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æäș€éą") && key.contains(replaceDateStr)) {
                upLimitValPriceBO.setTradeAmount(stockParseAndConvertService.convert(jo.get(key)));
            }
            if (key.contains("æąæç") && key.contains(replaceDateStr)&&!key.contains("ćæ¶")) {
                upLimitValPriceBO.setTurnOverRate(stockParseAndConvertService.convert(jo.get(key)));
            }
        }
        return upLimitValPriceBO;
    }


    private String getCallAuctionUpLimitInfoDescribe( UpLimitValPriceBO upLimitValPriceBO){

        StringBuffer sb=new StringBuffer();
        sb.append("ç«ä»·æ¶šćčïŒ"+stockParseAndConvertService.getIncreaseRateFormat(upLimitValPriceBO.getCallAuctionIncreaseRate())+"--æ¶šć\n");
        sb.append("ç«ä»·ééąïŒ"+stockParseAndConvertService.getMoneyFormat(upLimitValPriceBO.getCallAuctionTradeAmount())+"--"
                +stockParseAndConvertService.getMoneyFormat(upLimitValPriceBO.getTradeAmount())+"\n");
        sb.append("ç«ä»·æąæïŒ"+stockParseAndConvertService.getIncreaseRateFormat(upLimitValPriceBO.getCallAuctionTurnOverRate())+"--"
                +stockParseAndConvertService.getIncreaseRateFormat(upLimitValPriceBO.getTurnOverRate())+"\n");
        return sb.toString();
    }
}
