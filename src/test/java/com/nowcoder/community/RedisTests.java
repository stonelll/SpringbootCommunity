package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

        //多次访问同一个key
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    public void testHashes() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testLists() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,3));

    }

    //编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(
                new SessionCallback() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        String redisKey = "test:tx";

                        operations.multi(); //启用事务

                        operations.opsForSet().add(redisKey, "zhangsan");
                        operations.opsForSet().add(redisKey, "lisi");
                        operations.opsForSet().add(redisKey, "wangwu");
                        /**
                         * 由于redis对于事务的管理是在事务结束之前都把之前的任务放到一个队列里面, 等事务提交了后才会执行任务,
                         * 所以导致如果在事务还没提交之前就执行对redis中数据的查询的话会造成差错, 就像下面的查询会查询出来空集合一样
                         * 所以我们在使用redis的时候对于尽量把查询操作作用在事务管理前面或者后面
                         */
                        System.out.println(operations.opsForSet().members(redisKey));

                        return operations.exec();//提交事务
                    }
                }
        );
        System.out.println(obj);
    }



}
