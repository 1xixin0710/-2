package com.dlut.library.mapper;

import com.dlut.library.model.User;

public interface UserMapper {

    User findByUsername(String username);

    int countAll();

    int insert(User user);
}
