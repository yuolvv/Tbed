package cn.hellohao.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.oss.OSSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netease.cloud.auth.BasicCredentials;
import com.netease.cloud.auth.Credentials;
import com.netease.cloud.services.nos.NosClient;
import com.netease.cloud.services.nos.model.Bucket;
import com.netease.cloud.services.nos.model.CannedAccessControlList;
import com.netease.cloud.services.nos.transfer.TransferManager;

import cn.hellohao.dao.ImgMapper;
import cn.hellohao.pojo.Images;
import cn.hellohao.pojo.Keys;
import cn.hellohao.service.ImgService;

import javax.annotation.Resource;

@Service
public class ImgServiceImpl implements ImgService {
    @Autowired
    //@Resource
    private ImgMapper imgMapper;

    @Override
    public List<Images> selectimg(Integer userid) {
        // TODO Auto-generated method stub
        return imgMapper.selectimg(userid);
    }

    @Override
    public Integer deleimg(Integer id) {
        // TODO Auto-generated method stub
        return imgMapper.deleimg(id);
    }


    public Images selectByPrimaryKey(Integer id) {
        return imgMapper.selectByPrimaryKey(id);
    }

    //删除对象存储的图片文件
    public void delect(Keys key, String fileName) {
        // 初始化
        Credentials credentials = new BasicCredentials(key.getAccessKey(), key.getAccessSecret());
        NosClient nosClient = new NosClient(credentials);
        nosClient.setEndpoint(key.getEndpoint());
        // 初始化TransferManager
        TransferManager transferManager = new TransferManager(nosClient);
        //列举桶
        ArrayList bucketList = new ArrayList();
        String tname = "";
        for (Bucket bucket : nosClient.listBuckets()) {
            bucketList.add(bucket.getName());
        }
        for (Object object : bucketList) {
            tname = object.toString();
            //查看桶的ACL
            CannedAccessControlList acl = nosClient.getBucketAcl(object.toString());
            // bucket权限
        }
        //这种方法不能删除指定文件夹下的文件
        boolean isExist = nosClient.doesObjectExist(tname, fileName, null);
        System.out.println("文件是否存在：" + isExist);
        if (isExist) {
            nosClient.deleteObject(tname, fileName);
        }


    }

    //删除OSS对象存储的图片文件
    public void delectOSS(Keys key, String fileName) {
        // 初始化
// Endpoint以杭州为例，其它Region请按实际情况填写。
        String endpoint = key.getEndpoint();
// 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = key.getAccessKey();
        String accessKeySecret = key.getAccessSecret();
        String bucketName = key.getBucketname();
        String objectName = fileName;

// 创建OSSClient实例。
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
// 删除文件。
        ossClient.deleteObject(bucketName, objectName);
// 关闭OSSClient。
        ossClient.shutdown();

    }


    @Override
    public Integer counts(Integer userid) {
        // TODO Auto-generated method stub
        return imgMapper.counts(userid);
    }


    @Override
    public Integer countimg(Integer userid) {
        // TODO Auto-generated method stub
        return imgMapper.countimg(userid);
    }

    @Override
    public Integer setabnormal(String imgname) {
        return imgMapper.setabnormal(imgname);
    }

    @Override
    public Integer deleimgname(String imgname) {
        return imgMapper.deleimgname(imgname);
    }

    @Override
    public Integer deleall(Integer id) {
        return imgMapper.deleall(id);
    }

    @Override
    public List<Images> gettimeimg(String time) {
        return imgMapper.gettimeimg(time);
    }
}
