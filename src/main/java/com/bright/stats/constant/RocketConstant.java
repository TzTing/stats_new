package com.bright.stats.constant;

/**
 * @author: Tz
 * @Date: 2022/10/14 15:17
 */
public class RocketConstant {

    public static final String TOPIC_CONSUMER = "topic_consumer";

    public static final String TOPIC_CHECK = "topic_check";
    public static final String TOPIC_SUMMARY = "topic_summary";
    public static final String TOPIC_REPORT = "topic_report";
    public static final String TOPIC_WITHDRAW = "topic_withdraw";


    public static final String TOPIC_CHECK_NAME = "稽核";
    public static final String TOPIC_SUMMARY_NAME = "汇总";
    public static final String TOPIC_REPORT_NAME = "上报";
    public static final String TOPIC_WITHDRAW_NAME = "退回上报";


    public static final Integer CONSUMER_FLAG_SUCCESS = 1;
    public static final Integer CONSUMER_FLAG_BUSINESS_FAIL = -1;
    public static final Integer CONSUMER_FLAG_SYSTEM_FAIL = -2;
    public static final Integer CONSUMER_FLAG_TODO = 0;


}
