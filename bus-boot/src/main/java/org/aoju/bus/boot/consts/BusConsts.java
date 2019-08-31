/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
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
package org.aoju.bus.boot.consts;


/**
 * 全局常量配置
 *
 * @author Kimi Liu
 * @version 3.1.6
 * @since JDK 1.8
 */
public class BusConsts {

    /***
     * 应用名: 备注 @Value("${spring.application.name:@null}")
     */
    public static final String BUS_VERSION = "version";

    /***
     * 应用名: 备注 @Value("${spring.application.name:@null}")
     */
    public static final String BUS_APP_NAME = "spring.application.name";
    /***
     * 应用版本号
     */
    public static final String BUS_BOOT_VERSION = "bus-boot.version";
    /***
     * 应用版本信息
     */
    public static final String BUS_BOOT_FORMATTED_VERSION = "bus-boot.formatted-version";
    /***
     * 应用属性
     */
    public static final String BUS_BOOT_PROPERTIES = "BusConfigurationProperties";

}
