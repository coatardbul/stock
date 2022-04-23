package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * <p>
 * Note: 基础的解析和金额，百分比转换
 * <p>
 * Date: 2022/4/19
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockParseAndConvertService {


    public String getStockName(JSONObject jo){
        return  jo.getString("股票简称");
    }

    public String getStockCode(JSONObject jo){
        return  jo.getString("code");
    }






    /**
     * 获取金额
     * @param money
     * @return
     */
    public String getMoneyFormat(BigDecimal money){
        String moneyStr="";
        BigDecimal divide = money.divide(new BigDecimal(10000*10000), 2, BigDecimal.ROUND_HALF_DOWN);
        if(divide.compareTo(BigDecimal.ONE)>0){
            moneyStr=divide+"亿";
        }else {
            BigDecimal divide1 = money.divide(new BigDecimal(10000), 2, BigDecimal.ROUND_HALF_DOWN);
            moneyStr=divide1+"万";
        }
        return  moneyStr;
    }


    /**
     * 转换金额
     * @param value
     * @return
     */
    public BigDecimal convert(Object value) {
        if (value instanceof Integer) {
            return new BigDecimal((Integer) value);
        }
        if (value instanceof Long) {
            return new BigDecimal((Long) value);
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof String) {
            return new BigDecimal((String) value);
        }
        return (BigDecimal) value;
    }
}
