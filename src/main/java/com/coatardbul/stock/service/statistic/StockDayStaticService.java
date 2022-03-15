package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.CookieEnum;
import com.coatardbul.stock.common.util.BigRoot;
import com.coatardbul.stock.mapper.StockCookieMapper;
import com.coatardbul.stock.mapper.StockDateStaticMapper;
import com.coatardbul.stock.model.bo.StockStaticBO;
import com.coatardbul.stock.model.bo.StockStaticBaseBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.dto.StockExcelStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStaticQueryDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockCookie;
import com.coatardbul.stock.model.entity.StockDateStatic;
import com.coatardbul.stock.model.entity.StockExcelTemplate;
import com.coatardbul.stock.service.StockExcelTemplateService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import com.coatardbul.stock.service.base.StockStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:策略处理
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class StockDayStaticService {

    @Autowired
    RiverRemoteService riverRemoteService;

    @Autowired
    StockDateStaticMapper stockDateStaticMapper;
    @Autowired
    StockExcelTemplateService stockExcelTemplateService;

    @Autowired
    StockStrategyService stockStrategyService;



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
    public StockStaticBO getStatic(StockStaticQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        //模板数据
        StockExcelTemplate excelTemplateInfo = stockExcelTemplateService.getStandardInfo(dto.getExcelTemplateId(),dto.getDateStr());

        StockStaticBO result = new StockStaticBO();
        result.setDateStr(dto.getDateStr());
        //上涨家数
        StrategyBO riseStrategy = stockStrategyService.strategy(convert(dto, excelTemplateInfo.getRiseId(), excelTemplateInfo.getOrderStr(), null));
        //下跌家数
        StrategyBO failStrategy = stockStrategyService.strategy(convert(dto, excelTemplateInfo.getFailId(), excelTemplateInfo.getOrderStr(), null));
        //上涨家数-下跌家数
        result.setAdjs(riseStrategy.getTotalNum() - failStrategy.getTotalNum());

        //涨停
        StrategyBO limitUpStrategy = stockStrategyService.strategy(convert(dto, excelTemplateInfo.getLimitUpId(),
                excelTemplateInfo.getOrderStr(), excelTemplateInfo.getOrderBy()));
        //基本统计信息
        StockStaticBaseBO staticBase = getStaticBase(limitUpStrategy, excelTemplateInfo.getOrderStr());
        result.setStandardDeviation(staticBase.getStandardDeviation());
        result.setMedian(staticBase.getMedian());
        result.setRaiseLimitNum(staticBase.getRaiseLimitNum());

        //涨停类型1
        if (StringUtils.isNotBlank(excelTemplateInfo.getLimitUpOneId())) {
            StrategyBO limitUpStrategy1 = stockStrategyService.strategy(convert(dto, excelTemplateInfo.getLimitUpOneId(), excelTemplateInfo.getOrderStr(), excelTemplateInfo.getOrderBy()));
            //基本统计信息
            StockStaticBaseBO staticBase1 = getStaticBase(limitUpStrategy1, excelTemplateInfo.getOrderStr());
            result.setStandardDeviationOne(staticBase1.getStandardDeviation());
            result.setMedianOne(staticBase1.getMedian());
            result.setRaiseLimitNumOne(staticBase1.getRaiseLimitNum());
        }
        //涨停类型2
        if (StringUtils.isNotBlank(excelTemplateInfo.getLimitUpTwoId())) {
            StrategyBO limitUpStrategy1 = stockStrategyService.strategy(convert(dto, excelTemplateInfo.getLimitUpTwoId(), excelTemplateInfo.getOrderStr(), excelTemplateInfo.getOrderBy()));
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
        List<String> data = riverRemoteService.getDateIntervalList(dto.getDateBeginStr(),dto.getDateEndStr());
        //根据dto，日期查询信息
        for (String dateStr : data) {
            Constant.dateJobThreadPool.execute(()->{
                stockDateStaticMapper.deleteByPrimaryKey(dateStr);
                StockStaticQueryDTO temp = convert(dto, dateStr);
                StockStaticBO aStatic = null;
                try {
                    aStatic = getStatic(temp);
                } catch (Exception e) {
                log.error(e.getMessage(),e);
                }
                insertDate(aStatic);
            });
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


    public void saveExcel(StockExcelStaticQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        // 根据开始结束时间查询工作日信息
        List<String> data = riverRemoteService.getDateIntervalList(dto.getDateBeginStr(),dto.getDateEndStr());
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

    public List<StockDateStatic> getAllStatic(StockExcelStaticQueryDTO dto) {
        List<StockDateStatic> stockDateStatics = stockDateStaticMapper.selectAllByDateBetweenEqual(dto.getDateBeginStr(), dto.getDateEndStr());

        return stockDateStatics.stream().sorted(Comparator.comparing(StockDateStatic::getDate)).collect(Collectors.toList());
    }
}
