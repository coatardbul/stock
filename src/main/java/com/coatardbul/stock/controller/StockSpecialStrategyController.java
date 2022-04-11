package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.script.ScriptException;
import java.io.FileNotFoundException;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/specialStrategy")
public class StockSpecialStrategyController {
@Autowired
    StockSpecialStrategyService stockSpecialStrategyService;

    /**
     *获取2板以上涨停数据
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getUpLimitInfo", method = RequestMethod.POST)
    public CommonResult getUpLimitInfo(@Validated @RequestBody StockEmotionDayDTO dto)  {
        return CommonResult.success( stockSpecialStrategyService.getTwoAboveUpLimitInfo(dto));
    }


    /**
     *获取昨曾模式强弱信息
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getOnceUpLimitStrongWeakInfo", method = RequestMethod.POST)
    public CommonResult getOnceUpLimitStrongWeakInfo(@Validated @RequestBody StockStrategyQueryDTO dto) throws  NoSuchMethodException, ScriptException, FileNotFoundException {
        return CommonResult.success( stockSpecialStrategyService.getOnceUpLimitStrongWeakInfo(dto));
    }

    /**
     *获取涨停题材
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getUpLimitTheme", method = RequestMethod.POST)
    public CommonResult getUpLimitTheme(@Validated @RequestBody StockStrategyQueryDTO dto) throws  NoSuchMethodException, ScriptException, FileNotFoundException {
        return CommonResult.success( stockSpecialStrategyService.getUpLimitTheme(dto));
    }


}
