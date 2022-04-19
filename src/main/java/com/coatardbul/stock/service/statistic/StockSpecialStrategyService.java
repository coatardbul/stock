package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.PlateEnum;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.StockAnomalousBehaviorDetailMapper;
import com.coatardbul.stock.mapper.StockAnomalousBehaviorStaticMapper;
import com.coatardbul.stock.mapper.StockOptionalPoolMapper;
import com.coatardbul.stock.model.bo.StockUpLimitInfoBO;
import com.coatardbul.stock.model.bo.StockUpLimitNameBO;
import com.coatardbul.stock.model.bo.StrategyBO;
import com.coatardbul.stock.model.bo.LimitBaseInfoBO;
import com.coatardbul.stock.model.bo.LimitStrongWeakBO;
import com.coatardbul.stock.model.dto.StockEmotionDayDTO;
import com.coatardbul.stock.model.dto.StockLastUpLimitDetailDTO;
import com.coatardbul.stock.model.dto.StockStrategyQueryDTO;
import com.coatardbul.stock.model.entity.StockAnomalousBehaviorDetail;
import com.coatardbul.stock.model.entity.StockAnomalousBehaviorStatic;
import com.coatardbul.stock.model.entity.StockOptionalPool;
import com.coatardbul.stock.service.base.StockStrategyService;
import com.coatardbul.stock.service.romote.RiverRemoteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/4/9
 *
 * @author Su Xiaolei
 */
@Service
@Slf4j
public class StockSpecialStrategyService {

    @Autowired
    StockStrategyService stockStrategyService;
    @Autowired
    StockUpLimitValPriceService stockUpLimitValPriceService;
    @Autowired
    UpLimitStrongWeakService upLimitStrongWeakService;
    @Autowired
    RiverRemoteService riverRemoteService;
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    StockAnomalousBehaviorDetailMapper stockAnomalousBehaviorDetailMapper;
    @Autowired
    StockAnomalousBehaviorStaticMapper stockAnomalousBehaviorStaticMapper;
    @Autowired
    StockOptionalPoolMapper stockOptionalPoolMapper;

    public List<StockUpLimitNameBO> getTwoAboveUpLimitInfo(StockEmotionDayDTO dto) {

        List<StockUpLimitNameBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);

        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.emotionIntervalByDateThreadPool.submit(() -> {
                StockUpLimitNameBO stockUpLimitNameBO = new StockUpLimitNameBO();
                stockUpLimitNameBO.setUpLimitNum(num + "板");
                String upLimitNumScript = getUpLimitNumScript(num);
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                stockStrategyQueryDTO.setStockTemplateScript(upLimitNumScript);
                StrategyBO strategy = null;
                try {
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    JSONArray data = strategy.getData();
                    List<String> nameList = new ArrayList<>();
                    for (Object jo : data) {
                        nameList.add(((String) ((JSONObject) jo).get("股票简称")));
                    }
                    stockUpLimitNameBO.setNameList(nameList);
                    if (stockUpLimitNameBO.getNameList().size() > 0) {
                        result.add(stockUpLimitNameBO);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    countDownLatch.countDown();
                }

            });
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return result.stream().sorted(Comparator.comparing(StockUpLimitNameBO::getUpLimitNum).reversed()).collect(Collectors.toList());
    }


    /**
     * 非创业板，非st板块，{{lastDay3}}未涨停，{{lastDay2}}涨停，{{lastDay1}}涨停，{{today}}涨停，
     * 获取连续涨停的脚本
     *
     * @param num
     * @return
     */
    private String getUpLimitNumScript(int num) {
        StringBuffer sb = new StringBuffer();
        sb.append(" 非创业板，非st板块，");
        sb.append("{{lastDay" + (num) + "}}未涨停，");
        for (int i = num - 1; i > 0; i--) {
            sb.append("{{lastDay" + i + "}}涨停，");
        }
        sb.append("{{today}}涨停，");

        return sb.toString();
    }


    public StrategyBO getOnceUpLimitStrongWeakInfo(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        return stockStrategyService.strategy(dto);
    }


    public List<StockUpLimitInfoBO> getUpLimitTheme(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        StrategyBO strategy = stockStrategyService.strategy(dto);
        if (strategy.getTotalNum() > 0) {
            JSONArray data = strategy.getData();
            return rebuildThemeInfo(data);
        }
        return null;
    }

