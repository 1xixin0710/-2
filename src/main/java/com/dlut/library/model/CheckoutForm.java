package com.dlut.library.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

    @NotBlank(message = "请输入收货人姓名")
    @Size(max = 40, message = "收货人姓名不能超过40个字符")
    private String recipient;

    @NotBlank(message = "请输入联系电话")
    @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "联系电话格式不正确")
    private String phone;

    @NotBlank(message = "请选择配送方式")
    private String deliveryType;

    @NotBlank(message = "请输入收货地址")
    @Size(max = 180, message = "收货地址不能超过180个字符")
    private String address;

    @Size(max = 300, message = "订单备注不能超过300个字符")
    private String remark;

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(String deliveryType) {
        this.deliveryType = deliveryType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
