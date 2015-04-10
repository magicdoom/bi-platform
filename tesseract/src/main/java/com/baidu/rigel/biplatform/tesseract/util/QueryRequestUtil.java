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
package com.baidu.rigel.biplatform.tesseract.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.SqlQuery;
import com.baidu.rigel.biplatform.tesseract.isservice.search.agg.AggregateCompute;
import com.baidu.rigel.biplatform.tesseract.model.MemberNodeTree;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.Expression;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryMeasure;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryObject;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryRequest;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.Meta;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultRecord;
import com.baidu.rigel.biplatform.tesseract.resultset.isservice.SearchIndexResultSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * 
 * QueryRequestUtil
 * 
 * @author lijin
 *
 */
public class QueryRequestUtil {
    
    /**
     * SQL_STRING_FORMAT
     */
    private static final String SQL_STRING_FORMAT = "\'%s\'";
    
    private static Logger LOGGER = LoggerFactory.getLogger(QueryRequestUtil.class);
    /**
     * 
     * transQueryRequestAndList2Map:analyze andList of queryRequest ,trans
     * andList into Map<String,List<String>>
     * 
     * @param query
     *            queryRequest
     * @return Map<String,List<String>> the result map,whose key is property and
     *         value is leafvalues
     */
    private static Map<String, List<String>> transQueryRequestAndList2Map(QueryRequest query) {
        Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
        for (Expression expression : query.getWhere().getAndList()) {
            String fieldName = expression.getProperties();
            List<String> valueList = new ArrayList<String>();
            for (QueryObject qo : expression.getQueryValues()) {
                valueList.addAll(qo.getLeafValues());
            }
            resultMap.put(fieldName, valueList);
        }
        return resultMap;
    }
    
    /**
     * 
     * transQueryRequest2LeafMap: transfer QueryObject into
     * Map<String,Map<String, String>>
     * 
     * @param query
     *            query
     * @return Map<String,Map<String, String>> : key is propertie,and
     *         Map<String,String> key is leafvalue of QueryObject and value is
     *         value of QueryObject
     */
    public static Map<String, Map<String, Set<String>>> transQueryRequest2LeafMap(QueryRequest query) {
        if (query == null || query.getWhere() == null || query.getWhere().getAndList() == null) {
            throw new IllegalArgumentException();
        }
        
        Map<String, Map<String, Set<String>>> resultMap = new HashMap<String, Map<String, Set<String>>>();
        // process andList
        for (Expression ex : query.getWhere().getAndList()) {
            Map<String, Set<String>> curr = new HashMap<String, Set<String>>();
            if (resultMap.get(ex.getProperties()) != null) {
                curr = resultMap.get(ex.getProperties());
            }
            for (QueryObject qo : ex.getQueryValues()) {
                for (String leaf : qo.getLeafValues()) {
                    Set<String> valueSet = curr.get(leaf);
                    if (valueSet == null) {
                        valueSet = new HashSet<String>();
                    }
                    if(!qo.isSummary() && !StringUtils.equals(leaf, qo.getValue())){
                        valueSet.add(qo.getValue());
                    }
                    if(CollectionUtils.isNotEmpty(valueSet)){
                        curr.put(leaf, valueSet);
                    }
                }
            }
            if(query.getSelect().getQueryProperties().contains(ex.getProperties()) && !curr.isEmpty()){
                resultMap.put(ex.getProperties(), curr);
            }
        }
        
        return resultMap;
    }
    
    /**
     * 
     * transQueryRequest2LuceneQuery queryRequest->query for lucene
     * 
     * @param query
     *            queryRequest
     * @return Query query for lucene
     * @throws ParseException
     *             解析异常
     */
    public static Query transQueryRequest2LuceneQuery(QueryRequest query) throws ParseException {
        if (query == null || query.getWhere() == null) {
            throw new IllegalArgumentException();
        }
        BooleanQuery queryAll = new BooleanQuery();
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        // process where
        // process and condition
        Map<String, List<String>> andCondition = transQueryRequestAndList2Map(query);
        for (String fieldName : andCondition.keySet()) {
            //QueryParser parser = new QueryParser(fieldName, new StandardAnalyzer());
            BooleanQuery subQuery = new BooleanQuery();
            for (String qs : andCondition.get(fieldName)) {
                subQuery.add(new TermQuery(new Term(fieldName, qs)),
                        Occur.SHOULD);
            }
            queryAll.add(subQuery, Occur.MUST);
        }
        
        return queryAll;
    }
    
