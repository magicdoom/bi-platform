/**
 * Copyright (c) 2014 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.biplatform.ma.model.exception;

/**
 * 数据源信息读取异常
 * 
 * @author zhongyi
 *
 *         2014-7-29
 */
public class DBInfoReadException extends RuntimeException {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 148469294553038080L;
    
    /**
     * 
     * @param message
     */
    public DBInfoReadException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message
     * @param e
     */
    public DBInfoReadException(String message, Exception e) {
        super(message, e);
    }
    
    /**
     * 
     */
    public DBInfoReadException() {
        super();
    }
}