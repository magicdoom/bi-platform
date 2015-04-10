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
package com.baidu.rigel.biplatform.tesseract.isservice.index.service.impl;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.util.AnswerCoreConstant;
import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.DataDescInfo;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexState;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.store.service.impl.AbstractMetaService;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.IndexFileSystemConstants;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * 
 * IndexMetaService实现类
 * 
 * @author lijin
 *
 */
@Service("indexMetaService")
public class IndexMetaServiceImpl extends AbstractMetaService implements IndexMetaService {
    /**
     * LOGGER
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexMetaServiceImpl.class);
    /**
     * storeManager
     */
    @Resource
    private StoreManager storeManager;
    /**
     * isNodeService
     */
    @Resource(name = "isNodeService")
    private IsNodeService isNodeService;
    
    @Value("${index.indexInterval}")
    private int indexInterval;
    
    /**
     * DEFAULT_BLOCK_COUNT，默认每次申请索引块数
     */
    private static final int DEFAULT_BLOCK_COUNT = 1;
    
    /**
     * 
     * 查找索引元数据时key的类型
     * 
     * @author lijin
     *
     */
    private enum IndexMetasKeyType {
        /**
         * FactTable
         */
        FACT_TABLE, // 事实表名
        /**
         * CUBE_NAME
         */
        CUBE_NAME, // CUBE名称
        /**
         * CUBE_ID
         */
        CUBE_ID, // CUBE的ID
        /**
         * INDEX_META_ID
         */
        INDEX_META_ID,// indexMetaId
        /**
         * PRODUCT_LINE
         */
        PRODUCT_LINE // 产品线
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService
     * #initMiniCubeIndexMeta(java.util.List,
     * com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo)
     */
    @Override
    public List<IndexMeta> initMiniCubeIndexMeta(List<Cube> cubeList, DataSourceInfo dataSourceInfo) {
        /**
         * 初始化索引元数据，设置cube的维度、指标、事实表、数据源信息，设置索引元数据状态为UN_INIT;不进行索引分片的分配
         */
        List<IndexMeta> result = new ArrayList<IndexMeta>();
        if (cubeList == null || cubeList.size() == 0 || dataSourceInfo == null) {
            LOGGER.info("cubeList or dataSourceInfo in param list is null: [cubeList]:[" + cubeList
                + "][dataSourceInfo]:[" + dataSourceInfo + "]");
            return result;
        }
        
        // 初始化时，一个Cube对应一个IndexMeta
        for (Cube cube : cubeList) {
            MiniCube currCube = (MiniCube) cube;
            
            // 事实表
            List<String> factTableList = new ArrayList<String>();
            if (currCube.isMutilple()) {
                // currCube如果是分表的，则source是以","分隔的字符串
                for (String factTable : currCube.getSource().split(
                    TesseractConstant.MINI_CUBE_MULTI_FACTTABLE_SPLITTER)) {
                    factTableList.add(factTable);
                }
                
            } else {
                factTableList.add(currCube.getSource());
            }
            
            // cube涉及到的数据的描述信息
            DataDescInfo dataDescInfo = new DataDescInfo();
            
            dataDescInfo.setProductLine(currCube.getProductLine());
            // 设置事实表所在数据源key
            dataDescInfo.setSourceName(dataSourceInfo.getDataSourceKey());
            dataDescInfo.setSplitTable(currCube.isMutilple());
            dataDescInfo.setTableName(currCube.getSource());
            dataDescInfo.setTableNameList(factTableList);
            //dataDescInfo.setIdStr(idStr);
            if(StringUtils.isEmpty(dataDescInfo.getIdStr())){
                dataDescInfo.setIdStr(IndexFileSystemConstants.FACTTABLE_KEY);
            }
            IndexMeta idxMeta = new IndexMeta();
            // 设置索引元数据基本信息
            idxMeta.setIndexMetaId(String.valueOf(UUID.randomUUID()));
            idxMeta.setProductLine(currCube.getProductLine());
            idxMeta.setDataDescInfo(dataDescInfo);
            
            idxMeta.getCubeIdSet().add(currCube.getId());
            
            // 处理维度
            Set<String> dimSet = new HashSet<String>();
            if (currCube.getDimensions() != null) {
                for (String dimKey : currCube.getDimensions().keySet()) {
                    Dimension dim = currCube.getDimensions().get(dimKey);
                    // 处理维度不同层级
                    if (dim.getLevels() != null) {
                        for (String levelKey : dim.getLevels().keySet()) {
                            Level dimLevel = dim.getLevels().get(levelKey);
                            dimSet.add(dimLevel.getFactTableColumn());
                        }
                    }
                }
            }
            idxMeta.setDimSet(dimSet);

            // 处理指标
            Set<String> measureSet = new HashSet<String>();
            if (currCube.getMeasures() != null) {
                for (String measureKey : currCube.getMeasures().keySet()) {
                    Measure measure = currCube.getMeasures().get(measureKey);
                    if (measure.getType().equals(MeasureType.COMMON)) {
                        // 普通指标，直接加入到select表列中
                        measureSet.add(measure.getDefine());
                    } else if (measure.getType().equals(MeasureType.DEFINE)) {
                        // 当前不支持
                    } else if (measure.getType().equals(MeasureType.CAL)) {
                        // 当前不支持
                    }
                }
            }
            idxMeta.setMeasureSet(measureSet);
            
            idxMeta.setReplicaNum(IndexShard.getDefaultShardReplicaNum());
            idxMeta.setDataSourceInfo(dataSourceInfo);
            idxMeta.setDataDescInfo(dataDescInfo);
            
            // 设置状态
            idxMeta.setIdxState(IndexState.INDEX_UNINIT);
            result.add(idxMeta);
        }
        LOGGER.info("Finished init MiniCube IndexMeta");
        
        return result;
    }
    
