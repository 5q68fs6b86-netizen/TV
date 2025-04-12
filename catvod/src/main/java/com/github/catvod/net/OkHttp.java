 package com.github.catvod.net;

 import android.text.TextUtils; // Android 工具类，用于检查字符串

 import androidx.collection.ArrayMap; // 使用 ArrayMap 以优化 Android 性能

 import com.github.catvod.bean.Doh; // 引入自定义的 Doh Bean 类
 import com.github.catvod.net.interceptor.DirectResponseInterceptor; // 引入自定义的直接响应拦截器
 import com.github.catvod.net.interceptor.RequestInterceptor;      // 引入自定义的请求拦截器
 import com.github.catvod.net.interceptor.ResponseInterceptor;     // 引入自定义的响应拦截器
 import com.github.catvod.utils.Path;                              // 引入路径工具类

 import java.net.ProxySelector; // Java 网络代理选择器
 import java.util.Map;           // 引入 Map 接口
 import java.util.Objects;       // 引入 Objects 工具类
 import java.util.concurrent.TimeUnit; // 时间单位

 import okhttp3.Cache;           // OkHttp 缓存
 import okhttp3.Call;            // OkHttp 调用对象
 import okhttp3.Dns;             // OkHttp DNS 解析接口
 import okhttp3.FormBody;        // OkHttp 表单请求体
 import okhttp3.Headers;         // OkHttp 请求/响应头
 import okhttp3.HttpUrl;         // OkHttp URL 处理类
 import okhttp3.OkHttpClient;    // OkHttp 核心客户端类
 import okhttp3.Request;         // OkHttp 请求对象
 import okhttp3.RequestBody;     // OkHttp 请求体
 import okhttp3.dnsoverhttps.DnsOverHttps; // OkHttp DNS over HTTPS 实现

 public class OkHttp {

     // 默认超时时间：30秒 (单位：毫秒)
     private static final int TIMEOUT = 30 * 1000;
     // 默认缓存大小：100MB (单位：字节)
     private static final int CACHE = 100 * 1024 * 1024;
     // 存储系统默认的代理选择器
     private static final ProxySelector defaultSelector;

     // --- 实例变量 ---
     // 标记当前是否配置了自定义代理
     private boolean proxy;
     // DNS over HTTPS (DoH) 解析器实例
     private DnsOverHttps dns;
     // 缓存的共享 OkHttpClient 实例 (会根据配置变化而重置)
     private OkHttpClient client;
     // 自定义的代理选择器实例 (懒加载)
     private OkProxySelector selector;

     // 静态初始化块：在类加载时执行一次
     static {
         // 获取并保存 JVM 启动时的默认代理选择器，用于恢复设置
         defaultSelector = ProxySelector.getDefault();
     }

     // --- 单例模式 Holder ---
     // 使用静态内部类实现线程安全的懒加载单例
     private static class Loader {
         static volatile OkHttp INSTANCE = new OkHttp();
     }

     /**
      * 获取 OkHttp 管理器的单例实例。
      * @return OkHttp 的单例对象。
      */
     public static OkHttp get() {
         return Loader.INSTANCE;
     }

     /**
      * 获取当前配置的 DNS 解析器。
      * 如果配置了 DoH，则返回 DoH 解析器；否则返回系统默认解析器。
      * @return 当前应使用的 Dns 实例。
      */
     public static Dns dns() {
         // 优先使用自定义的 DoH 解析器
         return get().dns != null ? get().dns : Dns.SYSTEM;
     }

     /**
      * 配置 DNS over HTTPS (DoH)。
      * @param doh Doh 配置对象。如果 doh 或 doh.getUrl() 为空，则禁用 DoH。
      */
     public void setDoh(Doh doh) {
         if (doh == null || doh.getUrl().isEmpty()) {
             dns = null; // 禁用 DoH
         } else {
             try {
                 // 为 DoH 查询创建一个专用的 OkHttpClient (可带缓存)
                 // 注意：这里的 Path.doh() 需要是有效的缓存路径获取方法
                 OkHttpClient dohClient = new OkHttpClient.Builder().cache(new Cache(Path.doh(), CACHE)).build();
                 dns = new DnsOverHttps.Builder()
                         .client(dohClient) // 使用专用 client 进行 DoH 查询
                         .url(HttpUrl.get(doh.getUrl())) // 设置 DoH 服务器 URL
                         .bootstrapDnsHosts(doh.getHosts()) // 可选：指定用于解析 DoH 服务器域名的 IP 地址
                         .build();
             } catch (Exception e) {
                 // 配置 DoH 时出错，打印错误并回退到 null (禁用 DoH)
                 System.err.println("配置 DoH 时出错: " + e.getMessage());
                 e.printStackTrace();
                 dns = null;
             }
         }
         // DNS 配置已更改，重置缓存的 client 实例，强制下次重新构建
         client = null;
     }

     /**
      * 设置自定义代理。
      * @param proxy 代理字符串 (例如："http://user:pass@host:port", "socks://host:port")。
      *              如果为空或 null，则恢复使用系统默认代理。
      */
     public void setProxy(String proxy) {
         boolean useProxy = !TextUtils.isEmpty(proxy); // 判断是否需要使用代理
         if (useProxy) {
             // 设置自定义代理选择器为 JVM 默认
             ProxySelector.setDefault(selector());
             // 将代理信息配置给自定义选择器
             selector().setProxy(proxy);
         } else {
             // 恢复使用系统默认的代理选择器
             ProxySelector.setDefault(defaultSelector);
         }
         this.proxy = useProxy; // 更新代理使用标志
         // 代理配置已更改，重置缓存的 client 实例
         client = null;
     }

     /**
      * 获取自定义代理选择器实例 (懒加载)。
      * 如果实例不存在，则创建并返回。
      * @return OkProxySelector 实例。
      */
     public static OkProxySelector selector() {
         if (get().selector != null) return get().selector;
         return get().selector = new OkProxySelector();
     }

     /**
      * 获取共享的、配置好的 OkHttpClient 实例 (懒加载并缓存)。
      * 此客户端使用默认超时时间并允许重定向。
      * @return 共享的 OkHttpClient 实例。
      */
     public static OkHttpClient client() {
         // 如果缓存的 client 实例有效，直接返回
         if (get().client != null) return get().client;
         // 否则，调用 getBuilder() 创建新的实例，缓存并返回
         return get().client = getBuilder().build();
     }

     /**
      * 获取一个新的 OkHttpClient 实例，基于共享客户端但具有自定义超时。
      * 它会共享连接池等底层资源。
      * @param timeout 连接、读取、写入的超时时间 (毫秒)。
      * @return 具有指定超时的新 OkHttpClient 实例。
      */
     public static OkHttpClient client(int timeout) {
         // 使用共享 client 的配置作为基础，仅修改超时设置
         return client().newBuilder()
                 .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                 .readTimeout(timeout, TimeUnit.MILLISECONDS)
                 .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                 .build();
     }

     /**
      * 获取一个新的 OkHttpClient 实例，基于共享客户端，但禁用重定向并具有自定义超时。
      * @param timeout 连接、读取、写入的超时时间 (毫秒)。
      * @return 禁用重定向且具有指定超时的新 OkHttpClient 实例。
      */
     public static OkHttpClient noRedirect(int timeout) {
         return client().newBuilder()
                 .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                 .readTimeout(timeout, TimeUnit.MILLISECONDS)
                 .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                 .followRedirects(false)      // 禁止 HTTP 重定向
                 .followSslRedirects(false)   // 禁止 HTTPS 重定向
                 .build();
     }

     /**
      * 获取一个新的 OkHttpClient 实例，基于共享客户端，可配置重定向并具有自定义超时。
      * @param redirect true 表示允许重定向，false 表示禁止。
      * @param timeout 连接、读取、写入的超时时间 (毫秒)。
      * @return 具有指定设置的新 OkHttpClient 实例。
      */
     public static OkHttpClient client(boolean redirect, int timeout) {
         return redirect ? client(timeout) : noRedirect(timeout);
     }


     // --- 创建直接响应映射规则的辅助方法 ---
     private static Map<String, String> createDirectResponseMappings() {
         // 使用 ArrayMap (Android 优化) 或 java.util.HashMap (标准 Java)
         Map<String, String> mappings = new ArrayMap<>();

         // --- 在这里添加你的 URL -> 响应体 映射规则 ---
         mappings.put(
                 "https://cnb.cool/wexfnw/fnb/-/git/raw/main/api.txt", // 要拦截的 URL
                 "1hhWL3SIfv+9QngN5CVtXF82my+NrL/EFBmrFAJjMlo="       // 对应的直接响应内容
         );

         mappings.put(
                 "http://110.40.42.222:9595//item.php",          // 另一个要拦截的 URL
                 "4kZ6imgLKJoo01pBYaKg3xYg99lSMVQ4KDB57PESW3+TMaZy49nHMUn7CsZF41PE6v1OaCiRwrelEq/+ELdO1q3MKsPkdMPeuIrDMr+zgpgIgxYRn+WqRehFVQdOicn8DAIl2tfQvGdn/8Ggr/lEjwhVl4B+VhTCp3KGkkn1LFvfMvgCva8xtOxj296aQNZAAv2qvE+COMztN00yygLld25zf2HwP+E3DnQJlKQbVGEicepqRenOHkd85uZ6LPMn/vtfmvFwzNkp6PLQEyRcycgJ9kqtAsUOBZPt1qUhuDybrJPghB0ps5QgozS9LM1tWqZ5LzxElWZZBXUjIWGshPlTTpBDr25N3AJglSLE3FqPph8L9AhRy1WcHeCjGcSyHMRiK2Rhpl8h7a0qB5SZVQwYjjtaYoZo8fSzb+f2OweSL3mjWR80Cw/x0wu8x6KzZj4lFOMER1tHcvtpLbtyZDrCHF6igwPTxARkIwPh779la25DL5bNwKmtMIhIU6R8G+1xtLTOk3AVGLXUpXeFQnELkuYIhxcVOgD6w6DEYW+wrVmssg7xbQkNLyKLgr0j+2hLpgBTSg7wgZg3qnQBEWYKvaVqUmZ+Zoo2ol8OPAIFaBR86t+28L//OYY4k+63qL/3DlElL4lWItko3nCyD2xV/f+CLX5LYTjycNFs41HmHDpr4YQbJcKVOQA2glsP3O3ff+JJNA4aDiubp/9jgnkba7hYzC6EEyQ+KXKfcJTuMzoMfgUc7bX7hRyik0ux82htH6MANW0zwm4B8HHbhXsrDa5eEmgjbpLZieYdB6TCr2R9ITIERgZoVFrIzQCcL4HkJuLkYDHgnQnE7jVVIfzJHnD9urELu7EeAHyY8LRlKCJJLpSYZkBH42B6w6Uu6Gm2chmQGy36Rx2onDGsgeGKAOA3oD8pTPXQK/+ZnDlq9vGJQaQ4d7cKjeKMTRKf0oYaif2FnqOED1tG2V6UDxedw09suSSHYiRHPRtmD57XcjBiSVK4ySzPjJI8VDJp9aDr2a8q3jukqq1vK+7EzxBfghxNnRlRsNzJJMFeoqXtUv/PxLxBon6aUBOgG1Pc6wkiY8wPPRkBN5nqpcQOhQ==" // 对应的 JSON 响应
         );

         // 根据需要添加更多映射规则
         // mappings.put("http://another-url.com/data", "一些纯文本数据");
         // --- 映射规则添加结束 ---

         return mappings; // 返回包含所有规则的 Map
     }
     // --- 辅助方法结束 ---


     // --- 用于创建 OkHttpClient.Builder 的核心方法 ---
     private static OkHttpClient.Builder getBuilder() {
         // 1. 创建映射规则 Map
         Map<String, String> directMappings = createDirectResponseMappings();

         // 2. 创建 OkHttpClient.Builder
         OkHttpClient.Builder builder = new OkHttpClient.Builder()
                 // **** 添加拦截器 ****
                 // 最先添加直接响应拦截器，传入映射规则
                 .addInterceptor(new DirectResponseInterceptor(directMappings))
                 // 添加自定义请求拦截器 (在请求发送前修改请求)
                 .addInterceptor(new RequestInterceptor())
                 // 添加自定义网络拦截器 (能看到网络传输细节，如重定向)
                 .addNetworkInterceptor(new ResponseInterceptor())
                 // **** 拦截器添加完毕 ****

                 // 设置默认超时
                 .connectTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                 .readTimeout(TIMEOUT, TimeUnit.MILLISECONDS)
                 .writeTimeout(TIMEOUT, TimeUnit.MILLISECONDS)

                 // 设置 DNS 解析器 (可能是 DoH 或系统默认)
                 .dns(dns())

                 // 设置主机名验证器: ((hostname, session) -> true) 表示信任所有主机名
                 // !!! 警告：这通常是不安全的，会禁用 HTTPS 主机名验证，容易受到中间人攻击 !!!
                 // !!! 除非有明确且充分的理由，否则不应使用 !!!
                 .hostnameVerifier((hostname, session) -> true)

                 // 默认允许 HTTP/HTTPS 重定向
                 .followRedirects(true)
                 .followSslRedirects(true) // 默认也允许 SSL 重定向

                 // 设置自定义 SSL Socket Factory 和 Trust Manager
                 // 可能用于兼容旧版 Android、信任自签名证书等
                 // !!! 警告：如果 SSLCompat 或 TM 实现不当 (例如信任所有证书)，会带来严重安全风险 !!!
                 // 这里的 SSLCompat() 和 SSLCompat.TM 需要是项目中正确实现的类和对象
                 .sslSocketFactory(new SSLCompat(), SSLCompat.TM);

         // 根据 proxy 标志位设置代理选择器
         builder.proxySelector(get().proxy ? selector() : defaultSelector);

         // 返回配置好的 Builder
         return builder;
     }


     // --- 便捷的网络请求方法 ---

     /**
      * 执行简单的 GET 请求，并返回响应体字符串。
      * 如果 URL 不以 "http" 开头或发生错误，则返回空字符串。
      * @param url 要请求的 URL。
      * @return 响应体字符串，失败时返回空字符串。
      */
     public static String string(String url) {
         // 基础检查
         if (url == null || !url.startsWith("http")) return "";
         try {
             // 使用默认 client (共享实例，默认超时) 发起请求
             // 注意：这里直接调用 execute() 是同步执行，会阻塞当前线程
             return newCall(client(), url).execute().body().string();
         } catch (Exception e) {
             // 发生异常时打印堆栈并返回空字符串
             e.printStackTrace();
             return "";
         }
     }

     /**
      * 执行带自定义 Headers 的 GET 请求，并返回响应体字符串。
      * 如果发生错误，则返回空字符串。
      * @param url 要请求的 URL。
      * @param headers 请求头 Map。
      * @return 响应体字符串，失败时返回空字符串。
      */
     public static String string(String url, Map<String, String> headers) {
         // 基础检查
         if (url == null || headers == null) return "";
         try {
             // 使用默认 client，传入 Headers 对象
             return newCall(url, Headers.of(headers)).execute().body().string();
         } catch (Exception e) {
             e.printStackTrace();
             return "";
         }
     }

     // --- 用于创建 Call 对象的方法 ---

     /**
      * 使用默认共享 client 创建一个基本的 GET 请求 Call 对象。
      * @param url 请求 URL。
      * @return 可执行的 Call 对象。
      */
     public static Call newCall(String url) {
         return client().newCall(new Request.Builder().url(url).build());
     }

     /**
      * 使用指定的 OkHttpClient 实例创建一个基本的 GET 请求 Call 对象。
      * @param client 要使用的 OkHttpClient 实例。
      * @param url 请求 URL。
      * @return 可执行的 Call 对象。
      */
     public static Call newCall(OkHttpClient client, String url) {
         return client.newCall(new Request.Builder().url(url).build());
     }

     /**
      * 使用默认共享 client 创建一个带 Headers 的 GET 请求 Call 对象。
      * @param url 请求 URL。
      * @param headers 请求头。
      * @return 可执行的 Call 对象。
      */
     public static Call newCall(String url, Headers headers) {
         return client().newCall(new Request.Builder().url(url).headers(headers).build());
     }

     /**
      * 使用默认共享 client 创建一个带 Headers 和 GET 查询参数的请求 Call 对象。
      * @param url 基础 URL。
      * @param headers 请求头。
      * @param params GET 请求的查询参数 Map (推荐使用 ArrayMap)。
      * @return 可执行的 Call 对象。
      */
     public static Call newCall(String url, Headers headers, ArrayMap<String, String> params) {
         // 使用 buildUrl 辅助方法将参数附加到 URL
         return client().newCall(new Request.Builder().url(buildUrl(url, params)).headers(headers).build());
     }

     /**
      * 使用默认共享 client 创建一个带 Headers 和 POST 请求体的 Call 对象。
      * @param url 请求 URL。
      * @param headers 请求头。
      * @param body POST 请求体。
      * @return 可执行的 Call 对象。
      */
     public static Call newCall(String url, Headers headers, RequestBody body) {
         return client().newCall(new Request.Builder().url(url).headers(headers).post(body).build());
     }

     /**
      * 使用指定的 OkHttpClient 实例创建一个带 POST 请求体的 Call 对象。
      * @param client 要使用的 OkHttpClient 实例。
      * @param url 请求 URL。
      * @param body POST 请求体。
      * @return 可执行的 Call 对象。
      */
     public static Call newCall(OkHttpClient client, String url, RequestBody body) {
         return client.newCall(new Request.Builder().url(url).post(body).build());
     }


     // --- 辅助方法 ---

     /**
      * 将 ArrayMap<String, String> 转换为 OkHttp 的 FormBody (用于 application/x-www-form-urlencoded 类型的 POST 请求)。
      * @param params 参数 Map。
      * @return FormBody 对象。
      */
     public static FormBody toBody(ArrayMap<String, String> params) {
         FormBody.Builder body = new FormBody.Builder();
         if (params != null) {
             for (Map.Entry<String, String> entry : params.entrySet()) {
                 // getValue() 可能为 null，OkHttp add 方法需要非 null 值
                 body.add(entry.getKey(), Objects.toString(entry.getValue(), ""));
             }
         }
         return body.build();
     }

     /**
      * 私有辅助方法：将查询参数添加到 URL。
      * @param url 基础 URL 字符串。
      * @param params 查询参数 Map。
      * @return 包含查询参数的 HttpUrl 对象。
      */
     private static HttpUrl buildUrl(String url, ArrayMap<String, String> params) {
         // 使用 OkHttp 的 HttpUrl.Builder 来安全地构建 URL
         HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(url), "无效的 URL: " + url).newBuilder();
         if (params != null) {
             for (Map.Entry<String, String> entry : params.entrySet()) {
                 // getValue() 可能为 null，addQueryParameter 需要非 null 值
                 builder.addQueryParameter(entry.getKey(), Objects.toString(entry.getValue(), ""));
             }
         }
         return builder.build();
     }

     // 注意： SSLCompat() 和 SSLCompat.TM 需要在项目中实际定义和实现
     // 同样，OkProxySelector() 也需要实际定义和实现
     // Path.doh() 也需要是有效的路径获取方法
 }