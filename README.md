# GooglePlayZj

## 简介
基于Google Play进行封装,支持订阅和查询订阅状态两种功能

## 使用

### 1. 集成
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
```
	dependencies {
	        implementation 'com.github.jimUpdate:GooglePlayZj:1.0.1'
	}
```

### 2. 初始化
```
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
```
PaySdk.getInstance()
                .setSubListener('$RESULT_BACK')
                .doSubFlow(activity,skuId);
```
### 4.订阅查询
```
PaySdk.getInstance()
                .setQuerySubListener('$RESULT_BACK')
                .querySub();

```
