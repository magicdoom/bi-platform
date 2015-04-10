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
package com.baidu.rigel.biplatform.tesseract.isservice.search.agg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryMeasure;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.Aggregate;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;

/**
 * 
 * 聚集计算
 * 
 * @author lijin
 *
 */
public class AggregateCompute {
    
    private static Logger LOGGER = LoggerFactory.getLogger(AggregateCompute.class);
    
    public static List<SearchIndexResultRecord> distinct(List<SearchIndexResultRecord> dataList){
        return dataList.stream().distinct().collect(Collectors.toList());
    }
    
    /**
     * 聚集计算
     * 
     * @param dataList
     *            待计算的数据
     * @param query
     *            原始查询请求
     * @return LinkedList<ResultRecord> 计算后的数据
     */
    public static List<SearchIndexResultRecord> aggregate(List<SearchIndexResultRecord> dataList,
        int dimSize, List<QueryMeasure> queryMeasures) {
        
        if (CollectionUtils.isEmpty(queryMeasures) || CollectionUtils.isEmpty(dataList) ||dataList.size() == 1) {
            LOGGER.info("no need to group.");
            return dataList;
        }
        List<SearchIndexResultRecord> result = new ArrayList<SearchIndexResultRecord>();
        
//        Set<Integer> countIndex = Sets.newHashSet();
//        for (int i = 0 ; i < queryMeasures.size() ; i++) {
//            if (queryMeasures.get(i).getAggregator().equals(Aggregator.COUNT)) {
//                countIndex.add(dimSize + i);
//            }
//        }
        int arraySize = dataList.get(0).getFieldArraySize();
        
        long current = System.currentTimeMillis();
        Map<String, SearchIndexResultRecord> groupResult = dataList.parallelStream().collect(
                Collectors.groupingByConcurrent(SearchIndexResultRecord::getGroupBy,
                Collectors.reducing(new SearchIndexResultRecord(new Serializable[arraySize], null), (x,y) ->{
                    SearchIndexResultRecord var = new SearchIndexResultRecord(new Serializable[arraySize], y.getGroupBy());
                    try {
                        for(int i = 0; i < dimSize; i++) {
                            var.setField(i, y.getField(i));
                        }
                        int index = dimSize;
                        for(int i = 0; i < queryMeasures.size(); i++){
                            QueryMeasure measure = queryMeasures.get(i);
                            index = i + dimSize;
                            var.setField(i+dimSize, Aggregate.aggregate(x.getField(index), y.getField(index), measure.getAggregator()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    return var;
                })
            )
        );
        LOGGER.info("group agg(sum) cost: {}ms!", (System.currentTimeMillis() - current));
        result.addAll(groupResult.values());
        
        return result;
    }

    /**
     * @param resultQ
     * @param query
     * @return
     */
    public static List<SearchIndexResultRecord> aggregate(List<SearchIndexResultRecord> resultQ, QueryRequest query) {
        if (query.getGroupBy() != null && CollectionUtils.isNotEmpty(resultQ)) {
            int dimSize = query.getSelect().getQueryProperties().size();
            List<QueryMeasure> queryMeasures = query.getSelect().getQueryMeasures();
            return aggregate(resultQ, dimSize, queryMeasures);
        } else {
            return resultQ;
        }
    }
    
}
