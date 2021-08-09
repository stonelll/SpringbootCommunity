package com.athanstyx.community;

import com.athanstyx.community.dao.LoginTicketMapper;
import com.athanstyx.community.dao.MessageMapper;
import com.athanstyx.community.entity.LoginTicket;
import com.athanstyx.community.entity.Message;
import com.athanstyx.community.dao.DiscussPostMapper;
import com.athanstyx.community.dao.UserMapper;


import com.athanstyx.community.entity.DiscussPost;
import com.athanstyx.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        User liubei = userMapper.selectByName("liubei");
        System.out.println(liubei);

        User user1 = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user1);

    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());

    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.newcoder.com/102.png");
        System.out.println(rows);
    }

    @Autowired
    DiscussPostMapper discussPostMapper;
    @Test
    public void testSelectPosts(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(149, 0, 10, 0);
        for (DiscussPost e :discussPosts){
            System.out.println(e);
        }

        int i = discussPostMapper.selectDiscussPostRows(149);
        System.out.println(i);
    }

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Test
    public void testInsertLoginTicketMapper() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(loginTicket);

    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc", 1);

        loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectMessage() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        List<Message> letters = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : letters) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));

        System.out.println(messageMapper.selectLetterUnreadCount(131,"111_131"));


    }




}
