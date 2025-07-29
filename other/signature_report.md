# APK Signature Report

APK URL: https://raw.githubusercontent.com/FongMi/Release/refs/heads/okjack/apk/release/leanback-arm64_v8a.apk

## Signatures

- **MD5:** 
- **SHA1:** 	SHA2567223DE1AAE9E09110A3007C980AF081B1609A3FEC5BBBDFA7D9CEDB3525110A1
- **SHA-256:** SignaturealgorithmnameSHA256withRSA

## 使用方法

在 Hook.java 文件中，将以下行：
```java
info.signatures = new Signature[]{new Signature(Util.sign(name))};
```

替换为（使用 MD5 签名）：
```java
info.signatures = new Signature[]{new Signature("")};
```
