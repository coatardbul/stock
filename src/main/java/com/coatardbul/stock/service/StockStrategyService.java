package com.coatardbul.stock.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.api.CommonResult;
import com.coatardbul.stock.common.constants.CookieEnum;
import com.coatardbul.stock.common.exception.BusinessException;
import com.coatardbul.stock.common.util.BigRoot;
import com.coatardbul.stock.common.util.HttpUtil;
import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.feign.river.RiverServerFeign;
import com.coatardbul.stock.mapper.StockCookieMapper;
import com.coatardbul.stock.mapper.StockDateStaticMapper;
import com.coatardbul.stock.mapper.StockExcelTemplateMapper;
import com.coatardbul.stock.model.bo.StockStaticBO;
import com.coatardbul.stock.model.bo.StockStaticBaseBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.StrategyQueryBO;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockCookie;
import com.coatardbul.stock.model.entity.StockDateStatic;
import com.coatardbul.stock.model.entity.StockExcelTemplate;
import com.coatardbul.stock.model.feign.CalendarDateDTO;
import com.coatardbul.stock.model.feign.StockTemplateQueryDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    StockDateStaticMapper stockDateStaticMapper;
    @Autowired
    StockExcelTemplateService stockExcelTemplateService;
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


    public StrategyBO strategy(StockStrategyQueryDTO dto) throws BusinessException {
        StrategyBO result = new StrategyBO();
        //默认信息
        StrategyQueryBO defaultStrategyQuery = getDefaultStrategyQuery();
        //请求dto信息
        setRequestInfo(dto, defaultStrategyQuery);
        //http请求
        String jsonString = JsonUtil.toJson(defaultStrategyQuery);
        List<Header> headerList = new ArrayList<>();
        Header cookie = HttpUtil.getHead("Cookie", cookieValue);
        headerList.add(cookie);
        String response = null;
        response = HttpUtil.doPost(STRATEGY_URL, jsonString, headerList);
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

    /**
     * 将请求中的dto转换成策略对象
     *
     * @param dto
     * @param defaultStrategyQuery
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
            CommonResult<String> riverServerFeignResult = riverServerFeign.getQuery(stockTemplateQueryDto);
            if (riverServerFeignResult != null) {
                defaultStrategyQuery.setQuestion(riverServerFeignResult.getData());
            }
        }

    }


    /**
     * @param dt
     * @param id
     * @param excelTemplateOrderStr
     * @param orderBy
     * @return
     */
    private StockStrategyQueryDTO convert(StockStaticQueryDTO dt, String id, String excelTemplateOrderStr, String orderBy) {
        StockStrategyQueryDTO result = new StockStrategyQueryDTO();
        result.setRiverStockTemplateId(id);
        result.setDateStr(dt.getDateStr());
        result.setPageSize(dt.getPageSize());
        result.setPage(dt.getPage());
        result.setOrderStr(excelTemplateOrderStr);
        result.setOrderBy(orderBy);
        return result;
    }


    /**
     * 获取当前日期的统计信息
     *
     * @param dto
     * @return
     */
    public StockStaticBO getStatic(StockStaticQueryDTO dto) {
        //模板数据
        StockExcelTemplate excelTemplateInfo = stockExcelTemplateService.getStandardInfo(dto.getExcelTemplateId(),dto.getDateStr());

        StockStaticBO result = new StockStaticBO();
        result.setDateStr(dto.getDateStr());
        //上涨家数
        StrategyBO riseStrategy = strategy(convert(dto, excelTemplateInfo.getRiseId(), excelTemplateInfo.getOrderStr(), null));
        //下跌家数
        StrategyBO failStrategy = strategy(convert(dto, excelTemplateInfo.getFailId(), excelTemplateInfo.getOrderStr(), null));
        //上涨家数-下跌家数
        result.setAdjs(riseStrategy.getTotalNum() - failStrategy.getTotalNum());

        //涨停
        StrategyBO limitUpStrategy = strategy(convert(dto, excelTemplateInfo.getLimitUpId(),
                excelTemplateInfo.getOrderStr(), excelTemplateInfo.getOrderBy()));
        //基本统计信息
        StockStaticBaseBO staticBase = getStaticBase(limitUpStrategy, excelTemplateInfo.getOrderStr());
        result.setStandardDeviation(staticBase.getStandardDeviation());
        result.setMedian(staticBase.getMedian());
        result.setRaiseLimitNum(staticBase.getRaiseLimitNum());

        //涨停类型1
        if (StringUtils.isNotBlank(excelTemplateInfo.getLimitUpOneId())) {
            StrategyBO limitUpStrategy1 = strategy(convert(dto, excelTemplateInfo.getLimitUpOneId(), excelTemplateInfo.getOrderStr(), excelTemplateInfo.getOrderBy()));
            //基本统计信息
            StockStaticBaseBO staticBase1 = getStaticBase(limitUpStrategy1, excelTemplateInfo.getOrderStr());
            result.setStandardDeviationOne(staticBase1.getStandardDeviation());
            result.setMedianOne(staticBase1.getMedian());
            result.setRaiseLimitNumOne(staticBase1.getRaiseLimitNum());
        }
        //涨停类型2
        if (StringUtils.isNotBlank(excelTemplateInfo.getLimitUpTwoId())) {
            StrategyBO limitUpStrategy1 = strategy(convert(dto, excelTemplateInfo.getLimitUpTwoId(), excelTemplateInfo.getOrderStr(), excelTemplateInfo.getOrderBy()));
            //基本统计信息
            StockStaticBaseBO staticBase1 = getStaticBase(limitUpStrategy1, excelTemplateInfo.getOrderStr());
            result.setStandardDeviationTwo(staticBase1.getStandardDeviation());
            result.setMedianTwo(staticBase1.getMedian());
            result.setRaiseLimitNumTwo(staticBase1.getRaiseLimitNum());
        }
        return result;
    }

    /**
     * 获取统计的基本信息
     *
     * @param limitUpStrategy 查询的策略对象
     * @param orderStr        排序字段
     * @return
     */
    private StockStaticBaseBO getStaticBase(StrategyBO limitUpStrategy, String orderStr) {
        StockStaticBaseBO result = new StockStaticBaseBO();
        int medianindex = limitUpStrategy.getTotalNum() / 2;
        int medianSub = limitUpStrategy.getTotalNum() % 2;
        if (medianindex != 0) {
            JSONObject medianStrategy = null;
            if (medianSub == 0) {
                medianStrategy = limitUpStrategy.getData().getJSONObject(medianindex - 1);
            } else {
                medianStrategy = limitUpStrategy.getData().getJSONObject(medianindex);
            }
            //标的数
            result.setRaiseLimitNum(limitUpStrategy.getData().size());
            //中位数
            BigDecimal medianNum = medianStrategy.getBigDecimal(orderStr);
            BigDecimal variance = BigDecimal.ZERO;
            for (int i = 0; i < limitUpStrategy.getData().size(); i++) {
                if (limitUpStrategy.getData().getJSONObject(i).getBigDecimal(orderStr) == null) {
                    continue;
                }
                BigDecimal b = limitUpStrategy.getData().getJSONObject(i).getBigDecimal(orderStr).subtract(medianNum);
                variance = variance.add(b.multiply(b));
            }
            //方差
            variance = variance.divide(new BigDecimal(limitUpStrategy.getTotalNum() - 1), 4, BigDecimal.ROUND_HALF_UP);
            result.setMedian(medianNum);
            //标准差
            result.setStandardDeviation(BigRoot.bigRoot(variance, 2, 4, BigDecimal.ROUND_HALF_UP));
        }
        return result;
    }


    public void saveDate(StockExcelStaticQueryDTO dto) {
        // 根据开始结束时间查询工作日信息
        CalendarDateDTO query = new CalendarDateDTO();
        query.setBeginDate(dto.getDateBeginStr());
        query.setEndDate(dto.getDateEndStr());
        query.setDateProp(1);
        CommonResult<List<String>> date = riverServerFeign.getDate(query);
        List<String> data = date.getData();
        //根据dto，日期查询信息
        for (String dateStr : data) {
            stockDateStaticMapper.deleteByPrimaryKey(dateStr);
            StockStaticQueryDTO temp = convert(dto, dateStr);
            StockStaticBO aStatic = getStatic(temp);
            insertDate(aStatic);
        }

    }


    private StockStaticQueryDTO convert(StockExcelStaticQueryDTO dto, String dateStr) {
        StockStaticQueryDTO temp = new StockStaticQueryDTO();
        //模板数据
        temp.setExcelTemplateId(dto.getExcelTemplateId());
        temp.setDateStr(dateStr);
        temp.setPageSize(dto.getPageSize());
        temp.setPage(dto.getPage());
        return temp;
    }


    public void saveExcel(StockExcelStaticQueryDTO dto) {
        // 根据开始结束时间查询工作日信息
        CalendarDateDTO query = new CalendarDateDTO();
        query.setBeginDate(dto.getDateBeginStr());
        query.setEndDate(dto.getDateEndStr());
        query.setDateProp(1);
        CommonResult<List<String>> date = riverServerFeign.getDate(query);
        List<String> data = date.getData();


        //策略数据
        List<StockStaticBO> result = new ArrayList<>();
        ;
        //根据dto，日期查询信息
        for (String dateStr : data) {
            //有则过，无则更新
            StockDateStatic stockDateStatic = stockDateStaticMapper.selectByPrimaryKey(dateStr);
            if (stockDateStatic != null) {
                StockStaticBO aStatic = convert(stockDateStatic);
                result.add(aStatic);
                continue;
            }
            StockStaticQueryDTO temp = convert(dto, dateStr);
            StockStaticBO aStatic = getStatic(temp);
            insertDate(aStatic);
            result.add(aStatic);
        }
        String filePath = "/Users/coatardbul/Desktop";
        String fileName = "方差标准差.xlsx";
        write(filePath, fileName, result);

    }

    private StockStaticBO convert(StockDateStatic stockDateStatic) {
        StockStaticBO aStatic = new StockStaticBO();
        aStatic.setAdjs(stockDateStatic.getAdjs());

        aStatic.setMedian(stockDateStatic.getMedian());
        aStatic.setStandardDeviation(stockDateStatic.getStandardDeviation());
        aStatic.setRaiseLimitNum(stockDateStatic.getRaiseLimitNum());


        aStatic.setMedianOne(stockDateStatic.getMedianOne());
        aStatic.setStandardDeviationOne(stockDateStatic.getStandardDeviationOne());
        aStatic.setRaiseLimitNumOne(stockDateStatic.getRaiseLimitNumOne());

        aStatic.setMedianTwo(stockDateStatic.getMedianTwo());
        aStatic.setStandardDeviationTwo(stockDateStatic.getStandardDeviationTwo());
        aStatic.setRaiseLimitNumTwo(stockDateStatic.getRaiseLimitNumTwo());

        aStatic.setDateStr(stockDateStatic.getDate());
        return aStatic;
    }

    private void insertDate(StockStaticBO aStatic) {
        StockDateStatic stockDateStatic = convert(aStatic);
        stockDateStaticMapper.insert(stockDateStatic);
    }


    private StockDateStatic convert(StockStaticBO aStatic) {
        StockDateStatic stockDateStatic = new StockDateStatic();
        stockDateStatic.setDate(aStatic.getDateStr());
        stockDateStatic.setAdjs(aStatic.getAdjs());
        stockDateStatic.setMedian(aStatic.getMedian());
        stockDateStatic.setStandardDeviation(aStatic.getStandardDeviation());
        stockDateStatic.setRaiseLimitNum(aStatic.getRaiseLimitNum());


        stockDateStatic.setMedianOne(aStatic.getMedianOne());
        stockDateStatic.setStandardDeviationOne(aStatic.getStandardDeviationOne());
        stockDateStatic.setRaiseLimitNumOne(aStatic.getRaiseLimitNumOne());


        stockDateStatic.setMedianTwo(aStatic.getMedianTwo());
        stockDateStatic.setStandardDeviationTwo(aStatic.getStandardDeviationTwo());
        stockDateStatic.setRaiseLimitNumTwo(aStatic.getRaiseLimitNumTwo());

        return stockDateStatic;


    }


    private void write(String filePath, String fileName, List<StockStaticBO> result) {

        File excelFile = new File(filePath + "/" + fileName);
        InputStream is = null;
        try {
            is = new FileInputStream(excelFile);
            Workbook wb = null;
            // 根据文件后缀（xls/xlsx）进行判断
            if (excelFile.getName().endsWith("xls")) {
                wb = new HSSFWorkbook(is);
            } else if (excelFile.getName().endsWith("xlsx")) {
                wb = new XSSFWorkbook(is);
            }

            //对应第几张sheet，从0开始
            Sheet sheet = wb.getSheetAt(0);
            //获得行数
            int rowSize = sheet.getLastRowNum() + 1;
            for (int j = 0; j < rowSize; j++) {
                //遍历行
                Row row = sheet.getRow(j);
                if (row == null) {
                    //略过空行
                    continue;
                }
                //根据业务写入逻辑
                writeRowDate(result, j, row);
                FileOutputStream os;
                os = new FileOutputStream(excelFile);
                wb.write(os);
                os.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * @param result 放入的数据对象
     * @param j      遍历时候的行数
     * @param row    excel行对象
     */
    private void writeRowDate(List<StockStaticBO> result, int j, Row row) {
        //日期
        if (j == 0) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                String dateStr = stockStaticBO.getDateStr();
                if (row.getCell(i + 2) == null) {
                    row.createCell(i + 2).setCellValue(dateStr);
                } else {
                    row.getCell(i + 2).setCellValue(dateStr);
                }
            }
        }
        if (j == 1) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                BigDecimal standardDeviation = stockStaticBO.getStandardDeviation();
                row.createCell(i + 2).setCellValue(new Double(standardDeviation.toString()));
            }
        }
        if (j == 2) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                BigDecimal median = stockStaticBO.getMedian();
                row.createCell(i + 2).setCellValue(new Double(median.toString()));
            }
        }
        if (j == 3) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                Integer raiseLimitNum = stockStaticBO.getRaiseLimitNum();
                row.createCell(i + 2).setCellValue(raiseLimitNum);
            }
        }
        if (j == 4) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                Integer adjs = stockStaticBO.getAdjs();
                row.createCell(i + 2).setCellValue(new Double(adjs));
            }
        }

        //涨停1
        if (j == 5) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                BigDecimal standardDeviation = stockStaticBO.getStandardDeviationOne();
                row.createCell(i + 2).setCellValue(new Double(standardDeviation.toString()));
            }
        }
        if (j == 6) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                BigDecimal median = stockStaticBO.getMedianOne();
                row.createCell(i + 2).setCellValue(new Double(median.toString()));
            }
        }
        if (j == 7) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                Integer raiseLimitNum = stockStaticBO.getRaiseLimitNumOne();
                row.createCell(i + 2).setCellValue(raiseLimitNum);
            }
        }

        if (j == 8) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                BigDecimal standardDeviation = stockStaticBO.getStandardDeviationTwo();
               if(standardDeviation==null){
                   continue;
               }
                row.createCell(i + 2).setCellValue(new Double(standardDeviation.toString()));
            }
        }
        if (j == 9) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                BigDecimal median = stockStaticBO.getMedianTwo();
                if(median==null){
                    continue;
                }
                row.createCell(i + 2).setCellValue(new Double(median.toString()));
            }
        }
        if (j == 10) {
            for (int i = 0; i < result.size(); i++) {
                StockStaticBO stockStaticBO = result.get(i);
                Integer raiseLimitNum = stockStaticBO.getRaiseLimitNumTwo();
                if(raiseLimitNum==null){
                    continue;
                }
                row.createCell(i + 2).setCellValue(raiseLimitNum);
            }
        }
    }


    /**
     * 2022-12-12
     *
     * @param dateStr
     * @return
     */
    private String getOrderStr(String dateStr) {
        String s = dateStr.replaceAll("-", "");
        return "竞价涨幅[" + s + "]";
    }
}
