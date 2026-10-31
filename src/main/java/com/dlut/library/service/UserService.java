package com.dlut.library.service;

import com.dlut.library.mapper.UserMapper;
import com.dlut.library.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String username, String password) {
        String normalized = username == null ? "" : username.trim();
        User user = userMapper.findByUsername(normalized);
        if (user == null || !passwordEncoder.matches(password == null ? "" : password,
                user.getPasswordHash())) {
            return null;
        }
        user.setPassword(null);
        user.setPasswordHash(null);
        return user;
    }

    @Transactional
    public synchronized boolean register(User user) {
        String username = user.getUsername().trim();
        if (userMapper.findByUsername(username) != null) {
            return false;
        }
        user.setUsername(username);
        user.setDisplayName(user.getDisplayName().trim());
        user.setStudentNo(user.getStudentNo().trim());
        user.setDepartment(user.getDepartment().trim());
        user.setRole(userMapper.countAll() == 0 ? "ADMIN" : "CUSTOMER");
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setPassword(null);
        userMapper.insert(user);
        return true;
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Transactional
    public void changeRole(User actor, String username, String role) {
        if (!"ADMIN".equals(role) && !"CUSTOMER".equals(role)) {
            throw new IllegalArgumentException("角色参数不正确");
        }
        if (actor.getUsername().equals(username)) {
            throw new IllegalArgumentException("不能修改自己的管理员角色");
        }
        User target = userMapper.findByUsername(username);
        if (target == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (target.isAdmin() && "CUSTOMER".equals(role) && countAdmins() <= 1) {
            throw new IllegalArgumentException("系统至少需要保留一名管理员");
        }
        userMapper.updateRole(username, role);
    }

    public int countAdmins() {
        return userMapper.countByRole("ADMIN");
    }
}
