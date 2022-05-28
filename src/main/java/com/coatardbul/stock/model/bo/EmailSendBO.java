package com.coatardbul.stock.model.bo;

import lombok.Data;

import java.util.List;


/**
 * @author Su Xiaolei
 */@Data
public class EmailSendBO {

    /**
     * 发送人
     */
    private String sendEmailFrom;

    /**
     * 邮箱服务器，smtp.qq.com
     */
    private String emailProp;
    /**
     * 授权码
     */
    private String  sendFromAuthCode;
    /**
     * 收件人
     */
    private List<String> sendEmailToArray;
    /**
     * 收件人
     */
    private String sendEmailTo;

    /**
     * 抄送人
     */
    private List<String> sendEmailCopy;


    /**
     * 主题
     */
    private String topic;


    /**
     * 正文
     */
    private String textInfo;

}
