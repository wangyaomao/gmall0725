package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.manage.util.GmallUploadUtil;
import com.atguigu.gmall.service.BaseAttrService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class SkuController {

    @Reference
    SkuService skuService;

    @Reference
    BaseAttrService baseAttrService;

    @Reference
    SpuService spuService;

    @RequestMapping("spuImageList")
    @ResponseBody
    public List<SpuImage> spuImageList(String spuId){
        List<SpuImage> spuImages = spuService.spuImageList(spuId);

        return spuImages;
    }
    @RequestMapping("saveSku")
    @ResponseBody
    public String saveSku(SkuInfo skuInfo){

        // 保存sku
        skuService.saveSku(skuInfo);

        return "success";
    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> spuSaleAttrList(String spuId){

        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);
        return spuSaleAttrs;
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){

        // 根据spuId获得sku列表
        List<BaseAttrInfo> baseAttrInfos = baseAttrService.attrInfoList(catalog3Id);
        return baseAttrInfos;
    }

    @RequestMapping("skuInfoListBySpu")
    @ResponseBody
    public List<SkuInfo> skuInfoListBySpu(String spuId){

        // 根据spuId获得sku列表
        List<SkuInfo> skuInfos = skuService.skuInfoListBySpu(spuId);
        return skuInfos;
    }

}
