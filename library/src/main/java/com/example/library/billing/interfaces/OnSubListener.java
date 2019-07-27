package com.example.library.billing.interfaces;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import java.util.List;

/**
 * 执行订阅，结果回调接口
 */
public interface OnSubListener {
    /**
     *
     * @param billingResult 订阅结果，包含错误码、错误信息
     * @param purchases 订阅的信息实体，可以获取订单号、token...，可能为null
     */
    void subResult(BillingResult billingResult, List<Purchase> purchases);
}
