package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    //@Param是用于给参数取别名用的
    //如果要将这个参数变为动态参数,也就是在sql中使用<if>的时候, 如果只有一个参数就必须要加别名.
    int selectDiscussPostRows(@Param("userId") int userId);


    //增加帖子的方法
    int insertDiscussPost(DiscussPost discussPost);

    //查询贴子的详情
    DiscussPost selectDiscussPost(int id);

    //增加帖子评论的方法
    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);



}