    public List<StockUpLimitInfoBO> rebuildThemeInfo(JSONArray data) {
        //key 为题材名称 value为股票名称
        Map<String, List<String>> themeMap = new HashMap<>();

        //key 为股票名称 value为股票名称
        Map<String, LimitBaseInfoBO> nameMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jo = data.getJSONObject(i);
            //取里面的数组信息
            Set<String> keys = jo.keySet();
            String stockName = (String) jo.get("股票简称");
            LimitStrongWeakBO upLimitStrongWeak = upLimitStrongWeakService.getLimitStrongWeak(jo, "涨停明细数据");
            LimitBaseInfoBO upLimitBaseInfoBO = new LimitBaseInfoBO();
            BeanUtils.copyProperties(upLimitStrongWeak, upLimitBaseInfoBO);
            upLimitBaseInfoBO.setName(stockName);
            nameMap.put(stockName, upLimitBaseInfoBO);

            for (String key : keys) {
                if (key.contains("涨停原因类别")) {
                    String themeStr = (String) jo.get(key);
                    if (themeStr.contains("+")) {
                        for (String str : themeStr.split("\\+")) {
                            if (themeMap.containsKey(str)) {
                                themeMap.get(str).add(stockName);
                            } else {
                                List<String> name = new ArrayList<>();
                                name.add(stockName);
                                themeMap.put(str, name);
                            }
                        }
                    } else {
                        if (themeMap.containsKey(themeStr)) {
                            themeMap.get(themeStr).add(stockName);
                        } else {
                            List<String> name = new ArrayList<>();
                            name.add(stockName);
                            themeMap.put(themeStr, name);
                        }
                    }
                }
            }
        }
        List<StockUpLimitInfoBO> result = themeMap.entrySet().stream().map(o1 -> convert(o1, nameMap)).collect(Collectors.toList());
        result = result.stream().sorted(Comparator.comparing(StockUpLimitInfoBO::getNum)).collect(Collectors.toList());
        return result;
    }

    private StockUpLimitInfoBO convert(Map.Entry<String, List<String>> map, Map<String, LimitBaseInfoBO> nameMap) {
        StockUpLimitInfoBO stockUpLimitInfoBO = new StockUpLimitInfoBO();
        stockUpLimitInfoBO.setThemeName(map.getKey());
        List<LimitBaseInfoBO> nameList = new ArrayList<>();
        for (String name : map.getValue()) {
            nameList.add(nameMap.get(name));
        }
        stockUpLimitInfoBO.setNameList(nameList);
        stockUpLimitInfoBO.setNum(map.getValue().size());
        return stockUpLimitInfoBO;
    }

    public List<StockAnomalousBehaviorDetail> getAllAnomalousBehaviorData(StockLastUpLimitDetailDTO dto) {
        List<String> codeList = getCodeList(dto);
        List<StockAnomalousBehaviorDetail> stockAnomalousBehaviorDetails = stockAnomalousBehaviorDetailMapper.selectAllByCodeInAndDateBetweenEqual(codeList, dto.getBeginDateStr(), dto.getEndDateStr());
        return stockAnomalousBehaviorDetails;
    }

    /**
     * 构建过去涨停信息
     * 1.根据templatedId，和date构建
     * 2.根据时间区间和代码构建
     *
     * @param dto date为当日
     * @return
     */
    public void buildLastUpLimitInfo(StockLastUpLimitDetailDTO dto) {
        //获取code集合
        List<String> codeList = getCodeList(dto);
        //时间区间
        if (!StringUtils.isNotBlank(dto.getBeginDateStr()) || !StringUtils.isNotBlank(dto.getEndDateStr())) {
            dto.setEndDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(), -1));
            dto.setBeginDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(), -30));
        }

        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDateStr(), dto.getEndDateStr());
        for (String code : codeList) {
            //查询表中有的数据
            StockAnomalousBehaviorStatic stockAnomalousBehaviorStatic = stockAnomalousBehaviorStaticMapper.selectByCode(code);

            if (stockAnomalousBehaviorStatic == null) {
                asynAddAnormalousBehaviorDetail(code, dateIntervalList);
                //添加
                addOrUpdateStockAnomalousBehaviorStatic(code, dateIntervalList.get(0), dateIntervalList.get(dateIntervalList.size() - 1), true);
            } else {
                if (stockAnomalousBehaviorStatic.getEndDate().equals(dateIntervalList.get(dateIntervalList.size() - 1))) {
                    return;
                }
                List<String> dateIntervalList1 = riverRemoteService.getDateIntervalList(stockAnomalousBehaviorStatic.getEndDate(), dateIntervalList.get(dateIntervalList.size() - 1));
                asynAddAnormalousBehaviorDetail(code, dateIntervalList1);
                //更新
                addOrUpdateStockAnomalousBehaviorStatic(code, dateIntervalList1.get(0), dateIntervalList1.get(dateIntervalList1.size() - 1), false);

            }

        }
    }


    private List<String> getCodeList(StockLastUpLimitDetailDTO dto) {
        List<String> codeList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getRiverStockTemplateId()) && StringUtils.isNotBlank(dto.getDateStr())) {
            codeList.addAll(getCodeList(dto.getDateStr(), dto.getRiverStockTemplateId()));
        }
        if (StringUtils.isNotBlank(dto.getStockCode())) {
            codeList.add(dto.getStockCode());
        }
        if (dto.getPlateList() != null && dto.getPlateList().size() > 0) {
            List<StockOptionalPool> stockOptionalPools = stockOptionalPoolMapper.selectAllByNameLikeAndPlateIdIn(null, dto.getPlateList());
            if (stockOptionalPools != null && stockOptionalPools.size() > 0) {
                List<String> collect = stockOptionalPools.stream().map(StockOptionalPool::getCode).collect(Collectors.toList());
                codeList.addAll(collect);
            }
            codeList.add("1");
        }
        return codeList;
    }

    /**
     * @param code
     * @param beginDate
     * @param endDate
     * @param addOrUpdateFlag add 为true update 为false
     */
    private void addOrUpdateStockAnomalousBehaviorStatic(String code, String beginDate, String endDate, boolean addOrUpdateFlag) {
        StockAnomalousBehaviorStatic addStatic = new StockAnomalousBehaviorStatic();
        if (addOrUpdateFlag) {
            addStatic.setId(baseServerFeign.getSnowflakeId());
            addStatic.setCode(code);
//            addStatic.setName();
            addStatic.setBeginDate(beginDate);
            addStatic.setEndDate(endDate);
//            addStatic.setStaticDetail();
            stockAnomalousBehaviorStaticMapper.insertSelective(addStatic);
        } else {
            StockAnomalousBehaviorStatic updateInfo = stockAnomalousBehaviorStaticMapper.selectByCode(code);
            updateInfo.setEndDate(endDate);
            stockAnomalousBehaviorStaticMapper.updateByPrimaryKeySelective(updateInfo);
        }
    }


    private void asynAddAnormalousBehaviorDetail(String code, List<String> dateStrList) {
        for (String dateStr : dateStrList) {
            Constant.onceUpLimitThreadPool.submit(() -> {
                StockAnomalousBehaviorDetail stockAnomalousBehaviorDetail = stockAnomalousBehaviorDetailMapper.selectAllByCodeAndDate(code, dateStr);
                if (stockAnomalousBehaviorDetail == null) {
                    addStockAnomalousBehaviorDescribe(code, dateStr);
                }
            });
        }
    }


    private List<String> getCodeList(String dateStr, String riverStockTemplateId) {
        List<String> codeList = new ArrayList<>();
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setDateStr(dateStr);
        stockStrategyQueryDTO.setRiverStockTemplateId(riverStockTemplateId);
        StrategyBO strategy = null;
        try {
            strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            JSONArray data = strategy.getData();
            for (Object jo : data) {
                codeList.add(((String) ((JSONObject) jo).get("code")));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return codeList;
    }

    /**
     * 将股票的异动信息转换存入表中
     *
     * @param code
     * @param dateStr
     */
    private void addStockAnomalousBehaviorDescribe(String code, String dateStr) {

        //1.涨停信息
        StrategyBO strategyUpLimitBO = addDescribe(code, dateStr, StockTemplateEnum.STOCK_UP_LIMIT);
        if (strategyUpLimitBO != null) {
            return;
        }
        //2.跌停信息
        StrategyBO strategyDownLimitBO = addDescribe(code, dateStr, StockTemplateEnum.STOCK_DOWN_LIMIT);
        if (strategyDownLimitBO != null) {
            return;
        }
        //3.股票详细信息
        StrategyBO strategyDetailBO = addDescribe(code, dateStr, StockTemplateEnum.STOCK_DETAIL);
        if (strategyDetailBO != null) {
            return;
        }
    }


    /**
     * 将股票的异动信心转换存入表中
     *
     * @param code
     * @param dateStr
     */
    private StrategyBO addDescribe(String code, String dateStr, StockTemplateEnum stockTemplateEnum) {
        StrategyBO strategy = null;
        StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
        stockStrategyQueryDTO.setDateStr(dateStr);
        stockStrategyQueryDTO.setRiverStockTemplateId(stockTemplateEnum.getId());
        stockStrategyQueryDTO.setStockCode(code);
        try {
            strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
            if (strategy == null) {
                return null;
            }
            JSONObject jsonObject = strategy.getData().getJSONObject(0);
            StockAnomalousBehaviorDetail stockAnomalousBehaviorDetail = getStockAnomalousBehaviorDetail(jsonObject, stockTemplateEnum);
            if (StringUtils.isNotBlank(stockAnomalousBehaviorDetail.getUpLimitType())) {
                stockAnomalousBehaviorDetail.setId(baseServerFeign.getSnowflakeId());
                stockAnomalousBehaviorDetail.setDate(dateStr);
                stockAnomalousBehaviorDetail.setCode(code);
                stockAnomalousBehaviorDetailMapper.insertSelective(stockAnomalousBehaviorDetail);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return strategy;
    }


    /**
     * 获取单个股票，单个日期的单日详情存入表中
     *
     * @param jsonObject
     * @param stockTemplateEnum
     * @return
     */
    private StockAnomalousBehaviorDetail getStockAnomalousBehaviorDetail(JSONObject jsonObject, StockTemplateEnum stockTemplateEnum) {
        StockAnomalousBehaviorDetail stockAnomalousBehaviorDetail = new StockAnomalousBehaviorDetail();
        String stockName = jsonObject.getString("股票简称");
        stockAnomalousBehaviorDetail.setName(stockName);
        //描述类型
        String strongWeakType = null;
        //描述详情
        String strongWeakDescribe = null;
        //涨停信息
        if (StockTemplateEnum.STOCK_UP_LIMIT.getSign().equals(stockTemplateEnum.getSign())) {
            strongWeakType = upLimitStrongWeakService.getUpLimitStrongWeakType(jsonObject);
            strongWeakDescribe = upLimitStrongWeakService.getLimitStrongWeakDescribe(jsonObject);
        }
        //跌停信息
        if (StockTemplateEnum.STOCK_DOWN_LIMIT.getSign().equals(stockTemplateEnum.getSign())) {
            strongWeakType = upLimitStrongWeakService.getDownLimitStrongWeakType(jsonObject);
            strongWeakDescribe = upLimitStrongWeakService.getDownLimitStrongWeakDescribe(jsonObject);
        }
        //股票详情信息
        if (StockTemplateEnum.STOCK_DETAIL.getSign().equals(stockTemplateEnum.getSign())) {
            strongWeakType = upLimitStrongWeakService.getDetailStrongWeakType(jsonObject);
            strongWeakDescribe = upLimitStrongWeakService.getDetailStrongWeakDescribe(jsonObject);

        }
        stockAnomalousBehaviorDetail.setUpLimitType(strongWeakType);
        stockAnomalousBehaviorDetail.setUpLimitInfo(strongWeakDescribe);
        return stockAnomalousBehaviorDetail;

    }


    public void forceBuildLastUpLimitInfo(StockLastUpLimitDetailDTO dto) {
        List<String> codeList = getCodeList(dto);
        for (String code : codeList) {
            StockLastUpLimitDetailDTO target = new StockLastUpLimitDetailDTO();
            BeanUtils.copyProperties(dto, target);
            target.setStockCode(code);
            stockAnomalousBehaviorStaticMapper.deleteByCode(target.getStockCode());
            stockAnomalousBehaviorDetailMapper.deleteByCode(target.getStockCode());
            buildLastUpLimitInfo(target);
        }

    }

    public void supplementBuildLastUpLimitInfo(StockLastUpLimitDetailDTO dto) {
        StockAnomalousBehaviorStatic stockAnomalousBehaviorStatic = stockAnomalousBehaviorStaticMapper.selectByCode(dto.getStockCode());
        dto.setBeginDateStr(stockAnomalousBehaviorStatic.getBeginDate());
        dto.setEndDateStr(stockAnomalousBehaviorStatic.getEndDate());
        List<String> dateIntervalList = riverRemoteService.getDateIntervalList(stockAnomalousBehaviorStatic.getBeginDate(), stockAnomalousBehaviorStatic.getEndDate());
        //获取时间区间
        List<String> codeList = new ArrayList<>();
        if (StringUtils.isNotBlank(dto.getStockCode())) {
            codeList.add(dto.getStockCode());
        }
        asynAddAnormalousBehaviorDetail(dto.getStockCode(), dateIntervalList);
        //添加
        addOrUpdateStockAnomalousBehaviorStatic(dto.getStockCode(), dateIntervalList.get(0), dateIntervalList.get(dateIntervalList.size() - 1), true);
    }

    public void amAbOne(StockStrategyQueryDTO dto) {

        List<String> templatedIdList = new ArrayList<>();
        templatedIdList.add(StockTemplateEnum.TWO_PLATE_HIGH_EXPECT.getId());
        templatedIdList.add(StockTemplateEnum.TWO_PLATE_QUICK_TO_UP.getId());
        templatedIdList.add(StockTemplateEnum.ONE_UP_LIMIT_ONE_WORD.getId());

        for (String templateId : templatedIdList) {
            StockLastUpLimitDetailDTO stockLastUpLimitDetailDTO = new StockLastUpLimitDetailDTO();
            stockLastUpLimitDetailDTO.setRiverStockTemplateId(templateId);
            stockLastUpLimitDetailDTO.setDateStr(dto.getDateStr());
            forceBuildLastUpLimitInfo(stockLastUpLimitDetailDTO);
            //将code 添加到A板块
        }
        addOptionalPoolInfo(templatedIdList, PlateEnum.AM_OPEN_LOW_LIMIT_ONE.getId(), dto.getDateStr());
    }

    public void addOptionalPoolInfo(List<String> templatedIdList, String plateId, String dateStr) {
        stockOptionalPoolMapper.deleteByPlateId(plateId);
        for (String templateId : templatedIdList) {
            Constant.emotionByDateRangeThreadPool.submit(() -> {
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dateStr);
                stockStrategyQueryDTO.setRiverStockTemplateId(templateId);
                try {
                    StrategyBO strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    if (strategy == null) {
                        return;
                    }
                    JSONArray data = strategy.getData();
                    for (int i = 0; i < data.size(); i++) {
                        JSONObject jsonObject = data.getJSONObject(i);
                        StockOptionalPool stockOptionalPool = new StockOptionalPool();
                        stockOptionalPool.setId(baseServerFeign.getSnowflakeId());
                        stockOptionalPool.setCode(jsonObject.getString("code"));
                        stockOptionalPool.setName(jsonObject.getString("股票简称"));
                        stockOptionalPool.setPlateId(plateId);
                        stockOptionalPoolMapper.insertSelective(stockOptionalPool);
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    public void amAbTwo(StockStrategyQueryDTO dto) {
        List<String> templatedIdList = new ArrayList<>();
        templatedIdList.add(StockTemplateEnum.FIRST_UP_LIMIT_WATCH_TWO.getId());
        for (String templateId : templatedIdList) {
            StockLastUpLimitDetailDTO stockLastUpLimitDetailDTO = new StockLastUpLimitDetailDTO();
            stockLastUpLimitDetailDTO.setRiverStockTemplateId(templateId);
            stockLastUpLimitDetailDTO.setDateStr(dto.getDateStr());
            forceBuildLastUpLimitInfo(stockLastUpLimitDetailDTO);
            //将code 添加到A板块
        }
        addOptionalPoolInfo(templatedIdList, PlateEnum.AM_OPEN_LOW_LIMIT_TWO.getId(), dto.getDateStr());
    }
}
