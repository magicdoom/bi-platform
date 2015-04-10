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
package com.baidu.rigel.biplatform.tesseract.isservice.index.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.cache.StoreManager;
import com.baidu.rigel.biplatform.tesseract.dataquery.service.DataQueryService;
import com.baidu.rigel.biplatform.tesseract.datasource.DataSourcePoolService;
import com.baidu.rigel.biplatform.tesseract.datasource.impl.SqlDataSourceWrap;
import com.baidu.rigel.biplatform.tesseract.exception.DataSourceException;
import com.baidu.rigel.biplatform.tesseract.isservice.event.IndexMetaWriteImageEvent;
import com.baidu.rigel.biplatform.tesseract.isservice.event.IndexUpdateEvent;
import com.baidu.rigel.biplatform.tesseract.isservice.event.IndexUpdateEvent.IndexUpdateInfo;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchException;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexAndSearchExceptionType;
import com.baidu.rigel.biplatform.tesseract.isservice.exception.IndexMetaIsNullException;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexMetaService;
import com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexAction;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexMeta;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexShard;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.IndexState;
import com.baidu.rigel.biplatform.tesseract.isservice.meta.SqlQuery;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.IndexMessage;
import com.baidu.rigel.biplatform.tesseract.netty.message.isservice.ServerFeedbackMessage;
import com.baidu.rigel.biplatform.tesseract.node.meta.Node;
import com.baidu.rigel.biplatform.tesseract.node.meta.NodeState;
import com.baidu.rigel.biplatform.tesseract.node.service.IndexAndSearchClient;
import com.baidu.rigel.biplatform.tesseract.node.service.IsNodeService;
import com.baidu.rigel.biplatform.tesseract.resultset.TesseractResultSet;
import com.baidu.rigel.biplatform.tesseract.util.FileUtils;
import com.baidu.rigel.biplatform.tesseract.util.IndexFileSystemConstants;
import com.baidu.rigel.biplatform.tesseract.util.TesseractConstant;
import com.baidu.rigel.biplatform.tesseract.util.TesseractExceptionUtils;
import com.baidu.rigel.biplatform.tesseract.util.isservice.LogInfoConstants;

/**
 * IndexService 实现类
 * 
 * @author lijin
 *
 */
@Service("indexService")

public class IndexServiceImpl implements IndexService {
	/**
	 * LOGGER
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(IndexServiceImpl.class);
	/**
	 * LOGGER
	 */
	private static final String RESULT_KEY_DATA = "RESULT_KEY_DATA";
	/**
	 * RESULT_KEY_INDEXSHARD
	 */
	private static final String RESULT_KEY_INDEXSHARD = "RESULT_KEY_INDEXSHARD";

	/**
	 * RESULT_KEY_MAXID
	 */
	private static final String RESULT_KEY_MAXID = "RESULT_KEY_MAXID";
	
	@Value("${index.indexInterval}")
    private int indexInterval;

	/**
	 * indexMetaService
	 */
	@Resource
	private IndexMetaService indexMetaService;

	@Resource
	private IsNodeService isNodeService;

	@Resource
	private StoreManager storeManager;

	@Resource
	private DataSourcePoolService dataSourcePoolService;

	/**
	 * dataQueryService
	 */
	@Resource(name = "sqlDataQueryService")
	private DataQueryService dataQueryService;

	/**
	 * dataQueryService
	 */

	private IndexAndSearchClient isClient;

