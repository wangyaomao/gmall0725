package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.manage.util.GmallUploadUtil;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SpuController {

    @Reference
    SpuService spuService;

    @RequestMapping("fileUpload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile){

        String imgUrl = GmallUploadUtil.uploadImage(multipartFile);
        // 保存spu
        return imgUrl;
    }

    @RequestMapping("saveSpu")
    @ResponseBody
    public String saveSpu(SpuInfo spuInfo){
       spuService.saveSpu(spuInfo);

        // 保存spu
        return "success";
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> baseSaleAttrList(){
        // 销售属性列表
        List<BaseSaleAttr> baseSaleAttrs =  spuService.baseSaleAttrList();
        return baseSaleAttrs;
    }



    @RequestMapping("spuListPage")
    public String spuListPage(){
        return "spuListPage";
    }



    @RequestMapping("getSpuList")
    @ResponseBody
    public List<SpuInfo> getSpuList(String catalog3Id){

        // 调用后台查询一级分类的集合
        List<SpuInfo>  spuInfos = spuService.getSpuList(catalog3Id);
        return spuInfos;
    }

}
