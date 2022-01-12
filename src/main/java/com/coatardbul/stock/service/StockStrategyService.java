package com.coatardbul.stock.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.HttpUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.model.bo.StockStaticBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.StrategyQueryBO;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Note:
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
    private static final String STRATEGY_URL = "http://www.iwencai.com/customized/chart/get-robot-data";


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

    public StrategyBO strategy(StockStrategyQueryDTO dto) throws BusinessException {
        StrategyBO result = new StrategyBO();
        //默认信息
        StrategyQueryBO defaultStrategyQuery = getDefaultStrategyQuery();
        //请求dto信息
        setRequestInfo(dto, defaultStrategyQuery);

        //http请求
        String jsonString = JsonUtil.toJson(defaultStrategyQuery);
        List<Header> headerList = new ArrayList<>();
        Header cookie = HttpUtil.getHead("Cookie", dto.getCookie());
        headerList.add(cookie);
        String response = null;
        response = HttpUtil.doPost(STRATEGY_URL, jsonString, headerList);
        if (StringUtils.isNotBlank(response)) {
            //解析返回体
            JSONObject requestObject = JSONObject.parseObject(response);
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

    private void setRequestInfo(StockStrategyQueryDTO dto, StrategyQueryBO defaultStrategyQuery) {
        if (dto.getPageSize() != null && dto.getPage() != null) {
            defaultStrategyQuery.setPerpage(dto.getPageSize());
            defaultStrategyQuery.setPage(dto.getPage());
        }else {
            defaultStrategyQuery.setPerpage(300);
            defaultStrategyQuery.setPage(1);
        }
        defaultStrategyQuery.setSort_key(dto.getOrderStr());
        defaultStrategyQuery.setSort_order(dto.getOrderBy());
        // 此接口可以通过调用river获取实时动态数据
        defaultStrategyQuery.setQuestion(dto.getQueryStr());
        if (StringUtils.isNotBlank(dto.getTemplateId())) {
            //feign
            StockTemplateQueryDto stockTemplateQueryDto = new StockTemplateQueryDto();
            stockTemplateQueryDto.setId(dto.getTemplateId());
            stockTemplateQueryDto.setDateStr(dto.getDateStr());
            CommonResult<String> riverServerFeignResult = riverServerFeign.getQuery(stockTemplateQueryDto);
            if (riverServerFeignResult != null) {
                defaultStrategyQuery.setQuestion(riverServerFeignResult.getData());
            }
        }

    }

    private StockStrategyQueryDTO convert(StockStaticQueryDTO dt, String id, String orderStr, String orderBy) {
        StockStrategyQueryDTO result = new StockStrategyQueryDTO();
        result.setTemplateId(id);
        result.setDateStr(dt.getDateStr());
        result.setCookie(dt.getCookie());
        result.setPageSize(dt.getPageSize());
        result.setPage(dt.getPage());
        result.setOrderStr(orderStr);
        result.setOrderBy(orderBy);
        return result;
    }

    public StockStaticBO getStatic(StockStaticQueryDTO dto) {
        StockStaticBO result=new StockStaticBO();
        //上涨家数
        StrategyBO riseStrategy = strategy( convert(dto, dto.getRiseId(), null, null));
        //下跌家数
        StrategyBO failStrategy = strategy( convert(dto, dto.getFailId(), null, null));
        result.setAdjs(riseStrategy.getTotalNum()-failStrategy.getTotalNum());

        //涨停
        StrategyBO limitUpStrategy = strategy( convert(dto, dto.getLimitUpId(), dto.getOrderStr(), dto.getOrderBy()));
        int medianindex = limitUpStrategy.getTotalNum() / 2;
        if(medianindex!=0){
            JSONObject medianStrategy= limitUpStrategy.getData().getJSONObject(medianindex - 1);
            //中位数
            BigDecimal medianNum = medianStrategy.getBigDecimal(dto.getKeyStr());
            BigDecimal variance=BigDecimal.ZERO;
            for(int i=0;i<limitUpStrategy.getData().size();i++){
                BigDecimal b = limitUpStrategy.getData().getJSONObject(i).getBigDecimal(dto.getKeyStr()).subtract(medianNum);
                variance= variance.add(b.multiply(b));
            }
            //方差
            variance=variance.divide(new BigDecimal(limitUpStrategy.getTotalNum()-1),4, BigDecimal.ROUND_HALF_UP);

            result.setVariance(variance);
            result.setMedian(medianNum);
        }
        return result;
    }



}
