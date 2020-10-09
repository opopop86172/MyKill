package com.cmh.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService{
    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public int saveUserCount(Integer userId) {

        //根据不同的用户id生成调用次数的key
        String limitKey = "LIMIT" + "_" + userId;

        //获取redis中指定key的调用次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        int limit = 0;
        if(limitNum == null){
            stringRedisTemplate.opsForValue().set(limitKey, "1", 3600, TimeUnit.SECONDS);
        }else{
            //不是第一次
            limit = Integer.parseInt(limitNum)+1;
            stringRedisTemplate.opsForValue().set(limitKey, String.valueOf(limit), 3600, TimeUnit.SECONDS);
        }
        return limit;
    }

    @Override
    public boolean getUserCount(Integer userId) {
        //根据userid对应key获取调用次数
        String limitKey = "LIMIT"+ "_" + userId;
        //跟库用户调用次数的key获取redis中调用次数
        String limitNum = stringRedisTemplate.opsForValue().get(limitKey);
        if (limitNum == null) {
            //为空直接抛弃说明key出现异常
            log.error("该用户没有访问申请验证值记录，疑似异常");
            return true;
        }
        return Integer.parseInt(limitNum) > 10; //false代表没有超过 true代表超过
    }

}
