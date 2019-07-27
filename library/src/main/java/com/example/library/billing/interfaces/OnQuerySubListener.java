package com.example.library.billing.interfaces;

import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;

import java.util.List;

/**
 * 执行查询用户订阅状态的回调
 */
public interface OnQuerySubListener {
    /**
     *
     * @param billingResult 执行查询后的错误码、错误信息
     * @param purchases 查询得到所有订阅产品的信息实体，可能为null
     */
    void queryResult(BillingResult billingResult, List<Purchase> purchases);
}
