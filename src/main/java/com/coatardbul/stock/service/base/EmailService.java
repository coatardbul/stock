package com.coatardbul.stock.service.base;


import com.coatardbul.stock.common.util.JsonUtil;
import com.coatardbul.stock.model.bo.EmailSendBO;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.security.GeneralSecurityException;
import java.util.Properties;

/**
 * @author Su Xiaolei
 */
@Slf4j
@Service
public class EmailService {
    /**
     * 发送的邮箱账号
     */
    @Value("${email.service.sendFrom}")
    private String sendFrom;
    /**
     * 发送的授权码
     */
    @Value("${email.service.sendFromAuthCode}")
    private String sendFromAuthCode;
    /**
     * 邮箱服务器
     */
    @Value("${email.service.emailProp}")
    private String emailProp;


    public void sendProcess(String emailJsonStr) throws Exception {
        if (StringUtils.isNotBlank(emailJsonStr)) {
            sendProcess(JsonUtil.readToValue(emailJsonStr, EmailSendBO.class));
        }
    }

    public EmailSendBO getDefaultConnect() {
        EmailSendBO emailSendDTO = new EmailSendBO();
        emailSendDTO.setSendEmailFrom(sendFrom);
        emailSendDTO.setEmailProp(emailProp);
        emailSendDTO.setSendFromAuthCode(sendFromAuthCode);

        return emailSendDTO;
    }

    public void sendProcess(EmailSendBO emailSendDTO) throws Exception {

        Properties prop = getProperties();

        //使用JavaMail发送邮件的5个步骤

        //创建定义整个应用程序所需的环境信息的 Session 对象

        Session session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //发件人邮件用户名、授权码
                return new PasswordAuthentication(emailSendDTO.getSendEmailFrom(), emailSendDTO.getSendFromAuthCode());
            }
        });

        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);

        //2、通过session得到transport对象
        Transport ts = session.getTransport();

        //3、使用邮箱的用户名和授权码连上邮件服务器
        ts.connect(emailSendDTO.getEmailProp(), emailSendDTO.getSendEmailFrom(), emailSendDTO.getSendFromAuthCode());

        //4、创建邮件

        //创建邮件对象
        MimeMessage message = new MimeMessage(session);
        //指明邮件的发件人
        message.setFrom(new InternetAddress(emailSendDTO.getSendEmailFrom()));
        //指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
        if (emailSendDTO.getSendEmailToArray() != null && emailSendDTO.getSendEmailToArray().size() > 0) {
            Address[] addrs = new InternetAddress[emailSendDTO.getSendEmailToArray().size()];
            for (int i = 0; i < emailSendDTO.getSendEmailToArray().size(); i++) {
                addrs[i] = new InternetAddress(emailSendDTO.getSendEmailToArray().get(i),
                        MimeUtility.encodeText("北京希为科技-验证码", MimeUtility.mimeCharset("gb2312"), null));
            }
            message.setRecipients(Message.RecipientType.TO, addrs);
        }
        if (StringUtils.isNotBlank(emailSendDTO.getSendEmailTo())) {
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(emailSendDTO.getSendEmailTo()));
        }

        //邮件的标题
        message.setSubject(MimeUtility.encodeText(emailSendDTO.getTopic(), MimeUtility.mimeCharset("gb2312"), null));
        //邮件的文本内容
        message.setContent(emailSendDTO.getTextInfo(), "text/html;charset=gbk");

        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());

        ts.close();
    }

    /**
     * @param sendToEmail 发送到的邮箱账号
     * @param topic       文件标题
     * @param textStr
     * @throws Exception
     */
    public void sendProcess(String sendToEmail, String topic, String textStr) throws Exception {
        Properties prop = getProperties();

        //使用JavaMail发送邮件的5个步骤

        //创建定义整个应用程序所需的环境信息的 Session 对象

        Session session = Session.getDefaultInstance(prop, new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                //发件人邮件用户名、授权码
                return new PasswordAuthentication(sendFrom, sendFromAuthCode);
            }
        });


        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);

        //2、通过session得到transport对象
        Transport ts = session.getTransport();

        //3、使用邮箱的用户名和授权码连上邮件服务器
        ts.connect("smtp.qq.com", sendFrom, sendFromAuthCode);

        //4、创建邮件

        //创建邮件对象
        MimeMessage message = new MimeMessage(session);

        //指明邮件的发件人
        message.setFrom(new InternetAddress(sendFrom));

        //指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(sendToEmail));

        //邮件的标题
        message.setSubject(topic, "utf-8");

        //邮件的文本内容
        message.setContent(textStr, "text/html;charset=UTF-8");

        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());

        ts.close();
    }

    private Properties getProperties() throws GeneralSecurityException {
        Properties prop = new Properties();
        //设置QQ邮件服务器
        prop.setProperty("mail.host", emailProp);
        // 邮件发送协议
        prop.setProperty("mail.transport.protocol", "smtp");
        // 需要验证用户名密码
        prop.setProperty("mail.smtp.auth", "true");

        // 关于QQ邮箱，还要设置SSL加密，加上以下代码即可
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        prop.put("mail.smtp.ssl.enable", "true");
        prop.put("mail.smtp.ssl.socketFactory", sf);
        return prop;
    }


}
