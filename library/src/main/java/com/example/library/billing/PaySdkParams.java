package com.example.library.billing;

import com.example.library.billing.interfaces.OnQuerySubListener;
import com.example.library.billing.interfaces.OnSubListener;

/**
 * 为PaySdk提供所需参数
 */
public class PaySdkParams {

    OnSubListener subListener;
    OnQuerySubListener querySubListener;
    String publicKey;

    public OnSubListener getSubListener() {
        return subListener;
    }

    public OnQuerySubListener getQuerySubListener() {
        return querySubListener;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public PaySdkParams setPublicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public PaySdkParams setQuerySubListener(OnQuerySubListener querySubListener) {
        this.querySubListener = querySubListener;
        return this;
    }

    public PaySdkParams setSubListener(OnSubListener subListener) {
        this.subListener = subListener;
        return this;
    }

    @Override
    public String toString() {
        return "subListener="+subListener+" querySubListener="+querySubListener+" publicKey="+publicKey;
    }
}
