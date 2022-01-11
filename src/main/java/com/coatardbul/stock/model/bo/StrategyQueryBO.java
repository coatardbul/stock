package com.coatardbul.stock.model.bo;

import lombok.Data;

/**
 * <p>
 * Note:
 *    {
 *         "question": "2022年01月04日开盘量比大于5，2022年01月04日开盘涨幅大于4，非创业板，非st板块，上市时间大于半年，2022年01月04日9点23之前涨停，2021年12月31日涨停，市值小于650亿，2022年01月04日集合竞价金额大于1600万，2022年01月04日集合竞价金额小于9000万，2021年12月31日获利盘大于90%，2022年01月04日集合竞价交易金额排序，2022年01月04日开盘量比排序",
 *             "perpage": 50,
 *             "page": 1,
 *             "secondary_intent": "stock",
 *             "log_info": "{\"input_type\":\"typewrite\"}",
 *             "iwcpro": 1,
 *             "source": "Ths_iwencai_Xuangu",
 *             "version": "2.0",
 *             "query_area": "",
 *             "block_list": "",
 *             "add_info": "{\"urp\":{\"scene\":1,\"company\":1,\"business\":1},\"contentType\":\"json\",\"searchInfo\":true}"
 *     }
 * <p>
 * Date: 2022/1/5
 *
 * @author Su Xiaolei
 */
@Data
public class StrategyQueryBO {

    /**
     * 同花顺问句
     */
    private String question;
    /**
     * 页数
     */
    private Integer perpage;
    /**
     * 页码
     */
    private Integer page;

    private String secondary_intent;

    private String log_info;

    private  Integer iwcpro;

    private String source;

    private String version;

    private String  query_area;

    private String block_list;

    private String add_info;

    /**
     * 排序字段 "分时量比[20220104 09:25]"
     */
    private String sort_key;

    /**
     * 排序类型 desc
     */
    private String sort_order;


}
