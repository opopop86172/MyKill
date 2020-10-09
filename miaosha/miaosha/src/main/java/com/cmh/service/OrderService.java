package com.cmh.service;


public interface OrderService {
    int kill(Integer id); //用来处理秒杀的下单方法并返回订单id

    String getMd5(Integer id,Integer userid);//用来MD5签名的方法

    int kill(Integer id,Integer userid, String md5);//用来处理秒杀的下单方法并返回订单id 加入md5接口隐藏的方式

    //Integer createOrder(Integer id);


}
