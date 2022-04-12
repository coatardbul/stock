package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockUpLimitValPriceMapper;
import com.coatardbul.stock.model.bo.UpLimitDetailInfo;
import com.coatardbul.stock.model.bo.UpLimitStrongWeakBO;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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


    /**
     * 涨停分析
     *
     * @param upLimitDetailList
     * @param dateStr
     * @return
     */
    public UpLimitStrongWeakBO getUpLimitStrongWeakInfo(List<UpLimitDetailInfo> upLimitDetailList, String dateStr) {
        UpLimitStrongWeakBO upLimitStrongWeakBO = new UpLimitStrongWeakBO();
        upLimitStrongWeakBO.setDateStr(dateStr);
        upLimitStrongWeakBO.setFirstUpLimitDate(new Date(upLimitDetailList.get(0).getTime()));
        if (upLimitDetailList.get(upLimitDetailList.size() - 1).getOpenTime() != null) {
            upLimitStrongWeakBO.setLastUpLimitDate(new Date(upLimitDetailList.get(upLimitDetailList.size() - 1).getOpenTime()));
        } else {
            try {
                Date lastTime = DateTimeUtil.parseDateStr(dateStr + " 15:00:00", DateTimeUtil.YYYY_MM_DD_HH_MM_SS);
                upLimitStrongWeakBO.setLastUpLimitDate(lastTime);
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
        }
        //理想时间
        long subTime = getIdealDuration(upLimitStrongWeakBO);
        upLimitStrongWeakBO.setIdealDuration(subTime / 1000 / 60);
        long sumMinuter = upLimitDetailList.stream().map(UpLimitDetailInfo::getDuration).mapToLong(Long::longValue).sum() / 1000 / 60;
        upLimitStrongWeakBO.setDuration(sumMinuter);
        upLimitStrongWeakBO.setOpenNum(upLimitDetailList.size());
        upLimitStrongWeakBO.setFirstVol(upLimitDetailList.get(0).getFirstVol());
        upLimitStrongWeakBO.setHighestVol(upLimitDetailList.get(0).getHighestVol());
        //描述
        rebuildUpLimitStrongWeakDescribe(upLimitDetailList, upLimitStrongWeakBO);
        return upLimitStrongWeakBO;
    }


    private long getIdealDuration(UpLimitStrongWeakBO upLimitStrongWeakBO) {
        long subTime = 0L;
        try {
            Date amTime = DateTimeUtil.parseDateStr(upLimitStrongWeakBO.getDateStr() + " 11:30:00", DateTimeUtil.YYYY_MM_DD_HH_MM_SS);
            Date pmtTime = DateTimeUtil.parseDateStr(upLimitStrongWeakBO.getDateStr() + " 13:00:00", DateTimeUtil.YYYY_MM_DD_HH_MM_SS);

            if (upLimitStrongWeakBO.getLastUpLimitDate().compareTo(amTime) <= 0 || upLimitStrongWeakBO.getFirstUpLimitDate().compareTo(pmtTime) >= 0) {
                subTime = upLimitStrongWeakBO.getLastUpLimitDate().getTime() - upLimitStrongWeakBO.getFirstUpLimitDate().getTime();
            }

            if (upLimitStrongWeakBO.getFirstUpLimitDate().compareTo(amTime) <= 0 && upLimitStrongWeakBO.getLastUpLimitDate().compareTo(pmtTime) >= 0) {
                long time1 = amTime.getTime() - upLimitStrongWeakBO.getFirstUpLimitDate().getTime();
                long time2 = upLimitStrongWeakBO.getLastUpLimitDate().getTime() - pmtTime.getTime();
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
     * @param upLimitStrongWeakBO
     */
    private void rebuildUpLimitStrongWeakDescribe(List<UpLimitDetailInfo> upLimitDetailList, UpLimitStrongWeakBO upLimitStrongWeakBO) {
        String firstUpLimitTimeStr = DateTimeUtil.getDateFormat(upLimitStrongWeakBO.getFirstUpLimitDate(), DateTimeUtil.HH_MM);
        //封板时间早
        if (firstUpLimitTimeStr.compareTo("09:30") == 0) {
            if (upLimitDetailList.get(0).getDuration() / 1000 / 10 > 5) {
                if (upLimitStrongWeakBO.getDuration() > 4 * 60 * 0.9) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("T字强回封");
                } else {
                    upLimitStrongWeakBO.setStrongWeakDescribe("T字弱回封");
                }
            }
        } else if (firstUpLimitTimeStr.compareTo("10:00") < 0) {
            if (upLimitDetailList.size() > 3) {
                if (upLimitStrongWeakBO.getDuration() < 4 * 60 * 0.75) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("弱");
                } else if (upLimitStrongWeakBO.getDuration() < 4 * 60 * 0.9) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("弱中带强");
                } else {
                    upLimitStrongWeakBO.setStrongWeakDescribe("强势换手");
                }
            } else if (upLimitDetailList.size() == 2) {
                if (upLimitStrongWeakBO.getDuration() < 3 * 60) {
                    upLimitStrongWeakBO.setStrongWeakDescribe("中偏弱");
                } else {
                    upLimitStrongWeakBO.setStrongWeakDescribe("中");
                }
            } else {
                upLimitStrongWeakBO.setStrongWeakDescribe("强");
            }
        } else if (firstUpLimitTimeStr.compareTo("11:00") < 0) {
            if (upLimitDetailList.size() == 1) {
                upLimitStrongWeakBO.setStrongWeakDescribe("弱中带强");
            }
        } else {
            upLimitStrongWeakBO.setStrongWeakDescribe("未知");

        }
        if (upLimitStrongWeakBO.getFirstVol() > 10 * 100 * 10000) {
            upLimitStrongWeakBO.setStrongWeakDescribe(upLimitStrongWeakBO.getStrongWeakDescribe() + "     封单量大");
        } else {
            upLimitStrongWeakBO.setStrongWeakDescribe(upLimitStrongWeakBO.getStrongWeakDescribe() + "     封单量小");
        }
    }


    /**
     * 将涨停信息放到list中，并返回涨停时间
     *
     * @param jo
     * @param upLimitDetailList
     * @return YYYY-MM-DD
     */
    private String parseUpLimitDetail(JSONObject jo, List<UpLimitDetailInfo> upLimitDetailList) {
        String dateStr = null;
        //取里面的数组信息
        Set<String> keys = jo.keySet();
        for (String key : keys) {
            if (key.contains("涨停明细数据")) {
                try {
                    dateStr = DateTimeUtil.getDateFormat(DateTimeUtil.parseDateStr(key.trim().substring(7, 15), DateTimeUtil.YYYYMMDD), DateTimeUtil.YYYY_MM_DD);
                } catch (ParseException e) {
                    log.error("解析时间异常" + e.getMessage(), e);
                }
                //解析涨停数据
                String upLimitDetailStr = (String) jo.get(key);
                upLimitDetailList.addAll(JsonUtil.readToValue(upLimitDetailStr, new TypeReference<List<UpLimitDetailInfo>>() {
                }));
            }
        }
        return dateStr;
    }

    public UpLimitStrongWeakBO getUpLimitStrongWeak(JSONObject jo) {
        UpLimitStrongWeakBO upLimitStrongWeakInfo = null;
        String dateStr = null;
        //涨停信息
        List<UpLimitDetailInfo> upLimitDetailList = new ArrayList<>();
        //解析涨停信息
        dateStr = parseUpLimitDetail(jo, upLimitDetailList);
        if (upLimitDetailList.size() > 0) {
            //对单个涨停策略分析
            upLimitStrongWeakInfo = getUpLimitStrongWeakInfo(upLimitDetailList, dateStr);
        }
        //分析结果
        return upLimitStrongWeakInfo;
    }


    public String getUpLimitStrongWeakType(JSONObject jo) {
        UpLimitStrongWeakBO upLimitStrongWeak = getUpLimitStrongWeak(jo);

        if (upLimitStrongWeak.getLastUpLimitDate() == null) {
            return "涨停";
        }
        if ("15:00:00".equals(DateTimeUtil.getDateFormat(upLimitStrongWeak.getLastUpLimitDate(),DateTimeUtil.HH_MM_SS))) {
            return "涨停";
        }else {
            return "昨曾";
        }

    }


    public String getUpLimitStrongWeakDescribe(JSONObject jo) {
        String upLimitStrongWeakDescribe = null;
        UpLimitStrongWeakBO upLimitStrongWeak = getUpLimitStrongWeak(jo);
        if (upLimitStrongWeak != null) {
            upLimitStrongWeakDescribe = getUpLimitStrongWeakDescribe(upLimitStrongWeak);
        }
        //分析结果
        return upLimitStrongWeakDescribe;
    }

    public String getUpLimitStrongWeakDescribe(UpLimitStrongWeakBO up) {
        StringBuffer sb = new StringBuffer();
        sb.append("时间：" + up.getDateStr() + "\n");
        sb.append("首次涨停时间：" + DateTimeUtil.getDateFormat(up.getFirstUpLimitDate(), DateTimeUtil.HH_MM_SS) + "\n");
        if (up.getLastUpLimitDate() != null) {
            sb.append("最后一次涨停时间：" + DateTimeUtil.getDateFormat(up.getLastUpLimitDate(), DateTimeUtil.HH_MM_SS) + "\n");
            sb.append("理论封板时长：" + up.getIdealDuration() + "分钟  \n");
        }
        sb.append("封板时长：" + up.getDuration() + "分钟  \n");
        try {
            sb.append("开板时长：" + (up.getIdealDuration() - up.getDuration() - 1) + "分钟  \n");
            if (up.getIdealDuration() != null) {
                sb.append("封板比率：" + new BigDecimal(up.getDuration()).divide(new BigDecimal(up.getIdealDuration()), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "  \n");
            }
        } catch (Exception e) {

        }
        sb.append("开板次数：" + (up.getOpenNum() - 1) + "次数\n");
        sb.append("首次封单：" + new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "万   ");
        sb.append("最高封单：" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN).toString() + "万  \n");
        sb.append("封单差值：" + new BigDecimal(up.getHighestVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)
                .subtract(new BigDecimal(up.getFirstVol()).divide(new BigDecimal(100 * 10000), 2, BigDecimal.ROUND_HALF_DOWN)) + "万  \n");
        sb.append("评价描述：" + up.getStrongWeakDescribe() + "\n");
        return sb.toString();
    }


}
