package com.dlut.library.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Book {

    private Long id;

    @NotBlank(message = "请输入ISBN")
    @Pattern(regexp = "^(97[89])?\\d{9}[\\dX]$", message = "ISBN格式不正确")
    private String isbn;

    @NotBlank(message = "请输入书名")
    @Size(max = 100, message = "书名不能超过100个字符")
    private String title;

    @NotBlank(message = "请输入作者")
    @Size(max = 80, message = "作者不能超过80个字符")
    private String author;

    @NotBlank(message = "请输入出版社")
    private String publisher;

    @NotNull(message = "请输入出版年份")
    @Min(value = 1900, message = "出版年份不能早于1900年")
    @Max(value = 2026, message = "出版年份不能晚于2026年")
    private Integer publishYear;

    @NotBlank(message = "请选择分类")
    private String category;

    @NotBlank(message = "请选择语种")
    private String language;

    @NotBlank(message = "请输入索书号")
    private String callNumber;

    @NotBlank(message = "请选择馆藏地")
    private String location;

    @NotNull(message = "请输入馆藏册数")
    @Min(value = 1, message = "馆藏册数至少为1")
    @Max(value = 999, message = "馆藏册数不能超过999")
    private Integer totalCopies;

    private Integer availableCopies;

    private String coverUrl;

    @NotBlank(message = "请输入内容简介")
    @Size(max = 1200, message = "内容简介不能超过1200个字符")
    private String summary;

    private String tags;

    @NotEmpty(message = "请至少选择一个标签")
    private List<String> tagChoices = new ArrayList<>();

    @NotNull(message = "请选择入藏时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime acquisitionDate;

    @NotBlank(message = "请选择馆藏状态")
    private String status;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(Integer publishYear) {
        this.publishYear = publishYear;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }

    public Integer getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public List<String> getTagChoices() {
        return tagChoices;
    }

    public void setTagChoices(List<String> tagChoices) {
        this.tagChoices = tagChoices;
    }

    public LocalDateTime getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(LocalDateTime acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isAvailable() {
        return availableCopies != null && availableCopies > 0 && "在馆".equals(status);
    }

    public String getAvailabilityLabel() {
        return isAvailable() ? "可借" : "已借完";
    }
}
