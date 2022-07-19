package com.coatardbul.stock.controller;

import com.coatardbul.stock.common.annotation.WebLog;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.model.bo.QuartzBean;
import com.coatardbul.stock.service.statistic.StockQuartzService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/7/16
 *
 * @author Su Xiaolei
 */
@Slf4j
@RestController
@Api(tags = "定时任务相关")
@RequestMapping("/quartz")
public class StockQuartzController {
    @Autowired
    StockQuartzService stockQuartzService;

    /**
     * 获取所有任务，
     */
    @WebLog(value = "")
    @RequestMapping(path = "/getTasks", method = RequestMethod.POST)
    public CommonResult getTasks() throws Exception {
        return CommonResult.success(stockQuartzService.getTasks());
    }

    /**
     * 创建定时任务
     */
    @WebLog(value = "")
    @RequestMapping(path = "/createScheduleJobCron", method = RequestMethod.POST)
    public CommonResult createScheduleJobCron(@Validated @RequestBody QuartzBean dto) throws Exception {
        stockQuartzService. xxx();
        stockQuartzService.createScheduleJobCron(dto);
        return CommonResult.success(null);
    }

    /**
     * 暂停任务
     */
    @WebLog(value = "")
    @RequestMapping(path = "/pauseScheduleJob", method = RequestMethod.POST)
    public CommonResult pauseScheduleJob(@Validated @RequestBody QuartzBean dto) throws Exception {
        stockQuartzService.pauseScheduleJob(dto.getJobName(), dto.getJobGroupName());
        return CommonResult.success(null);
    }
    /**
     * 恢复任务
     */
    @WebLog(value = "")
    @RequestMapping(path = "/resumeScheduleJob", method = RequestMethod.POST)
    public CommonResult resumeScheduleJob(@Validated @RequestBody QuartzBean dto) throws Exception {
        stockQuartzService.resumeScheduleJob(dto.getJobName(), dto.getJobGroupName());
        return CommonResult.success(null);
    }
    /**
     * 删除任务
     */
    @WebLog(value = "")
    @RequestMapping(path = "/deleteScheduleJob", method = RequestMethod.POST)
    public CommonResult deleteScheduleJob(@Validated @RequestBody QuartzBean dto) throws Exception {
        stockQuartzService.deleteScheduleJob(dto.getJobName(), dto.getJobGroupName());
        return CommonResult.success(null);
    }
    /**
     * 运行一次
     */
    @WebLog(value = "")
    @RequestMapping(path = "/runOnce", method = RequestMethod.POST)
    public CommonResult runOnce(@Validated @RequestBody QuartzBean dto) throws Exception {
        stockQuartzService.runOnce(dto.getJobName(), dto.getJobGroupName());
        return CommonResult.success(null);
    }
}
