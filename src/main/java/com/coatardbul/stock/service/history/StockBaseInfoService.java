package com.coatardbul.stock.service.history;

import com.coatardbul.stock.common.constants.PlateTypeEnum;
import com.coatardbul.stock.mapper.BaseInfoDictMapper;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.model.entity.BaseInfoDict;
import com.coatardbul.stock.service.history.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class StockBaseInfoService extends BaseService {

    @Resource
    private BaseInfoDictMapper baseInfoDictMapper;

    public void refreshModuleBaseInfo(StockPriceRequestDTO dto) throws Exception {
        stockProcess1(dto);
    }

    public void stockProcess1(StockPriceRequestDTO dto) throws Exception {
        // 获得Http客户端(可以理解为:你得先有一个浏览器;注意:实际上HttpClient与浏览器是不一样的)
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 参数
//        StringBuffer params = new StringBuffer();
//        // 字符数据最好encoding以下;这样一来，某些特殊字符才能传过去(如:某人的名字就是“&”,不encoding的话,传不过去)
//        params.append("id=" + num);

        // 创建Post请求
        HttpGet httpPost = new HttpGet(getStockUrl(dto.getCode()));
        // 设置ContentType(注:如果只是传普通参数的话,ContentType不一定非要用application/json)
        httpPost.setHeader("Referer", "http://q.10jqka.com.cn/");

//        httpPost.setHeader("X-Requested-With", "XMLHttpRequest");
//
        httpPost.setHeader("hexin-v", "A8EHh9oiRftNCqnPF3rDG9_Y1gbY7jWWX2PZ9CMWvkklEu9waz5FsO-y6cKw");

//        httpPost.setHeader("Accept", "text/html, */*; q=0.01");
//
//        httpPost.setHeader("Cookie", "searchGuide=sg; spversion=20130314; __utmc=156575163; userid=558402098; u_name=mo_558402098; escapename=mo_558402098; __utmz=156575163.1625873044.2.2.utmcsr=upass.10jqka.com.cn|utmccn=(referral)|utmcmd=referral|utmcct=/; user=MDptb181NTg0MDIwOTg6Ok5vbmU6NTAwOjU2ODQwMjA5ODo1LDEsNDA7NiwxLDQwOzcsMTExMTExMTExMTEwLDQwOzgsMTExMTAxMTEwMDAwMTExMTEwMDEwMDEwMDEwMDAwMDAsNDA7MzMsMDAwMTAwMDAwMDAwLDI4NDszNiwxMDAxMTExMTAwMDAxMTAwMTAxMTExMTEsMjg0OzQ2LDAwMDAxMTExMTAwMDAwMTExMTExMTExMSwyODQ7NTEsMTEwMDAwMDAwMDAwMDAwMCwyODQ7NTgsMDAwMDAwMDAwMDAwMDAwMDEsMjg0Ozc4LDEsMjg0Ozg3LDAwMDAwMDAwMDAwMDAwMDAwMDAxMDAwMCwyODQ7NDQsMTEsNDA7MSwxMDEsNDA7MiwxLDQwOzMsMSw0MDsxMDIsMSw0MDoyNDo6OjU1ODQwMjA5ODoxNjI1ODgwOTcyOjo6MTYwODYyNDE4MDo4NjQwMDowOjE1NzNhYzQ1MDMzZWE2N2IyMWYyZGY2NzYzOTZiY2U4MjpkZWZhdWx0XzQ6MQ%3D%3D; ticket=63f719be8d4557b7126c646b88294be4; utk=3764f6e5870a2838bf10b1bf7bf924bc; SL_GWPT_Show_Hide_tmp=1; SL_wptGlobTipTmp=1; Hm_lvt_78c58f01938e4d85eaf619eae71b4ed1=1625890095,1625931131,1625931428,1625988465; __utma=156575163.4560880.1625871201.1625931430.1625988472.6; historystock=002280%7C*%7C300248%7C*%7C002724%7C*%7C300003%7C*%7C605389; log=; Hm_lpvt_78c58f01938e4d85eaf619eae71b4ed1=1626019828; v=A9cRQYDwe8VpqP9JBvMNNbXqYEAiHKv_hfEv0CkE8KYMl_k2Mew7zpXAv0Q6");


        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Post请求
            response = httpClient.execute(httpPost);
            response.setHeader("Content-Type", "text/html; charset=UTF-8");
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            log.info("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                log.info("响应内容长度为:" + responseEntity.getContentLength());
                String responseStr = EntityUtils.toString(responseEntity, "utf-8");
                log.info("响应内容为:" + responseStr);
                //解析response，存入数据
                parseAndSaveDate(responseStr, dto);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw  new Exception(e.getMessage());
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected String getStockUrl(String code) {
        return "http://q.10jqka.com.cn/index/index/board/all/field/zdf/order/desc/page/" + code + "/ajax/1/";
    }

    @Override
    protected void parseAndSaveDate(String responseStr, StockPriceRequestDTO dto) throws IOException {
        Document doc = Jsoup.parse(responseStr);
        // 返回第一个
        Element body1 = doc.select("body").get(0);

        List<Node> nodes = body1.childNode(0).childNode(3).childNodes();
        for (Node n : nodes) {
            if (n instanceof TextNode) {
                continue;
            }
           BaseInfoDict moduleBaeInfo = getModuleBaeInfo(n);
            BaseInfoDict baseInfoDict = baseInfoDictMapper.selectByPrimaryKey(moduleBaeInfo.getCode());
            if(baseInfoDict==null){
                baseInfoDictMapper.insert(moduleBaeInfo);
            }
        }
    }


    private BaseInfoDict getModuleBaeInfo(Node node) {
        BaseInfoDict baseBO = new BaseInfoDict();
        String url = node.childNode(3).childNodes().get(0).attributes().get("href");
        baseBO.setType(PlateTypeEnum.STOCK.getType());
        baseBO.setUrl(url);
        baseBO.setCode(node.childNode(3).childNodes().get(0).childNodes().get(0).toString());
        baseBO.setName(node.childNode(5).childNodes().get(0).childNodes().get(0).toString());

        return baseBO;
    }


    public int deleteByPrimaryKey(String code) {
        return baseInfoDictMapper.deleteByPrimaryKey(code);
    }

    public int insert(BaseInfoDict record) {
        return baseInfoDictMapper.insert(record);
    }

    public int insertSelective(BaseInfoDict record) {
        return baseInfoDictMapper.insertSelective(record);
    }

    public BaseInfoDict selectByPrimaryKey(String code) {
        return baseInfoDictMapper.selectByPrimaryKey(code);
    }

    public int updateByPrimaryKeySelective(BaseInfoDict record) {
        return baseInfoDictMapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(BaseInfoDict record) {
        return baseInfoDictMapper.updateByPrimaryKey(record);
    }

    public int batchInsert(List<BaseInfoDict> list) {
        return baseInfoDictMapper.batchInsert(list);
    }
}

