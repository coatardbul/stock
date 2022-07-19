package com.coatardbul.stock.service.statistic;

import com.coatardbul.stock.model.bo.QuartzBean;
import com.xxl.job.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
public class StockQuartzService {

    @Autowired
    private Scheduler scheduler;


    public void xxx() {
        QuartzBean quartzBean = new QuartzBean();
        quartzBean.setJobClass("com.coatardbul.stock.service.statistic.MyTask");
        quartzBean.setJobName("job1");
        JobDataMap map = new JobDataMap();
        map.put("userId", "123456");
        quartzBean.setJobDataMap(map);
        Date now = new Date();
        quartzBean.setStartTime(DateUtils.addSeconds(now, 10));
        quartzBean.setInterval(10);
        quartzBean.setEndTime(DateUtils.addMinutes(now, 1));

        quartzBean.setJobName("job1");
        quartzBean.setCronExpression("*/5 * * * * ?");

        try {
            createScheduleJobCron(quartzBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 创建定时任务Simple
     * quartzBean.getInterval()==null表示单次提醒，
     * 否则循环提醒（quartzBean.getEndTime()!=null）
     *
     * @param quartzBean
     */
    public void createScheduleJobSimple(QuartzBean quartzBean) throws Exception {
        //获取到定时任务的执行类  必须是类的绝对路径名称
        //定时任务类需要是job类的具体实现 QuartzJobBean是job的抽象类。
        Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(quartzBean.getJobClass());
        // 构建定时任务信息
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null)
                .setJobData(quartzBean.getJobDataMap())
                .build();
        // 设置定时任务执行方式
        SimpleScheduleBuilder simpleScheduleBuilder = null;
        if (quartzBean.getInterval() == null) { //单次
            simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        } else { //循环
            simpleScheduleBuilder = SimpleScheduleBuilder.repeatMinutelyForever(quartzBean.getInterval());
        }
        // 构建触发器trigger
        Trigger trigger = null;
        if (quartzBean.getInterval() == null) { //单次
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null)
                    .withSchedule(simpleScheduleBuilder)
                    .startAt(quartzBean.getStartTime())
                    .build();
        } else { //循环
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null)
                    .withSchedule(simpleScheduleBuilder)
                    .startAt(quartzBean.getStartTime())
                    .endAt(quartzBean.getEndTime())
                    .build();
        }
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 创建定时任务Cron
     * 定时任务创建之后默认启动状态
     *
     * @param quartzBean 定时任务信息类
     * @throws Exception
     */
    public void createScheduleJobCron(QuartzBean quartzBean) throws Exception {
        //获取到定时任务的执行类  必须是类的绝对路径名称
        //定时任务类需要是job类的具体实现 QuartzJobBean是job的抽象类。
        Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(quartzBean.getJobClass());
        // 构建定时任务信息
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null).setJobData(quartzBean.getJobDataMap()).build();
        // 设置定时任务执行方式
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(quartzBean.getCronExpression());
        // 构建触发器trigger
        CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(quartzBean.getJobName()).withSchedule(scheduleBuilder).build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 根据任务名称暂停定时任务
     *
     * @param jobName  定时任务名称
     * @param jobGroup 任务组（没有分组传值null）
     * @throws Exception
     */
    public void pauseScheduleJob(String jobName, String jobGroup) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, StringUtils.isNotBlank(jobGroup) ? jobGroup : null);
        scheduler.pauseJob(jobKey);
    }

    /**
     * 根据任务名称恢复定时任务
     *
     * @param jobName  定时任务名
     * @param jobGroup 任务组（没有分组传值null）
     * @throws SchedulerException
     */
    public void resumeScheduleJob(String jobName, String jobGroup) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, StringUtils.isNotBlank(jobGroup) ? jobGroup : null);
        scheduler.resumeJob(jobKey);
    }

    /**
     * 根据任务名称立即运行一次定时任务
     *
     * @param jobName  定时任务名称
     * @param jobGroup 任务组（没有分组传值null）
     * @throws SchedulerException
     */
    public void runOnce(String jobName, String jobGroup) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName,StringUtils.isNotBlank(jobGroup) ? jobGroup : null);
        scheduler.triggerJob(jobKey);
    }

    /**
     * 更新定时任务Simple
     *
     * @param quartzBean 定时任务信息类
     * @throws SchedulerException
     */
    public void updateScheduleJobSimple(QuartzBean quartzBean) throws Exception {
        //获取到对应任务的触发器
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null);
        // 设置定时任务执行方式
        SimpleScheduleBuilder simpleScheduleBuilder = null;
        if (quartzBean.getInterval() == null) { //单次
            simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        } else { //循环
            simpleScheduleBuilder = SimpleScheduleBuilder.repeatMinutelyForever(quartzBean.getInterval());
        }
        // 构建触发器trigger
        Trigger trigger = null;
        if (quartzBean.getInterval() == null) { //单次
            trigger = TriggerBuilder.newTrigger()
                    .withIdentity(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null)
                    .withSchedule(simpleScheduleBuilder)
                    .startAt(quartzBean.getStartTime())
                    .build();
        } else { //循环
            TriggerBuilder.newTrigger()
                    .withIdentity(quartzBean.getJobName(), StringUtils.isNotBlank(quartzBean.getJobGroupName()) ? quartzBean.getJobGroupName() : null)
                    .withSchedule(simpleScheduleBuilder)
                    .startAt(quartzBean.getStartTime())
                    .endAt(quartzBean.getEndTime())
                    .build();
        }
        //重置对应的job
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    /**
     * 更新定时任务Cron
     *
     * @param quartzBean 定时任务信息类
     * @throws SchedulerException
     */
    public void updateScheduleJobCron(QuartzBean quartzBean) throws Exception {
        //获取到对应任务的触发器
        TriggerKey triggerKey = TriggerKey.triggerKey(quartzBean.getJobName());
        //设置定时任务执行方式
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(quartzBean.getCronExpression());
        //重新构建任务的触发器trigger
        CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
        //重置对应的job
        scheduler.rescheduleJob(triggerKey, trigger);
    }

    /**
     * 根据定时任务名称从调度器当中删除定时任务
     *
     * @param jobName  定时任务名称
     * @param jobGroup 任务组（没有分组传值null）
     * @throws SchedulerException
     */
    public void deleteScheduleJob(String jobName, String jobGroup) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, StringUtils.isNotBlank(jobGroup) ? jobGroup : null);
        scheduler.deleteJob(jobKey);
    }

    /**
     * 获取任务状态
     *
     * @param jobName
     * @param jobGroup 任务组（没有分组传值null）
     * @return (" BLOCKED ", " 阻塞 ");
     * ("COMPLETE", "完成");
     * ("ERROR", "出错");
     * ("NONE", "不存在");
     * ("NORMAL", "正常");
     * ("PAUSED", "暂停");
     */
    public String getScheduleJobStatus(String jobName, String jobGroup) throws Exception {
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, StringUtils.isNotBlank(jobGroup) ? jobGroup : null);
        Trigger.TriggerState state = scheduler.getTriggerState(triggerKey);
        return state.name();
    }

    /**
     * 根据定时任务名称来判断任务是否存在
     *
     * @param jobName  定时任务名称
     * @param jobGroup 任务组（没有分组传值null）
     * @throws SchedulerException
     */
    public Boolean checkExistsScheduleJob(String jobName, String jobGroup) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, StringUtils.isNotBlank(jobGroup) ? jobGroup : null);
        return scheduler.checkExists(jobKey);
    }

    /**
     * 根据任务組刪除定時任务
     *
     * @param jobGroup 任务组
     * @throws SchedulerException
     */
    public Boolean deleteGroupJob(String jobGroup) throws Exception {
        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(jobGroup);
        Set<JobKey> jobkeySet = scheduler.getJobKeys(matcher);
        List<JobKey> jobkeyList = new ArrayList<JobKey>();
        jobkeyList.addAll(jobkeySet);
        return scheduler.deleteJobs(jobkeyList);
    }



    /**
     * 根据任务組批量查询出jobkey
     *
     * @param jobGroup 任务组
     * @throws SchedulerException
     */
    public void batchQueryGroupJob(List<JobKey> jobkeyList, String jobGroup) throws Exception {
        GroupMatcher<JobKey> matcher = GroupMatcher.groupEquals(jobGroup);
        Set<JobKey> jobkeySet = scheduler.getJobKeys(matcher);
        jobkeyList.addAll(jobkeySet);
    }

    /**
     * 根据jobkey查询job详情
     *
     * @param jobKey
     * @return
     */
    public JobDetail queryJobDetail(JobKey jobKey) {
        JobDetail jobDetail = null;
        try {
            jobDetail = scheduler.getJobDetail(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return jobDetail;
    }

    public boolean checkExists(String jobName, String jobGroupName) {
        try {
            JobKey jobKey = JobKey.jobKey(jobName, jobGroupName);
            return  scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有JOB任务列表
     * @return
     */
    public List<QuartzBean> getTasks() {
        List<QuartzBean> resultList = new ArrayList<>();
        try {
            GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
            Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                for (Trigger trigger : triggers) {
                    QuartzBean result=new QuartzBean();
                    result.setJobName(jobKey.getName());
                    result.setJobGroupName(jobKey.getGroup());
                    result.setStartTime(trigger.getStartTime());
                    result.setEndTime(trigger.getEndTime());
                    result.setTriggerKey("触发器:" + trigger.getKey());
                    Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    result.setJobStatus(triggerState.name());
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    result.setJobClass(jobDetail.getJobClass().getName());
                    result.setDescription(jobDetail.getDescription());
                    result.setJobDataMap(jobDetail.getJobDataMap());
                    if (trigger instanceof CronTrigger) {
                        CronTrigger cronTrigger = (CronTrigger) trigger;
                        String cronExpression = cronTrigger.getCronExpression();
                       result.setCronExpression(cronExpression);
                    }
                    if (trigger instanceof SimpleTrigger) {
                        SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                        if ("triggerListener".equals(simpleTrigger.getKey().getName())){
                            continue;
                        }
                        Date fireTimeAfter = simpleTrigger.getFireTimeAfter(new Date());
                        String format = DateUtil.format(fireTimeAfter, "yyyy-MM-dd HH:mm:ss");
                        log.info("simple任务名称：{} 任务状态：{} 下次执行时间：{}",jobKey.getName().replace("job",""),triggerState.name(),format);
                    }

                    resultList.add(result);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return resultList;
    }


}


