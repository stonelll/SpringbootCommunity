package com.athanstyx.community.controller;

import com.athanstyx.community.util.HostHolder;
import com.athanstyx.community.annotation.LoginRequired;
import com.athanstyx.community.entity.User;
import com.athanstyx.community.service.FollowService;
import com.athanstyx.community.service.LikeService;
import com.athanstyx.community.service.UserService;
import com.athanstyx.community.util.CommunityConstant;
import com.athanstyx.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        /**
         * 判断是否上传了图片
         */
        if (headerImage == null) {
            model.addAttribute("error", "您还没有上传图片!");
            return "/site/setting";
        }
        /**
         * 1. 查看文件的后缀
         * 2. 判断文件后缀是否为空
         */
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }
        /**
         * 生成随机文件名
         */
        fileName = CommunityUtil.generaterUUID() + suffix;
        /**
         * 确定文件存放路径
         */
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败, 服务器发生异常!",e);
        }
        /**
         * 更新当前用户头像的路径(提供的是web访问路径方式, 而不是说我们访问电脑的那个路径)
         * http://localhost:8080/community/user/header/xxx.png
         */
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        /**
         * 服务器存放的路径
         */
        fileName = uploadPath + "/" + fileName;
        /**
         * 解析后缀
         */
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        /**
         * 响应图片
         */
        response.setContentType("image/" + suffix);
        try(
                FileInputStream fis = new FileInputStream(fileName);
                ServletOutputStream outputStream = response.getOutputStream();
                ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1){
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败:" + e.getMessage());
        }

    }

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.POST)
    public String updatePassword(Model model, String passwordOld, String passwordNew){
//        if(passwordOld == null){
//            logger.error("error", "请输入密码!");
//            model.addAttribute("error","请输入密码!");
//            return "/site/setting";
//        }

        User user = hostHolder.getUser();
        if(user == null){
//            model.addAttribute("error", "请登录账户!");
            return "/site/login";
        }

        passwordOld = CommunityUtil.MD5(passwordOld + user.getSalt());
        if(!passwordOld.equals(user.getPassword())){
            model.addAttribute("passwordError", "请输入正确的密码");
            return "/site/setting";
        }

        passwordNew = CommunityUtil.MD5(passwordNew + user.getSalt());
        userService.updatePassword(user.getId(), passwordNew);
        hostHolder.clear();
        return "/site/login";

    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(Model model, @PathVariable("userId") int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        //用户
        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followerCount", followerCount);
        // 是否已经关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }




}
