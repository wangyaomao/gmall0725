package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;

    @Reference
    SpuService spuService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap map, HttpServletRequest request){
        String remoteAddr = request.getRemoteAddr();
        // 查询当前sku的详情
        SkuInfo skuInfo =skuService.getSkuById(skuId,remoteAddr);
        map.put("skuInfo",skuInfo);

        // 通过skuId获得spuId
        String spuId = skuInfo.getSpuId();

        // 根据spuId查询销售属性集合
        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrListBySpuId(spuId,skuId);
        map.put("spuSaleAttrListCheckBySku",spuSaleAttrs);

        // 根据spuId制作页面销售属性的hash表
        // 销售属性组合：skuId
        List<SkuInfo> skuInfos = skuService.skuSaleAttrValueListBySpu(spuId);

        HashMap<String, String> stringStringHashMap = new HashMap<>();
        for (SkuInfo info : skuInfos) {
            String skuSaleAttrValueIdsKey = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValueIdsKey = skuSaleAttrValueIdsKey +"|"+skuSaleAttrValue.getSaleAttrValueId();
            }
            String skuIdValue = info.getId();
            stringStringHashMap.put(skuSaleAttrValueIdsKey,skuIdValue);
        }

        String s = JSON.toJSONString(stringStringHashMap);

        map.put("valuesSkuJson",s);
        return "item";
    }

    @RequestMapping("test")
    public String test(ModelMap map){

        // 取值
        map.put("hello" ,"hello thymeleaf");
        // 判断
        map.put("num","1");
        // 循环
        List<String> list = new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            list.add("数据"+i);
        }
        map.put("list",list);
        map.put("name","蜘蛛侠");

        List<UserInfo> userInfos = null;//new ArrayList<>();

/*        for (int i = 0; i <5 ; i++) {
            UserInfo userInfo = new UserInfo();
            userInfo.setName("张"+i);
            userInfos.add(userInfo);
        }*/

        map.put("userInfos",userInfos);

        UserInfo userInfo = null;// new UserInfo();
       // userInfo.setName("隔壁老王");
        map.put("userInfo",userInfo);

        return "test";
    }
}
