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
package com.baidu.rigel.biplatform.ac.model;


/**
 * 时间粒度类型
 * 
 * @author xiaoming.chen
 * 
 */
public enum TimeType {
    /**
     * TimeYear
     */
    TimeYear("yyyy", 1), // 年时间粒度
    /**
     * TimeHalfYear
     */
    TimeHalfYear(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 2), // 半年时间粒度
    /**
     * TimeQuarter
     */
    TimeQuarter(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 3), // 季度时间粒度
    /**
     * TimeMonth
     */
    TimeMonth(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 4), // 月时间粒度
    /**
     * TimeWeekly
     */
    TimeWeekly(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 5), // 周时间粒度
    /**
     * TimeDay
     */
    TimeDay(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 6), // 日时间粒度
    /**
     * TimeHour
     */
    TimeHour(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 7), // 小时时间粒度
    /**
     * TimeMinute
     */
    TimeMinute(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 8), // 分钟时间粒度
    /**
     * TimeSecond
     */
    TimeSecond(TimeDimCommonKeystore.DEFAULT_TIME_FORMAT, 9); // 秒时间粒度
    
    /**
     * format 时间默认格式
     */
    private String format;
    /**
     * id 时间类型ID
     */
    private int id;
    
    /**
     * construct with format and type id
     * @param format time format
     * @param id time enum id
     */
    private TimeType(String format, int id) {
        this.format = format;
        this.id = id;
    }
    
    /**
     * getter method for property format
     * 
     * @return the format
     */
    public String getFormat() {
        return format;
    }
    
    /**
     * getter method for property id
     * 
     * @return the id
     */
    public int getId() {
        return id;
    }
    
}
