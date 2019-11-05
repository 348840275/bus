/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.extra;

import org.aoju.bus.core.beans.copier.CopyOptions;
import org.aoju.bus.core.beans.copier.ValueProvider;
import org.aoju.bus.core.consts.Charset;
import org.aoju.bus.core.consts.Httpd;
import org.aoju.bus.core.consts.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Servlet相关工具类封装
 *
 * @author Kimi Liu
 * @version 5.0.9
 * @since JDK 1.8+
 */
public class ServletUtils {

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String[]> getParams(ServletRequest request) {
        final Map<String, String[]> map = request.getParameterMap();
        return Collections.unmodifiableMap(map);
    }

    /**
     * 获得所有请求参数
     *
     * @param request 请求对象{@link ServletRequest}
     * @return Map
     */
    public static Map<String, String> getParamMap(ServletRequest request) {
        Map<String, String> params = new HashMap<String, String>();
        for (Map.Entry<String, String[]> entry : getParams(request).entrySet()) {
            params.put(entry.getKey(), ArrayUtils.join(entry.getValue(), Symbol.COMMA));
        }
        return params;
    }

    /**
     * 获取请求体
     * 调用该方法后，getParam方法将失效
     *
     * @param request {@link ServletRequest}
     * @return 获得请求体
     */
    public static String getBody(ServletRequest request) {
        try {
            return IoUtils.read(request.getReader());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 获取请求体byte[]
     * 调用该方法后，getParam方法将失效
     *
     * @param request {@link ServletRequest}
     * @return 获得请求体byte[]
     */
    public static byte[] getBodyBytes(ServletRequest request) {
        try {
            return IoUtils.readBytes(request.getInputStream());
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>         Bean类型
     * @param request     ServletRequest
     * @param bean        Bean
     * @param copyOptions 注入时的设置
     * @return Bean
     * @since 3.0.4
     */
    public static <T> T fillBean(final ServletRequest request, T bean, CopyOptions copyOptions) {
        final String beanName = StringUtils.lowerFirst(bean.getClass().getSimpleName());
        return BeanUtils.fillBean(bean, new ValueProvider<String>() {
            @Override
            public Object value(String key, Type valueType) {
                String value = request.getParameter(key);
                if (StringUtils.isEmpty(value)) {
                    // 使用类名前缀尝试查找值
                    value = request.getParameter(beanName + Symbol.DOT + key);
                    if (StringUtils.isEmpty(value)) {
                        // 此处取得的值为空时跳过，包括null和""
                        value = null;
                    }
                }
                return value;
            }

            @Override
            public boolean containsKey(String key) {
                // 对于Servlet来说，返回值null意味着无此参数
                return (null != request.getParameter(key)) || (null != request.getParameter(beanName + Symbol.DOT + key));
            }
        }, copyOptions);
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>           Bean类型
     * @param request       {@link ServletRequest}
     * @param bean          Bean
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T fillBean(ServletRequest request, T bean, boolean isIgnoreError) {
        return fillBean(request, bean, CopyOptions.create().setIgnoreError(isIgnoreError));
    }

    /**
     * ServletRequest 参数转Bean
     *
     * @param <T>           Bean类型
     * @param request       ServletRequest
     * @param beanClass     Bean Class
     * @param isIgnoreError 是否忽略注入错误
     * @return Bean
     */
    public static <T> T toBean(ServletRequest request, Class<T> beanClass, boolean isIgnoreError) {
        return fillBean(request, ReflectUtils.newInstance(beanClass), isIgnoreError);
    }

    /**
     * 获取客户端IP
     * 默认检测的Header:
     * <pre>
     * 1、X-Forwarded-For
     * 2、X-Real-IP
     * 3、Proxy-Client-IP
     * 4、WL-Proxy-Client-IP
     * </pre>
     *
     * <p>
     * otherHeaderNames参数用于自定义检测的Header
     * 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     * </p>
     *
     * @param request          请求对象{@link HttpServletRequest}
     * @param otherHeaderNames 其他自定义头文件，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIP(HttpServletRequest request, String... otherHeaderNames) {
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"};
        if (ArrayUtils.isNotEmpty(otherHeaderNames)) {
            headers = ArrayUtils.addAll(headers, otherHeaderNames);
        }

        return getClientIPByHeader(request, headers);
    }

    /**
     * 获取客户端IP
     *
     * <p>
     * headerNames参数用于自定义检测的Header
     * 需要注意的是，使用此方法获取的客户IP地址必须在Http服务器（例如Nginx）中配置头信息，否则容易造成IP伪造。
     * </p>
     *
     * @param request     请求对象{@link HttpServletRequest}
     * @param headerNames 自定义头，通常在Http服务器（例如Nginx）中配置
     * @return IP地址
     */
    public static String getClientIPByHeader(HttpServletRequest request, String... headerNames) {
        String ip;
        for (String header : headerNames) {
            ip = request.getHeader(header);
            if (false == isUnknow(ip)) {
                return getMultistageReverseProxyIp(ip);
            }
        }

        ip = request.getRemoteAddr();
        return getMultistageReverseProxyIp(ip);
    }

    /**
     * 忽略大小写获得请求header中的信息
     *
     * @param request        请求对象{@link HttpServletRequest}
     * @param nameIgnoreCase 忽略大小写头信息的KEY
     * @return header值
     */
    public final static String getHeaderIgnoreCase(HttpServletRequest request, String nameIgnoreCase) {
        Enumeration<String> names = request.getHeaderNames();
        String name = null;
        while (names.hasMoreElements()) {
            name = names.nextElement();
            if (name != null && name.equalsIgnoreCase(nameIgnoreCase)) {
                return request.getHeader(name);
            }
        }

        return null;
    }

    /**
     * 获得请求header中的信息
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @param name    头信息的KEY
     * @param charset 字符集
     * @return header值
     */
    public final static String getHeader(HttpServletRequest request, String name, String charset) {
        final String header = request.getHeader(name);
        if (null != header) {
            try {
                return new String(header.getBytes(Charset.ISO_8859_1), charset);
            } catch (UnsupportedEncodingException e) {
                throw new InstrumentException(StringUtils.format("Error charset {} for http request header.", charset));
            }
        }
        return null;
    }

    /**
     * 客户浏览器是否为IE
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 客户浏览器是否为IE
     */
    public static boolean isIE(HttpServletRequest request) {
        String userAgent = getHeaderIgnoreCase(request, "User-Agent");
        if (StringUtils.isNotBlank(userAgent)) {
            userAgent = userAgent.toUpperCase();
            if (userAgent.contains("MSIE") || userAgent.contains("TRIDENT")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否为GET请求
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 是否为GET请求
     */
    public static boolean isGetMethod(HttpServletRequest request) {
        return Httpd.GET.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否为POST请求
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 是否为POST请求
     */
    public static boolean isPostMethod(HttpServletRequest request) {
        return Httpd.POST.equalsIgnoreCase(request.getMethod());
    }

    /**
     * 是否为Multipart类型表单，此类型表单用于文件上传
     *
     * @param request 请求对象{@link HttpServletRequest}
     * @return 是否为Multipart类型表单，此类型表单用于文件上传
     */
    public static boolean isMultipart(HttpServletRequest request) {
        if (false == isPostMethod(request)) {
            return false;
        }

        String contentType = request.getContentType();
        if (StringUtils.isBlank(contentType)) {
            return false;
        }
        if (contentType.toLowerCase().startsWith("multipart/")) {
            return true;
        }

        return false;
    }

    /**
     * 获得指定的Cookie
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @param name               cookie名
     * @return Cookie对象
     */
    public final static Cookie getCookie(HttpServletRequest httpServletRequest, String name) {
        final Map<String, Cookie> cookieMap = readCookieMap(httpServletRequest);
        return cookieMap == null ? null : cookieMap.get(name);
    }

    /**
     * 将cookie封装到Map里面
     *
     * @param httpServletRequest {@link HttpServletRequest}
     * @return Cookie map
     */
    public final static Map<String, Cookie> readCookieMap(HttpServletRequest httpServletRequest) {
        Map<String, Cookie> cookieMap = new HashMap<String, Cookie>();
        Cookie[] cookies = httpServletRequest.getCookies();
        if (null == cookies) {
            return null;
        }
        for (Cookie cookie : cookies) {
            cookieMap.put(cookie.getName().toLowerCase(), cookie);
        }
        return cookieMap;
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param cookie   Servlet Cookie对象
     */
    public final static void addCookie(HttpServletResponse response, Cookie cookie) {
        response.addCookie(cookie);
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param name     Cookie名
     * @param value    Cookie值
     */
    public final static void addCookie(HttpServletResponse response, String name, String value) {
        response.addCookie(new Cookie(name, value));
    }

    /**
     * 设定返回给客户端的Cookie
     *
     * @param response        响应对象{@link HttpServletResponse}
     * @param name            cookie名
     * @param value           cookie值
     * @param maxAgeInSeconds -1: 关闭浏览器清除Cookie. 0: 立即清除Cookie. &gt;0 : Cookie存在的秒数.
     * @param path            Cookie的有效路径
     * @param domain          the domain name within which this cookie is visible; form is according to RFC 2109
     */
    public final static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds, String path, String domain) {
        Cookie cookie = new Cookie(name, value);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(maxAgeInSeconds);
        cookie.setPath(path);
        addCookie(response, cookie);
    }

    /**
     * 设定返回给客户端的Cookie
     * Path: "/"
     * No Domain
     *
     * @param response        响应对象{@link HttpServletResponse}
     * @param name            cookie名
     * @param value           cookie值
     * @param maxAgeInSeconds -1: 关闭浏览器清除Cookie. 0: 立即清除Cookie. &gt;0 : Cookie存在的秒数.
     */
    public final static void addCookie(HttpServletResponse response, String name, String value, int maxAgeInSeconds) {
        addCookie(response, name, value, maxAgeInSeconds, "/", null);
    }

    /**
     * 获得PrintWriter
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @return 获得PrintWriter
     * @throws InstrumentException IO异常
     */
    public static PrintWriter getWriter(HttpServletResponse response) throws InstrumentException {
        try {
            return response.getWriter();
        } catch (IOException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 返回数据给客户端
     *
     * @param response    响应对象{@link HttpServletResponse}
     * @param text        返回的内容
     * @param contentType 返回的类型
     */
    public static void write(HttpServletResponse response, String text, String contentType) {
        response.setContentType(contentType);
        Writer writer = null;
        try {
            writer = response.getWriter();
            writer.write(text);
            writer.flush();
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(writer);
        }
    }

    /**
     * 返回文件给客户端
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param file     写出的文件对象
     */
    public static void write(HttpServletResponse response, File file) {
        final String fileName = file.getName();
        final String contentType = ObjectUtils.defaultIfNull(FileUtils.getMimeType(fileName), "application/octet-stream");
        BufferedInputStream in = null;
        try {
            in = FileUtils.getInputStream(file);
            write(response, in, contentType, fileName);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 返回数据给客户端
     *
     * @param response    响应对象{@link HttpServletResponse}
     * @param in          需要返回客户端的内容
     * @param contentType 返回的类型
     * @param fileName    文件名
     */
    public static void write(HttpServletResponse response, InputStream in, String contentType, String fileName) {
        final String charset = ObjectUtils.defaultIfNull(response.getCharacterEncoding(), Charset.DEFAULT_UTF_8);
        response.setHeader("Content-Disposition", StringUtils.format("attachment;filename={}", UriUtils.encode(fileName, charset)));
        response.setContentType(contentType);
        write(response, in);
    }

    /**
     * 返回数据给客户端
     *
     * @param response    响应对象{@link HttpServletResponse}
     * @param in          需要返回客户端的内容
     * @param contentType 返回的类型
     */
    public static void write(HttpServletResponse response, InputStream in, String contentType) {
        response.setContentType(contentType);
        write(response, in);
    }

    /**
     * 返回数据给客户端
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param in       需要返回客户端的内容
     */
    public static void write(HttpServletResponse response, InputStream in) {
        write(response, in, IoUtils.DEFAULT_BUFFER_SIZE);
    }

    /**
     * 返回数据给客户端
     *
     * @param response   响应对象{@link HttpServletResponse}
     * @param in         需要返回客户端的内容
     * @param bufferSize 缓存大小
     */
    public static void write(HttpServletResponse response, InputStream in, int bufferSize) {
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            IoUtils.copy(in, out, bufferSize);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            IoUtils.close(out);
            IoUtils.close(in);
        }
    }

    /**
     * 设置响应的Header
     *
     * @param response 响应对象{@link HttpServletResponse}
     * @param name     名
     * @param value    值，可以是String，Date， int
     */
    public static void setHeader(HttpServletResponse response, String name, Object value) {
        if (value instanceof String) {
            response.setHeader(name, (String) value);
        } else if (Date.class.isAssignableFrom(value.getClass())) {
            response.setDateHeader(name, ((Date) value).getTime());
        } else if (value instanceof Integer || "int".equals(value.getClass().getSimpleName().toLowerCase())) {
            response.setIntHeader(name, (Integer) value);
        } else {
            response.setHeader(name, value.toString());
        }
    }

    /**
     * 从多级反向代理中获得第一个非unknown IP地址
     *
     * @param ip 获得的IP地址
     * @return 第一个非unknown IP地址
     */
    private static String getMultistageReverseProxyIp(String ip) {
        // 多级反向代理检测
        if (ip != null && ip.indexOf(",") > 0) {
            final String[] ips = ip.trim().split(",");
            for (String subIp : ips) {
                if (false == isUnknow(subIp)) {
                    ip = subIp;
                    break;
                }
            }
        }
        return ip;
    }

    /**
     * 检测给定字符串是否为未知，多用于检测HTTP请求相关
     *
     * @param checkString 被检测的字符串
     * @return 是否未知
     */
    private static boolean isUnknow(String checkString) {
        return StringUtils.isBlank(checkString) || "unknown".equalsIgnoreCase(checkString);
    }

}
