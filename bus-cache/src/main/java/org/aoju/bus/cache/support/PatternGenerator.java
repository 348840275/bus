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
package org.aoju.bus.cache.support;

import org.aoju.bus.cache.annotation.CacheKey;
import org.aoju.bus.cache.entity.CacheHolder;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Kimi Liu
 * @version 3.5.7
 * @since JDK 1.8
 */
public class PatternGenerator {

    private static final ConcurrentMap<Method, String> patterns = new ConcurrentHashMap<>();

    public static String generatePattern(CacheHolder cacheHolder) {
        return patterns.computeIfAbsent(cacheHolder.getMethod(), (method) -> doPatternCombiner(cacheHolder));
    }

    private static String doPatternCombiner(CacheHolder cacheHolder) {
        StringBuilder sb = new StringBuilder(cacheHolder.getPrefix());
        Collection<CacheKey> cacheKeys = cacheHolder.getCacheKeyMap().values();
        for (CacheKey cacheKey : cacheKeys) {
            sb.append(cacheKey.value());
        }

        return sb.toString();
    }
}
