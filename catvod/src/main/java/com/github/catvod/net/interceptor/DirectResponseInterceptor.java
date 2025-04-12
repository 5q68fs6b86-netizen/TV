 package com.github.catvod.net.interceptor; // 或者你的包名

 import java.io.IOException;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Objects; // 引入 Objects 用于 null 检查

 import okhttp3.Interceptor;
 import okhttp3.MediaType;
 import okhttp3.Protocol;
 import okhttp3.Request;
 import okhttp3.Response;
 import okhttp3.ResponseBody;

 public class DirectResponseInterceptor implements Interceptor {

     // --- 配置 ---
     // 默认的响应 MIME 类型 (如果不同 URL 需要不同类型，可以进一步扩展)
     private static final MediaType RESPONSE_MEDIA_TYPE = MediaType.parse("text/plain; charset=utf-8");
     // --- 配置结束 ---

     // 用于存储 URL -> 响应体 映射的 Map
     private final Map<String, String> urlResponseMap;

     /**
      * 拦截器的构造函数。
      * @param mappings 一个 Map，其中 key 是要拦截的精确 URL 字符串，
      *                 value 是要作为响应体返回的字符串内容。
      *                 此 Map 不能为 null。
      */
     public DirectResponseInterceptor(Map<String, String> mappings) {
         // 使用 Objects.requireNonNull 确保 mappings 不为 null
         // 使用 Collections.unmodifiableMap 保证映射关系在创建后不被修改，更安全
         this.urlResponseMap = Collections.unmodifiableMap(Objects.requireNonNull(mappings, "映射 Map 不能为空"));
     }

     @Override
     public Response intercept(Chain chain) throws IOException {
         Request request = chain.request();
         String requestUrlString = request.url().toString(); // 获取请求的 URL 字符串

         // 检查请求的 URL 是否在我们的映射 Map 中作为键存在
         String directResponseBody = urlResponseMap.get(requestUrlString);

         if (directResponseBody != null) {
             // URL 匹配！构造并返回预定义的假响应

             System.out.println("DirectResponseInterceptor: 拦截到请求 " + requestUrlString); // 可选：日志输出

             // 1. 使用 Map 中对应的值创建响应体 (ResponseBody)
             ResponseBody responseBody = ResponseBody.create(RESPONSE_MEDIA_TYPE, directResponseBody);

             // 2. 构建响应 (Response)
             Response fakeResponse = new Response.Builder()
                     .request(request) // 关联原始请求
                     .protocol(Protocol.HTTP_1_1) // 指定协议 (HTTP/1.1 或 HTTP/2)
                     .code(200) // 设置 HTTP 状态码 (200 OK)
                     .message("OK (直接响应)") // 自定义状态消息
                     .body(responseBody) // 设置我们创建的响应体
                     .addHeader("Content-Type", RESPONSE_MEDIA_TYPE.toString()) // 添加 Content-Type 头
                     // OkHttp 会根据 body 自动计算 Content-Length
                     // .addHeader("Content-Length", String.valueOf(directResponseBody.getBytes(StandardCharsets.UTF_8).length))
                     .addHeader("X-Intercepted-By", "DirectResponseInterceptor") // 可选：添加自定义头表明被拦截
                     .build();

             // 3. 返回假响应，请求链在此中断，不会进行网络请求
             return fakeResponse;
         } else {
             // URL 在 Map 中未找到，让请求继续正常处理
             return chain.proceed(request);
         }
     }
 }