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
package com.baidu.rigel.biplatform.tesseract.resultset.isservice;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.tesseract.isservice.search.agg.AggregatorMethod;

/**
 * 
 * StatFieldObject
 * 
 * @author lijin
 *
 */
public class StatFieldObject {
    
    /**
     * 参与计算的字段名
     */
    private String statFieldName;
    /**
     * 参与计算的字段值
     */
    private String statFieldString;
    
    /**
     * 计算结果
     */
    private String statFieldResult;
    /**
     * 计算类型
     */
    private Aggregator aggType;
    /**
     * 计算方法
     */
    private AggregatorMethod aggMethod;
    
    /**
     * constructor with no param
     */
    public StatFieldObject() {
        super();
    }
    
    /**
     * constructor
     * 
     * @param statFieldName
     *            statFieldName
     * @param statField
     *            statField
     * @param aggType
     *            aggType
     * @param aggMethod
     *            aggMethod
     */
    public StatFieldObject(String statFieldName, String statFieldString, String statFieldResult,
        Aggregator aggType, AggregatorMethod aggMethod) {
        super();
        this.statFieldName = statFieldName;
        this.statFieldString = statFieldString;
        this.statFieldResult = statFieldResult;
        this.aggType = aggType;
        this.aggMethod = aggMethod;
    }
    
    /**
     * getter method for property statFieldName
     * 
     * @return the statFieldName
     */
    public String getStatFieldName() {
        return statFieldName;
    }
    
    /**
     * setter method for property statFieldName
     * 
     * @param statFieldName
     *            the statFieldName to set
     */
    public void setStatFieldName(String statFieldName) {
        this.statFieldName = statFieldName;
    }
    
    /**
     * getter method for property statField
     * 
     * @return the statField
     */
    public String getStatFieldString() {
        return this.statFieldString;
    }
    
    /**
     * setter method for property statField
     * 
     * @param statField
     *            the statField to set
     */
    public void setStatFieldString(String statFieldString) {
        this.statFieldString = statFieldString;
    }
    
    /**
     * getter method for property aggType
     * 
     * @return the aggType
     */
    public Aggregator getAggType() {
        return aggType;
    }
    
    /**
     * setter method for property aggType
     * 
     * @param aggType
     *            the aggType to set
     */
    public void setAggType(Aggregator aggType) {
        this.aggType = aggType;
    }
    
    /**
     * getter method for property aggMethod
     * 
     * @return the aggMethod
     */
    public AggregatorMethod getAggMethod() {
        return aggMethod;
    }
    
    /**
     * setter method for property aggMethod
     * 
     * @param aggMethod
     *            the aggMethod to set
     */
    public void setAggMethod(AggregatorMethod aggMethod) {
        this.aggMethod = aggMethod;
    }
    
    /**
     * 
     * 聚合操作
     * 
     * @param so
     *            so
     * @return StatFieldObject StatFieldObject
     */
    public StatFieldObject doAgg(StatFieldObject so) {
        return this.aggMethod.doAgggregate(this, so);
    }
    
    /**
     * getter method for property statFieldResult
     * 
     * @return the statFieldResult
     */
    public String getStatFieldResult() {
        return statFieldResult;
    }
    
    /**
     * setter method for property statFieldResult
     * 
     * @param statFieldResult
     *            the statFieldResult to set
     */
    public void setStatFieldResult(String statFieldResult) {
        this.statFieldResult = statFieldResult;
    }
    
}
