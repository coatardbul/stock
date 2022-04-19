package com.coatardbul.stock.common.constants;

public enum StockTemplateEnum {

    a1("ssssssssss","外侧模式振幅10个点，5亿","1477323946260037632"),
    a2("ssssssssss","连涨3天","1480198675199295488"),
    a3("ssssssssss","连涨4天以上","1480198844502376448"),
    a4("ssssssssss","连涨2天","1480203054681817088"),
    a5("ssssssssss","外侧模式振幅8个点，5亿","1480918944930988032"),
    a6("ssssssssss","下跌个数","1480921345708654592"),
    a7("ssssssssss","上涨个数","1481166585669484544"),
    a8("ssssssssss","昨日涨停（计算方差，标准差）","1481232028362997760"),
    a9("ssssssssss","连涨2天以上集合竞价","1481302460344696832"),
    a10("ssssssssss","昨日涨停,非一字（计算方差，标准差）","1482611456372506624"),
    a11("ssssssssss","创业板，科创板，昨日涨停","1483032575030198272"),
    a12("ssssssssss","连涨2天以上（计算方差，标准差）","1483037371107770368"),
    a13("ssssssssss","查看自选板块竞价数据","1483760841617702912"),
    a14("ssssssssss","主板一字跌停","1491461635838181376"),
    a15("ssssssssss","主板跌停（包括一字跌停）","1491463259323236352"),
    a16("ssssssssss","主板炸板","1491464964349755392"),
    a17("ssssssssss","主板炸板回封","1491465472548405248"),
    a18("ssssssssss","主板涨停","1491466098237898752"),
    a19("ssssssssss","主板炸板未回封","1491467156385300480"),
    a20("ssssssssss","连涨2天以上","1492575555046998016"),
    a21("ssssssssss","上涨个数（只算当日）","1492577515351441408"),
    a22("ssssssssss","下跌个数（只算当日）","1492577632515129344"),
    a23("ssssssssss","分型突破","1499296750098317312"),
    a24("ssssssssss","高换手涨停","1501580937186639872"),
    a25("ssssssssss","昨曾模式","1501584345410961408"),
    a26("ssssssssss","外侧模式，涨停预警","1501588045365903360"),
    a27("ssssssssss","分型涨停（专门查看使用）","1501926185377071104"),
    a28("ssssssssss","隔空剑","1502896170119331840"),
    a29("ssssssssss","破剑式（喇叭口出击）","1502896274603638784"),
    TWO_PLATE_HIGH_EXPECT("TWO_PLATE_HIGH_EXPECT","二板烂板高预期","1505216687434235904"),
    TWO_PLATE_QUICK_TO_UP("TWO_PLATE_QUICK_TO_UP","二板高预期快速冲板","1505357737901555712"),
    ONE_UP_LIMIT_ONE_WORD("ONE_UP_LIMIT_ONE_WORD","首板一字","1505911842550185984"),
    a33("ssssssssss","二板栏板分歧板","1506055241966157824"),
    FIRST_UP_LIMIT_WATCH_TWO("FIRST_UP_LIMIT_WATCH_TWO","首次涨停（查看二板栏板）","1506450265249808384"),
    a35("ssssssssss","5日10日均线上扬","1508081049291325440"),
    STOCK_UP_LIMIT("STOCK_UP_LIMIT","股票涨停信息","1509349533765730304"),
    a37("ssssssssss","明日涨停","1510610979782787072"),
    a38("ssssssssss","连涨2天（计算方差，标准差）","1512591115088429056"),
    a39("ssssssssss","连涨3天以上（计算标准差，方差）","1512592043300487168"),
    STOCK_DETAIL("STOCK_DETAIL","股票详情","1515522893696598016"),
    STOCK_DOWN_LIMIT("STOCK_DOWN_LIMIT","股票跌停信息","1515527745369669632"),
    strategwwy("strategy", "同花顺问财", "12312312");

    private String sign;

    private String desc;

    private String id;

    StockTemplateEnum(String sign, String desc, String id) {
        this.sign = sign;
        this.desc = desc;
        this.id = id;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
