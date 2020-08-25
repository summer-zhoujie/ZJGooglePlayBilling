# ZJGooglePlayBilling

## 简介
基于Google Play结算库进行二次封装,支持订阅和查询订阅状态两种功能

## 使用

### 1. 集成
```java
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
```java
dependencies {
	implementation 'com.github.jimUpdate:GooglePlayZj:1.0.4'
}
```

### 2. 初始化
```java
public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PaySdk.getInstance()
                .setPublicKey('$Public_Key')
                .init(this);
    }
}
```
### 3.订阅
```java
PaySdk.getInstance()
                .setSubListener('$RESULT_BACK')
                .doSubFlow(activity,skuId);
```
### 4.订阅查询
```java
PaySdk.getInstance()
                .setQuerySubListener('$RESULT_BACK')
                .querySub();

```