    /**
     * 
     * transQueryRequest2SqlQuery queryRequest->sqlQuery
     * 
     * @param query
     *            query
     * @return SqlQuery SqlQuery
     */
    public static SqlQuery transQueryRequest2SqlQuery(QueryRequest query) {
        if (query == null || query.getWhere() == null) {
            throw new IllegalArgumentException();
        }
        
        SqlQuery result = new SqlQuery();
        // 处理from
        if(query.getGroupBy()!=null){
            result.setGroupBy(query.getGroupBy().getGroups());
        }
        
        LinkedList<String> fromList = new LinkedList<String>();
        fromList.add(query.getFrom().getFrom());
        result.setFromList(fromList);
        // 处理limit
        if (query.getLimit() != null) {
            result.setLimitMap(query.getLimit().getStart(), query.getLimit().getSize());
        }
        
        // 处理select
        // getQueryProperties
        Set<String> selectList = Sets.newLinkedHashSet();
        if (query.getSelect() != null) {
            selectList.addAll(query.getSelect().getQueryProperties());
            if (CollectionUtils.isNotEmpty(query.getSelect().getQueryMeasures())) {
                for (QueryMeasure qm : query.getSelect().getQueryMeasures()) {
                    selectList.add(qm.getProperties());
                }
            }
        }
        
        // 处理where
        Map<String, List<String>> andCondition = transQueryRequestAndList2Map(query);
        List<String> whereList = new ArrayList<String>();
        for (String key : andCondition.keySet()) {
            selectList.add(key);
            StringBuilder sb = new StringBuilder();
            sb.append(key);
            sb.append(" in (");
            sb.append(StringUtils.join(transValue2SqlString(andCondition.get(key)), ","));
            sb.append(")");
            whereList.add(sb.toString());
        }
        result.setWhereList(whereList);
        result.getSelectList().addAll(selectList);
        return result;
    }
    
    /**
     * 
     * transValue2SqlString
     * 
     * @param valueList
     *            valueList
     * @return List<String>
     */
    private static List<String> transValue2SqlString(List<String> valueList) {
        List<String> result = new ArrayList<String>();
        if (valueList == null || valueList.size() == 0) {
            return result;
        }
        for (String key : valueList) {
            String sqlKey = String.format(SQL_STRING_FORMAT, key);
            result.add(sqlKey);
        }
        return result;
    }
    
//    /**
//     * 
//     * mapLeafValue2Value
//     * 
//     * @param srcResultSet
//     *            srcResultSet
//     * @param query
//     *            QueryRequest
//     * @return LinkedList<ResultRecord>
//     * @throws NoSuchFieldException
//     *             NoSuchFieldException
//     */
//    public static LinkedList<ResultRecord> mapLeafValue2Value(TesseractResultSet srcResultSet,
//            QueryRequest query) throws NoSuchFieldException {
//        TesseractResultSet dataSet = srcResultSet;
//        LinkedList<ResultRecord> transList = new LinkedList<ResultRecord>();
//        Map<String, Map<String, Set<String>>> leafValueMap = QueryRequestUtil
//            .transQueryRequest2LeafMap(query);
//        
//        if (dataSet != null && dataSet.size() != 0 && dataSet instanceof SearchResultSet) {
//            ResultRecord record = null;
//            while ((record = ((SearchResultSet) dataSet).getResultQ().poll()) != null) {
//                // 替换维度数据的明细节点的上层结点信息
//                if (!MapUtils.isEmpty(leafValueMap)) {
//                    transList.addAll(mapLeafValue2ValueOfRecord(record, leafValueMap));
//                } else {
//                    transList.add(record);
//                }
//                
//            }
//        }
//        
//        return transList;
//    }
    
    
    /** 
     * collectAllMem
     * @param queryContext
     * @return
     */
    private static Map<String, String> collectAllMem(QueryContext queryContext) {
        Map<String,String> allDimVal = new HashMap<String, String>();
        if (CollectionUtils.isNotEmpty(queryContext.getColumnMemberTrees())) {
            queryContext.getColumnMemberTrees().forEach(tree -> {
                allDimVal.putAll(coolectAllMem(tree));
            });
        }
        
        if (CollectionUtils.isNotEmpty(queryContext.getRowMemberTrees())) {
            queryContext.getRowMemberTrees().forEach(tree -> {
                allDimVal.putAll(coolectAllMem(tree));
            });
        }
        
        return allDimVal;
    }
    
    
    
