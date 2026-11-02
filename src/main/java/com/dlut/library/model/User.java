package com.dlut.library.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class User {

    private Long id;

    @NotBlank(message = "请输入用户名")
    @Pattern(regexp = "^[A-Za-z0-9_]{4,20}$", message = "用户名需为4至20位字母、数字或下划线")
    private String username;

    @NotBlank(message = "请输入姓名")
    @Size(max = 30, message = "姓名不能超过30个字符")
    private String displayName;

    @NotBlank(message = "请输入密码")
    @Size(min = 8, max = 40, message = "密码长度应为8至40位")
    private String password;

    private String passwordHash;
    private String role;

    @NotBlank(message = "请输入学号或工号")
    @Pattern(regexp = "^[A-Za-z0-9-]{5,20}$", message = "学号或工号格式不正确")
    private String studentNo;

    @NotBlank(message = "请输入院系")
    @Size(max = 50, message = "院系不能超过50个字符")
    private String department;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isLibrarian() {
        return "LIBRARIAN".equals(role);
    }

    public String getRoleLabel() {
        return isLibrarian() ? "馆员" : "读者";
    }
}
