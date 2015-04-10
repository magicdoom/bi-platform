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
package com.baidu.rigel.biplatform.tesseract.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.redis.RedisAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import com.baidu.rigel.biplatform.cache.util.ApplicationContextHelper;
import com.baidu.rigel.biplatform.parser.RegisterFunction;
import com.baidu.rigel.biplatform.parser.exception.RegisterFunctionException;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.RelativeRate;
import com.baidu.rigel.biplatform.tesseract.dataquery.udf.SimilitudeRate;

/**
 * Tesseract项目启动入口
 * 
 * @author xiaoming.chen
 *
 */
@Configuration
@ComponentScan(basePackages = "com.baidu.rigel.biplatform.tesseract")
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, RedisAutoConfiguration.class})
@ImportResource({"conf/applicationContext-cache.xml","conf/applicationContext-tesseract.xml"})
public class TesseractApplication {

    /**
     * 启动Tesseract
     * 
     * @param args 启动参数
     * @throws RegisterFunctionException 
     */
    public static void main(String[] args) throws RegisterFunctionException {

        ConfigurableApplicationContext  context = SpringApplication.run(TesseractApplication.class);
        
        ApplicationContextHelper.setContext(context);
        
        RegisterFunction.register("rRate", RelativeRate.class);
        RegisterFunction.register("sRate", SimilitudeRate.class);
        
//        CacheManager cacheManager = (CacheManager) context.getBean("redisCacheManager");
//        
//        cacheManager.getCache("test").put("key", "val");
    }
}