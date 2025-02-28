package cn.hellohao.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import cn.hellohao.pojo.*;
import cn.hellohao.service.*;
import cn.hellohao.service.impl.OSSImageupload;
import cn.hellohao.utils.Sentence;
import cn.hellohao.utils.SetText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSONArray;

import cn.hellohao.service.impl.NOSImageupload;

@Controller
public class UpdateImgController {
    @Autowired
    private NOSImageupload nOSImageupload;
    @Autowired
    private UserService userService;
    @Autowired
    private KeysService keysService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private OSSImageupload ossImageupload;
    @Autowired
    private ConfigService configService;


    @RequestMapping({"/", "/index"})
    public String indexImg(Model model, HttpServletRequest request, HttpSession httpSession) throws Exception {
        Config config = configService.getSourceype();//查询当前系统使用的存储源类型。
        Keys key = keysService.selectKeys(config.getSourcekey());//然后根据类型再查询key
        if(key!=null)
        {
            if(key.getStorageType()!=0 || key.getStorageType()!=null){
                if(key.getStorageType()==1){
                    nOSImageupload.Initialize(key);//实例化网易
                    System.out.println("NOS初始化成功。");
                }else if (key.getStorageType()==2){
                    OSSImageupload.Initialize(key);
                    System.out.println("OSS初始化成功。");
                }else if(key.getStorageType()==3){
                    //初始化腾讯云
                }else if(key.getStorageType()==4){
                    //初始化七牛云
                }else{
                    System.err.println("未获取到对象存储参数，初始化失败。");
                }
            }
        }

        User u = (User) httpSession.getAttribute("user");
        String email = (String) httpSession.getAttribute("email");
        if (email != null) {
            //登陆成功
            Integer ret = userService.login(u.getEmail(), u.getPassword());
            if (ret > 0) {
                User user = userService.getUsers(u.getEmail());
                model.addAttribute("username", user.getUsername());
                model.addAttribute("level", user.getLevel());
                model.addAttribute("loginid", 100);
                model.addAttribute("imgcount", 3);
                model.addAttribute("fileSize", 5120);

            } else {
                model.addAttribute("loginid", -1);
                model.addAttribute("imgcount", 1);
            }
        } else {
            model.addAttribute("loginid", -2);
            model.addAttribute("imgcount", 1);
            model.addAttribute("fileSize", 3072);

        }
        model.addAttribute("config", config);
        return "index";

    }

    @RequestMapping(value = "/upimg")
    @ResponseBody
    public String upimg(HttpServletRequest request, HttpServletResponse response, HttpSession session
            , @RequestParam(value = "file", required = false) MultipartFile[] file) throws Exception {
        Config config = configService.getSourceype();//查询当前系统使用的存储源类型。
        Keys key = keysService.selectKeys(config.getSourcekey());
        long stime = System.currentTimeMillis();
        User u = (User) session.getAttribute("user");
        String username = "tourist";
        if (u != null) {
            username = u.getUsername();
        }
        JSONArray jsonArray = new JSONArray();
        Map<String, MultipartFile> map = new HashMap<>();
        for (MultipartFile multipartFile : file) {
            // 获取ImageReader对象的迭代器
            //获取文件名
            String fileName = multipartFile.getOriginalFilename();
            String lastname = fileName.substring(fileName.lastIndexOf(".") + 1);//获取文件后缀
            if (!multipartFile.isEmpty()) { //判断文件是否为空
                map.put(lastname, multipartFile);
                //multipartFile.getOriginalFilename();  //文件名
                //multipartFile.getSize();  //文件大小
            }
        }
        Map<String, Integer> m = null;

        if(key.getStorageType()==1){
            m = nOSImageupload.Imageupload(map, username);
        }else if (key.getStorageType()==2){
            m = ossImageupload.ImageuploadOSS(map, username);
        }else if(key.getStorageType()==3){
            //初始化腾讯云
        }else if(key.getStorageType()==4){
            //初始化七牛云
        }else{
            System.err.println("未获取到对象存储参数，上传失败。");
        }

        Images img = new Images();
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String times = df.format(new Date());
        System.out.println("上传图片的时间是："+times);
        for (Map.Entry<String, Integer> entry : m.entrySet()) {
            jsonArray.add(entry.getKey());
            img.setImgurl(entry.getKey());//图片链接
            img.setUpdatetime(times);
            img.setSource(key.getStorageType());
            if (u == null) {
                img.setUserid(0);//用户id
            } else {
                img.setUserid(u.getId());//用户id
            }
            img.setSizes((entry.getValue()) / 1024);
            img.setImgname(SetText.getSubString(entry.getKey(), key.getRequestAddress() + "/", ""));
            img.setAbnormal(0);
            userService.insertimg(img);
            long etime = System.currentTimeMillis();
            System.out.println("上传图片所用时长：" + String.valueOf(etime - stime) + "ms");
        }
        //开辟新进程鉴黄。现在已经改成定时器
        //查询鉴黄功能是否启动，1为启用
//        Imgreview imgreview = imgreviewService.selectByPrimaryKey(1);
//        if (imgreview.getUsing() == 1) {
//            //上传完成后开辟新的线程进行图片鉴黄。
//            JianHuangThread thread = new JianHuangThread(imgreviewService, key, u, m);
//            thread.start();
//        }

        return jsonArray.toString();
    }

    //刪除用戶
    @RequestMapping("/sentence")
    @ResponseBody
    public String sentence(HttpSession session, Integer id) {
        JSONArray jsonArray = new JSONArray();
        String text = Sentence.getURLContent();
        jsonArray.add(text);
        return jsonArray.toString();
    }


}
