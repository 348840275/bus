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
package org.aoju.bus.http.cache;

import org.aoju.bus.core.io.segment.*;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.header.Headers;
import org.aoju.bus.http.internal.http.HttpCodec;
import org.aoju.bus.http.internal.http.HttpHeaders;
import org.aoju.bus.http.internal.http.HttpMethod;
import org.aoju.bus.http.internal.http.RealResponseBody;
import org.aoju.bus.http.offers.Interceptor;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

/**
 * Serves requests from the cache and writes responses to the cache.
 *
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public final class CacheInterceptor implements Interceptor {

    final InternalCache cache;

    public CacheInterceptor(InternalCache cache) {
        this.cache = cache;
    }

    private static Response stripBody(Response response) {
        return response != null && response.body() != null
                ? response.newBuilder().body(null).build()
                : response;
    }

    private static Headers combine(Headers cachedHeaders, Headers networkHeaders) {
        Headers.Builder result = new Headers.Builder();

        for (int i = 0, size = cachedHeaders.size(); i < size; i++) {
            String fieldName = cachedHeaders.name(i);
            String value = cachedHeaders.value(i);
            if ("Warning".equalsIgnoreCase(fieldName) && value.startsWith("1")) {
                continue; // Drop 100-level freshness warnings.
            }
            if (isContentSpecificHeader(fieldName) || !isEndToEnd(fieldName)
                    || networkHeaders.get(fieldName) == null) {
                Internal.instance.addLenient(result, fieldName, value);
            }
        }

        for (int i = 0, size = networkHeaders.size(); i < size; i++) {
            String fieldName = networkHeaders.name(i);
            if (!isContentSpecificHeader(fieldName) && isEndToEnd(fieldName)) {
                Internal.instance.addLenient(result, fieldName, networkHeaders.value(i));
            }
        }

        return result.build();
    }

    static boolean isEndToEnd(String fieldName) {
        return !"Connection".equalsIgnoreCase(fieldName)
                && !"Keep-Alive".equalsIgnoreCase(fieldName)
                && !"Proxy-Authenticate".equalsIgnoreCase(fieldName)
                && !"Proxy-Authorization".equalsIgnoreCase(fieldName)
                && !"TE".equalsIgnoreCase(fieldName)
                && !"Trailers".equalsIgnoreCase(fieldName)
                && !"Transfer-Encoding".equalsIgnoreCase(fieldName)
                && !"Upgrade".equalsIgnoreCase(fieldName);
    }

    static boolean isContentSpecificHeader(String fieldName) {
        return "Content-Length".equalsIgnoreCase(fieldName)
                || "Content-Encoding".equalsIgnoreCase(fieldName)
                || "Content-Type".equalsIgnoreCase(fieldName);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response cacheCandidate = cache != null
                ? cache.get(chain.request())
                : null;

        long now = System.currentTimeMillis();

        CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
        Request networkRequest = strategy.networkRequest;
        Response cacheResponse = strategy.cacheResponse;

        if (cache != null) {
            cache.trackResponse(strategy);
        }

        if (cacheCandidate != null && cacheResponse == null) {
            Internal.closeQuietly(cacheCandidate.body());
        }

        if (networkRequest == null && cacheResponse == null) {
            return new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(504)
                    .message("Unsatisfiable Request (only-if-cached)")
                    .body(Internal.EMPTY_RESPONSE)
                    .sentRequestAtMillis(-1L)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();
        }

        if (networkRequest == null) {
            return cacheResponse.newBuilder()
                    .cacheResponse(stripBody(cacheResponse))
                    .build();
        }

        Response networkResponse = null;
        try {
            networkResponse = chain.proceed(networkRequest);
        } finally {
            if (networkResponse == null && cacheCandidate != null) {
                Internal.closeQuietly(cacheCandidate.body());
            }
        }

        if (cacheResponse != null) {
            if (networkResponse.code() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                Response response = cacheResponse.newBuilder()
                        .headers(combine(cacheResponse.headers(), networkResponse.headers()))
                        .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
                        .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
                        .cacheResponse(stripBody(cacheResponse))
                        .networkResponse(stripBody(networkResponse))
                        .build();
                networkResponse.body().close();

                cache.trackConditionalCacheHit();
                cache.update(cacheResponse, response);
                return response;
            } else {
                Internal.closeQuietly(cacheResponse.body());
            }
        }

        Response response = networkResponse.newBuilder()
                .cacheResponse(stripBody(cacheResponse))
                .networkResponse(stripBody(networkResponse))
                .build();

        if (cache != null) {
            if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
                CacheRequest cacheRequest = cache.put(response);
                return cacheWritingResponse(cacheRequest, response);
            }

            if (HttpMethod.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    // The cache cannot be written.
                }
            }
        }

        return response;
    }

    private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response)
            throws IOException {
        if (cacheRequest == null) return response;
        Sink cacheBodyUnbuffered = cacheRequest.body();
        if (cacheBodyUnbuffered == null) return response;

        final BufferSource source = response.body().source();
        final BufferSink cacheBody = IoUtils.buffer(cacheBodyUnbuffered);

        Source cacheWritingSource = new Source() {
            boolean cacheRequestClosed;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead;
                try {
                    bytesRead = source.read(sink, byteCount);
                } catch (IOException e) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        cacheRequest.abort();
                    }
                    throw e;
                }

                if (bytesRead == -1) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        cacheBody.close();
                    }
                    return -1;
                }

                sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
                cacheBody.emitCompleteSegments();
                return bytesRead;
            }

            @Override
            public Timeout timeout() {
                return source.timeout();
            }

            @Override
            public void close() throws IOException {
                if (!cacheRequestClosed
                        && !Internal.discard(this, HttpCodec.DISCARD_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                    cacheRequestClosed = true;
                    cacheRequest.abort();
                }
                source.close();
            }
        };

        String contentType = response.header("Content-Type");
        long contentLength = response.body().contentLength();
        return response.newBuilder()
                .body(new RealResponseBody(contentType, contentLength, IoUtils.buffer(cacheWritingSource)))
                .build();
    }

}
