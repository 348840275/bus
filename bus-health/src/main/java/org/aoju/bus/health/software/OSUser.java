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
package org.aoju.bus.health.software;

import org.aoju.bus.core.lang.System;
import org.aoju.bus.health.Systemd;

/**
 * <p>
 * OSUser class.
 * </p>
 *
 * @author Kimi Liu
 * @version 5.3.9
 * @since JDK 1.8+
 */
public class OSUser {

    private String USER_ID;
    private String USER_NAME = Systemd.get(System.USER_NAME, false);
    private String USER_HOME = Systemd.get(System.USER_HOME, false);
    private String USER_DIR = Systemd.get(System.USER_DIR, false);
    private String USER_LANGUAGE = Systemd.get(System.USER_LANGUAGE, false);
    private String USER_COUNTRY = ((Systemd.get(System.USER_COUNTRY, false) == null)
            ? Systemd.get(System.USER_REGION, false) : Systemd.get(System.USER_COUNTRY, false));
    private String JAVA_IO_TMPDIR = Systemd.get(System.IO_TMPDIR, false);

    /**
     * <p>
     * Getter for the field <code>userId</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserId() {
        return this.USER_ID;
    }

    /**
     * <p>
     * Setter for the field <code>userId</code>.
     * </p>
     *
     * @param userId a {@link java.lang.String} object.
     */
    public void setUserId(String userId) {
        this.USER_ID = userId;
    }

    /**
     * <p>
     * Getter for the field <code>userName</code>.
     * </p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUserName() {
        return this.USER_NAME;
    }

    /**
     * <p>
     * Setter for the field <code>userName</code>.
     * </p>
     *
     * @param userName a {@link java.lang.String} object.
     */
    public void setUserName(String userName) {
        this.USER_NAME = userName;
    }

    /**
     * 取得当前登录用户的home目录（取自系统属性：<code>user.home</code>）
     *
     * <p>
     * 例如：<code>"/home/admin"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getHomeDir() {
        return USER_HOME;
    }

    /**
     * 取得当前目录（取自系统属性：<code>user.dir</code>）
     *
     * <p>
     * 例如：<code>"/home/admin/working"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     * @since Java 1.1
     */
    public final String getCurrentDir() {
        return USER_DIR;
    }

    /**
     * 取得临时目录（取自系统属性：<code>java.io.tmpdir</code>）
     *
     * <p>
     * 例如：<code>"/tmp"</code>
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getTempDir() {
        return JAVA_IO_TMPDIR;
    }

    /**
     * 取得当前登录用户的语言设置（取自系统属性：<code>user.language</code>）
     *
     * <p>
     * 例如：<code>"zh"</code>、<code>"en"</code>等
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getLanguage() {
        return USER_LANGUAGE;
    }

    /**
     * 取得当前登录用户的国家或区域设置（取自系统属性：JDK1.4 <code>user.country</code>或JDK1.2 <code>user.region</code>）
     *
     * <p>
     * 例如：<code>"CN"</code>、<code>"US"</code>等
     * </p>
     *
     * @return 属性值, 如果不能取得（因为Java安全限制）或值不存在,则返回<code>null</code>
     */
    public final String getCountry() {
        return USER_COUNTRY;
    }

}