    public List<IndexMeta> getIndexMetasByDataSourceKey(String dataSourceKey) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "getIndexMetasByDataSourceKey", dataSourceKey));
        if (StringUtils.isEmpty(dataSourceKey)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "getIndexMetasByDataSourceKey", dataSourceKey));
            throw new IllegalArgumentException();
        }
        
        List<IndexMeta> metaList = super.getStoreMetaListByStoreKey(IndexMeta.getDataStoreName(),
            dataSourceKey);
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
            "getIndexMetasByDataSourceKey", dataSourceKey));
        
        return metaList;
    }
    
    /**
     * 
     * 跟据查找索引元数据时key的类型查找索引元数据
     * 
     * @param type
     *            查找索引元数据时key的类型
     * @param storeKey
     *            在索引元数据空间中的存储key
     * @return Map<String, List<IndexMeta>>
     */
    private Map<String, List<IndexMeta>> getIndexMetasByKeyType(IndexMetasKeyType type,
            String storeKey) {
        Map<String, List<IndexMeta>> result = new HashMap<String, List<IndexMeta>>();
        
        List<IndexMeta> metaList = super.getStoreMetaListByStoreKey(IndexMeta.getDataStoreName(),
            storeKey);
        
        result = getIndexMetasByKeyTypeFromMetaList(metaList, type);
        return result;
    }
    
    /**
     * 
     * 从meta列表中，跟据查找索引元数据时key的类型重新组织数据
     * 
     * @param metaList
     *            元数据列表
     * @param type
     *            查找索引元数据时key的类型
     * @return Map<String, List<IndexMeta>>
     */
    private Map<String, List<IndexMeta>> getIndexMetasByKeyTypeFromMetaList(
            List<IndexMeta> metaList, IndexMetasKeyType type) {
        Map<String, List<IndexMeta>> result = new HashMap<String, List<IndexMeta>>();
        if (metaList == null) {
            return result;
        }
        for (IndexMeta idxMeta : metaList) {
            String idxKey = null;
            if (type.equals(IndexMetasKeyType.FACT_TABLE)
                    || type.equals(IndexMetasKeyType.PRODUCT_LINE)) {
                if (type.equals(IndexMetasKeyType.FACT_TABLE)) {
                    idxKey = idxMeta.getDataDescInfo().getTableName();
                } else if (type.equals(IndexMetasKeyType.PRODUCT_LINE)) {
                    idxKey = idxMeta.getProductLine();
                }
                List<IndexMeta> idxMetaList = result.get(idxKey);
                if (idxMetaList == null) {
                    idxMetaList = new ArrayList<IndexMeta>();
                }
                idxMetaList.add(idxMeta);
                result.put(idxKey, idxMetaList);
            } else if (type.equals(IndexMetasKeyType.CUBE_NAME)) {
                Set<String> idxKeySet = idxMeta.getCubeIdSet();
                for (String key : idxKeySet) {
                    List<IndexMeta> idxMetaList = result.get(key);
                    if (idxMetaList == null) {
                        idxMetaList = new ArrayList<IndexMeta>();
                    }
                    idxMetaList.add(idxMeta);
                    result.put(key, idxMetaList);
                }
            } else if (type.equals(IndexMetasKeyType.CUBE_ID)) {
                
            }
            
        }
        return result;
    }
    
    @Override
    public List<IndexShard> getIndexShardListByProductLine(String productLineName, String storeKey) {
        if (productLineName == null || storeKey == null || productLineName.equals("")
                || storeKey.equals("")) {
            LOGGER.info("can not find Cube:[ProductLineName:" + productLineName
                + "] in Store:[StoreKey:" + storeKey + "]");
            return null;
        }
        Map<String, List<IndexMeta>> idxMetaMap = getIndexMetasByKeyType(
            IndexMetasKeyType.PRODUCT_LINE, storeKey);
        List<IndexMeta> idxMetaList = new ArrayList<IndexMeta>();
        
        if (idxMetaMap.containsKey(productLineName)) {
            idxMetaList = idxMetaMap.get(productLineName);
        } else {
            LOGGER.info("can not find indexMetas for productLineName:[" + productLineName + "]");
            return null;
        }
        List<IndexShard> idxShardList = new ArrayList<IndexShard>();
        for (IndexMeta idxMeta : idxMetaList) {
            idxShardList.addAll(idxMeta.getIdxShardList());
        }
        return idxShardList;
    }
    
    @Override
    public List<IndexMeta> getIndexMetasByFactTableName(String factTableName, String storeKey) {
        if (factTableName == null || storeKey == null || factTableName.equals("")
                || storeKey.equals("")) {
            LOGGER.info("can not find Cube:[FactTableName:" + factTableName
                + "] in Store:[StoreKey:" + storeKey + "]");
            return null;
        }
        Map<String, List<IndexMeta>> idxMetaMap = getIndexMetasByKeyType(
            IndexMetasKeyType.FACT_TABLE, storeKey);
        if (idxMetaMap.containsKey(factTableName)) {
            return idxMetaMap.get(factTableName);
        } else {
            LOGGER.info("can not find indexMetas for factTable:[" + factTableName + "]");
            return null;
        }
        
    }
    
    
    @Override
	public IndexMeta getIndexMetaByIndexMetaId(String idxMetaId, String storeKey) {
		if (idxMetaId == null || storeKey == null || idxMetaId.equals("")
				|| storeKey.equals("")) {
			LOGGER.info("can not find IndexMeta:[idxMetaId:" + idxMetaId
					+ "] in Store:[StoreKey:" + storeKey + "]");
			return null;
		}
		IndexMeta result = null;
		List<IndexMeta> metaList = super.getStoreMetaListByStoreKey(
				IndexMeta.getDataStoreName(), storeKey);

		if (!CollectionUtils.isEmpty(metaList)) {
			for (IndexMeta meta : metaList) {
				if (meta.getIndexMetaId().equals(idxMetaId)) {
					result = meta;
					break;
				}
			}
		} else {
			LOGGER.info("can not find indexMeta for IndexMetaId:[" + idxMetaId
					+ "]");
			return null;
		}
		return result;
	}
    
    @Override
    public IndexMeta getIndexMetaByCubeId(String cubeId, String storeKey) {
        if (cubeId == null || storeKey == null || cubeId.equals("") || storeKey.equals("")) {
            LOGGER.info("can not find Cube:[CubeId:" + cubeId + "] in Store:[StoreKey:" + storeKey
                + "]");
            return null;
        }
        Map<String, List<IndexMeta>> idxMetaMap = getIndexMetasByKeyType(
            IndexMetasKeyType.CUBE_NAME, storeKey);
        if (idxMetaMap != null && idxMetaMap.get(cubeId) != null
            && idxMetaMap.get(cubeId).size() > 0) {
            return idxMetaMap.get(cubeId).get(0);
        } else {
            LOGGER.info("can not find indexMeta for cube:[" + cubeId + "]");
            return null;
        }
        
    }
    
    @Override
    public boolean saveOrUpdateIndexMeta(IndexMeta idxMeta) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
            "saveOrUpdateIndexMeta", "[indexMeta:" + idxMeta + "]"));
        if (idxMeta == null) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "saveOrUpdateIndexMeta", "[indexMeta:" + idxMeta + "]"));
            throw new IllegalArgumentException();
        }
        long idxVersion = System.currentTimeMillis();
        idxMeta.setIdxVersion(idxVersion);
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
            "saveOrUpdateIndexMeta", "[indexMeta:" + idxMeta + "]",
            "set index version as currentTimeMillis"));
        if (idxMeta.getIdxShardList() != null) {
            for (IndexShard idxShard : idxMeta.getIdxShardList()) {
                idxShard.setIdxVersion(idxMeta.getIdxVersion());
                
                LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
                    "saveOrUpdateIndexMeta", "[indexMeta:" + idxMeta + "][idxShard:"+idxShard.getShardName()+"]", "saving node"));
                
            }
        }
        boolean result = super.saveOrUpdateMetaStore(idxMeta, IndexMeta.getDataStoreName());
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
            "saveOrUpdateIndexMeta", "[indexMeta:" + idxMeta + "]"));
        return result;
    }
    
    /**
     * 
     * 索引元数据相似度模型
     * 
     * @author lijin
     *
     */
    @SuppressWarnings("rawtypes")
    private class IndexMetaSimilarityScore implements Comparable {
        /**
         * 维度评分
         */
        private int dimScore;
        /**
         * 指标评分
         */
        private int measureScore;
        /**
         * 总分
         */
        private int totalScore;
        
        public IndexMetaSimilarityScore() {
            totalScore = 0;
            dimScore = 0;
            measureScore = 0;
        }
        
        public IndexMetaSimilarityScore(int dimScore, int measureScore) {
            this.dimScore = dimScore;
            this.measureScore = measureScore;
            totalScore = this.dimScore * 10 + this.measureScore;
        }
        
        public int getDimScore() {
            return dimScore;
        }
        
        public int getTotalScore() {
            totalScore = this.dimScore * 10 + this.measureScore;
            return totalScore;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj != null && (obj instanceof IndexMetaSimilarityScore)) {
                IndexMetaSimilarityScore sobj = (IndexMetaSimilarityScore) obj;
                if ((this.dimScore == sobj.dimScore) && (this.measureScore == sobj.measureScore)
                        && (this.totalScore == sobj.totalScore)) {
                    return true;
                }
            }
            return false;
        }
        
        @Override
        public int compareTo(Object o) {
            if (this.equals(o)) {
                return 0;
            } else {
                if (o != null && (o instanceof IndexMetaSimilarityScore)) {
                    IndexMetaSimilarityScore so = (IndexMetaSimilarityScore) o;
                    if (this.dimScore > so.dimScore) {
                        return 1;
                    } else if ((this.dimScore == so.dimScore) && (this.totalScore > so.totalScore)) {
                        return 1;
                    }
                }
            }
            return -1;
        }
        
    }
    
    /**
     * 
     * 对索引元数据列表idxMetaList中的每个索引元数据与idxMeta做相似度计算
     * 
     * @param idxMetaList
     *            索引元数据列表
     * @param idxMeta
     *            索引元数据
     * @return Map<IndexMetaSimilarityScore, List<IndexMeta>>
     */
    private Map<IndexMetaSimilarityScore, List<IndexMeta>> getMostSimilarIndexMeta(
        List<IndexMeta> idxMetaList, IndexMeta idxMeta) {
        // 相似度打分模型定义：有一个维度/指标相同，计一分；满分=维度数+指标数
        List<IndexMeta> mostSimilarIndexMetaList = new ArrayList<IndexMeta>();
        IndexMetaSimilarityScore maxSimilarity = new IndexMetaSimilarityScore();
        for (IndexMeta currIdxMeta : idxMetaList) {
            IndexMetaSimilarityScore currScore = getSimilarityOfIndexMeta(idxMeta, currIdxMeta);
            if (maxSimilarity.compareTo(currScore) < 0) {
                mostSimilarIndexMetaList.clear();
                mostSimilarIndexMetaList.add(currIdxMeta);
                maxSimilarity = currScore;
            } else if (maxSimilarity.equals(currScore)) {
                mostSimilarIndexMetaList.add(currIdxMeta);
            }
        }
        Map<IndexMetaSimilarityScore, List<IndexMeta>> result = new HashMap<IndexMetaSimilarityScore, List<IndexMeta>>();
        if (maxSimilarity.getTotalScore() > 0) {
            result.put(maxSimilarity, mostSimilarIndexMetaList);
        }
        
        return result;
    }
    
    /**
     * 
     * 计算两个索引元数据的相似度
     * 
     * @param idxMeta1
     *            索引元数据1
     * @param idxMeta2
     *            索引元数据2
     * @return IndexMetaSimilarityScore 相似度
     */
    private IndexMetaSimilarityScore getSimilarityOfIndexMeta(IndexMeta idxMeta1, IndexMeta idxMeta2) {
        int dimScore = 0;
        int measureScore = 0;
        if (idxMeta1 == null
            || idxMeta2 == null
            || !idxMeta1.getDataSourceInfo().getDataSourceKey()
                .equals(idxMeta2.getDataSourceInfo().getDataSourceKey())) {
            
            return new IndexMetaSimilarityScore();
        }
        if (idxMeta1 != null && idxMeta2 != null) {
            // 维度信息
            Set<String> dimInfoSet1 = idxMeta1.getDimSet();
            Set<String> dimInfoSet2 = idxMeta2.getDimSet();
            Collection<String> dimInfoIntersection = getIntersectionOf2Collection(dimInfoSet1,dimInfoSet2);
            if (dimInfoIntersection != null) {
                dimScore += dimInfoIntersection.size();
            }
            // 指标信息
            Set<String> measureSet1 = idxMeta1.getMeasureSet();
            Set<String> measureSet2 = idxMeta2.getMeasureSet();
            Collection<String> measureIntersection = getIntersectionOf2Collection(
                    measureSet1, measureSet2);
            if (measureIntersection != null) {
                measureScore += measureIntersection.size();
            }
        }
        return new IndexMetaSimilarityScore(dimScore, measureScore);
        
    }
    
    /**
     * 
     * 计算两个集合的交集
     * 
     * @param col1
     *            集合1
     * @param col2
     *            集合2
     * @return Collection<T> 交集
     */
    private <T> Collection<T> getIntersectionOf2Collection(Collection<T> col1, Collection<T> col2) {
        List<T> result = new ArrayList<T>();
        if (col1 != null && col2 != null) {
            for (T dimInfo : col1) {
                if (col2.contains(dimInfo)) {
                    result.add(dimInfo);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 
     * 从当前的索引分片信息中获取所在的机器节点信息
     * 
     * @param idxShardList
     *            索引分片列表
     * @param clusterName
     *            集群名称
     * @return List<Node> 机器节点信息
     */
    private List<Node> getNodeListForExistIndexShard(List<IndexShard> idxShardList,String clusterName) {
        List<Node> nodeList = new ArrayList<Node>();
        if (idxShardList != null && !StringUtils.isEmpty(clusterName)) {
            for (IndexShard idxShard : idxShardList) {
                List<Node> idxShardNodeList=this.isNodeService.getAvailableNodeListByIndexShard(idxShard, clusterName);
                if(!CollectionUtils.isEmpty(idxShardNodeList)){
                	nodeList.addAll(idxShardNodeList);
                }
            }
        }
        return nodeList;
        
    }
    
    /**
     * 
     * 合并索引元数据
     * 
     * @param idxMeta
     *            索引元数据
     * @return IndexMeta
     */
    @Override
    public IndexMeta mergeIndexMeta(IndexMeta indexMeta) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "mergeIndexMeta",
            "[IndexMeta:" + indexMeta + "]"));
        IndexMeta idxMeta = this.getIndexMetaByIndexMetaId(indexMeta.getIndexMetaId(), indexMeta.getStoreKey());
        if(idxMeta==null){
        	idxMeta=indexMeta;
        }
        
        if (idxMeta == null || idxMeta.getStoreKey().equals("")) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "mergeIndexMeta", "[IndexMeta:" + indexMeta + "]"));
            throw new IllegalArgumentException();
        }
        
        // s1 get all the index meta with the same facttable of idxMeta
        Map<String, List<IndexMeta>> factTableIdxMetaMap = this.getIndexMetasByKeyType(
            IndexMetasKeyType.FACT_TABLE, idxMeta.getStoreKey());
        
        if (factTableIdxMetaMap != null
                && factTableIdxMetaMap.containsKey(idxMeta.getDataDescInfo().getTableName())) {
            // 当前idxMeta的事实表已经有对应的索引
            // 跟据索引情况判断是否需要合并索引
            List<IndexMeta> currFactTableIdxMetaList = factTableIdxMetaMap.get(idxMeta
                .getDataDescInfo().getTableName());
            /**
             * 合并策略： 0.维度、指标完全相同，可以复用 1.维度相同，指标不同，可以合并
             */
            
            // s2 calculate score of idxMeta
            IndexMetaSimilarityScore mainScore = new IndexMetaSimilarityScore(idxMeta.getDimSet().size(), idxMeta.getMeasureSet().size());
            // s3 calculate the most similar indexMeta with curr idxMeta
            Map<IndexMetaSimilarityScore, List<IndexMeta>> mostSimilarIndexMetaMap = getMostSimilarIndexMeta(
                currFactTableIdxMetaList, idxMeta);
            // s4 apply merge strategy
            if (mostSimilarIndexMetaMap != null && mostSimilarIndexMetaMap.size() != 0) {
                for (IndexMetaSimilarityScore mScore : mostSimilarIndexMetaMap.keySet()) {
                    if (mScore.compareTo(mainScore) == 0) {
                        // 合并策略：0.维度、指标完全相同，直接复用
                        IndexMeta currIdxMeta = mostSimilarIndexMetaMap.get(mScore).get(0);
                        currIdxMeta.getCubeIdSet().addAll(idxMeta.getCubeIdSet());
                        if (currIdxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE)) {
                            currIdxMeta.setIdxState(IndexState.INDEX_AVAILABLE_MERGE);
                        }
                        
                        idxMeta = currIdxMeta;
                    } else if (mScore.compareTo(mainScore) < 0
                        && (mScore.getDimScore() == mainScore.getDimScore())) {
                        // 合并策略：1.维度相同，指标不同，合并后复用
                        IndexMeta currIdxMeta = mostSimilarIndexMetaMap.get(mScore).get(0);
                        currIdxMeta.getCubeIdMergeSet().addAll(idxMeta.getCubeIdSet());
                        currIdxMeta.getMeasureInfoMergeSet().addAll(idxMeta.getMeasureSet());
                        // 设置currIdxMeta的状态为需要合并
                        currIdxMeta.setIdxState(IndexState.INDEX_AVAILABLE_NEEDMERGE);
                        idxMeta = currIdxMeta;
                    }// --暂不支持。2.维度相差<=2个，规模在可接受范围内，可以合并；否则不能合并；
                }
            }
        }
        
        
        if((idxMeta.getLocked().equals(Boolean.FALSE)) || ((System.currentTimeMillis()-idxMeta.getIdxVersion()) > this.indexInterval)){
        	idxMeta.setLocked(Boolean.FALSE);
        	
        }
        // 更新索引元数据
        this.saveOrUpdateIndexMeta(idxMeta);
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "mergeIndexMeta",
            "[IndexMeta:" + indexMeta + "]"));
        return idxMeta;
    }
    
    @Override
    public IndexMeta assignIndexShard(IndexMeta idxMeta, String clusterName) {
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "assignIndexShard",
            "[idxMeta:" + idxMeta + "][clusterName:" + clusterName + "]"));
        
        if (idxMeta == null || idxMeta.getStoreKey().equals("") || StringUtils.isEmpty(clusterName)) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "assignIndexShard", "[idxMeta:" + idxMeta + "][clusterName:" + clusterName + "]"));
            throw new IllegalArgumentException();
        }
        
        // 如果当前索引元数据没有索引分片或者已有的分片已满， 分配索引分片
        if (idxMeta.getIdxShardList() == null || idxMeta.getIdxShardList().size() == 0
                || isIndexShardFull(idxMeta)) {
        	Map<Node, Integer> assignedNodeMap = new HashMap<Node, Integer>();
            /*
            // 当前产品线的索引列表
            List<IndexShard> idxShardList = getIndexShardListByProductLine(
                idxMeta.getProductLine(), idxMeta.getStoreKey());
            
            // 当前产品线的索引分片所在的结点列表
            List<Node> idxShardNodeList = this.getNodeListForExistIndexShard(idxShardList,idxMeta.getClusterName());
            
            
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "assignIndexShard", "assign node begin"));
            Map<Node, Integer> assignedNodeMap = new HashMap<Node, Integer>();
            if (idxShardNodeList == null || idxShardNodeList.size() == 0) {
                assignedNodeMap = this.isNodeService.assignFreeNode(DEFAULT_BLOCK_COUNT,
                    clusterName);
            } else {
                assignedNodeMap = this.isNodeService.assignFreeNodeByNodeList(idxShardNodeList,
                    DEFAULT_BLOCK_COUNT, clusterName);
            }
            */
            
            
            assignedNodeMap = this.isNodeService.assignFreeNode(DEFAULT_BLOCK_COUNT,
                    clusterName);
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "assignIndexShard", "assign node end"));
            
            // 拼indexShard的分片名前缀：indexMetaId+"_shard"+shardId
            StringBuffer sb = new StringBuffer();
            //sb.append(idxMeta.getDataSourceInfo().getDataSourceKey());
            sb.append(idxMeta.getIndexMetaId());
            sb.append("_");
