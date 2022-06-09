package com.coatardbul.stock.service.base;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.CookieEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.common.util.TongHuaShunUtil;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockCookieMapper;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.StrategyQueryBO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockCookie;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.feign.StockTemplateQueryDTO;
import com.coatardbul.stock.service.statistic.UpLimitStrongWeakService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.conn.ConnectTimeoutException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:策略处理，单一指向同花顺问句查询
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockStrategyService {
    private static final String ID_SPLIT = ",";

    @Autowired
    RiverServerFeign riverServerFeign;

    @Autowired
    HttpService httpService;

    @Autowired
    UpLimitStrongWeakService upLimitStrongWeakService;
    //同花顺问财地址
    private static final String STRATEGY_URL = "http://www.iwencai.com/customized/chart/get-robot-data";

    private static final String STATUS_CODE = "status_code";

    private static final String STATUS_MSG = "status_msg";

    private static final String STATUS_SUCCESS = "0";


    private String cookieValue;

    @Autowired
    public void refreshCookie() {
        List<StockCookie> stockCookies = stockCookieMapper.selectAll();
        if (stockCookies != null && stockCookies.size() > 0) {
            cookieValue = stockCookies.stream().filter(o1 -> CookieEnum.strategy.getCode().equals(o1.getTypeKey()))
                    .collect(Collectors.toList()).get(0).getCookieValue();
        }
    }

    public void setCookieValue(String cookieValue) {
        this.cookieValue = cookieValue;
    }

    @Autowired
    StockCookieMapper stockCookieMapper;

    /**
     * 获取默认策略查询对象
     *
     * @return
     */
    private StrategyQueryBO getDefaultStrategyQuery() {
        StrategyQueryBO result = new StrategyQueryBO();
        result.setSecondary_intent("stock");
        result.setLog_info("{\\\"input_type\\\":\\\"typewrite\\\"}");
        result.setIwcpro(1);
        result.setSource("Ths_iwencai_Xuangu");
        result.setVersion("2.0");
        result.setPerpage(100);
        result.setPage(1);
//        result.setQuery_area();
//        result.setBlock_list();

        result.setAdd_info("");
        return result;
    }

    /**
     * 策略查询，支持两种模式
     * 1.传入id，日期，时间
     * 2.直接传入问句
     *
     * @param dto
     * @return
     * @throws BusinessException
     */
    public StrategyBO strategy(StockStrategyQueryDTO dto) throws BusinessException, NoSuchMethodException, ScriptException, FileNotFoundException {
        List<StrategyBO> list = batchStrategy(dto);
        if (list.size() == 1) {
            return list.get(0);
        }
        if (list.size() > 1) {
            return calcStrategy(list);
        }
        return new StrategyBO();
    }

    /**
     * 合并list数据，取交集
     *
     * @param list
     * @return
     */
    public StrategyBO calcStrategy(List<StrategyBO> list) {
        StrategyBO result = new StrategyBO();
        JSONArray jsonArray = new JSONArray();
        Map<String, JSONObject> codeInfo = new HashMap<>();
        Map<String, Integer> codeNum = new HashMap<>();
        for (int i = 0; i < list.get(0).getData().size(); i++) {
            JSONObject jsonObject = list.get(0).getData().getJSONObject(i);
            codeInfo.put(jsonObject.getString("code"), jsonObject);
            codeNum.put(jsonObject.getString("code"), 1);
        }
        for (int i = 1; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).getData().size(); j++) {
                JSONObject jsonObject = list.get(i).getData().getJSONObject(j);
                if (codeNum.containsKey(jsonObject.getString("code"))) {
                    codeNum.put(jsonObject.getString("code"), codeNum.get(jsonObject.getString("code")) + 1);
                }
            }
        }
        for (Map.Entry<String, Integer> map : codeNum.entrySet()) {
            if (map.getValue() == list.size()) {
                jsonArray.add(codeInfo.get(map.getKey()));
            }
        }
        result.setData(jsonArray);
        result.setTotalNum(jsonArray.size());
        return result;
    }

    /**
     * 多个策略，用逗号隔开，
     *
     * @param dto
     * @return
     */
    public List<StrategyBO> batchStrategy(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        List<StrategyBO> list = new ArrayList<>();
        CountDownLatch countDownLatch = null;
        if (StringUtils.isNotBlank(dto.getRiverStockTemplateId()) && dto.getRiverStockTemplateId().contains(ID_SPLIT)) {
            String[] split = dto.getRiverStockTemplateId().split(ID_SPLIT);
            countDownLatch = new CountDownLatch(split.length);
            for (String id : split) {
                CountDownLatch finalCountDownLatch = countDownLatch;
                Constant.abThreadPool.submit(() -> {
                    StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                    BeanUtils.copyProperties(dto, stockStrategyQueryDTO);
                    stockStrategyQueryDTO.setRiverStockTemplateId(id);
                    try {
                        StrategyBO strategyBO = strategyCommon(stockStrategyQueryDTO);
                        list.add(strategyBO);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        finalCountDownLatch.countDown();
                    }
                });
            }
        } else if (StringUtils.isNotBlank(dto.getRiverStockTemplateSign()) && dto.getRiverStockTemplateSign().contains(ID_SPLIT)) {
            String[] split = dto.getRiverStockTemplateSign().split(ID_SPLIT);
            countDownLatch = new CountDownLatch(split.length);
            for (String objectSign : split) {
                CountDownLatch finalCountDownLatch1 = countDownLatch;
                Constant.abThreadPool.submit(() -> {
                    StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                    BeanUtils.copyProperties(dto, stockStrategyQueryDTO);
                    stockStrategyQueryDTO.setRiverStockTemplateSign(objectSign);
                    try {
                        StrategyBO strategyBO = strategyCommon(stockStrategyQueryDTO);
                        list.add(strategyBO);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        finalCountDownLatch1.countDown();
                    }

                });
            }
        } else {
            StrategyBO strategyBO = strategyCommon(dto);
            list.add(strategyBO);
        }
        if (countDownLatch != null) {
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        return list;
    }


    public StrategyBO strategyCommon(StockStrategyQueryDTO dto) throws BusinessException, NoSuchMethodException, ScriptException, FileNotFoundException {
        StrategyBO result = new StrategyBO();
        //获取策略返回
        String response = getStrategyResponseStr(dto);
        if (StringUtils.isNotBlank(response)) {
            //解析返回体
            JSONObject requestObject = null;
            try {
                requestObject = JSONObject.parseObject(response);
            } catch (JSONException e) {
                throw new BusinessException("解析http请求返回的数据异常,返回字符串为：" + response + " 异常信息：" + e.getMessage());
            }
            if (!STATUS_SUCCESS.equals(requestObject.getString(STATUS_CODE))) {
                throw new BusinessException("请求同花顺策略问句异常，" + requestObject.getString(STATUS_MSG));
            }
            //基础信息
            JSONArray componentsArray = requestObject.getJSONObject("data").getJSONArray("answer")
                    .getJSONObject(0).getJSONArray("txt").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("components");

            JSONObject baseObject = null;
            if (componentsArray.size() == 1) {
                baseObject = componentsArray.getJSONObject(0).getJSONObject("data");
            } else {
                baseObject = componentsArray.getJSONObject(componentsArray.size() - 1).getJSONObject("data");
            }
            //解析的数据信息
            JSONArray data = baseObject.getJSONArray("datas");
            if (data == null) {
                return null;
            }
            //总数
            Integer totalNum = baseObject.getJSONObject("meta").getJSONObject("extra").getObject("row_count", Integer.class);
            log.info("策略查询返回数据总数：" + data.size());
//            log.info("策略查询返回数据总数：" + data.size() + "数据详情" + data.toString());
            result.setData(data);
            result.setTotalNum(totalNum);
            addUpLimitDescribe(result);
        }
        return result;
    }

    /**
     * 添加涨停描述
     *
     * @param strategyBO
     */
    private void addUpLimitDescribe(StrategyBO strategyBO) {
        JSONArray jsonArray = strategyBO.getData();
        if (jsonArray == null || jsonArray.size() == 0) {
            return;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            rebuild(jsonArray.getJSONObject(i));
        }
    }

    private void rebuild(JSONObject jo) {
        String upLimitStrongWeakDescribe = upLimitStrongWeakService.getLimitStrongWeakDescribe(jo);
        if (StringUtils.isNotBlank(upLimitStrongWeakDescribe)) {
            jo.put("涨停强弱概览", upLimitStrongWeakDescribe);
        }
        String limitStrongWeakRangeVolDescribe = upLimitStrongWeakService.getLimitStrongWeakRangeVolDescribe(jo);
        if (StringUtils.isNotBlank(limitStrongWeakRangeVolDescribe)) {
            jo.put("封单范围", limitStrongWeakRangeVolDescribe);
        }
        String limitStrongWeakFirstSubVolDescribe = upLimitStrongWeakService.getLimitStrongWeakFirstSubVolDescribe(jo);
        if (StringUtils.isNotBlank(limitStrongWeakFirstSubVolDescribe)) {
            jo.put("首次封单差值", limitStrongWeakFirstSubVolDescribe);
        }
        String limitStrongWeakValidSubVolDescribe = upLimitStrongWeakService.getLimitStrongWeakValidSubVolDescribe(jo);
        if (StringUtils.isNotBlank(limitStrongWeakValidSubVolDescribe)) {
            jo.put("有效封单差值", limitStrongWeakValidSubVolDescribe);
        }
        String limitStrongWeakValidTimeRateDescribe = upLimitStrongWeakService.getLimitStrongWeakValidTimeRateDescribe(jo);
        if (StringUtils.isNotBlank(limitStrongWeakValidTimeRateDescribe)) {
            jo.put("封单比率", limitStrongWeakValidTimeRateDescribe);
        }
        String limitStrongWeakFirstUpLimitTimeDescribe = upLimitStrongWeakService.getLimitStrongWeakFirstUpLimitTimeDescribe(jo);
        if (StringUtils.isNotBlank(limitStrongWeakFirstUpLimitTimeDescribe)) {
            jo.put("首次涨停时间", limitStrongWeakFirstUpLimitTimeDescribe);
        }
        String limitStrongWeakOpenNumDescribe = upLimitStrongWeakService.getLimitStrongWeakOpenNumDescribe(jo);
        if (StringUtils.isNotBlank(limitStrongWeakOpenNumDescribe)) {
            jo.put("打开涨停次数", limitStrongWeakOpenNumDescribe);
        }
    }

    private String getStrategyResponseStr(StockStrategyQueryDTO dto) throws BusinessException, NoSuchMethodException, ScriptException, FileNotFoundException {
        //默认信息
        StrategyQueryBO defaultStrategyQuery = getDefaultStrategyQuery();
        //请求dto信息
        setRequestInfo(dto, defaultStrategyQuery);
        //http请求
        String jsonString = JsonUtil.toJson(defaultStrategyQuery);
        List<Header> headerList = new ArrayList<>();
        String heXinStr = TongHuaShunUtil.getHeXinStr();
        Header cookie = httpService.getHead("Cookie", cookieValue + heXinStr);
        Header hexin = httpService.getHead("hexin-v", heXinStr);
        Header orign =httpService.getHead ("Origin", "http://www.iwencai.com");

        headerList.add(cookie);
        headerList.add(hexin);
        headerList.add(orign);
        log.info("策略查询传递参数" + jsonString);
        String result = null;
        int retryNum = 5;
        while (retryNum > 0) {
            try {
                result = httpService.doPost(STRATEGY_URL, jsonString, headerList);
            } catch (ConnectTimeoutException e) {
                retryNum--;
                continue;
            }
            if (StringUtils.isNotBlank(result)) {
                break;
            }
        }
        return result;
    }

    /**
     * 将请求中的dto转换成策略对象
     *
     * @param dto                  抽象请求数据
     * @param defaultStrategyQuery 策略对象
     */
    private void setRequestInfo(StockStrategyQueryDTO dto, StrategyQueryBO defaultStrategyQuery) {
        if (dto.getPageSize() != null && dto.getPage() != null) {
            defaultStrategyQuery.setPerpage(dto.getPageSize());
            defaultStrategyQuery.setPage(dto.getPage());
        } else {
            defaultStrategyQuery.setPerpage(300);
            defaultStrategyQuery.setPage(1);
        }
        defaultStrategyQuery.setSort_key(dto.getOrderStr());
        defaultStrategyQuery.setSort_order(dto.getOrderBy());
        // 此接口可以通过调用river获取实时动态数据
        if (StringUtils.isNotBlank(dto.getQueryStr())) {
            defaultStrategyQuery.setQuestion(dto.getQueryStr());
        } else {
            //feign
            StockTemplateQueryDTO stockTemplateQueryDto = new StockTemplateQueryDTO();
            stockTemplateQueryDto.setId(dto.getRiverStockTemplateId());
            stockTemplateQueryDto.setDateStr(dto.getDateStr());
            stockTemplateQueryDto.setTimeStr(dto.getTimeStr());
            stockTemplateQueryDto.setStockCode(dto.getStockCode());
            stockTemplateQueryDto.setObjectSign(dto.getRiverStockTemplateSign());
            stockTemplateQueryDto.setThemeStr(dto.getThemeStr());
            stockTemplateQueryDto.setStockScript(dto.getStockTemplateScript());
            CommonResult<String> riverServerFeignResult = riverServerFeign.getQuery(stockTemplateQueryDto);
            if (riverServerFeignResult != null) {
                defaultStrategyQuery.setQuestion(riverServerFeignResult.getData());
            }
        }


    }


    /**
     * 获取模型对象中的模板id集合
     *
     * @param stockStaticTemplate
     * @return
     * @throws IllegalAccessException
     */
    public List<String> getTemplateIdList(StockStaticTemplate stockStaticTemplate) throws IllegalAccessException {
        List<String> result = new ArrayList<>();
        //根据标识获取对应的对象解析id数据
        Class classBySign = StockStaticModuleUtil.getClassBySign(stockStaticTemplate.getObjectSign());
        Object o = JsonUtil.readToValue(stockStaticTemplate.getObjectStr(), classBySign);
        //获取类里面的所有属性集合
        ReflexUtil.singleReadAStringAttributeList(o, result);
        return result;
    }

}
