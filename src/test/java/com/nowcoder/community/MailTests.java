package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import jdk.jshell.EvalException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("81452559@qq.com","Test","Welcome to MailSenderHelper");
    }

    @Test
    public void testHTMLMail() {
        Context context = new Context();
        context.setVariable("username", "stone");

        String content = templateEngine.process("mail/demo", context);
        System.out.println(content);

        mailClient.sendMail("81452559@qq.com","HTML", content);
    }

}
