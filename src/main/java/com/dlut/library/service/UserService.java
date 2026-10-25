package com.dlut.library.service;

import com.dlut.library.mapper.UserMapper;
import com.dlut.library.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setRole(userMapper.countAll() == 0 ? "LIBRARIAN" : "READER");
        user.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        user.setPassword(null);
        userMapper.insert(user);
        return true;
    }
}
