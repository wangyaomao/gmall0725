package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    RedisUtil redisUtil;
    @Override
    public CartInfo selectCartExists(CartInfo cartInfo) {
        CartInfo cartInfo1 = new CartInfo();
        cartInfo1.setUserId(cartInfo.getUserId());
        cartInfo1.setSkuId(cartInfo.getSkuId());
        CartInfo cartInfo2 = cartInfoMapper.selectOne(cartInfo1);
        return cartInfo2;
    }

    @Override
    public void addCart(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void updateCart(CartInfo cartInfoDb) {

        Example e = new Example(CartInfo.class);
        e.createCriteria().andEqualTo("userId",cartInfoDb.getUserId()).andEqualTo("skuId",cartInfoDb.getSkuId());

        cartInfoMapper.updateByExampleSelective(cartInfoDb,e);
    }

    @Override
    public void flushCache(String userId) {
        Jedis jedis = redisUtil.getJedis();

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);

        jedis.del("user:"+userId+":cart");

        Map<String,String> map = new HashMap<>();
        for (CartInfo info : cartInfos) {
            map.put(info.getSkuId(),JSON.toJSONString(info));
        }
        jedis.hmset("user:"+userId+":cart",map);

        jedis.close();
    }

    @Override
    public List<CartInfo> getCartListFromCache(String userId) {
        List<CartInfo> cartInfos = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();

        List<String> hvals = jedis.hvals("user:" + userId + ":cart");

        if(null!=hvals&&hvals.size()>0){
            for (String hval : hvals) {
                CartInfo cartInfo = new CartInfo();
                cartInfo = JSON.parseObject(hval,CartInfo.class);
                cartInfos.add(cartInfo);
            }
        }
        jedis.close();

        return cartInfos;
    }

    @Override
    public List<CartInfo> getCartListByUserId(String userId) {
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfos = cartInfoMapper.select(cartInfo);
        return cartInfos;
    }

    @Override
    public void mergCart(List<CartInfo> cartListCookie, String userId) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartListDb = cartInfoMapper.select(cartInfo);

        for (CartInfo cartInfoCookie : cartListCookie) {
            boolean b = if_new_cart(cartListDb, cartInfoCookie);

            if(b){
                // 在db中新增购物车
                cartInfoCookie.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCookie);
            }else{
                // 更新db
                for (CartInfo cartInfoDb : cartListDb) {
                    if(cartInfoDb.getSkuId().equals(cartInfoCookie.getSkuId())){
                        cartInfoDb.setSkuNum(cartInfoDb.getSkuNum()+cartInfoCookie.getSkuNum());
                        cartInfoDb.setCartPrice(cartInfoDb.getSkuPrice().multiply(new BigDecimal(cartInfoDb.getSkuNum())));
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDb);
                    }
                }
            }
        }

        // 刷新缓存
        flushCache(userId);

    }

    private boolean if_new_cart(List<CartInfo> cartInfos, CartInfo cartInfo) {

        boolean b = true;

        for (CartInfo info : cartInfos) {
            if(cartInfo.getSkuId().equals(info.getSkuId())){
                b = false;;
            }
        }
        return b;
    }
}
