package com.dlut.library.mapper;

import com.dlut.library.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    User findByUsername(String username);

    List<User> findAll();

    int countAll();

    int countByRole(String role);

    int insert(User user);

    int updateRole(@Param("username") String username, @Param("role") String role);
}
