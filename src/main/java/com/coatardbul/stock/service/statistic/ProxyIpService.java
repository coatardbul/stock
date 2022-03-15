package com.coatardbul.stock.service.statistic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.coatardbul.stock.common.util.DateTimeUtil;
import com.coatardbul.stock.feign.river.BaseServerFeign;
import com.coatardbul.stock.mapper.ProxyIpMapper;
import com.coatardbul.stock.model.dto.ProxyIpQueryDTO;
import com.coatardbul.stock.model.entity.ProxyIp;
import com.coatardbul.stock.service.base.HttpService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * Note:
 * <p>
 * Date: 2022/3/14
 *
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class ProxyIpService {
    @Autowired
    BaseServerFeign baseServerFeign;
    @Autowired
    ProxyIpMapper proxyIpMapper;

    @Autowired
    HttpService httpService;
    public void addIpProcess(ProxyIpQueryDTO dto) {
        StringBuilder url = new StringBuilder("https://proxyapi.horocn.com/api/v2/proxies?");
        url.append("order_id=").append(dto.getOrderId());
        url.append("&num=").append(dto.getNum());
        url.append("&format=").append(dto.getFormat());
        url.append("&line_separator=").append(dto.getLineSeparator());
        url.append("&can_repeat=").append(dto.getCanRepeat());
        url.append("&user_token=").append(dto.getUserToken());
        String response = httpService.doGet(url.toString());
        Integer code = (Integer) JSONObject.parseObject(response).get("code");
        if (0 == code) {
            JSONArray data = JSONObject.parseObject(response).getJSONArray("data");
            for (int i = 0; i < data.size(); i++) {
                JSONObject jsonObject = data.getJSONObject(i);
                ProxyIp proxyIp = new ProxyIp();
                proxyIp.setId(baseServerFeign.getSnowflakeId());
                proxyIp.setIp((String) jsonObject.get("host"));
                proxyIp.setPort((String) jsonObject.get("port"));
                proxyIp.setCountry((String) jsonObject.get("country_cn"));
                proxyIp.setProvince((String) jsonObject.get("province_cn"));
                proxyIp.setCity((String) jsonObject.get("city_cn"));
                proxyIp.setCreateTime(new Date());
                proxyIpMapper.insertSelective(proxyIp);
            }
        }
    }


    /**
     * 获取最新的代理ip，端口
     * @return
     */
    public HttpHost getNewProxyHttpHost(){
        Date beforeDate = DateTimeUtil.getBeforeDate(1, Calendar.MINUTE);
        List<ProxyIp> proxyIps = proxyIpMapper.selectAllByCreateTimeGreaterThanEqualAndUseTimeLessThanEqual(beforeDate, 8);
        if(proxyIps!=null &&proxyIps.size()>0){
            ProxyIp proxyIp = proxyIps.get(0);
            proxyIp.setUseTime(proxyIp.getUseTime()+1);
            proxyIpMapper.updateByPrimaryKey(proxyIp);
            return  new HttpHost(proxyIp.getIp(),Integer.valueOf(proxyIp.getPort()));
        }else {
            return null;
        }
    }
}
