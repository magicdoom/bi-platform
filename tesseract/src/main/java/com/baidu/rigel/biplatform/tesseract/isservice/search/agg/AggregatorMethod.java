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
package com.baidu.rigel.biplatform.tesseract.isservice.search.agg;

import java.util.HashMap;
import java.util.Map;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.StatFieldObject;

/**
 * AggregatorMethod 聚集方法的抽像类
 * 
 * @author lijin
 *
 */
public abstract class AggregatorMethod {
    
    private static Map<Aggregator, AggregatorMethod> AGG_INSTANCE_MAP;
    
    private Aggregator aggType;
    
    /**
     * 
     */
    public AggregatorMethod(Aggregator aggType) {
        this.aggType = aggType;
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
    
    public abstract StatFieldObject doAgggregate(StatFieldObject leftStatFieldObject,
            StatFieldObject rightStatFieldObject);
    
    public static AggregatorMethod getAggregatorMethod(Aggregator aggType) {
        if (aggType == null) {
            return null;
        }
        AggregatorMethod result = null;
        if (AGG_INSTANCE_MAP == null) {
            AGG_INSTANCE_MAP = new HashMap<Aggregator, AggregatorMethod>();
        }
        if (AGG_INSTANCE_MAP.containsKey(aggType)) {
            result = AGG_INSTANCE_MAP.get(aggType);
        } else {
            if (aggType.equals(Aggregator.SUM)) {
                result = AggregatorMethodSum.getAggregatorMethodInstance();
            } else if (aggType.equals(Aggregator.COUNT)) {
                result = AggregatorMethodCount.getAggregatorMethodInstance();
            }
            
            AGG_INSTANCE_MAP.put(aggType, result);
        }
        
        return result;
    }
    
}
