package me.tianxing.util;

import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.Map;
import java.util.Properties;

/**
 * Created by TX on 2016/8/5.
 */
@Service
public class MailSender implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(MailSender.class);

    private JavaMailSenderImpl mailSender;

    @Autowired
    private VelocityEngine velocityEngine;

    public boolean sendWithHTMLTemplate(String to, String subject, String template,
                                        Map<String, Object> model) {
        try {
            String nickname = MimeUtility.encodeText("Grubbyskyer");
            // this email address must be equal to the mailSender.username
            // or it will throws javax.mail.AuthenticationFailedException: 535 Error: authentication failed
            InternetAddress from = new InternetAddress(nickname + "<gsh_grubbyskyer@163.com>");
//            InternetAddress from = new InternetAddress(nickname + "<296065540@qq.com>");
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
            String result = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, template, "UTF-8", model);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(result, true);
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception e) {
            logger.error("发送邮件失败！" + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 163.com, works fine
        mailSender = new JavaMailSenderImpl();
        mailSender.setUsername("gsh_grubbyskyer@163.com");
        // this is the authentication code, not the password
        mailSender.setPassword("tianxing018039");
        mailSender.setHost("smtp.163.com");
        mailSender.setPort(465);
        mailSender.setProtocol("smtps");
        mailSender.setDefaultEncoding("utf8");
        Properties javaMailProperties = new Properties();
        javaMailProperties.put("mail.smtp.ssl.enable", true);
        mailSender.setJavaMailProperties(javaMailProperties);

        // qq.com with SSL, can't connect to the server, something wrong...
//        mailSender = new JavaMailSenderImpl();
//        mailSender.setUsername("296065540@qq.com");
//        mailSender.setPassword("gggggg");
//        mailSender.setHost("smtp.qq.com");
//        mailSender.setPort(465);
//        mailSender.setProtocol("smtps");
//        mailSender.setDefaultEncoding("utf8");
//        Properties javaMailProperties = new Properties();
//        javaMailProperties.put("mail.smtp.ssl.enable", true);
//        mailSender.setJavaMailProperties(javaMailProperties);

    }
}
