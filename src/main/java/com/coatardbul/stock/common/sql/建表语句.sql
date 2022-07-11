create table if not exists proxy_ip
(
    id varchar(32) not null
        primary key,
    ip varchar(32) null comment 'ip',
    port varchar(32) null comment '端口',
    country varchar(64) null comment '国家',
    province varchar(74) null comment '省',
    city varchar(64) null comment '城市',
    create_time datetime null,
    use_time int default 0 null comment '最多8次'
);

create table if not exists stock_anomalous_behavior_detail
(
    id varchar(32) not null
        primary key,
    date varchar(32) null,
    code varchar(32) null,
    name varchar(256) null,
    up_limit_type varchar(32) null,
    up_limit_info text null,
    constraint stock_anomalous_behavior_detail_code_date_uindex
        unique (code, date)
)
    comment '股票异常行为详情';

create table if not exists stock_anomalous_behavior_static
(
    id varchar(32) not null
        primary key,
    code varchar(32) null,
    name varchar(256) null comment '股票名称',
    begin_date varchar(32) null comment '统计开始时间',
    end_date varchar(32) null comment '统计结束时间',
    static_detail text null comment '统计详情',
    constraint stock_anomalous_behavior_static_code_uindex
        unique (code)
)
    comment '股票异常行为概览表';

create table if not exists stock_cookie
(
    id varchar(32) not null
        primary key,
    type_key varchar(256) null comment '英文描述key',
    cookie_value text null,
    remark varchar(1024) null comment '描述'
);

create table if not exists stock_day_emotion
(
    id varchar(32) not null comment '主键'
        primary key,
    date varchar(10) null,
    object_static_Array text null comment '对象集合',
    object_sign varchar(64) null comment '根据对象解析对应的数据，保留字段',
    remark varchar(1024) null comment '如果是组合计算，需要标注名称'
)
    comment '股票情绪';

create table if not exists stock_define_static
(
    id varchar(32) not null
        primary key,
    date varchar(32) null comment '日期',
    name varchar(1024) null,
    median varchar(32) null comment '中位数',
    std varchar(32) null comment '标准差',
    object_sign varchar(32) null comment '对象标识'
)
    comment '自定义统计';

create table if not exists stock_filter
(
    id varchar(32) not null
        primary key,
    date varchar(32) null comment '过滤的日期',
    template_sign varchar(256) null comment '问句模板标识',
    stock_code varchar(32) null comment '股票代码',
    `explain` text null comment '智能描述',
    status int null comment '状态：0默认状态 1.标记状态（即重点管住）'
)
    comment '股票过滤';

create table if not exists stock_minuter_emotion
(
    id varchar(32) not null comment '主键'
        primary key,
    date varchar(10) null,
    object_static_Array text null comment '对象集合',
    object_sign varchar(64) null comment '根据对象解析对应的数据，保留字段',
    template_id varchar(32) null comment '模板id',
    name varchar(1024) null comment '如果是组合计算，需要标注名称'
)
    comment '股票情绪';

create table if not exists stock_module_mapper
(
    stock_code varchar(6) not null comment '股票代码',
    module_code varchar(6) not null comment '板块代码',
    primary key (stock_code, module_code)
);

create table if not exists stock_optional_plate
(
    id varchar(32) not null
        primary key,
    name varchar(1024) null comment '板块名称',
    plate_sign varchar(32) null comment '板块标识',
    remark text null
)
    comment '自选板块';

create table if not exists stock_optional_pool
(
    id varchar(32) not null
        primary key,
    code varchar(32) null,
    name varchar(256) null,
    plate_id varchar(32) null comment '板块id'
);

create table if not exists stock_scatter_static
(
    id varchar(32) not null comment '主键'
        primary key,
    date varchar(10) null,
    object_static_Array text null comment '对象集合',
    object_sign varchar(64) null comment '根据对象解析对应的数据，保留字段',
    remark varchar(1024) null comment '对象说明'
)
    comment '股票散点统计';

create table if not exists stock_static_template
(
    id varchar(32) not null
        primary key,
    static_latitude varchar(32) null comment '统计纬度;按照天统计，按照分钟统计',
    remark text null comment '描述：作用和实现方式，统计是否叠加',
    order_by text null comment '排序字段，有的时候需要排序，按照数组惊醒排序',
    object_sign varchar(64) null comment '存储对象，可能是对象，可能是全限定名称',
    object_str text null comment '对象体'
)
    comment '股票统计模板';

create table if not exists stock_strategy_watch
(
    id varchar(32) not null
        primary key,
    templated_id varchar(32) null,
    begin_time varchar(32) null comment '开始时间',
    end_time varchar(32) null comment '结束时间',
    type int null comment '1.已购股票
2.定时任务策略扫描
3.需要发送邮件
4.买入，卖出
5.策略模拟',
    is_open_trade int null comment '是否开启交易',
    watch_level int null comment '1. 必须全程开启
2.=----
3.----',
    strategy_sign varchar(512) null comment '策略标识，固定策略信息',
    buy_condition varchar(1024) null comment '买入条件'
);

