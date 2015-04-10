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

import java.math.BigDecimal;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.StatFieldObject;

/**
 * AggregatorMethodSum 聚集方法-加法
 * 
 * @author lijin
 *
 */
public class AggregatorMethodSum extends AggregatorMethod {
    /**
     * INSTANCE
     */
    private static AggregatorMethod INSTANCE;
    
    /**
     * 
     * Constructor by no param
     */
    private AggregatorMethodSum() {
        super(Aggregator.SUM);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.isservice.search.agg.AgggregatorMethod
     * #doAgggregator(com.baidu.rigel.biplatform.tesseract.resultset.isservice.
     * StatFieldObject)
     */
    @Override
    public StatFieldObject doAgggregate(StatFieldObject leftStatFieldObject,
        StatFieldObject rightStatFieldObject) {
        BigDecimal rightValue = new BigDecimal(rightStatFieldObject.getStatFieldString());
        BigDecimal leftValue = new BigDecimal(leftStatFieldObject.getStatFieldString());
        
        BigDecimal resultValue = leftValue.add(rightValue);
        StatFieldObject result = new StatFieldObject(leftStatFieldObject.getStatFieldName(),resultValue.toString(),
            resultValue.toString(), leftStatFieldObject.getAggType(), leftStatFieldObject.getAggMethod());
        
        
        return result;
    }
    
    /**
     * 
     * getAggregatorMethodInstance
     * @return AggregatorMethod
     */
    public synchronized static AggregatorMethod getAggregatorMethodInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AggregatorMethodSum();
        }
        return INSTANCE;
    }
    
}
