package com.dlut.library.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Product {

    private Long id;

    @NotBlank(message = "请输入 ISBN")
    @Pattern(regexp = "^(97[89])?\\d{9}[\\dX]$", message = "ISBN 格式不正确")
    private String isbn;

    @NotBlank(message = "请输入商品名称")
    @Size(max = 120, message = "商品名称不能超过120个字符")
    private String name;

    @NotBlank(message = "请输入作者")
    @Size(max = 100, message = "作者不能超过100个字符")
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

    @NotNull(message = "请输入销售价")
    @DecimalMin(value = "0.01", message = "销售价必须大于0")
    @Digits(integer = 8, fraction = 2, message = "销售价格式不正确")
    private BigDecimal price;

    @NotNull(message = "请输入划线价")
    @DecimalMin(value = "0.01", message = "划线价必须大于0")
    @Digits(integer = 8, fraction = 2, message = "划线价格式不正确")
    private BigDecimal originalPrice;

    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能小于0")
    @Max(value = 99999, message = "库存数量过大")
    private Integer stock;

    private Integer soldCount;

    private String coverUrl;

    @NotBlank(message = "请输入商品介绍")
    @Size(max = 1500, message = "商品介绍不能超过1500个字符")
    private String summary;

    private String tags;

    @NotEmpty(message = "请至少选择一个标签")
    private List<String> tagChoices = new ArrayList<>();

    @NotNull(message = "请选择上架时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime listedAt;

    @NotBlank(message = "请选择商品状态")
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
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

    public LocalDateTime getListedAt() {
        return listedAt;
    }

    public void setListedAt(LocalDateTime listedAt) {
        this.listedAt = listedAt;
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
        return stock != null && stock > 0 && "上架".equals(status);
    }

    public String getStockLabel() {
        return isAvailable() ? "现货" : "暂时缺货";
    }

    public String getDiscountLabel() {
        if (price == null || originalPrice == null
                || originalPrice.compareTo(price) <= 0) {
            return "";
        }
        BigDecimal discount = price.multiply(BigDecimal.TEN)
                .divide(originalPrice, 1, RoundingMode.HALF_UP);
        return discount.stripTrailingZeros().toPlainString() + "折";
    }
}