    /** 
     * coolectAllMem
     * @param memberNodeTree
     * @return
     */
    
    /** 
     * coolectAllMem
     * @param memberNodeTree
     * @return
     */
    private static Map<String, String> coolectAllMem(MemberNodeTree memberNodeTree) {
        Map<String,String> allDimVal = new HashMap<String, String>();
        if (memberNodeTree.isSummary()) {
            allDimVal.put(memberNodeTree.getQuerySource(), memberNodeTree.getName());
            return allDimVal;
        } else {
            if(memberNodeTree.getChildren().size() == 1) {
                return coolectAllMem(memberNodeTree.getChildren().get(0));
            }
            return allDimVal;
        }
    }
    
    
    public static SearchIndexResultSet processGroupBy(SearchIndexResultSet dataSet,
            QueryRequest query, QueryContext queryContext) throws NoSuchFieldException {
        
        List<SearchIndexResultRecord> transList = null;
        long current = System.currentTimeMillis();
        Map<String, Map<String, Set<String>>> leafValueMap = QueryRequestUtil
            .transQueryRequest2LeafMap(query);
        Map<String,String> allDimVal = collectAllMem(queryContext);
        
        
        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " to collect leaf map.");
        current = System.currentTimeMillis();
        List<String> groupList = Lists.newArrayList(query.getGroupBy().getGroups());
        List<QueryMeasure> queryMeasures = query.getSelect().getQueryMeasures();
        // 这里开始算值都得将count改成sum了
        queryMeasures.forEach(measure -> {
            if(measure.getAggregator().equals(Aggregator.COUNT)){
                measure.setAggregator(Aggregator.SUM);
            }
        });
        Meta meta = dataSet.getMeta();
        int dimSize = query.getSelect().getQueryProperties().size();
        if (dataSet != null && dataSet.size() != 0 && dataSet instanceof SearchIndexResultSet) {
            transList = dataSet.getDataList();
        
            if(MapUtils.isNotEmpty(leafValueMap)) {
                // 如果一个叶子对应多个父节点，克隆一个再塞回去
                List<SearchIndexResultRecord> copyLeafRecords = new ArrayList<SearchIndexResultRecord>();
                transList.forEach( record -> {
                    leafValueMap.forEach((prop,valueMap) -> {
                        try {
                            String currValue = record.getField(meta.getFieldIndex(prop)) != null ? record.getField(
                                    meta.getFieldIndex(prop)).toString() : null;
                                Set<String> valueSet = leafValueMap.get(prop).get(currValue);
                                if(valueSet != null){
                                    int i = 0;
                                    for (String value : valueSet) {
                                        if(i > 0) {
                                            // 如果一个节点有多个父亲，那么在算总的汇总值得时候，会有数据问题。
                                            SearchIndexResultRecord newRec = DeepcopyUtils.deepCopy(record);
                                            newRec.setField(meta.getFieldIndex(prop), value);
                                            generateGroupBy(newRec, groupList, meta);
                                            copyLeafRecords.add(newRec);
                                        }else {
                                            record.setField(meta.getFieldIndex(prop), value);
                                            generateGroupBy(record, groupList, meta);
                                        }
                                        i++;
                                    }
                                }
                        } catch (Exception e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
                });
                if(CollectionUtils.isNotEmpty(copyLeafRecords)) {
                    // 处理汇总节点的时候，得进行下处理和过滤
                    transList.addAll(copyLeafRecords);
                }
                transList = AggregateCompute.aggregate(transList, dimSize, queryMeasures);
            }
        } else {
            return dataSet;
        }
        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " to map leaf.");
        current = System.currentTimeMillis();
        
        if(CollectionUtils.isEmpty(queryMeasures)){
            
            dataSet.setDataList(AggregateCompute.distinct(transList));
            return dataSet;
        }
        
        
        
        if(MapUtils.isNotEmpty(allDimVal)){
//            List<ResultRecord> preResultList = DeepcopyUtils.deepCopy(transList);
            for(String properties : allDimVal.keySet()){
                LinkedList<SearchIndexResultRecord> summaryCalcList = new LinkedList<SearchIndexResultRecord>();
                for(SearchIndexResultRecord record : transList){
                    SearchIndexResultRecord vRecord = DeepcopyUtils.deepCopy(record);
                    vRecord.setField(meta.getFieldIndex(properties), allDimVal.get(properties));
//                    generateGroupBy(vRecord, groupList);
                    vRecord.setGroupBy(allDimVal.get(properties));
                    summaryCalcList.add(vRecord);
                }
                transList.addAll(AggregateCompute.aggregate(summaryCalcList, dimSize, queryMeasures));
            }
        }
        dataSet.setDataList(transList);
        LOGGER.info("cost :" + (System.currentTimeMillis() - current) + " aggregator leaf.");
        return dataSet;
    }
    
//    /**
//     * 
//     * mapLeafValue2ValueOfRecord
//     * 
//     * @param record
//     *            ResultRecord
//     * @param leafValueMap
//     *            leafValueMap
//     * @return List<ResultRecord>
//     * @throws NoSuchFieldException
//     *             NoSuchFieldException
//     */
//    public static List<ResultRecord> mapLeafValue2ValueOfRecord(ResultRecord record,
//            Map<String, Map<String, Set<String>>> leafValueMap, List<String> groups) throws NoSuchFieldException {
//        //TODO 考虑将groups改成List，后续不用遍历meta中的所有直接遍历groups就可以
//        if (record == null || leafValueMap == null || leafValueMap.isEmpty()) {
//            return new ArrayList<ResultRecord>();
//        }
//        List<ResultRecord> result = new ArrayList<>();
//        
//        List<ResultRecord> tmpResult = new ArrayList<ResultRecord>();
//        for (String properties : leafValueMap.keySet()) {
//            String currValue = record.getField(properties) != null ? record.getField(
//                properties).toString() : null;
//            Set<String> valueSet = leafValueMap.get(properties).get(currValue);
//            if(valueSet != null){
//                for (String value : valueSet) {
//                    ResultRecord vRecord = DeepcopyUtils.deepCopy(record);
//                    vRecord.setField(properties, value);
//                    generateGroupBy(vRecord, groups);
//                    tmpResult.add(vRecord);
//                }
//            }
//        }
//        if(CollectionUtils.isEmpty(tmpResult)){
//            generateGroupBy(record, groups);
//            result.add(record);
//        }else{
//            result.addAll(tmpResult);
//        }
//        
//        return result;
//        
//    }
    
    public static void generateGroupBy(SearchIndexResultRecord record, List<String> groups, Meta meta) throws NoSuchFieldException{
        if(CollectionUtils.isNotEmpty(groups)){
            String groupBy = "";
            Serializable field = null;
            for(String name : meta.getFieldNameArray()){
                if(groups.contains(name)){
                    field = record.getField(meta.getFieldIndex(name));
                    if(field != null) {
                        groupBy += field.toString() + ",";
                    }
                }
            }
            record.setGroupBy(groupBy);
        }
    }
    
}
