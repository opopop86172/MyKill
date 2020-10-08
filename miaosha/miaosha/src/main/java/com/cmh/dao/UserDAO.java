package com.cmh.dao;

import com.cmh.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDAO {
    User findById(Integer id);
}
