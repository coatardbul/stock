package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.CookieEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.BigRoot;
import com.coatardbul.stock.common.util.HttpUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.common.util.ReflexUtil;
import com.coatardbul.stock.common.util.StockStaticModuleUtil;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockCookieMapper;
import com.coatardbul.stock.mapper.StockDateStaticMapper;
import com.coatardbul.stock.model.bo.StockStaticBO;
import com.coatardbul.stock.model.bo.StockStaticBaseBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.StrategyQueryBO;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockCookie;
import com.coatardbul.stock.model.entity.StockDateStatic;
import com.coatardbul.stock.model.entity.StockStaticTemplate;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import com.coatardbul.stock.service.StockExcelTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @Autowired
    RiverServerFeign riverServerFeign;
    @Autowired
    StockDateStaticMapper stockDateStaticMapper;
    @Autowired
    StockExcelTemplateService stockExcelTemplateService;
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
     * @param dto
     * @return
     * @throws BusinessException
     */
    public StrategyBO strategy(StockStrategyQueryDTO dto) throws BusinessException {
        StrategyBO result = new StrategyBO();
        //获取策略返回
        String response = getStrategyResponseStr(dto);
        if (StringUtils.isNotBlank(response)) {
            //解析返回体
            JSONObject requestObject = JSONObject.parseObject(response);
            if (!STATUS_SUCCESS.equals(requestObject.getString(STATUS_CODE))) {
                throw new BusinessException("请求同花顺策略问句异常，" + requestObject.getString(STATUS_MSG));
            }
            //基础信息
            JSONObject baseObject = requestObject.getJSONObject("data").getJSONArray("answer")
                    .getJSONObject(0).getJSONArray("txt").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("components")
                    .getJSONObject(0).getJSONObject("data");
            //解析的数据信息
            JSONArray data = baseObject.getJSONArray("datas");
            //总数
            Integer totalNum = baseObject.getJSONObject("meta").getJSONObject("extra").getObject("row_count", Integer.class);
            result.setData(data);
            result.setTotalNum(totalNum);
        }
        return result;
    }



    private String getStrategyResponseStr(StockStrategyQueryDTO dto) throws BusinessException {
        //默认信息
        StrategyQueryBO defaultStrategyQuery = getDefaultStrategyQuery();
        //请求dto信息
        setRequestInfo(dto, defaultStrategyQuery);
        //http请求
        String jsonString = JsonUtil.toJson(defaultStrategyQuery);
        List<Header> headerList = new ArrayList<>();
        Header cookie = HttpUtil.getHead("Cookie", cookieValue);
        headerList.add(cookie);
        return  HttpUtil.doPost(STRATEGY_URL, jsonString, headerList);
    }

    /**
     * 将请求中的dto转换成策略对象
     *
     * @param dto  抽象请求数据
     * @param defaultStrategyQuery   策略对象
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
        defaultStrategyQuery.setQuestion(dto.getQueryStr());
        if (StringUtils.isNotBlank(dto.getRiverStockTemplateId())) {
            //feign
            StockTemplateQueryDto stockTemplateQueryDto = new StockTemplateQueryDto();
            stockTemplateQueryDto.setId(dto.getRiverStockTemplateId());
            stockTemplateQueryDto.setDateStr(dto.getDateStr());
            stockTemplateQueryDto.setTimeStr(dto.getTimeStr());
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