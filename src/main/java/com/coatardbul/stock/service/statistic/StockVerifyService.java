package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.StaticLatitudeEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockMinuterEmotionMapper;
import com.coatardbul.stock.mapper.StockStaticTemplateMapper;
import com.coatardbul.stock.model.bo.AxiosAllDataBo;
import com.coatardbul.stock.model.bo.AxiosBaseBo;
import com.coatardbul.stock.model.bo.AxiosYinfoDataBo;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockEmotionDayRangeDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockMinuterEmotion;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.feign.StockTemplateDto;
import com.coatardbul.stock.model.feign.StockTimeInterval;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/8
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockVerifyService {

    @Autowired
    RiverRemoteService riverRemoteService;

    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockStaticTemplateMapper stockStaticTemplateMapper;




    /**
     * ????????????
     * ????????????????????????
     *
     * @param dateStr YYYY-MM-DD
     * @throws ParseException
     */
    public void verifyDateStr(String  dateStr) throws ParseException {
        Date date = DateTimeUtil.parseDateStr(dateStr, DateTimeUtil.YYYY_MM_DD);
        if (new Date().compareTo(date) < 0) {
            throw new BusinessException("????????????"+dateStr+"????????????????????????????????????");
        }
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dateStr,dateStr);
        if (dateIntervalList == null || dateIntervalList.size() == 0) {
            throw new BusinessException("????????????");
        }
    }

    /**
     * ??????????????????
     * @param dateStr YYYY-MM-DD
     * @return true ????????????
     * @throws ParseException
     */
    public Boolean isIllegalDate(String  dateStr) throws ParseException {
        Date date = DateTimeUtil.parseDateStr(dateStr, DateTimeUtil.YYYY_MM_DD);
        if (new Date().compareTo(date) < 0) {
            return true;
        }
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dateStr,dateStr);
        if (dateIntervalList == null || dateIntervalList.size() == 0) {
            return true;
        }
        return false;
    }


    /**
     * ????????????
     * ????????????????????????
     *
     * @param dateStr YYYY-MM-DD
     * @throws ParseException
     */
    public void verifyDateTimeStr(String  dateStr,String timeStr) throws ParseException {
        Date date=DateTimeUtil.parseDateStr(dateStr+timeStr,DateTimeUtil.YYYY_MM_DD+DateTimeUtil.HH_MM);
        if (new Date().compareTo(date) < 0) {
            throw new BusinessException("??????"+dateStr+timeStr+"????????????????????????????????????");
        }
    }

    /**
     * ?????????????????????
     * @param dateStr
     * @param timeStr
     * @return
     * @throws ParseException
     */
    public Boolean isIllegalDateTimeStr(String  dateStr,String timeStr) throws ParseException {
        Date date=DateTimeUtil.parseDateStr(dateStr+timeStr,DateTimeUtil.YYYY_MM_DD+DateTimeUtil.HH_MM);
        if (new Date().compareTo(date) < 0) {
            return true;
        }
        if(timeStr.compareTo("09:30")<0){
            return true;
        }
        if(timeStr.compareTo("11:30")>0&&timeStr.compareTo("13:00")<0){
            return true;
        }
        return false;
    }


    public StockStaticTemplate verifyObjectSign(String objectSign) {
        List<StockStaticTemplate> stockStaticTemplates = stockStaticTemplateMapper.selectAllByObjectSign(objectSign);
        if (stockStaticTemplates == null || stockStaticTemplates.size() == 0) {
            throw new BusinessException("??????????????????");
        }
        //??????????????????
        return stockStaticTemplates.get(0);
    }

    /**
     * ????????????????????????
     *
     * @param timeInterval
     * @return
     */
    public List<String> getRemoteTimeInterval(Integer timeInterval) {
        StockTimeInterval stockTimeInterval = new StockTimeInterval();
        stockTimeInterval.setIntervalType(timeInterval);
        CommonResult<List<String>> timeIntervalList = riverServerFeign.getTimeIntervalList(stockTimeInterval);
        List<String> timeIntervalListData = timeIntervalList.getData();
        if (timeIntervalListData == null || timeIntervalListData.size() == 0) {
            throw new BusinessException("??????????????????????????????????????????????????????");
        }
        return timeIntervalListData;
    }

}
