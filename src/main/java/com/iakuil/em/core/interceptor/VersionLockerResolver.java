/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 342252328@qq.com
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
package com.iakuil.em.core.interceptor;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Objects;
import java.util.regex.Pattern;

class VersionLockerResolver {

    private VersionLockerResolver() {}

    static boolean resolve(MetaObject mo, String versionColumn) {
        String originalSql = (String) mo.getValue("boundSql.sql");
        MappedStatement ms = (MappedStatement) mo.getValue("mappedStatement");
        if (Objects.equals(ms.getSqlCommandType(), SqlCommandType.UPDATE)) {
            // sql中包含version = ?
            return Pattern.matches("[\\s\\S]*?" + versionColumn + "[\\s\\S]*?=[\\s\\S]*?\\?[\\s\\S]*?", originalSql.toLowerCase());
        }
        return false;
    }
}