//            if (!idxMeta.getDataDescInfo().isSplitTable()) {
//                sb.append(idxMeta.getDataDescInfo().getTableName());
//            } else {
//                // FIXME Jin 临时写法，后续要改
//                sb.append(idxMeta.getDataDescInfo().getTableName().split("_")[0]);
//            }
//            sb.append("_");
            sb.append("shard");
            sb.append("_");
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "assignIndexShard", "prefix of shardname:" + sb.toString()));
            
            // 获取 shardId，shardId是以事实表为基础进行分配，有可能同一事实表的多个Indexmeta中的索引分片的ID是连续的
            Long shardId = 0L;
//            List<IndexMeta> idxMetaList = this.getIndexMetasByFactTableName(
//                idxMeta.getFacttableName(), idxMeta.getStoreKey());
            List<IndexMeta> idxMetaList=new ArrayList<IndexMeta>();
            idxMetaList.add(idxMeta);
            
            if (idxMetaList != null && idxMetaList.size() > 0 && !CollectionUtils.isEmpty(idxMeta.getIdxShardList())) {               
                List<IndexShard> tmpList= getIndexShardListFromIndexMetaListOrderbyShardId(idxMetaList);
                shardId=tmpList.get(tmpList.size()-1).getShardId();
                shardId++;
            }
            
            List<IndexShard> assignIndexShardList = new ArrayList<IndexShard>();
            for (Node node : assignedNodeMap.keySet()) {
                int currNodeShardNum = assignedNodeMap.get(node);
                for (int i = 0; i < currNodeShardNum; i++) {
                    IndexShard idxShard = new IndexShard(sb.toString() + shardId.toString(), node);
                    idxShard.setShardId(shardId);
                    idxShard.setDiskSize(0);
                    idxShard.setFull(false);
                    idxShard.setClusterName(clusterName);
                    // 设置索引文件路径=datasourceinfo.getKey+"/"+facttablename+shardname
                    // 只设置一次
                    String idxFilePathPrefix = idxMeta.getIndexMetaFileDirPath();
                    idxShard.setFilePath(idxFilePathPrefix + idxShard.getShardName()+File.separator+IndexMeta.getIndexFilePathUpdate());
                    idxShard.setIdxFilePath(idxFilePathPrefix + idxShard.getShardName()+File.separator+IndexMeta.getIndexFilePathIndex());
                    
                    assignIndexShardList.add(idxShard);
                    
                    shardId = shardId + 1;
                }
            }
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
                "assignIndexShard", " " + assignIndexShardList.size()
                    + " idxShards assigned success "));
            idxMeta.getIdxShardList().addAll(assignIndexShardList);
            
        }
        // 第一次申请索引分片时获取集群名称
        if (StringUtils.isEmpty(idxMeta.getClusterName())) {
            idxMeta.setClusterName(clusterName);
        }
        // 更新索引元数据
        this.saveOrUpdateIndexMeta(idxMeta);
        
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "assignIndexShard",
            "[idxMeta:" + idxMeta + "]"));
        return idxMeta;
        
    }
    
    public boolean isIndexShardFull(IndexMeta idxMeta) {
        boolean isFull = false;
        if (idxMeta != null && idxMeta.getIdxShardList() != null) {
            int i = 0;
            for (; i < idxMeta.getIdxShardList().size(); i++) {
                if (!idxMeta.getIdxShardList().get(i).isFull()) {
                    break;
                }
            }
            if (i >= idxMeta.getIdxShardList().size()) {
                isFull = true;
            }
        }
        return isFull;
    }
    
    /**
     * 
     * 从索引元数据列表中得到有序的索引分片
     * 
     * @param idxMetaList
     *            索引元数据列表
     * @return List<IndexShard> 索引分片
     */
    private List<IndexShard> getIndexShardListFromIndexMetaListOrderbyShardId(
        List<IndexMeta> idxMetaList) {
        List<IndexShard> result = new ArrayList<IndexShard>();
        if (idxMetaList != null && idxMetaList.size() != 0) {
            for (IndexMeta idxMeta : idxMetaList) {
                result.addAll(idxMeta.getIdxShardList());
            }
        }
        result.sort((o1, o2) -> {
            if (o1 != null && o2 != null && o1.getShardId() != null && o2.getShardId() != null) {
                return o1.getShardId().compareTo(o2.getShardId());
            } else if (o1 != null && o2 != null && o1.getShardId() != null
                && o2.getShardId() == null) {
                return 1;
            } else if (o1 != null && o2 != null && o1.getShardId() == null
                && o2.getShardId() != null) {
                return -1;
            } else if (o1 == null || o2 == null) {
                return 0;
            }
            return 0;
        });
        
        return result;
    }
    
    public void saveIndexMetaImage(Node node) {
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "saveNodeImage",
            "[node:" + node + "]"));
        if (node == null || StringUtils.isEmpty(node.getClusterName())) {
            LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
                "saveNodeImage", "[node:" + node + "]"));
            throw new IllegalArgumentException();
        }
        FileUtils.write(node.getImageFilePath(), SerializationUtils.serialize(node), true);
        LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "saveNodeImage",
            "[node:" + node + "]"));
    }

	/* (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService#saveIndexMetaLocally(com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta)
	 */
	@Override
	public boolean saveIndexMetaLocally(IndexMeta idxMeta) throws Exception {
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "saveIndexMetaLocally",
	            "[idxMeta:" + idxMeta + "]"));
		if(idxMeta == null ){
			LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_ERROR, "saveIndexMetaLocally",
		            "[idxMeta:" + idxMeta + "]"));
			return false;
		}
		Node currNode=this.isNodeService.getCurrentNode();
		File idxMetaFileDir=new File(currNode.getIndexBaseDir()+idxMeta.getIndexMetaFileDirPath());
		if(idxMetaFileDir.isDirectory() && idxMetaFileDir.exists()){
			//如果是目录并且存在
			String idxMetaImageNewFileName=idxMetaFileDir+File.separator+idxMeta.getIndexMetaId() + IndexFileSystemConstants.INDEX_META_IMAGE_FILE_NEW;
			String idxMetaImageBakFileName=idxMetaFileDir+File.separator+idxMeta.getIndexMetaId() + IndexFileSystemConstants.INDEX_META_IMAGE_FILE_BAK;
			String idxMetaImageFileName=idxMetaFileDir+File.separator+idxMeta.getIndexMetaId() + IndexFileSystemConstants.INDEX_META_IMAGE_FILE_SAVED;
			String idxMetaStr=AnswerCoreConstant.GSON.toJson(idxMeta, new TypeToken<IndexMeta>(){}.getType());
			
			File idxMetaImageNewFile=new File(idxMetaImageNewFileName);
			File idxMetaImageBakFile=new File(idxMetaImageBakFileName);
			File idxMetaImageFile=new File(idxMetaImageFileName);
			
			if(idxMetaImageNewFile.exists()){
				idxMetaImageNewFile.deleteOnExit();
			}
			
			
			Boolean result=FileUtils.write(idxMetaImageNewFileName, idxMetaStr.getBytes(), Boolean.TRUE);
			if(result){
				
				if(idxMetaImageBakFile.exists()){
					idxMetaImageBakFile.deleteOnExit();
				}
				if(idxMetaImageFile.exists()){
					FileUtils.copyFile(idxMetaImageFile, idxMetaImageBakFile);
					LOGGER.info(String
							.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
									"saveIndexMetaLocally",
									"copyFile("
											+ idxMetaImageFile
													.getAbsolutePath()
											+ " --> "
											+ idxMetaImageBakFile
													.getAbsolutePath() + ")"));
					idxMetaImageFile.deleteOnExit();
					LOGGER.info(String
							.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
									"saveIndexMetaLocally",
									"deleteFile("
											+ idxMetaImageFile
													.getAbsolutePath()
											+ ")"));
				}				
				
				FileUtils.copyFile(idxMetaImageNewFile, idxMetaImageFile);
				LOGGER.info(String
						.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
								"saveIndexMetaLocally",
								"copyFile("
										+ idxMetaImageNewFile
												.getAbsolutePath()
										+ " --> "
										+ idxMetaImageFile
												.getAbsolutePath() + ")"));
				
			}
		}else{
			LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_ERROR, "saveIndexMetaLocally",
		            "[idxMeta:" + idxMeta + "][idxMetaFileDir:"+idxMetaFileDir.getAbsolutePath()+" does not exist]"));
		}
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "saveIndexMetaLocally",
	            "[idxMeta:" + idxMeta + "]"));
		return false;
	}

	/* (non-Javadoc)
	 * @see com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService#loadIndexMetasLocalImage(java.lang.String, java.lang.String)
	 */
	@Override
	public List<IndexMeta> loadIndexMetasLocalImage(String idxBaseDir, String currNodeKey, String clusterName) {
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "loadIndexMetasLocalImage",
	            "[idxBaseDir:" + idxBaseDir + "][currNodeKey:"+currNodeKey+"][clusterName:"+clusterName+"]"));
		List<IndexMeta> result=new ArrayList<IndexMeta>();
		if(!StringUtils.isEmpty(idxBaseDir) && !StringUtils.isEmpty(currNodeKey)){
			File idxBaseDirFile=new File(idxBaseDir);
			if(FileUtils.isEmptyDir(idxBaseDirFile)){
				return null;
			}else if (FileUtils.isExistGivingFileSuffix(idxBaseDirFile, IndexFileSystemConstants.INDEX_META_IMAGE_FILE_SAVED)){
				
				String[] fileNames=idxBaseDirFile.list(new FileUtils.LocalImageFilenameFilter(IndexFileSystemConstants.INDEX_META_IMAGE_FILE_SAVED));
				for(String filePath:fileNames){
					byte[] fileByte=FileUtils.readFile(idxBaseDir+File.separator+filePath);
					
					String fileStr=new String(fileByte);
					StringReader sr=new StringReader(fileStr);
					JsonReader jr=new JsonReader(sr);
					jr.setLenient(true);
					IndexMeta currMeta=AnswerCoreConstant.GSON.fromJson(jr,  new TypeToken<IndexMeta>(){}.getType());
					
					//设置currNodeKey
					for(IndexShard idxShard:currMeta.getIdxShardList()){
						idxShard.setNodeKey(currNodeKey);
						idxShard.setClusterName(clusterName);
						idxShard.getReplicaNodeKeyList().clear();						
					}
					currMeta.setClusterName(clusterName);
					
					result.add(currMeta);
				}
			}else {
				for(String currDsDir:idxBaseDirFile.list()){
					//遍历每个数据源，查看索引数据
					List<IndexMeta> tmpResult=loadIndexMetasLocalImage(idxBaseDir+File.separator+currDsDir,currNodeKey,clusterName);
					if(CollectionUtils.isNotEmpty(tmpResult)){
						result.addAll(tmpResult);
					}
				}
			}
		}
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "loadIndexMetasLocalImage",
	            "[idxBaseDir:" + idxBaseDir + "][currNodeKey:"+currNodeKey+"][clusterName:"+clusterName+"][result.size:"+result.size()+"]"));
		return result;
	}
	
	@Override
	public void recoverLocalIndexMetaWithCluster(List<IndexMeta> idxMetaList,String clusterName){
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN, "recoverLocalIndexMetaWithCluster",
	            "[idxMetaList.size:"+idxMetaList+"][clusterName:"+clusterName+"]"));
		if(!CollectionUtils.isEmpty(idxMetaList) && !StringUtils.isEmpty(clusterName)){
			for(IndexMeta idxMeta:idxMetaList){
				IndexMeta remoteMeta=this.getIndexMetaByCubeId(idxMeta.getCubeIdSet().toArray(new String[0])[0], IndexMeta.getDataStoreName());				
				if(remoteMeta==null){
					//1. 集群中不存在，则直接恢复
					this.saveOrUpdateIndexMeta(idxMeta);
					LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM, "recoverLocalIndexMetaWithCluster",
				            "can not find idxMetaId:"+idxMeta.getIndexMetaId()+" from cluster,save it directly"));
				}else{
					//2. 集群中存在，合并索引分片
					for(IndexShard localIndexShard:idxMeta.getIdxShardList()){
						if(remoteMeta.getIdxShardList().contains(localIndexShard)){
							//2.1 remoteMeta中存在该分片，则将该分片的nodeKey加到remoteMeta对应分片的机器列表中
							int idx=remoteMeta.getIdxShardList().indexOf(localIndexShard);
							IndexShard remoteShard=remoteMeta.getIdxShardList().get(idx);
							remoteShard.getReplicaNodeKeyList().add(localIndexShard.getNodeKey());
						}else{
							//2.2 remoteMeta中不存在该分片，则将该分片直接加到remoteMeta的分片列表中
							remoteMeta.getIdxShardList().add(localIndexShard);
						}
					}
					this.saveOrUpdateIndexMeta(remoteMeta);
					LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM, "recoverLocalIndexMetaWithCluster",
				            "merge remoteMeta with local,save it"));
				}
			}
		}
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END, "recoverLocalIndexMetaWithCluster",
	            "[idxMetaList.size:"+idxMetaList+"][clusterName:"+clusterName+"]"));
	}
    
	
	
    
    
}
