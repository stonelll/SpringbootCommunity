package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated //声明这个组件不推荐使用了
public interface LoginTicketMapper {
    /*
    * 一般来说通过dao层来实现对数据的增删改查有2种方式
    * 第一种就是再写一个mapper.xml来写sql语句进行操作
    * 第二种方法就是使用注解的方式来实现
    * (这里我们通过注解的方式来实现)
    * */

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //在这种注解中写动态的sql使用if的时候需要在前后加上<script>脚本的标签,然后里面就和在xml中写是一样的了
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);

}
