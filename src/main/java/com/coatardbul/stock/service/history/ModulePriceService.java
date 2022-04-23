package com.coatardbul.stock.service.history;

import com.coatardbul.stock.model.bo.ModuleBaseBO;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.service.history.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.coatardbul.stock.model.entity.ModulePrice;
import com.coatardbul.stock.mapper.ModulePriceMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ModulePriceService extends BaseService {

    @Resource
    private ModulePriceMapper modulePriceMapper;


    public void refreshModuleBaseInfo(StockPriceRequestDTO dto) {
        stockProcess(dto);
    }

    @Override
    protected String getStockUrl(String code) {
        return "http://q.10jqka.com.cn/gn/";
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
            getModuleBaeInfo(n);
        }
    }


    private List<ModuleBaseBO> getModuleBaeInfo(Node node) {
        List<ModuleBaseBO> result = new ArrayList<>();
        List<Node> nodes = node.childNode(3).childNodes();
        for (Node n : nodes) {
            if (n instanceof TextNode) {
                continue;
            }
            ModuleBaseBO baseBO = new ModuleBaseBO();
            String url = n.attributes().get("href");
            baseBO.setUrl(url);
            baseBO.setCode(url.substring(url.length() - 7, url.length() - 1));
            baseBO.setName(n.childNodes().get(0).toString());
            result.add(baseBO);
        }
        return result;
    }

    public int deleteByPrimaryKey(String code) {
        return modulePriceMapper.deleteByPrimaryKey(code);
    }

    public int insert(ModulePrice record) {
        return modulePriceMapper.insert(record);
    }

    public int insertSelective(ModulePrice record) {
        return modulePriceMapper.insertSelective(record);
    }

    public ModulePrice selectByPrimaryKey(String code) {
        return modulePriceMapper.selectByPrimaryKey(code);
    }

    public int updateByPrimaryKeySelective(ModulePrice record) {
        return modulePriceMapper.updateByPrimaryKeySelective(record);
    }

    public int updateByPrimaryKey(ModulePrice record) {
        return modulePriceMapper.updateByPrimaryKey(record);
    }
}



