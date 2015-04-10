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
package com.baidu.rigel.biplatform.ma.file.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 
 * 文件服务之客户端配置类
 * 
 * @author jiangyichao
 *
 */
public final class ClientConfig {

    /**
     * 默认读取文件大小，单位为M
     */
    private static String FILE_READ_SIZE_KEY = "biplatform.ma.file.read.maxsize";

    /**
     * 服务器请求地址
     */
    private static String SERVER_ADDRESS_KEY = "biplatform.ma.fileserver.inetaddress";

    
    /**
     * 文件服务器写文件访问端口
     */
    private static String FILE_SERVER_WRITE_PORT = "biplatform.ma.file.write.port";

    /**
     * 文件服务器写文件访问端口
     */
    private static String FILE_SERVER_READ_PORT = "biplatform.ma.file.read.port";
    
    /**
     * 配置信息
     */
    private static final Properties CONFIG = new Properties();

    private ClientConfig() {
    }
    
    /**
     * 
     * @return 返回读取文件大小
     */
    public static double getFileReadSize() {
        final int defaultSizeBlock = 1024;
        return Double.valueOf(CONFIG.getProperty(FILE_READ_SIZE_KEY, 
                String.valueOf(defaultSizeBlock * defaultSizeBlock)));
    }

    /**
     *
     * @return 返回服务器访问地址
     */
    public static String getServerAddress() {
        return CONFIG.getProperty(SERVER_ADDRESS_KEY, "localhost");
    }

    /**
     * 静态初始块
     */
    static {
        loads();
    }

    /**
     * 加载客户端配置文件，获得编码方式，以及默认读取文件大小
     */
    public static synchronized void loads() {
        InputStream is = null;
        try {
            is = ClientConfig.class.getClassLoader().getResourceAsStream("application.properties");
            CONFIG.load(is);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 获取文件存储路径
     * 
     * @return 文件存储路径
     */
    public static String getServerReadPort() {
        return CONFIG.getProperty(FILE_SERVER_READ_PORT, "80");
    }

    /**
     * 获取写文件服务文件服务器端口
     * @return
     */
    public static int getServerWritePort() {
        return Integer.valueOf(CONFIG.getProperty(FILE_SERVER_WRITE_PORT, "9090"));
    }
}