create table if not exists stock_template_predict
(
    id varchar(32) not null
        primary key,
    date varchar(10) null comment 'YYYY-MM-DD日期',
    templated_id varchar(32) null comment '模板id',
    hold_day int null comment '持有天数',
    sale_time varchar(32) null comment '卖出时间，年月日时分',
    code varchar(6) null comment '股票代码',
    name varchar(128) null,
    market_value decimal(32) null comment '市值',
    buy_price decimal(10,2) null comment '买入价格',
    buy_increase_rate decimal(10,4) null comment '买入涨幅比例',
    close_increase_rate decimal(10,4) null comment '收盘涨幅',
    detail text null comment '具体详情',
    turnover_rate decimal(10,2) null comment '换手率',
    sale_price decimal(10,2) null comment '卖出价格',
    buy_time varchar(32) null comment '买入时间 年月日时分'
)
    comment '股票模型预测表';

create table if not exists stock_theme_static
(
    id varchar(32) not null
        primary key,
    date varchar(32) not null,
    theme varchar(256) null comment '题材',
    object_static_Array text null comment '对象',
    object_sign varchar(256) null comment '对象标识'
)
    comment '股票题材统计';

create table if not exists stock_trade_buy_config
(
    id varchar(32) not null
        primary key,
    template_id varchar(32) null comment '模板id',
    template_name varchar(1024) null comment '模板名称',
    all_money decimal(12,2) null comment '买入金额',
    sub_money decimal(12,2) null comment '剩余仓位金额',
    all_num int null comment '可以买入的次数',
    sub_num int null comment '剩余次数',
    proportion decimal(12,4) null comment '仓位占比',
    type int null comment '1.直接买入
2.邮件提醒
3.固定策略'
);

create table if not exists stock_trade_buy_job
(
    id varchar(32) not null
        primary key,
    code varchar(32) null comment '股票代码',
    name varchar(256) null comment '名称',
    amount varchar(32) null comment '数据，以100为单位',
    price varchar(32) null comment '买入价格',
    all_money varchar(32) null comment '总金额',
    type int null comment '买入类型：
1.定时买入
2.条件买入
3.邮件提醒',
    sell_date varchar(32) null comment '买入日期',
    sell_time varchar(32) null comment '买入时间',
    sell_script varchar(32) null comment '条件买入出脚本',
    status int null comment '卖出状态：1 未买入 2 已买入'
);

create table if not exists stock_trade_sell_job
(
    id varchar(32) not null
        primary key,
    code varchar(32) null comment '股票代码',
    name varchar(256) null comment '名称',
    amount varchar(32) null comment '数据，以100为单位',
    price varchar(32) null comment '卖出价格',
    type int null comment '卖出类型：
1.定时卖出，需要卖出时间
2.条件卖出
3.邮件提醒',
    sell_date varchar(32) null comment '卖出日期',
    sell_time varchar(32) null comment '卖出时间',
    sell_script varchar(32) null comment '条件卖出脚本',
    status int null comment '卖出状态：1 未卖出 2 已卖出'
);

create table if not exists stock_trade_url
(
    id varchar(32) not null
        primary key,
    url varchar(1024) null comment '路径',
    sign varchar(256) null comment '标识',
    remark varchar(1024) null comment '描述',
    validate_key varchar(256) null
);

create table if not exists stock_trade_user
(
    id varchar(32) not null
        primary key,
    account varchar(32) null comment '账号',
    password varchar(32) null comment '密码',
    expire_time datetime null comment '过期时间',
    cookie varchar(1024) null
);

create table if not exists stock_up_limit_val_price
(
    id varchar(32) not null
        primary key,
    code varchar(32) null comment '股票代码',
    name varchar(64) null comment '股票名称',
    begin_date varchar(32) null comment '开始日期',
    end_date varchar(32) null comment '结束日期',
    object_array text null comment '对象信息',
    strong_weak_array text null comment '每日涨停强弱分析'
)
    comment '股票涨停两家';

create table if not exists stock_uplimit_analyze
(
    id varchar(32) not null
        primary key,
    date varchar(32) null,
    object_sign varchar(32) null,
    json_detail varchar(2048) null,
    code varchar(32) null,
    last_turn_over varchar(32) null comment '昨日换手率',
    last_vol varchar(32) null comment '昨日量比',
    compression_division varchar(32) null comment '昨日承压',
    auction_turn_over varchar(32) null comment '竞价换手率',
    auction_increase varchar(32) null comment '竞价涨幅',
    new_increase varchar(32) null comment '最新涨幅',
    circulation_market_value varchar(32) null comment '流通市值',
    current_price varchar(32) null comment '目前价格',
    auction_vol varchar(32) null comment '竞价量比',
    market_value varchar(32) null comment '市值'
);

create table if not exists stock_warn_log
(
    id varchar(32) not null
        primary key,
    stock_code varchar(32) null comment '股票代码',
    stock_name varchar(1024) null comment '股票名称',
    template_id varchar(32) null comment '模板id',
    template_name varchar(2048) null comment '模板名称',
    date varchar(32) null comment '创建日期',
    create_time datetime null
)
    comment '预警日志表';

create table if not exists stock_wave_static
(
    id varchar(32) not null comment '主键'
        primary key,
    date varchar(10) null,
    object_static_Array text null comment '对象集合',
    object_sign varchar(64) null comment '根据对象解析对应的数据，保留字段',
    stock_code varchar(32) null comment '股票code',
    stock_name varchar(64) null comment '股票名称',
    remark varchar(1024) null comment '对象说明'
)
    comment '股票波动';

