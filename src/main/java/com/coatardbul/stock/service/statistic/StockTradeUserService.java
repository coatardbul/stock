package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.mapper.StockTradeUrlMapper;
import com.coatardbul.stock.mapper.StockTradeUserMapper;
import com.coatardbul.stock.model.bo.StockTradeBO;
import com.coatardbul.stock.model.dto.StockTradeLoginDTO;
import com.coatardbul.stock.model.dto.StockUserCookieDTO;
import com.coatardbul.stock.model.entity.StockTradeUser;
import com.coatardbul.stock.service.base.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/6/3
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockTradeUserService {


    @Autowired
    StockTradeBaseService stockTradeBaseService;


    @Autowired
    StockTradeUrlMapper stockTradeUrlMapper;

    public void updateCookie(StockUserCookieDTO dto) {
        if(StringUtils.isNotBlank(dto.getCookie())){
            stockTradeBaseService.updateCookie(dto);
        }
        stockTradeUrlMapper.updateValidateKey(dto.getValidatekey());
    }
}
