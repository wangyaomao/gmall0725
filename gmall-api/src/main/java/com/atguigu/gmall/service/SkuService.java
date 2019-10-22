package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;

import java.util.List;

public interface SkuService {
    List<SkuInfo> skuInfoListBySpu(String spuId);

    void saveSku(SkuInfo skuInfo);

    SkuInfo getSkuById(String skuId,String remoteAddr);

    List<SkuInfo> skuSaleAttrValueListBySpu(String spuId);

    List<SkuInfo> getMySkuInfo(String catalog3Id);
}
