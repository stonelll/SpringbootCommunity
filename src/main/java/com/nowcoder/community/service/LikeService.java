package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞

    /**
     *
     * @param userId 哪一个用户点的赞
     * @param entityType 给什么类型点的赞,1帖子or2评论
     * @param entityId 给这个类型的哪一个点的赞
     * reasons for annotation: To better manage the two transaction, the code should refactored here.
     *                 (1. for the entity like count 2. for count of like for a user)
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//
//        //判断这个userid点过赞没
//        boolean alreadyLike = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (alreadyLike) {
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        }else{
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);//查询该实体是否被该用户点过赞
                // 因为redis事务是先将所有事务放在队列中，最后再一一提交的，所以查询得放在事务的外面，
                // 要不然的话就会导致查询的结果和事务提交后的结果不一致的问题。

                operations.multi(); // open transaction

                if (isMember) {
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();// end transaction
            }
        });
    }

    //当我们已经赞过了的时候,显示已赞+赞的数量
    //当我们没赞过的时候,显示赞+赞的数量

    //查询某实体的点赞的数量
    public long finEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    //查询某人的获得的点赞状态
    public int findUserLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }


}