	/**
	 * Constructor by no param
	 */
	public IndexServiceImpl() {
		super();
		this.isClient = IndexAndSearchClient.getNodeClient();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.baidu.rigel.biplatform.tesseract.isservice.index.service.IndexService
	 * #initMiniCubeIndex(java.util.List,
	 * com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo, boolean,
	 * boolean)
	 */
	@Override
	public boolean initMiniCubeIndex(List<Cube> cubeList,
			DataSourceInfo dataSourceInfo, boolean indexAsap, boolean limited)
			throws IndexAndSearchException {
		/**
		 * 当通过MiniCubeConnection.publishCubes(List<String> cubes, DataSourceInfo
		 * dataSourceInfo);通知索引服务端建立索引数据
		 */
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"initMiniCubeIndex", "[cubeList:" + cubeList
						+ "][dataSourceInfo:" + dataSourceInfo + "][indexAsap:"
						+ indexAsap + "][limited:" + limited + "]"));

		// step 1 process cubeList and fill indexMeta infomation
		List<IndexMeta> idxMetaList = this.indexMetaService
				.initMiniCubeIndexMeta(cubeList, dataSourceInfo);

		if (idxMetaList.size() == 0) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"initMiniCubeIndex", "[cubeList:" + cubeList
							+ "][dataSourceInfo:" + dataSourceInfo
							+ "][indexAsap:" + indexAsap + "][limited:"
							+ limited + "]", "Init MiniCube IndexMeta failed"));
			return false;
		} else {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"initMiniCubeIndex", "Success init " + idxMetaList.size()
							+ " MiniCube"));
		}

		// step 2 merge indexMeta with exist indexMetas and update indexMeta
		LOGGER.info(String.format(
				LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
				"initMiniCubeIndex", "Merging IndexMeta with exist indexMetas"));

		LinkedList<IndexMeta> idxMetaListForIndex = new LinkedList<IndexMeta>();
		for (IndexMeta idxMeta : idxMetaList) {
			idxMeta = this.indexMetaService.mergeIndexMeta(idxMeta);

			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"initMiniCubeIndex",
					"Merge indexMeta success. After merge:["
							+ idxMeta.toString() + "]"));

			idxMetaListForIndex.add(idxMeta);
		}

		// step 3 if(indexAsap) then call doIndex else return

		if (indexAsap) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"initMiniCubeIndex", "index as soon as possible"));
			// if need index as soon as possible
			IndexAction idxAction = IndexAction.INDEX_INIT;
			if (limited) {
				idxAction = IndexAction.INDEX_INIT_LIMITED;
			}
			while (idxMetaListForIndex.size() > 0) {
				IndexMeta idxMeta = idxMetaListForIndex.poll();
				if (idxMeta.getIdxState().equals(
						IndexState.INDEX_AVAILABLE_MERGE)) {
					idxMeta.setIdxState(IndexState.INDEX_AVAILABLE);
					this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);
					continue;
				} else if (idxMeta.getIdxState().equals(
						IndexState.INDEX_AVAILABLE_NEEDMERGE)) {
					idxAction = IndexAction.INDEX_MERGE;
				}

				try {
					doIndexByIndexAction(idxMeta, idxAction, null);
					
				} catch (Exception e) {
					LOGGER.error(String.format(
							LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
							"initMiniCubeIndex", "[cubeList:" + cubeList
									+ "][dataSourceInfo:" + dataSourceInfo
									+ "][indexAsap:" + indexAsap + "][limited:"
									+ limited + "]"), e);

					String message = TesseractExceptionUtils
							.getExceptionMessage(
									IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
									IndexAndSearchExceptionType.INDEX_EXCEPTION);
					throw new IndexAndSearchException(message, e,
							IndexAndSearchExceptionType.INDEX_EXCEPTION);
				} finally {
					LOGGER.info(String
							.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
									"initMiniCubeIndex", "[Index indexmeta : "
											+ idxMeta.toString()));
				}
			}
		}
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"initMiniCubeIndex", "[cubeList:" + cubeList
						+ "][dataSourceInfo:" + dataSourceInfo + "][indexAsap:"
						+ indexAsap + "][limited:" + limited + "]"));
		return true;
	}

	/**
	 * publishIndexUpdateEvent 发布索引更新事件，通知集群中的各节点刷新自己的Searcher
	 * 
	 * @param metaList
	 *            索引元数据列表
	 * @throws Exception
	 *             可能抛出异常
	 */
	private void publishIndexUpdateEvent(List<IndexMeta> metaList) throws Exception {
		if (metaList != null && metaList.size() > 0) {
			List<String> idxServiceList = new ArrayList<String>();
			List<String> idxNoServiceList = new ArrayList<String>();
			for (IndexMeta meta : metaList) {
				for (IndexShard idxShard : meta.getIdxShardList()) {
					/**
					 * TODO 发出的更新事件中，索引的路径不该有机器节点的indexbase信息，应该是由各机器收到消息后，在自己的索引数据目录内进行匹配
					 */
					idxServiceList.add(idxShard.getIdxFilePath());
					idxNoServiceList.add(idxShard.getFilePath());
				}
			}
			IndexUpdateInfo udpateInfo = new IndexUpdateInfo(idxServiceList,
					idxNoServiceList);
			IndexUpdateEvent updateEvent = new IndexUpdateEvent(udpateInfo);
			this.storeManager.postEvent(updateEvent);
		}
	}
	
	/**
	 * publistIndexMetaWriteEvent 发布索引元数据保存事件，通知集群中的各节点保存相关的元数据镜像
	 * @param metaList
	 * @throws Exception
	 */
	private void publistIndexMetaWriteEvent(IndexMeta meta) throws Exception{
		if(meta!=null){
			IndexMetaWriteImageEvent event=new IndexMetaWriteImageEvent(meta);
			LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
					"updateIndexByDataSourceKey", "post IndexMetaWriteImageEvent with IndexMetaId:"+meta.getIndexMetaId()));
			this.storeManager.postEvent(event);
		}
	}

	@Override
	public void updateIndexByDataSourceKey(String dataSourceKey, String[] factTableNames,
			Map<String, Map<String, BigDecimal>> dataSetMap)
			throws IndexAndSearchException {

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"updateIndexByDataSourceKey", dataSourceKey));
		if (StringUtils.isEmpty(dataSourceKey)) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"updateIndexByDataSourceKey", dataSourceKey));
			throw new IllegalArgumentException();
		}

		List<IndexMeta> metaList = new ArrayList<IndexMeta>();
		IndexAction idxAction = IndexAction.INDEX_UPDATE;

		if (MapUtils.isEmpty(dataSetMap)) {
			if (!ArrayUtils.isEmpty(factTableNames)) {
				for (String factTableName : factTableNames) {
					List<IndexMeta> fTableMetaList = this.indexMetaService
							.getIndexMetasByFactTableName(factTableName,
									dataSourceKey);
					if (!CollectionUtils.isEmpty(fTableMetaList)) {
						metaList.addAll(fTableMetaList);
					} else {
						LOGGER.info(String.format(
								LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
								"updateIndexByDataSourceKey", dataSourceKey,
								"can not find IndexMeta for Facttable:["
										+ factTableNames + "]"));
					}
				}
			} else {
				metaList = this.indexMetaService
						.getIndexMetasByDataSourceKey(dataSourceKey);
			}
		} else {
			idxAction = IndexAction.INDEX_MOD;
			for (String factTableName : dataSetMap.keySet()) {
				List<IndexMeta> fTableMetaList = this.indexMetaService
						.getIndexMetasByFactTableName(factTableName,
								dataSourceKey);
				if (!CollectionUtils.isEmpty(fTableMetaList)) {
					metaList.addAll(fTableMetaList);
				}
			}

		}

		for (IndexMeta meta : metaList) {
			Map<String, BigDecimal> tableDataSetMap = null;
			if (!MapUtils.isEmpty(dataSetMap)) {
				tableDataSetMap = dataSetMap.get(meta.getFacttableName());
			}
			try {
				doIndexByIndexAction(meta, idxAction, tableDataSetMap);
			} catch (Exception e) {
				LOGGER.info(String.format(
						LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
						"updateIndexByDataSourceKey",
						"DataSourceKey:[" + dataSourceKey + "] FactTable:["
								+ meta.getFacttableName() + "] IndexMetaId:["
								+ meta.getIndexMetaId() + "]"));
				String message = TesseractExceptionUtils.getExceptionMessage(
						IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
						IndexAndSearchExceptionType.INDEX_EXCEPTION);
				throw new IndexAndSearchException(message, e.getCause(),
						IndexAndSearchExceptionType.INDEX_EXCEPTION);
			}
		}
		try {
			publishIndexUpdateEvent(metaList);
		} catch (Exception e) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"updateIndexByDataSourceKey", dataSourceKey));
			String message = TesseractExceptionUtils.getExceptionMessage(
					IndexAndSearchException.INDEXEXCEPTION_MESSAGE,
					IndexAndSearchExceptionType.INDEX_UPDATE_EXCEPTION);
			throw new IndexAndSearchException(message, e.getCause(),
					IndexAndSearchExceptionType.INDEX_EXCEPTION);
		}

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"updateIndexByDataSourceKey", dataSourceKey));
	}

	/**
	 * doIndexByIndexAction
	 * @param indexMeta 索引元数据
	 * @param idxAction 索引动作
	 * @param dataMap 修订数据时，提供修订的起止范围
	 * @throws Exception 有可能抛出异常
	 */
	private void doIndexByIndexAction(IndexMeta indexMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap)
			throws Exception {
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"doIndexByIndexAction", "[indexMeta:" + indexMeta
						+ "][idxAction:" + idxAction + "]"));
		IndexMeta idxMeta = this.indexMetaService.getIndexMetaByIndexMetaId(indexMeta.getIndexMetaId(), indexMeta.getStoreKey());
		
		if ((idxMeta.getLocked().equals(Boolean.FALSE)) || ((System.currentTimeMillis()-idxMeta.getIdxVersion()) > this.indexInterval)) {
			idxMeta.setLocked(Boolean.TRUE);
			this.indexMetaService.saveIndexMetaLocally(idxMeta);
		}else {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"doIndexByIndexAction",
					"[indexMeta:" + indexMeta.getIndexMetaId() + ", Locked:"
							+ idxMeta.getLocked() + ", last update:"
							+ idxMeta.getIdxVersion() + " ,indexInterval:"
							+ this.indexInterval + ",test (System.currentTimeMillis()-idxMeta.getIdxVersion()):"+(System.currentTimeMillis()-idxMeta.getIdxVersion())+"]","[skip index]"));
			return ;
		}		
		
		if (idxMeta == null || idxAction == null) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_EXCEPTION,
					"doIndexByIndexAction", "[indexMeta:" + indexMeta
							+ "][idxAction:" + idxAction + "]"));
			throw new IllegalArgumentException();
		}
		// 1. IndexMeta-->SQLQuery
		Map<String, SqlQuery> sqlQueryMap = transIndexMeta2SQLQuery(idxMeta, idxAction, dataMap);
		// 2. get a connection
		SqlDataSourceWrap dataSourceWrape = (SqlDataSourceWrap) this.dataSourcePoolService
				.getDataSourceByKey(idxMeta.getDataSourceInfo());
		if (dataSourceWrape == null) {
			LOGGER.info(String.format(
					LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS,
					"doIndexByIndexAction", "[indexMeta:" + indexMeta
							+ "][idxAction:" + idxAction + "]"),
					"getDataSourceByKey return null");
			throw new DataSourceException();
		}
		// 3. get data & write index
		Map<String, BigDecimal> maxDataIdMap = new HashMap<String, BigDecimal>();

		for (String tableName : sqlQueryMap.keySet()) {
			SqlQuery sqlQuery = sqlQueryMap.get(tableName);

			if (!StringUtils.isEmpty(sqlQuery.getIdName())) {
				sqlQuery.getOrderBy().add(sqlQuery.getIdName());
			}

			long total = 0;
			BigDecimal currMaxId = BigDecimal.valueOf(-1);

			if (idxMeta.getDataDescInfo().getMaxDataId(tableName) != null
					&& !idxAction.getFromScratch()) {
				// 数据正常更新，最上次的最大ID
				currMaxId = idxMeta.getDataDescInfo().getMaxDataId(tableName);
			}
			// 其它情况，init、merge情况currMaxId=0，mod情况currMaxId可以为0
			String currWhereStr = sqlQuery.getIdName() + " > "
					+ currMaxId.longValue();
			sqlQuery.getWhereList().add(currWhereStr);
			if (idxAction.equals(IndexAction.INDEX_INIT_LIMITED)) {
				total = IndexFileSystemConstants.INDEX_DATA_TOTAL_IN_LIMITEDMODEL;
			} else {
				total = this.dataQueryService.queryForLongWithSql(
						getCountSQLBySQLQuery(sqlQuery), dataSourceWrape);
			}

			boolean isLastPiece = false;
			long pcount = IndexFileSystemConstants.FETCH_SIZE_FROM_DATASOURCE;
			// 目前是跟据数据量进行划分
			if (pcount > total) {
				pcount = total;
			}
			if (!StringUtils.isEmpty(sqlQuery.getIdName())
					&& !CollectionUtils.isEmpty(sqlQuery.getSelectList())) {
				sqlQuery.getSelectList().add(sqlQuery.getIdName());
			}

			for (int i = 0; i * pcount < total; i++) {
				long limitStart = 0;
				long limitEnd = pcount;
				if ((i + 1) * pcount >= total
						|| idxAction.equals(IndexAction.INDEX_MOD)) {
					isLastPiece = true;
				}

				if (sqlQuery.getWhereList().contains(currWhereStr)) {
					sqlQuery.getWhereList().remove(currWhereStr);
				}

				currWhereStr = sqlQuery.getIdName() + " > "
						+ currMaxId.longValue();
				sqlQuery.getWhereList().add(currWhereStr);

				TesseractResultSet currResult = this.dataQueryService
						.queryForDocListWithSQLQuery(sqlQuery, dataSourceWrape,
								limitStart, limitEnd);

				IndexShard currIdxShard = null;
				int currIdxShardIdx = -1;

				while (currResult.size() != 0) {
					// 当前数据待处理，获取待处理的索引分片
					if (currIdxShard == null) {
						currIdxShardIdx = this.getIndexShardByIndexAction(
								idxMeta, idxAction, currIdxShardIdx);
						currIdxShard = idxMeta.getIdxShardList().get(
								currIdxShardIdx);
						currIdxShard.setFull(Boolean.FALSE);
					}

					// 处理
					Map<String, Object> result = writeIndex(currResult,	idxAction, currIdxShard, isLastPiece, sqlQuery.getIdName());
					
					//更新时间戳
					this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);

					currResult = (TesseractResultSet) result.get(RESULT_KEY_DATA);
					currMaxId = (BigDecimal) result.get(RESULT_KEY_MAXID);
					currIdxShard = (IndexShard) result.get(RESULT_KEY_INDEXSHARD);
					if (currIdxShard.isFull() || isLastPiece) {
						// 设置当前分片的状态为内容已变更
						currIdxShard = null;
						if (idxAction.equals(IndexAction.INDEX_MOD)
								|| idxAction.equals(IndexAction.INDEX_MERGE)
								|| idxAction
										.equals(IndexAction.INDEX_MERGE_NORMAL)) {
							currIdxShardIdx++;
							if(idxAction.equals(IndexAction.INDEX_MERGE_NORMAL)){
								idxAction=IndexAction.INDEX_MERGE;
							}							
							if(currIdxShardIdx>=idxMeta.getIdxShardList().size()){
								idxAction=IndexAction.INDEX_NORMAL;
							}
						} else {
							currIdxShardIdx = -1;
							if (!idxAction.equals(IndexAction.INDEX_MOD)
									&& !idxAction
											.equals(IndexAction.INDEX_MERGE_NORMAL)) {
								idxAction = IndexAction.INDEX_NORMAL;
							}
						}

					}else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
						idxAction = IndexAction.INDEX_MERGE_NORMAL;
					} else if (!idxAction.equals(IndexAction.INDEX_MOD)
							&& !idxAction
									.equals(IndexAction.INDEX_MERGE_NORMAL)) {
						idxAction = IndexAction.INDEX_NORMAL;
					}

				}

			}

			maxDataIdMap.put(tableName, currMaxId);

		}

		if (!idxAction.equals(IndexAction.INDEX_MOD)) {
			// 除了修订的情况外，init merge update都需要保存上次索引后的最大id
			idxMeta.getDataDescInfo().setMaxDataIdMap(maxDataIdMap);
		}

		if (idxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE_NEEDMERGE)) {
			idxMeta.getCubeIdSet().addAll(idxMeta.getCubeIdMergeSet());
			idxMeta.getCubeIdMergeSet().clear();

			idxMeta.getDimSet().addAll(idxMeta.getDimInfoMergeSet());
			idxMeta.getMeasureSet().addAll(idxMeta.getMeasureInfoMergeSet());
			idxMeta.getDimInfoMergeSet().clear();
			idxMeta.getMeasureInfoMergeSet().clear();

		}

		if (idxMeta.getIdxState().equals(IndexState.INDEX_AVAILABLE_NEEDMERGE)
				|| idxMeta.getIdxState().equals(IndexState.INDEX_UNINIT)) {
			idxMeta.setIdxState(IndexState.INDEX_AVAILABLE);
		}

