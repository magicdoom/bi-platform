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

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * StatFieldRecord
 * 
 * @author lijin
 *
 */
public class StatFieldRecord {
    
    /**
     * statFieldMap
     */
    private Map<String, StatFieldObject> statFieldMap;
    
    /**
     * 
     */
    public StatFieldRecord() {
        super();
    }
    
    /**
     * @param statFieldMap
     */
    public StatFieldRecord(Map<String, StatFieldObject> statFieldMap) {
        super();
        this.statFieldMap = statFieldMap;
    }
    
    public StatFieldRecord aggregate(StatFieldRecord sr) {
        StatFieldRecord result = null;
        if (this.statFieldMap == null) {
            return sr;
        }
        if (sr == null || sr.statFieldMap == null
                || this.statFieldMap.keySet().size() != sr.statFieldMap.keySet().size()) {
            return this;
        }
        Map<String, StatFieldObject> resultMap = new HashMap<String, StatFieldObject>();
        for (String statFieldName : this.statFieldMap.keySet()) {
            StatFieldObject curr = this.statFieldMap.get(statFieldName).doAgg(
                    sr.statFieldMap.get(statFieldName));
            resultMap.put(statFieldName, curr);
        }
        result = new StatFieldRecord(resultMap);
        return result;
    }
    
    /**
     * getter method for property statFieldMap
     * 
     * @return the statFieldMap
     */
    public Map<String, StatFieldObject> getStatFieldMap() {
        return statFieldMap;
    }
    
    /**
     * setter method for property statFieldMap
     * 
     * @param statFieldMap
     *            the statFieldMap to set
     */
    public void setStatFieldMap(Map<String, StatFieldObject> statFieldMap) {
        this.statFieldMap = statFieldMap;
    }
    
}
