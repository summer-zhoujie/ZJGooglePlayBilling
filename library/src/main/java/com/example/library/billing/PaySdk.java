package com.example.library.billing;

import android.app.Activity;
import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.example.library.billing.interfaces.OnQuerySubListener;
import com.example.library.billing.interfaces.OnSubListener;
import com.example.library.billing.utils.Security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PaySdk implements PurchasesUpdatedListener {


    //** single instace

    public static PaySdk getInstance() {
        return PaySdk.InstanceHolder.PaySdk;
    }


    private static class InstanceHolder {
        private static PaySdk PaySdk = new PaySdk();
    }

    private PaySdk() {
    }


    //** PurchasesUpdatedListener

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        List<Purchase> resultPurchases = purchases;
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            PaySdkLog.d("beforeHandle=" + purchases2String(purchases));
            resultPurchases = handlePurchase(purchases);
            PaySdkLog.d("afterHandle=" + purchases2String(resultPurchases));
        }

        if (params.getSubListener() != null) {
            params.getSubListener().subResult(billingResult, resultPurchases);
        }
    }


    //** variables

    private PaySdkParams params = new PaySdkParams();
    private BillingClient client;


    //** funcs-public

    /**
     * @param publicKey the base64-encoded public key to use for verifying.
     */
    public PaySdk setPublicKey(String publicKey) {
        params.setPublicKey(publicKey);
        return this;
    }

    /**
     * 设置查询订阅的监听
     */
    public PaySdk setQuerySubListener(OnQuerySubListener querySubListener) {
        params.setQuerySubListener(querySubListener);
        return this;
    }

    /**
     * 设置订阅的监听
     */
    public PaySdk setSubListener(OnSubListener subListener) {
        params.setSubListener(subListener);
        return this;
    }

    /**
     * 初始化PaySdk（可重复调用）
     */
    public void init(Application application) {
        PaySdkLog.d("init, params = " + params.toString());
        if (client == null) {
            client = BillingClient.newBuilder(application)
                    .enablePendingPurchases()
                    .setListener(this)
                    .build();
        }
    }

    /**
     * release 资源(可重复调用)
     */
    public void release() {
        PaySdkLog.d("release");
        if (client != null && client.isReady()) {
            client.endConnection();
        }
    }

    /**
     * 执行订阅流程
     * @param skuId Google Console 配置的订阅产品Id
     */
    public void doSubFlow(Activity activity, String skuId) {
        if (!paramsCheck()) {
            notifySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, "params error"), null);
            return;
        }

        startConnectAsyncIfNot(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    if (client.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).getResponseCode() == BillingClient.BillingResponseCode.OK) {
                        ArrayList<String> skuList = new ArrayList<>();
                        skuList.add(skuId);
                        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
                        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS);
                        client.querySkuDetailsAsync(params.build(), (billingResultQuery, skuDetailsList) -> {
                            if (billingResultQuery.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                BillingFlowParams purchaseParams = BillingFlowParams.newBuilder()
                                        .setSkuDetails(skuDetailsList.get(0)).build();
                                client.launchBillingFlow(activity, purchaseParams);
                            } else {
                                notifySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, billingResult2String(billingResultQuery)), null);
                            }
                        });
                    } else {
                        notifySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, "SkuType.SUBS feature is not supported"), null);
                    }
                } else {
                    notifySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, billingResult2String(billingResult)), null);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                notifySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, "onBillingServiceDisconnected"), null);
            }
        });
    }

    /**
     * 执行查询订阅流程
     */
    public void querySub(){
        if (!paramsCheck()) {
            notifyQuerySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, "params error"), null);
            return;
        }
        startConnectAsyncIfNot(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    Purchase.PurchasesResult queryResult = client.queryPurchases(BillingClient.SkuType.SUBS);

                    BillingResult build = BillingResult.newBuilder()
                            .setResponseCode(queryResult.getResponseCode())
                            .setDebugMessage(queryResult.getBillingResult().getDebugMessage())
                            .build();
                    PaySdkLog.d( "purchasesResult="
                            + queryResult.getResponseCode()
                            + queryResult.getBillingResult().getDebugMessage()
                            + " listResult="+purchases2String(queryResult.getPurchasesList()));
                    if (queryResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                            && queryResult.getPurchasesList() != null
                            && !queryResult.getPurchasesList().isEmpty()) {
                        List<Purchase> purchasesList = queryResult.getPurchasesList();
                        for (Purchase purchase : purchasesList) {
                            Log.d("=summerzhou=", "PayUtils.querySkuPurchases: result.isAcknowledged=" + purchase.isAcknowledged());
                        }
                    }

                    notifyQuerySubListenerIfNotNone(build,queryResult.getPurchasesList());
                } else {
                    notifyQuerySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, billingResult2String(billingResult)), null);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                notifyQuerySubListenerIfNotNone(wrapBillingResult(BillingClient.BillingResponseCode.ERROR, "onBillingServiceDisconnected"), null);
            }
        });
    }

    //** funcs-private

    private void notifyQuerySubListenerIfNotNone(BillingResult result, List<Purchase> purchases) {
        if (params.getQuerySubListener() != null) {
            params.getQuerySubListener().queryResult(result, purchases);
        }
    }

    private void notifySubListenerIfNotNone(BillingResult result, List<Purchase> purchases) {
        if (params.getSubListener() != null) {
            params.getSubListener().subResult(result, purchases);
        }
    }

    private BillingResult wrapBillingResult(@BillingClient.BillingResponseCode int code, String msg) {
        return BillingResult
                .newBuilder()
                .setDebugMessage(msg)
                .setResponseCode(code)
                .build();
    }

    private void startConnectAsyncIfNot(BillingClientStateListener connectListener) {
        if (client != null && client.isReady()) {
            client.startConnection(connectListener);
        } else {
            BillingResult build = BillingResult.newBuilder()
                    .setResponseCode(BillingClient.BillingResponseCode.OK)
                    .setDebugMessage("clien `s connect is ready")
                    .build();
            connectListener.onBillingSetupFinished(build);
        }
    }

    private boolean paramsCheck() {
        if (TextUtils.isEmpty(params.getPublicKey()) || client == null) {
            return false;
        }

        return true;
    }

    private String billingResult2String(BillingResult result){
        return "errorcode=" + result.getResponseCode() + " errorMsg=" + result.getDebugMessage();
    }

    private String purchases2String(List<Purchase> purchases) {
        if (purchases == null || purchases.isEmpty()) {
            return "null";
        }

        StringBuilder stringBuffer = new StringBuilder();
        for (Purchase purchase : purchases) {
            stringBuffer.append(purchase.getOriginalJson());
            stringBuffer.append("\n");
        }

        return stringBuffer.toString();
    }

    /**
     * Handles the purchase
     * <p>Note: Notice that for each purchase, we check if signature is valid on the client.
     * It's recommended to move this check into your backend.
     * See {@link Security#verifyPurchase(String, String, String)}
     * </p>
     *
     * @param purchases Purchase to be handled
     */
    private List<Purchase> handlePurchase(List<Purchase> purchases) {

        if (purchases == null || purchases.isEmpty()) {
            return null;
        }

        List<Purchase> result = new ArrayList<>();

        for (int i = 0; i < purchases.size(); i++) {
            Purchase purchase = purchases.get(i);
            if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
                continue;
            }


            if (!purchase.isAcknowledged()) {
                PaySdkLog.d("is not Acknowledged " + purchase.getOriginalJson());
                AcknowledgePurchaseParams acknowledgePurchaseParams =
                        AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                client.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    PaySdkLog.d("do Acknowledged msg=[" + billingResult.getResponseCode() + "]" + billingResult.getDebugMessage());
                });
            }
            result.add(purchase);
        }

        return result;
    }

    /**
     * Verifies that the purchase was signed correctly for this developer's public key.
     * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
     * replace this method with "constant true" if they decompile/rebuild your app.
     * </p>
     */
    private boolean verifyValidSignature(String signedData, String signature) {
        // Some sanity checks to see if the developer (that's you!) really followed the
        // instructions to run this sample (don't put these checks on your app!)
        if (params.getPublicKey().contains("CONSTRUCT_YOUR")) {
            throw new RuntimeException("Please update your app's public key at: "
                    + "BASE_64_ENCODED_PUBLIC_KEY");
        }

        try {
            return Security.verifyPurchase(params.getPublicKey(), signedData, signature);
        } catch (IOException e) {
            PaySdkLog.d("Got an exception trying to validate a purchase: " + e);
            return false;
        }
    }
}
