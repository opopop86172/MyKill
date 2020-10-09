package com.cmh.service;

import com.cmh.dao.OrderDAO;
import com.cmh.dao.StockDAO;
import com.cmh.dao.UserDAO;
import com.cmh.entity.Order;
import com.cmh.entity.Stock;
import com.cmh.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;


@Service
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService{
    @Resource
    private OrderDAO orderDAO;
    @Resource
    private StockDAO stockDAO;
    @Resource
    private UserDAO userDAO;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getMd5(Integer id, Integer userid) {

        //检验用户的合法性
        User user = userDAO.findById(userid);
        if(user==null)throw new RuntimeException("用户信息不存在!");
        log.info("用户信息:[{}]",user.toString());

        //检验商品的合法行
        Stock stock = stockDAO.checkStock(id);
        if(stock==null) throw new RuntimeException("商品信息不合法!");
        log.info("商品信息:[{}]",stock.toString());

        //生成hashkey
        String hashKey = "KEY_"+userid+"_"+id;
        //生成md5//这里!QS#是一个盐 随机生成
        String key = DigestUtils.md5DigestAsHex((userid+id+"!Q*jS#").getBytes());
        stringRedisTemplate.opsForValue().set(hashKey, key, 120, TimeUnit.SECONDS);

        log.info("Redis写入：[{}] [{}]", hashKey, key);
        return key;
    }

    @Override
    public int kill(Integer id, Integer userid, String md5) {
        //检验redis中秒杀商品是否超时
        if(!stringRedisTemplate.hasKey("kill"+id))
            throw new RuntimeException("当前商品的抢购活动已经结束了");

        //验证签名
        String hashKey = "KEY_"+userid+"_"+id;
        String s = stringRedisTemplate.opsForValue().get(hashKey);
        if(s==null) throw new RuntimeException("没有携带验证签名，请求不合法！");
        if(!s.equals(md5)) throw new RuntimeException("请求数据不合法，请稍后再试");

        //校验库存
        Stock stock = checkStock(id);
        //更新库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    @Override
    public int kill(Integer id) {
        //redis检验抢购时间
        if(!stringRedisTemplate.hasKey("kill" +id)){
            throw new RuntimeException("秒杀超时，活动已经结束啦!");
        }
        //校验库存
        Stock stock = checkStock(id);
        //扣库存
        updateSale(stock);
        //下订单
        return createOrder(stock);
    }

    //校验库存
    private Stock checkStock(Integer id) {
        Stock stock = stockDAO.checkStock(id);
        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }
    //扣库存
    private void updateSale(Stock stock){
        //在sql层面完成销量加1 和版本号加1 并且根据商品id和版本号同时查询更新的商品
        int updateRows = stockDAO.updateSale(stock);
        if (updateRows==0){
            throw new RuntimeException("请购失败，请重试！！！");
        }
    }
    //创建订单
    private Integer createOrder(Stock stock){

        Order order = new Order();
        order.setSid(stock.getId()).setName(stock.getName()).setCreateDate(new Date());
        orderDAO.createOrder(order);
        return order.getId();
    }
}
