package com.dlut.library.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Loan {

    private Long id;
    private String loanNo;
    private Long bookId;
    private String bookTitle;
    private String bookCoverUrl;
    private String callNumber;

    @NotBlank(message = "请选择借阅类型")
    private String borrowType;

    @NotBlank(message = "请选择取书地点")
    private String pickupLocation;

    @NotBlank(message = "请输入联系电话")
    @Size(max = 40, message = "联系电话不能超过40个字符")
    private String contact;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String note;

    @NotNull(message = "请选择预约取书时间")
    @FutureOrPresent(message = "取书时间不能早于当前时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime scheduledPickupAt;

    @NotNull(message = "请选择应还日期")
    @FutureOrPresent(message = "应还日期不能早于今天")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    private String readerUsername;
    private String readerName;
    private LocalDateTime borrowDate;
    private LocalDateTime returnedAt;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }

    public void setBookCoverUrl(String bookCoverUrl) {
        this.bookCoverUrl = bookCoverUrl;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getBorrowType() {
        return borrowType;
    }

    public void setBorrowType(String borrowType) {
        this.borrowType = borrowType;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getScheduledPickupAt() {
        return scheduledPickupAt;
    }

    public void setScheduledPickupAt(LocalDateTime scheduledPickupAt) {
        this.scheduledPickupAt = scheduledPickupAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getReaderUsername() {
        return readerUsername;
    }

    public void setReaderUsername(String readerUsername) {
        this.readerUsername = readerUsername;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public LocalDateTime getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDateTime borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDateTime getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
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

    public boolean isOverdue() {
        return "借阅中".equals(status)
                && dueDate != null
                && dueDate.isBefore(LocalDate.now());
    }

    public String getDisplayStatus() {
        return isOverdue() ? "已逾期" : status;
    }
}
