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
package com.baidu.rigel.biplatform.ma.model.ds;

/**
 * 
 * 数据来源类型
 * @author david.wang
 *
 */
public enum SourceType {
    
    /**
     * 关系数据库
     */
    RELATION_DATABASE, 
    
    /**
     * excel 文件
     */
    EXCEL, 
    
    /**
     * csv 文件
     */
    CSV,
    
    /**
     * 普通文本文件
     */
    TXT,
    
    /**
     * HDFS文件系统
     */
    HDFS,
    
    /**
     * 列式数据存储介质
     */
    COL_DATABASE;
}
