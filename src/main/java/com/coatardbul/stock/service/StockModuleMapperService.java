package com.coatardbul.stock.service;

import com.coatardbul.stock.common.constants.PlateTypeEnum;
import com.coatardbul.stock.mapper.BaseInfoDictMapper;
import com.coatardbul.stock.model.dto.StockPriceRequestDTO;
import com.coatardbul.stock.model.entity.BaseInfoDict;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import com.coatardbul.stock.mapper.StockModuleMapperMapper;
import com.coatardbul.stock.model.entity.StockModuleMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StockModuleMapperService extends BaseService {

    @Resource
    private StockModuleMapperMapper stockModuleMapperMapper;
    @Autowired
    BaseInfoDictMapper baseInfoDictMapper;

    public void refreshStockModuleMapper(StockPriceRequestDTO dto) {
        if (StringUtils.isNotBlank(dto.getCode())) {
            stockProcess(dto);

        } else {

            //获取基础信息中的股票代码，寻找映射
            List<BaseInfoDict> baseInfoDicts = baseInfoDictMapper.selectAllByType(PlateTypeEnum.STOCK.getType());
            if (baseInfoDicts != null && baseInfoDicts.size() > 0) {
                for (BaseInfoDict b : baseInfoDicts) {
                    dto.setCode(b.getCode());
                    stockProcess(dto);
                }
            }

        }
    }

    @Override
    protected String getStockUrl(String code) {
        return "http://stockpage.10jqka.com.cn/" + code + "/";
    }

    @Override
    protected void parseAndSaveDate(String responseStr, StockPriceRequestDTO dto) throws IOException {
        Document doc = Jsoup.parse(responseStr);
        // 返回第一个
        Element body1 = doc.select("body").get(0);

        Node node = body1.childNode(21).childNode(3).childNode(5).childNode(3);

        getStockModuleMapperAndSaveDate(node,dto);

    }



    private  void getStockModuleMapperAndSaveDate(Node node, StockPriceRequestDTO dto) {
        stockModuleMapperMapper.deleteByStockCode(dto.getCode());
//        //地域
//        String territory = node.childNode(5).childNode(0).toString().trim();
//        List<String> territoryList = new ArrayList<>();
//        territoryList.add(territory);
//        List<BaseInfoDict> territoryBaseInfoDicts = baseInfoDictMapper.selectAllByTypeAndNameIn(PlateTypeEnum.TERRITORY.getType(), territoryList);
//        Map<String, String> territoryMap = territoryBaseInfoDicts.stream().collect(Collectors.toMap(BaseInfoDict::getName, BaseInfoDict::getCode));
//        StockModuleMapper stockModuleMapper = new StockModuleMapper();
//        stockModuleMapper.setStockCode(dto.getCode());
//        stockModuleMapper.setModuleCode(territoryMap.get(territory));
//        stockModuleMapperMapper.insert(stockModuleMapper);

        //概念
        String concept = node.childNode(9).attributes().get("title");
        String[] conceptArr = concept.split("，");
        List<String> conceptList = Arrays.asList(conceptArr).stream().map(o1->o1.trim()).collect(Collectors.toList());
        List<BaseInfoDict> baseInfoDicts = baseInfoDictMapper.selectAllByTypeAndNameIn(PlateTypeEnum.CONCEPT.getType(), conceptList);
        //key-name value-code  概念相关
        Map<String, String> conceptMap = baseInfoDicts.stream().collect(Collectors.toMap(BaseInfoDict::getName, BaseInfoDict::getCode));
        for (String s:conceptList) {
            StockModuleMapper conceptMapper = new StockModuleMapper();
            conceptMapper.setStockCode(dto.getCode());
            conceptMapper.setModuleCode(conceptMap.get(s));
            if(StringUtils.isNotBlank(conceptMapper.getModuleCode())){
                stockModuleMapperMapper.insert(conceptMapper);
            }
        }
    }

}
