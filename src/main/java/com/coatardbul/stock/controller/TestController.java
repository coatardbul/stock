package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.service.statistic.StockMinuteEmotinStaticService;
import com.coatardbul.stock.service.statistic.StockSpecialStrategyService;
import com.coatardbul.stock.task.DayStatisticJob;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/2/18
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "")
@RequestMapping("/test")
public class TestController {
    @Autowired
    StockSpecialStrategyService stockSpecialStrategyService;

    @WebLog(value = "")
    @RequestMapping(path = "/test", method = RequestMethod.POST)
    public void dayStatic() throws FileNotFoundException, ScriptException, NoSuchMethodException {
        // 获取JS执行引擎
        ScriptEngine se = new ScriptEngineManager().getEngineByName("javascript");
        // 获取变量
        String paht=System.getProperty("user.dir");
        FileReader fileReader = new FileReader(paht+"/js/aes.min.js");
        se.eval(fileReader);
        // 是否可调用
        if (se instanceof Invocable) {
            Invocable in = (Invocable) se;
            String result = (String) in.invokeFunction("v");
            log.info("获得的结果：" + result);

        }


    }



}
