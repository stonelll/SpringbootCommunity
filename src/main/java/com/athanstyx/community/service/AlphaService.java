package com.athanstyx.community.service;

import com.athanstyx.community.dao.UserMapper;
import com.athanstyx.community.util.CommunityUtil;
import com.athanstyx.community.dao.DiscussPostMapper;
import com.athanstyx.community.entity.DiscussPost;
import com.athanstyx.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

@Service
public class AlphaService {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public AlphaService(){
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init(){
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy(){
        System.out.println("销毁AlphaService");
    }

    //声明式事务管理:有2种 一种是xml配置aop来实现, 还有一种是通过注解实现, 也就是下面的方式

    /*事务的传播机制是指的是当在执行事务A中调用了事务B, 对于事务的处理*/
    //REQUIRED:支持当前事务(在上面的场景中A就是当前事务),如果不存在就创建新的事务.
    //REQUIRES_NEW:创建一个新的事务, 并且暂停当前事务(事务A)
    //NESTED:如果当前存在事务(事务A), 则嵌套在这个事务中执行(指的是有独立的提交和回滚),如果当前事务不存在就和REQUIRED一样
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        //新增用户
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generaterUUID().substring(0, 5));
        user.setPassword(CommunityUtil.MD5("123" + user.getSalt()));
        user.setEmail("alpha@qq.com");
        user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //新增帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("新人报道!");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";
    }

    //编程式事务管理, 比较麻烦, 但由于声明式管理只能管理一个方法,但如果我们一个方法种有10个步骤, 我只需要对其中的1个步骤用事务管理的话用编程式事务管理就可以做到

    @Autowired
    private TransactionTemplate transactionTemplate;

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {//新增用户
                User user = new User();
                user.setUsername("beta");
                user.setSalt(CommunityUtil.generaterUUID().substring(0, 5));
                user.setPassword(CommunityUtil.MD5("123" + user.getSalt()));
                user.setEmail("beta@qq.com");
                user.setHeaderUrl("http://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                //新增帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("Hello2");
                post.setContent("新人报道2!");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                Integer.valueOf("abc");

                return "ok";
            }
        });

    }

}
