package com.coatardbul.stock.service;

import com.coatardbul.stock.common.constants.PlateTypeEnum;
import com.coatardbul.stock.mapper.BaseInfoDictMapper;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.model.entity.BaseInfoDict;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class IndustryBaseInfoService extends BaseService {

    @Resource
    private BaseInfoDictMapper baseInfoDictMapper;

    public void refreshModuleBaseInfo(StockPriceRequestDTO dto) {
        baseInfoDictMapper.deleteByType(PlateTypeEnum.INDUSTRY.getType());
        stockProcess(dto);
    }

    @Override
    protected String getStockUrl(String code) {
        return "http://q.10jqka.com.cn/thshy/";
    }

    @Override
    protected void parseAndSaveDate(String responseStr, StockPriceRequestDTO dto) throws IOException {
        Document doc = Jsoup.parse(responseStr);
        // 返回第一个
        Element body1 = doc.select("body").get(0);

        List<Node> nodes = body1.childNode(2).childNode(1).childNode(1).childNodes();
        for (Node n : nodes) {
            if (n instanceof TextNode) {
                continue;
            }
            List<BaseInfoDict> moduleBaeInfo = getModuleBaeInfo(n);
            baseInfoDictMapper.batchInsert(moduleBaeInfo);
        }
    }


    private List<BaseInfoDict> getModuleBaeInfo(Node node) {
        List<BaseInfoDict> result = new ArrayList<>();
        List<Node> nodes = node.childNode(3).childNodes();
        for (Node n : nodes) {
            if (n instanceof TextNode) {
                continue;
            }
            BaseInfoDict baseBO = new BaseInfoDict();
            String url = n.attributes().get("href");
            baseBO.setType(PlateTypeEnum.INDUSTRY.getType());
            baseBO.setUrl(url);
            baseBO.setCode(url.substring(url.length() - 7, url.length() - 1));
            baseBO.setName(n.childNodes().get(0).toString());
            result.add(baseBO);
        }
        return result;
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

