package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.constants.Constant;
import com.coatardbul.stock.common.constants.PlateEnum;
import com.coatardbul.stock.common.constants.StockTemplateEnum;
import com.coatardbul.stock.common.exception.BusinessException;
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
    @Autowired
    StockParseAndConvertService stockParseAndConvertService;

    /**
     * 两板及两板以上数据，最高到8板
     *
     * @param dto
     * @return
     */
    public List<StockUpLimitNameBO> getTwoAboveUpLimitInfo(StockEmotionDayDTO dto) {

        List<StockUpLimitNameBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);
        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.abThreadPool.execute(() -> {
                //涨停脚本语句
                String upLimitNumScript = getUpLimitNumScript(num);
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                stockStrategyQueryDTO.setStockTemplateScript(upLimitNumScript);
                StrategyBO strategy = null;
                try {
                    //策略查询
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    JSONArray data = strategy.getData();
                    List<String> nameList = new ArrayList<>();
                    for (int j = 0; j < data.size(); j++) {
                        nameList.add(stockParseAndConvertService.getStockName(data.getJSONObject(j)));
                    }
                    StockUpLimitNameBO stockUpLimitNameBO = new StockUpLimitNameBO();
                    stockUpLimitNameBO.setUpLimitNum(num + "板");
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

    public Object getUpLimitSign(StockEmotionDayDTO dto) {
        dto.setDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(),-1));
        List<StockUpLimitNameBO> result = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(7);
        for (int i = 2; i < 9; i++) {
            final int num = i;
            Constant.abThreadPool.execute(() -> {
                //涨停脚本语句
                String upLimitNumScript = getUpLimitNumScript(num);
                StockStrategyQueryDTO stockStrategyQueryDTO = new StockStrategyQueryDTO();
                stockStrategyQueryDTO.setDateStr(dto.getDateStr());
                stockStrategyQueryDTO.setStockTemplateScript(upLimitNumScript);
                StrategyBO strategy = null;
                try {
                    //策略查询
                    strategy = stockStrategyService.strategy(stockStrategyQueryDTO);
                    JSONArray data = strategy.getData();
                    List<String> codeList = new ArrayList<>();
                    for (int j = 0; j < data.size(); j++) {
                        codeList.add(stockParseAndConvertService.getStockCode(data.getJSONObject(j)));
                    }
                    StockUpLimitNameBO stockUpLimitNameBO = new StockUpLimitNameBO();
                    stockUpLimitNameBO.setUpLimitNum((num+1) + "板");
                    stockUpLimitNameBO.setCodeList(codeList);
                    if (stockUpLimitNameBO.getCodeList().size() > 0) {
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


    /**
     * 涨停题材
     *
     * @param dto
     * @return
     * @throws NoSuchMethodException
     * @throws ScriptException
     * @throws FileNotFoundException
     */
    public List<StockUpLimitInfoBO> getUpLimitTheme(StockStrategyQueryDTO dto) throws NoSuchMethodException, ScriptException, FileNotFoundException {
        StrategyBO strategy = stockStrategyService.strategy(dto);
        if (strategy.getTotalNum() > 0) {
            JSONArray data = strategy.getData();
            return rebuildThemeInfo(data);
        }
        return null;
    }

    /**
     * 题材相关
     *
     * @param data
     * @return
     */
    public List<StockUpLimitInfoBO> rebuildThemeInfo(JSONArray data) {
        //key 为题材名称 value为股票名称
        Map<String, List<String>> themeMap = new HashMap<>();

        //key 为股票名称 value为股票名称
        Map<String, LimitBaseInfoBO> nameMap = new HashMap<>();
        for (int i = 0; i < data.size(); i++) {
            JSONObject jo = data.getJSONObject(i);
            //取里面的数组信息
            Set<String> keys = jo.keySet();
            String stockName = stockParseAndConvertService.getStockName(jo);
            LimitStrongWeakBO upLimitStrongWeak = upLimitStrongWeakService.getLimitStrongWeak(jo, "涨停明细数据");
            LimitBaseInfoBO upLimitBaseInfoBO = new LimitBaseInfoBO();
            BeanUtils.copyProperties(upLimitStrongWeak, upLimitBaseInfoBO);
            upLimitBaseInfoBO.setName(stockName);
            upLimitBaseInfoBO.setCode(jo.getString("code"));

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
        List<String> codeList = getCodeListByStrategy(dto);
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
        List<String> codeList = getCodeListByStrategy(dto);
        //时间区间
        if (!StringUtils.isNotBlank(dto.getBeginDateStr()) || !StringUtils.isNotBlank(dto.getEndDateStr())) {
            dto.setEndDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(), -1));
            dto.setBeginDateStr(riverRemoteService.getSpecialDay(dto.getDateStr(), -30));
        }
        for (String code : codeList) {
            //查询表中有的数据
            StockAnomalousBehaviorStatic stockAnomalousBehaviorStatic = stockAnomalousBehaviorStaticMapper.selectByCode(code);
            //新增
            if (stockAnomalousBehaviorStatic == null) {
                List<String> dateIntervalList = riverRemoteService.getDateIntervalList(dto.getBeginDateStr(), dto.getEndDateStr());
                asynAddAnormalousBehaviorDetail(code, dateIntervalList);
                //添加
                addOrUpdateStockAnomalousBehaviorStatic(code, dateIntervalList.get(0), dateIntervalList.get(dateIntervalList.size() - 1), true);
            } else {
                //更新，有日期
                try {
                    StockLastUpLimitDetailDTO addDateStrInfo = getAddDateInfo(dto, stockAnomalousBehaviorStatic);
                    List<String> dateIntervalList = riverRemoteService.getDateIntervalList(addDateStrInfo.getBeginDateStr(), addDateStrInfo.getEndDateStr());
                    asynAddAnormalousBehaviorDetail(code, dateIntervalList);
                    //更新
                    if(dto.getEndDateStr().compareTo(stockAnomalousBehaviorStatic.getBeginDate())<=0){
                        addOrUpdateStockAnomalousBehaviorStatic(code, addDateStrInfo.getBeginDateStr(), stockAnomalousBehaviorStatic.getEndDate(), false);
                    }else {
                        addOrUpdateStockAnomalousBehaviorStatic(code, stockAnomalousBehaviorStatic.getBeginDate(), addDateStrInfo.getEndDateStr(), false);
                    }
                }catch (Exception e){
                    log.error(e.getMessage(),e);
                }
            }
        }
    }


    /**
     * 获取过滤后的数据，传入的开始结束时间必须得与表中开始结束时间粘合
     *
     * @return
     */
    private StockLastUpLimitDetailDTO getAddDateInfo(StockLastUpLimitDetailDTO dto, StockAnomalousBehaviorStatic stockAnomalousBehaviorStatic) {
        StockLastUpLimitDetailDTO result = new StockLastUpLimitDetailDTO();
        if (dto.getEndDateStr().compareTo(stockAnomalousBehaviorStatic.getEndDate()) > 0) {
            result.setBeginDateStr(stockAnomalousBehaviorStatic.getEndDate());
            result.setEndDateStr(dto.getEndDateStr());
        } else if (dto.getEndDateStr().compareTo(stockAnomalousBehaviorStatic.getEndDate()) == 0) {
            if (dto.getBeginDateStr().compareTo(stockAnomalousBehaviorStatic.getBeginDate()) >= 0) {
                throw new BusinessException("传入的日期区间在表中已经存在");
            } else {
                result.setBeginDateStr(dto.getBeginDateStr());
                result.setEndDateStr(stockAnomalousBehaviorStatic.getBeginDate());
            }
        } else {
            result.setBeginDateStr(dto.getBeginDateStr());
            result.setEndDateStr(stockAnomalousBehaviorStatic.getBeginDate());
        }
        return result;

    }


    private List<String> getCodeListByStrategy(StockLastUpLimitDetailDTO dto) {
        List<String> codeList = new ArrayList<>();
        //策略查询
        if (StringUtils.isNotBlank(dto.getRiverStockTemplateId()) && StringUtils.isNotBlank(dto.getDateStr())) {
            codeList.addAll(getCodeListByStrategy(dto.getDateStr(), dto.getRiverStockTemplateId()));
        }
        //单只股票
        if (StringUtils.isNotBlank(dto.getStockCode())) {
            codeList.add(dto.getStockCode());
        }
        //自选板块
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


    /**
     * 异步添加 详情的移动信息
     * @param code
     * @param dateStrList
     */
    private void asynAddAnormalousBehaviorDetail(String code, List<String> dateStrList) {
        for (String dateStr : dateStrList) {
            Constant.abThreadPool.execute(() -> {
                StockAnomalousBehaviorDetail stockAnomalousBehaviorDetail = stockAnomalousBehaviorDetailMapper.selectAllByCodeAndDate(code, dateStr);
                if (stockAnomalousBehaviorDetail == null) {
                    addStockAnomalousBehaviorDescribe(code, dateStr);
                }
            });
        }
    }


    /**
     * 根据策略code数据
     * @param dateStr
     * @param riverStockTemplateId
     * @return
     */
    private List<String> getCodeListByStrategy(String dateStr, String riverStockTemplateId) {
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
        String stockName = stockParseAndConvertService.getStockName(jsonObject);
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


    /**
     * 强制更新
     * @param dto
     */
    public void forceBuildLastUpLimitInfo(StockLastUpLimitDetailDTO dto) {
        List<String> codeList = getCodeListByStrategy(dto);
        for (String code : codeList) {
            StockLastUpLimitDetailDTO target = new StockLastUpLimitDetailDTO();
            BeanUtils.copyProperties(dto, target);
            target.setStockCode(code);
            stockAnomalousBehaviorStaticMapper.deleteByCode(target.getStockCode());
            stockAnomalousBehaviorDetailMapper.deleteByCode(target.getStockCode());
            buildLastUpLimitInfo(target);
        }

    }


    /**
     * 收盘预览中的最上面的数据
     * @param dto
     */
    public void amAbOne(StockStrategyQueryDTO dto) {
        List<String> templatedIdList = new ArrayList<>();
        templatedIdList.add(StockTemplateEnum.TWO_PLATE_HIGH_EXPECT.getId());
        templatedIdList.add(StockTemplateEnum.TWO_PLATE_QUICK_TO_UP.getId());
        templatedIdList.add(StockTemplateEnum.ONE_UP_LIMIT_ONE_WORD.getId());

        for (String templateId : templatedIdList) {
            StockLastUpLimitDetailDTO stockLastUpLimitDetailDTO = new StockLastUpLimitDetailDTO();
            stockLastUpLimitDetailDTO.setRiverStockTemplateId(templateId);
            stockLastUpLimitDetailDTO.setDateStr(dto.getDateStr());
            //强制更新到表中
            forceBuildLastUpLimitInfo(stockLastUpLimitDetailDTO);
        }
        //将code 添加到A板块
        addOptionalPoolInfo(templatedIdList, PlateEnum.AM_OPEN_LOW_LIMIT_ONE.getId(), dto.getDateStr());
    }

    /**
     * 添加自选股票信息
     * @param templatedIdList
     * @param plateId
     * @param dateStr
     */
    public void addOptionalPoolInfo(List<String> templatedIdList, String plateId, String dateStr) {
        stockOptionalPoolMapper.deleteByPlateId(plateId);
        for (String templateId : templatedIdList) {
            Constant.abThreadPool.execute(() -> {
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
                        stockOptionalPool.setCode(stockParseAndConvertService.getStockCode(jsonObject));
                        stockOptionalPool.setName(stockParseAndConvertService.getStockName(jsonObject));
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
            //强制更新到表中
            forceBuildLastUpLimitInfo(stockLastUpLimitDetailDTO);
        }
        //将code 添加到A板块
        addOptionalPoolInfo(templatedIdList, PlateEnum.AM_OPEN_LOW_LIMIT_TWO.getId(), dto.getDateStr());
    }

    public void deleteAnomalousBehaviorData(StockLastUpLimitDetailDTO dto) {
        stockAnomalousBehaviorStaticMapper.deleteByCode(dto.getStockCode());
        stockAnomalousBehaviorDetailMapper.deleteByCode(dto.getStockCode());
    }


    public  List<StockAnomalousBehaviorStatic> getAbStatic(StockLastUpLimitDetailDTO dto) {
        //根据板块查询
        if(dto.getPlateList()!=null &&dto.getPlateList().size()>0){
            return stockAnomalousBehaviorStaticMapper.selectAllByCodeIn(dto.getPlateList());
        }
       return stockAnomalousBehaviorStaticMapper.selectAll();
    }


}
