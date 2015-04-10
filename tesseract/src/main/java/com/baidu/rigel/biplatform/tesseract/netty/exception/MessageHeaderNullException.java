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
/**
 * 
 */
package com.baidu.rigel.biplatform.tesseract.netty.exception;

/**
 * MessageHeaderNullException,传递消息时，消息头为null
 * 
 * @author lijin
 *
 */
public class MessageHeaderNullException extends Exception {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -6654569116000838204L;
    
    /**
     * 
     * Constructor by no param
     */
    public MessageHeaderNullException() {
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param message
     *            message
     */
    public MessageHeaderNullException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param cause
     *            cause
     */
    public MessageHeaderNullException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param message
     *            message
     * @param cause
     *            cause
     */
    public MessageHeaderNullException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }
    
    /**
     * 
     * Constructor by
     * 
     * @param message
     *            message
     * @param cause
     *            cause
     * @param enableSuppression
     *            enableSuppression
     * @param writableStackTrace
     *            writableStackTrace
     */
    public MessageHeaderNullException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }
    
}
