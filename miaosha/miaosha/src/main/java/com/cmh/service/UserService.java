package com.cmh.service;

public interface UserService {
    //想redis中写入用户访问次数
    int saveUserCount(Integer userId);
    //判断单位时间调用次数
    boolean getUserCount(Integer userId);
}