//		for (IndexShard idxShard : idxMeta.getIdxShardList()) {
//			if (idxShard.isUpdate()) {
//				String servicePath = idxShard.getFilePath();
//				String bakFilePath = idxShard.getIdxFilePath();
//				idxShard.setIdxFilePath(servicePath);
//				idxShard.setFilePath(bakFilePath);
//				idxShard.setIdxState(IndexState.INDEX_AVAILABLE);
//			} else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
//				idxMeta.getIdxShardList().remove(idxShard);
//			}
//		}
		
		Iterator<IndexShard> idxShardIt=idxMeta.getIdxShardList().iterator();
		while(idxShardIt.hasNext()){
			IndexShard idxShard=idxShardIt.next();
			if (idxShard.isUpdate()) {
				String servicePath = idxShard.getFilePath();
				String bakFilePath = idxShard.getIdxFilePath();
				idxShard.setIdxFilePath(servicePath);
				idxShard.setFilePath(bakFilePath);
				idxShard.setIdxState(IndexState.INDEX_AVAILABLE);
				if(idxAction.equals(IndexAction.INDEX_MOD) && (idxShard.getShardId() < idxMeta.getIdxShardList().size())){
					idxShard.setFull(Boolean.TRUE);
				}
			} else if (idxAction.equals(IndexAction.INDEX_MERGE)) {
				idxShardIt.remove();
			}
		}
		
		
		idxMeta.setLocked(Boolean.FALSE);
		this.indexMetaService.saveOrUpdateIndexMeta(idxMeta);
		publistIndexMetaWriteEvent(idxMeta);
		
		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"doIndex", "[indexMeta:" + indexMeta + "][idxAction:"
						+ idxAction + "]"));
		return;

	}

	/**
	 * getIndexShardByIndexAction 获取当前待处理的分片的数组下标
	 * 
	 * @param idxMeta
	 *            索引元数据
	 * @param idxAction
	 *            动作
	 * @param idxShardIdx
	 *            要获取的数组下标
	 * @return
	 */
	private int getIndexShardByIndexAction(IndexMeta idxMeta,
			IndexAction idxAction, int idxShardIdx) {
		if (idxAction.getFromScratch() && idxShardIdx >= 0
				&& idxShardIdx < idxMeta.getIdxShardList().size()) {
			// idxShardIdx正常，且idxAction为从0开始的，则直接反回
			return idxShardIdx;
		} else if (idxAction.getFromScratch() && idxShardIdx == -1
				&& !CollectionUtils.isEmpty(idxMeta.getIdxShardList())) {
			for (int i = 0; i < idxMeta.getIdxShardList().size(); i++) {
				if (!idxMeta.getIdxShardList().get(i).isUpdate()) {
					idxShardIdx = i;
					break;
				}
			}

			return idxShardIdx;
		} else {
			return getFreeIndexShardIndexForIndex(idxMeta);
		}

	}	

	

	/**
	 * 从索引元数据中获取空闲索引分片的数组下标
	 * 
	 * @param indexMeta
	 * @return int 若找到，返回>-1的值，否则为-1
	 */
	private int getFreeIndexShardIndexForIndex(IndexMeta indexMeta) {
		int result = -1;
		IndexMeta idxMeta = indexMeta;
		if (idxMeta == null || idxMeta.getIdxShardList() == null) {
			throw new IllegalArgumentException();
		}
		if (this.indexMetaService.isIndexShardFull(idxMeta)) {
			idxMeta = this.indexMetaService.assignIndexShard(idxMeta,
					this.isNodeService.getCurrentNode().getClusterName());
		}
		for (int i = 0; i < idxMeta.getIdxShardList().size(); i++) {
			if (idxMeta.getIdxShardList().get(i) != null
					&& !idxMeta.getIdxShardList().get(i).isFull()) {
				result = i;
				break;
			}
		}
		return result;
	}

	

	/**
	 * 
	 * 跟据sqlQuery得到select count语句
	 * 
	 * @param sqlQuery
	 *            sqlQuery
	 * @return String select count语句
	 */
	private String getCountSQLBySQLQuery(SqlQuery sqlQuery) {
		StringBuffer sb = new StringBuffer();
		sb.append("select count(*) from (");
		sb.append(sqlQuery.toSql());
		sb.append(") as t");
		return sb.toString();
	}

	/**
	 * 
	 * writeIndex 向指定的分片中写数据，如果该分片写满或者写入的数据是最后一片，则启动数据拷贝线程
	 * 
	 * @param data
	 *            要写的数据
	 * @param isUpdate
	 *            是否是更新
	 * @param idxShard
	 *            索引分片
	 * @param lastPiece
	 *            是否是最后一片数据
	 * @return Map<String,Object>
	 * @throws IndexAndSearchException
	 *             可能抛出的异常
	 * 
	 */
	public Map<String, Object> writeIndex(TesseractResultSet data,
			IndexAction idxAction, IndexShard idxShard, boolean lastPiece,
			String idName) throws IndexAndSearchException {

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_BEGIN,
				"writeIndex", "[data:" + data + "][idxAction:" + idxAction
						+ "][idxShard:" + idxShard + "][lastPiece:" + lastPiece
						+ "][idName:" + idName + "]"));
		
		Node node=this.isNodeService.getNodeByNodeKey(idxShard.getClusterName(), idxShard.getNodeKey(), Boolean.TRUE);
		
		IndexMessage message = null;
		message = isClient.index(data, idxAction, idxShard,node, idName, lastPiece);

		LOGGER.info(String.format(
				LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
				"writeIndex", "index success"));

		idxShard.setDiskSize(message.getDiskSize());

		if (idxShard.isFull() || lastPiece) {
			// 设置提供服务的目录：
			String absoluteIdxFilePath = message.getIdxServicePath();			
			idxShard.setUpdate(Boolean.TRUE);
			// 启动数据copy线程拷贝数据到备分节点上
			
			Map<String,Node> assignedNodeMap=new HashMap<String,Node>();
			if (CollectionUtils.isEmpty(idxShard.getReplicaNodeKeyList())) {
				
				assignedNodeMap=this.isNodeService.assignFreeNodeForReplica(IndexShard.getDefaultShardReplicaNum() - 1,
								idxShard.getNodeKey(),idxShard.getClusterName());

				if (MapUtils.isNotEmpty(assignedNodeMap)) {
					if (assignedNodeMap.keySet().contains(idxShard.getNodeKey())) {
						assignedNodeMap.remove(idxShard.getNodeKey());
					}
					List<String> replicaNodeKeyList= new ArrayList<String>();
					if(MapUtils.isNotEmpty(assignedNodeMap)){
						replicaNodeKeyList.addAll(assignedNodeMap.keySet());
					}					
					idxShard.setReplicaNodeKeyList(replicaNodeKeyList);
				}

			}

			

			for (Node currNode : assignedNodeMap.values()) {
				int retryTimes = 0;
				// 拷贝到指定的目录
				String targetFilePath = idxShard.getAbsoluteFilePath(currNode.getIndexBaseDir());
				ServerFeedbackMessage backMessage = null;
				while (retryTimes < TesseractConstant.RETRY_TIMES) {
					backMessage = isClient.copyIndexDataToRemoteNode(
							absoluteIdxFilePath, targetFilePath, true, currNode);
					if (backMessage.getResult().equals(FileUtils.SUCC)) {
						currNode.setNodeState(NodeState.NODE_AVAILABLE);
						this.isNodeService.saveOrUpdateNodeInfo(currNode);
						LOGGER.info(String
								.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
										"writeIndex", "copy index success to "
												+ currNode));
						break;
					} else {
						LOGGER.info(String
								.format(LogInfoConstants.INFO_PATTERN_FUNCTION_PROCESS_NO_PARAM,
										"writeIndex", "retry copy index to "
												+ currNode));
						retryTimes++;
					}

				}

			}
		}

		Map<String, Object> result = new HashMap<String, Object>();
		result.put(RESULT_KEY_INDEXSHARD, idxShard);
		result.put(RESULT_KEY_DATA, message.getDataBody());
		result.put(RESULT_KEY_MAXID, message.getMaxId());

		LOGGER.info(String.format(LogInfoConstants.INFO_PATTERN_FUNCTION_END,
				"writeIndex", "[data:" + data + "][idxAction:" + idxAction
						+ "][idxShard:" + idxShard + "][lastPiece:" + lastPiece
						+ "][idName:" + idName + "]"));
		return result;

	}

	

	/**
	 * 根据IndexMeta获取从事实表中取数据的SQLQuery对像
	 * 
	 * @param idxMeta
	 *            当前的idxMeta
	 * @param idxAction
	 *            索引动作
	 * @return Map<String, SqlQuery> 返回sqlquery对像
	 * @throws IndexMetaIsNullException
	 *             当idxMeta为空时会抛出异常
	 */
	private Map<String, SqlQuery> transIndexMeta2SQLQuery(IndexMeta idxMeta,
			IndexAction idxAction, Map<String, BigDecimal> dataMap)
			throws IndexMetaIsNullException {
		Map<String, SqlQuery> result = new HashMap<String, SqlQuery>();
		if (idxMeta == null || idxMeta.getDataDescInfo() == null) {
			throw generateIndexMetaIsNullException(idxMeta);
		}

		if (idxAction.equals(IndexAction.INDEX_MOD)
				&& MapUtils.isEmpty(dataMap)) {
			throw new IllegalArgumentException();
		}

		boolean needMerge = Boolean.FALSE;
		if (idxAction.equals(IndexAction.INDEX_MERGE)) {
			needMerge = Boolean.TRUE;
		}
		Set<String> selectList = idxMeta.getSelectList(needMerge);
		if (selectList == null) {
			selectList = new HashSet<String>();
		}
		String idName = idxMeta.getDataDescInfo().getIdStr();
		BigDecimal start = null;
		BigDecimal end = null;
		if (idxAction.equals(IndexAction.INDEX_MOD)
				&& !MapUtils.isEmpty(dataMap)) {
			start = dataMap.get(IndexFileSystemConstants.MOD_KEY_START);
			end = dataMap.get(IndexFileSystemConstants.MOD_KEY_END);
		}
		for (String tableName : idxMeta.getDataDescInfo().getTableNameList()) {
			SqlQuery sqlQuery = new SqlQuery();
			LinkedList<String> fromList = new LinkedList<String>();
			fromList.add(tableName);
			sqlQuery.setFromList(fromList);
			sqlQuery.getSelectList().addAll(selectList);
			result.put(tableName, sqlQuery);
			if (!StringUtils.isEmpty(idName)) {
				sqlQuery.setIdName(idName);
			}
			if (start != null) {
				sqlQuery.getWhereList().add(idName + " >= " + start);
				if (end != null) {
					sqlQuery.getWhereList().add(idName + " <= " + end);
				}
			}

		}

		return result;
	}

	/**
	 * 
	 * 统一的生成IndexMetaIsNullException
	 * 
	 * @param idxMeta
	 *            索引元数据
	 * @return IndexMetaIsNullException
	 */
	private IndexMetaIsNullException generateIndexMetaIsNullException(
			IndexMeta idxMeta) {
		StringBuffer sb = new StringBuffer();
		if (idxMeta == null) {
			sb.append("IndexMeta [");
			sb.append(idxMeta);
			sb.append("]");
		} else {
			sb.append(idxMeta.toString());
		}
		LOGGER.info("IndexMetaIsNullException ocurred:" + sb.toString());
		return new IndexMetaIsNullException(sb.toString());
	}



    
}
