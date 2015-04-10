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
package com.baidu.rigel.biplatform.ma.report.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.query.data.DataModel;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;
import com.baidu.rigel.biplatform.ma.report.exception.PivotTableParseException;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.MeasureTopSetting;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction.MeasureOrderDesc;
import com.baidu.rigel.biplatform.ma.report.query.QueryContext;
import com.baidu.rigel.biplatform.ma.report.query.ReportRuntimeModel;
import com.baidu.rigel.biplatform.ma.report.query.ResultSet;
import com.baidu.rigel.biplatform.ma.report.query.pivottable.PivotTable;
import com.baidu.rigel.biplatform.ma.report.service.AnalysisChartBuildService;
import com.baidu.rigel.biplatform.ma.report.service.QueryBuildService;
import com.baidu.rigel.biplatform.ma.report.utils.ItemUtils;
import com.baidu.rigel.biplatform.ma.report.utils.QueryUtils;
import com.baidu.rigel.biplatform.ma.report.utils.ReportDesignModelUtils;
import com.baidu.rigel.biplatform.ma.resource.utils.DataModelUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 
 * QueryAction构建服务
 * 
 * @author zhongyi
 *
 *         2014-8-5
 */
@Service("queryBuildService")
public class QueryActionBuildServiceImpl implements QueryBuildService {
    
    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(QueryActionBuildServiceImpl.class);
    
    /**
     * analysisChartBuildService
     */
    @Resource
    private AnalysisChartBuildService analysisChartBuildService;
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.QueryActionBuildService#
     * generateQueryAction()
     */
    @Override
    public QueryAction generateTableQueryAction(ReportDesignModel model, String areaId,
            Map<String, Object> context) {
        
        ExtendArea targetArea = getRealExtendArea(model, areaId, context);
        LogicModel targetLogicModel = targetArea.getLogicModel();
        String cubeId = targetArea.getCubeId();
        return generateQueryAction(model.getSchema(),
                cubeId, targetLogicModel, context, areaId, false, model);
    }

    /* 
     * (non-Javadoc)
     * @see com.baidu.rigel.biplatform.ma.report.service.
     * QueryBuildService#generateTableQueryActionForDrill(com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel, 
     * java.lang.String, java.util.Map)
     */
    @Override
    public QueryAction generateTableQueryActionForDrill(ReportDesignModel model, String areaId,
            Map<String, Object> contextParams, int targetIndex) {
        
        ReportDesignModel drillModel = DeepcopyUtils.deepCopy(model);
        ExtendArea targetArea = getRealExtendArea(drillModel, areaId, contextParams);
        LogicModel targetLogicModel = targetArea.getLogicModel();
        String cubeId = targetArea.getCubeId();
        List<String> rowDimsAhead = Lists.newArrayList();
        int target = targetIndex;
        for (int i = 0; i < targetIndex; i++) {
            Item rowDim = targetLogicModel.getRows()[i];
            rowDimsAhead.add(rowDim.getOlapElementId());
        }
        for (String rowAhead : rowDimsAhead) {
            Item item = targetLogicModel.removeRow(rowAhead);
            targetLogicModel.addSlice(item);
            target--;
        }
        QueryAction action = generateQueryAction(drillModel.getSchema(),
                cubeId, targetLogicModel, contextParams, areaId, false, model);
        /**
         * 把下钻的值存下来
         */
        Item item = targetLogicModel.getRows()[target];
        if (item != null && contextParams.containsKey(item.getOlapElementId())) {
            action.getDrillDimValues().put(item,
                    contextParams.get(item.getOlapElementId()));
        }
        return action;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public QueryAction generateChartQueryAction(ReportDesignModel model, String areaId,
                Map<String, Object> context, String[] indNames, ReportRuntimeModel runTimeModel)
                throws QueryModelBuildException {
        ExtendArea targetArea = DeepcopyUtils.deepCopy(model).getExtendById(areaId);
        LogicModel targetLogicModel = null;
        String cubeId = targetArea.getCubeId();
        /**
         * generateQueryAction方法中需要指定logicModel所在的区域id，
         * logicModelAreaId即该区域id
         */
        String logicModelAreaId = areaId;
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_CHART) {
            /**
             * 针对liteOlap表的查询
             */
            ExtendArea referenceArea = model.getExtendById(targetArea.getReferenceAreaId());
            logicModelAreaId = referenceArea.getId();
            cubeId = referenceArea.getCubeId();
            /**
             * 找到liteOlap的父区域，获取logicModel
             */
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) referenceArea;
            targetLogicModel = referenceArea.getLogicModel();
            /**
             * 针对liteOlap表的图形分析区查询，产生新的logicModel row和col来自用户选中的行和指标
             * row和col的获取步骤： 1. 拿到前端传来的指标id，拿到存储在runtimeModel中的selectedRowIds 2.
             * 根据当前状态，生成查询表所使用的queryAction 3. 根据queryAction得到结果缓存中表的查询结果 4.
             * 按照1中得到的参数，从结果中找到行上的维度和指标 5. 将维度、指标拼成rows和cols
             */
            List<Item> rows = Lists.newArrayList();
            List<Item> cols = Lists.newArrayList();
            /**
             * 1.
             */
            
            String[] selectedRowIds = runTimeModel.getSelectedRowIds().toArray(new String[0]);
            if (selectedRowIds.length > 1) {
                logger.warn("More than one line selected, do not support! Query as One line!");
            }
            /**
             * 2.
             */
            QueryContext queryContextForTable = runTimeModel.getLocalContextByAreaId(
                liteOlapArea.getTableAreaId());
            QueryAction actionForTable = generateTableQueryAction(model, liteOlapArea.getTableAreaId(),
                queryContextForTable.getParams());
            /**
             * 3.
             */
            ResultSet resultSet = runTimeModel.getPreviousQueryResult(actionForTable);
            if (resultSet == null) {
                logger.error("There is no result of table for querying liteOlap Chart!!");
                throw new RuntimeException("There is no result of table for querying liteOlap Chart!!");
            }
            /**
             * 按照选中行ID得到行上的维度值
             */
            String[] uniqNames = com.baidu.rigel.biplatform.ac.util
                .DataModelUtils.parseNodeUniqueNameToNodeValueArray(selectedRowIds[0]);
            /**
             * 从logicmodel里面找到时间维度
             */
            List<String> timeItemIds = runTimeModel.getTimeDimItemIds();
            Item timeDimItem = null;
            for (String timeItemId : timeItemIds) {
                timeDimItem = targetLogicModel.getItemByOlapElementId(timeItemId);
                if (timeDimItem != null) {
                    break;
                }
            }
            /**
             * 从context里面查看，否是有时间维度
             */
            if (timeDimItem == null) {
                for (String key : context.keySet()) {
                    OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(model.getSchema(),
                        cubeId, key);
                    if (element != null && element instanceof TimeDimension) {
                        timeDimItem = new Item();
                        timeDimItem.setAreaId(areaId);
                        timeDimItem.setCubeId(cubeId);
                        timeDimItem.setId(element.getId());
                        timeDimItem.setOlapElementId(element.getId());
                        timeDimItem.setPositionType(PositionType.X);
                        timeDimItem.setSchemaId(model.getSchema().getId());
                    }
                }
            }
            for (String uniqName : uniqNames) {
                String dimName = MetaNameUtil.getDimNameFromUniqueName(uniqName);
                Map<String, Item> store = runTimeModel.getUniversalItemStore().get(liteOlapArea.getId());
                if (CollectionUtils.isEmpty(store)) {
                    String msg = "The item map of area (" + liteOlapArea.getId() + ") is Empty!";
                    logger.error(msg);
                    throw new RuntimeException(msg);
                }
                Item row = store.get(dimName);
                if (row == null) {
                    String msg = String.format("Dimension(%s) Not found in the store of Area(%s)!",
                            dimName, liteOlapArea.getId());
                    logger.error(msg);
                    throw new RuntimeException(msg);
                }
                rows.add(row);
                context.put(row.getOlapElementId(), uniqName);
            }
            for (String indName : indNames) {
                cols.add(liteOlapArea.listAllItems().get(indName));
            }
            LogicModel tmp = analysisChartBuildService.generateTrendChartModel(targetLogicModel,
                    model.getSchema(), liteOlapArea.getCubeId(),
                    DeepcopyUtils.deepCopy(rows), DeepcopyUtils.deepCopy(cols), timeDimItem);
            return generateQueryAction(model.getSchema(),
                cubeId, tmp, context, logicModelAreaId, true, model);
        } else {
            targetLogicModel = targetArea.getLogicModel();
            LogicModel cpModel = DeepcopyUtils.deepCopy(targetArea.getLogicModel());
            List<String> timeItemIds = runTimeModel.getTimeDimItemIds();
            Item timeDimItem = null;
            for (String timeItemId : timeItemIds) {
                timeDimItem = cpModel.getItemByOlapElementId(timeItemId);
                if (timeDimItem != null) {
                    break;
                }
            }
            if (timeDimItem != null && timeDimItem.getPositionType() == PositionType.X) { // 时间序列图
                Map<String, Object> params = timeDimItem.getParams();
                params.put("range", true);
                timeDimItem.setParams(params);
                context.put("time_line", timeDimItem);
            }
            if (cpModel != null && !CollectionUtils.isEmpty(cpModel.getSelectionMeasures())) {
                cpModel.addColumns(cpModel.getSelectionMeasures().values().toArray(new Item[0]));
            }
            // 修正查询条件，重新设置查询指标
            Object index = context.get(Constants.CHART_SELECTED_MEASURE);
            if (index != null) {
            	modifyModel(cpModel, Integer.valueOf(index.toString()));
            }
//            targetLogicModel = DeepcopyUtils.deepCopy(targetLogicModel);
           return generateQueryAction(model.getSchema(),
               cubeId, cpModel, context, logicModelAreaId, false, model);
        }
        
    }
    
    /**
     * 修正查询条件
     * @param model
     * @param index
     */
    private void modifyModel(LogicModel model, Integer index) {
    	Item[] items = new Item[1];
    	Item[] selMeasures = model.getSelectionMeasures().values().toArray(new Item[0]);
    	if (index >= selMeasures.length) {
    		throw new IndexOutOfBoundsException("索引越界");
    	}
    	items = new Item[]{ selMeasures[index] };
		model.resetColumns(items);
	}

	/**
     * 生成QueryAction
     * 
     * @param targetLogicModel
     * @param context
     * @return
     */
    private QueryAction generateQueryAction(Schema schema, String cubeId,
            LogicModel targetLogicModel, Map<String, Object> context,
            String areaId, boolean needTimeRange, ReportDesignModel reportModel) {
        final Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            return null;
        }
        if (targetLogicModel == null) {
            return null;
        }
        QueryAction action = new QueryAction();
        action.setExtendAreaId(areaId);
        
        /**
         * TODO 生成一个独立的id
         */
        String id = UuidGeneratorUtils.generate();
        action.setId(id);
        
        /**
         * TODO 生成path
         */
        String queryPath = "";
        action.setQueryPath(queryPath);
        
        Cube oriCube4QuestionModel = genCube4QuestionModel (schema, cubeId,
                targetLogicModel, context, areaId, reportModel);
        
        Map<Item, Object> columns = genereateItemValues(schema,
                cubeId, targetLogicModel.getColumns(), context, needTimeRange, oriCube4QuestionModel);
        action.setColumns(columns);
        
        Map<Item, Object> rows = genereateItemValues(schema,
                cubeId, targetLogicModel.getRows(), context, needTimeRange, oriCube4QuestionModel);
        action.setRows(rows);
        
        Map<Item, Object> slices = genereateItemValues(schema,
                cubeId, targetLogicModel.getSlices(), context, needTimeRange, oriCube4QuestionModel);
        action.setSlices(slices);
        
        fillFilterBlankDesc (areaId, reportModel, action);
        
        QueryAction.MeasureOrderDesc orderDesc = genOrderDesc (targetLogicModel, context, action, cube);
        logger.info ("[INFO] -------- order desc = " + orderDesc);
        action.setMeasureOrderDesc(orderDesc);
        return action;
    }

    private Cube genCube4QuestionModel(Schema schema, String cubeId,
            LogicModel targetLogicModel, Map<String, Object> context,
            String areaId, ReportDesignModel reportModel) {
        Cube oriCube4QuestionModel = null;
        try {
            oriCube4QuestionModel = QueryUtils.getCubeWithExtendArea(reportModel, reportModel.getExtendById(areaId));
        } catch (QueryModelBuildException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        for (String key : context.keySet()) {
            OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(schema, cubeId, key);
//            Object level = context.get(key + "_level");
            if (element != null && !targetLogicModel.containsOlapElement(element.getId())) {
                Item item = new Item();
                item.setAreaId(areaId);
                item.setCubeId(cubeId);
                item.setId(element.getId());
                item.setOlapElementId(element.getId());
                item.setPositionType(PositionType.S);
                item.setSchemaId(schema.getId());
                targetLogicModel.addSlice(item);
            }
            
            // TODO 修正过滤条件
            if (oriCube4QuestionModel != null) {
                oriCube4QuestionModel.getDimensions().values().forEach(dim -> {
                    if (dim.getId().equals(key)) {
                        Item item = new Item();
                        if (targetLogicModel.getItemByOlapElementId(key) != null) {
                            item = targetLogicModel.getItemByOlapElementId(key) ;
                        }
                        item.setAreaId(areaId);
                        item.setCubeId(cubeId);
                        item.setId(dim.getId());
                        item.setOlapElementId(dim.getId());
                        if (item.getPositionType() == null) {
                            item.setPositionType(PositionType.S);
                        }
                        item.setSchemaId(schema.getId());
                        targetLogicModel.addSlice(item);
                    }
                });
            }
        }
        return oriCube4QuestionModel;
    }

    private MeasureOrderDesc genOrderDesc(LogicModel targetLogicModel,
            Map<String, Object> context, QueryAction action, final Cube cube) {
        Map<String, Measure> measures = cube.getMeasures();
        MeasureTopSetting topSet = targetLogicModel.getTopSetting();
        if (!action.getColumns().isEmpty()) {
            if (topSet == null) {
                Measure[] tmp = action.getColumns().keySet().stream().filter(item -> {
                    return cube.getMeasures().get(item.getOlapElementId()) != null;
                }).map(item -> {
                    return cube.getMeasures().get(item.getOlapElementId());
                }).toArray(Measure[] :: new);
                if (tmp != null && tmp.length > 0 && context.get(Constants.NEED_LIMITED) == null) {
                    if ( isTimeDimOnFirstCol(action.getRows (), cube)) {
                        return new MeasureOrderDesc(tmp[0].getName(), "NONE", 500);
                    }
                    return new MeasureOrderDesc(tmp[0].getName(), "DESC", 500);
                } else  if (context.get ("time_line") != null) { // 时间序列图
                    return new MeasureOrderDesc (
                            tmp[0].getName (), "NONE", Integer.MAX_VALUE);
                } else {
                    context.remove(Constants.NEED_LIMITED);
                    boolean isTimeDimOnFirstCol = isTimeDimOnFirstCol(action.getRows (), cube);
                    if (isTimeDimOnFirstCol) {
                        return new MeasureOrderDesc(tmp[0].getName(), "NONE", Integer.MAX_VALUE);
                    }
                    return new MeasureOrderDesc(tmp[0].getName(), "DESC", Integer.MAX_VALUE);
                }
            } else {
                	if (context.get("time_line") != null) { //时间序列图
                    return  new MeasureOrderDesc(
                            measures.get(topSet.getMeasureId()).getName(), "NONE", Integer.MAX_VALUE);
                	}
                String olapElementId = action.getColumns().keySet().toArray(new Item[0])[0].getOlapElementId();
                return  new MeasureOrderDesc(measures.get(olapElementId).getName(),
                        topSet.getTopType().name(), topSet.getRecordSize());
            }
        }
        return null;
    }

    private boolean isTimeDimOnFirstCol(Map<Item, Object> rows, Cube cube) {
        if (rows  == null || rows.size () == 0) {
            return false;
        }
        Item[] items = rows.keySet ().toArray (new Item[0]);
        Dimension dim = cube.getDimensions ().get (items[0].getOlapElementId ());
        return dim instanceof TimeDimension;
    }

    /**
     * 
     * @param areaId
     * @param reportModel
     * @param action
     */
    private void fillFilterBlankDesc(String areaId,
            ReportDesignModel reportModel, QueryAction action) {
        ExtendArea area = reportModel.getExtendById(areaId);
        if (area.getType() == ExtendAreaType.TABLE || area.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            Object filterBlank = area.getOtherSetting().get(Constants.FILTER_BLANK);
            if (filterBlank == null) {
                action.setFilterBlank(false);
            } else {
                action.setFilterBlank(Boolean.valueOf(filterBlank.toString()));
            }
        }
    }
    
    /**
     * 
     * @param items
     * @param values
     * @param oriCube 修正后的cube
     * @return
     */
    private Map<Item, Object> genereateItemValues(Schema schema,
            String cubeId, Item[] items, Map<String, Object> values,
            boolean timeRange, Cube oriCube) {
        /**
         * item必须保证顺序
         */
        Map<Item, Object> itemValues = Maps.newLinkedHashMap();
        boolean update = true;
        if (CollectionUtils.isEmpty(values)) {
            update = false;
        }
        for (Item item : items) {
            String elementId = item.getOlapElementId();
            if (StringUtils.isEmpty(elementId)) {
                continue;
            }
            // 是否会影响其他值
            Object showLevel = values.get(item.getOlapElementId() + "_level");
            if (showLevel != null) {
                item.getParams().put(Constants.LEVEL, Integer.valueOf(showLevel.toString()));
            }
            OlapElement element = ItemUtils.getOlapElementByItem(item, schema, cubeId);
            if (element == null) {
                for (Dimension dim : oriCube.getDimensions().values()) {
                    if (dim.getId().equals(item.getOlapElementId())) {
                        element = dim;
                        break;
                    }
                }
            }
            Object value = null;
            // TODO 支持url传参数，需后续修改,dirty solution
            // 第一个条件判断是否包含url规定的参数
            // 第二个条件判断是否为下钻,下钻不走此流程
            if ((values.containsKey(Constants.ORG_NAME) || values.containsKey(Constants.APP_NAME)) && 
                    ! (values.containsKey("action") && values.get("action").equals("expand")) &&
                    element instanceof StandardDimension
                    && (item.getPositionType() == PositionType.X || item.getPositionType() == PositionType.S)
                   ) {
                StandardDimension standardDim = (StandardDimension) element;
                Map<String, Level> levels = standardDim.getLevels();
                List<String> list = new ArrayList<String>();
                if (levels != null) {
                    for (String key : levels.keySet()) {
                        Level level = levels.get(key);
                        // 获取level所属的维度
                        Dimension dim = level.getDimension();
                        String tableName = dim.getTableName();
                        String name = dim.getName();
                        // 维度的列名
                        String columnName = name.replace(tableName + "_", "");
                        if (columnName.equals(Constants.ORG_NAME) || values.containsKey(Constants.APP_NAME)) {
                            Object val = values.get(columnName);
                            if (val instanceof String) {
                                String [] vals = ((String) val).split(",");
                                for (int i = 0; i < vals.length; i++) {
                                    String temp = "[" + standardDim.getName() + "]";
                                    temp += ".[" + vals[i] + "]";
                                    list.add(temp);
                                }
                            }
                        }
                    }
                }
                value = list.toArray(new String[0]);
            } else {
                value = update && values.containsKey(elementId) ? values.get(elementId) : item
                    .getParams().get(elementId);
            }
                
            // 时间维度特殊处理
            if (value != null && element instanceof TimeDimension 
                && !value.toString().toLowerCase().contains("all")) {
                String start;
                String end;
                try {
                    JSONObject json = new JSONObject(String.valueOf(value));
                    /**
                     * TODO 考虑月/周/年等
                     */
                    start = json.getString("start").replace("-", "");
                    end = json.getString("end").replace("-", "");
                    if (item.getParams().get("range") != null && start.equals(end)) {
                        TimeRangeDetail tail = TimeUtils.getDays (TimeRangeDetail.getTime(start), 30, 0);
                        start = tail.getStart();
                        end = tail.getEnd();
                    } else {
                        TimeDimension timeDim = (TimeDimension) element;
                        Map<String, String> time= TimeUtils.getTimeCondition(start, end, timeDim.getDataTimeType());
                        start = time.get("start");
                        end = time.get("end");
                    }
                } catch (Exception e) {
                    logger.warn(
                            "Time Condition not Correct. Maybe from row."
                            + " Try to use it as UniqueName. Time: " + value, e);
                    if (value instanceof String[]) {
                        String[] dates = (String[]) value;
                        /**
                         * TODO 如果有多选时间，把第一个时间和最后一个时间作为start和end，不支持间断时间
                         * 以后要考虑重构，支持间断时间
                         */
                        start = parseToDate(dates[0]);
                        end = parseToDate(dates[dates.length - 1]);
                    } else {
                        start = parseToDate(String.valueOf(value));
                        end = parseToDate(String.valueOf(value));
                    }
                    
                }
                TimeRangeDetail range = new TimeRangeDetail(start, end);
                if (timeRange && start.equals(end)) {
                    /**
                     * 如果是时间区域，并且时间参数中起始和结束相同，把时间扩展为start过去一个月以来的数据
                     */
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                    Date startDate = new Date();
                    try {
                        startDate = df.parse(start);
                    } catch (ParseException e) {
                        logger.error("Date Format Error. Use current date instead. ", e);
                    }
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(startDate);
                    calendar.add(Calendar.MONTH, -1);
                    range = new TimeRangeDetail(df.format(calendar.getTime()), end);
                }
                /**
                 * TODO 
                 * modify by jiangyichao at 2014-11-10
                 *  仅处理单选
                 */
                String[] days = new String[range.getDays().length];
                StringBuilder message = new StringBuilder();
                for (int i = 0; i < days.length; i++) {
                    days[i] = "[" + element.getName() + "].[" + range.getDays()[i] + "]";
                    message.append(" " + days[i]);
                }
                value = days;
                itemValues.put(item, value);
                logger.debug("[DEBUG] --- ---" + message);
            } else if (value instanceof String && !StringUtils.isEmpty(value)) {
                itemValues.put(item, value.toString().split(","));
            } else {
                itemValues.put(item, value);
            }
        }
        return itemValues;
    }
    
    private String parseToDate(String uniqueName) {
        String[] valueParts = StringUtils.split(uniqueName, "].[");
        /**
         * TODO 假定时间的uniqueName是[***].[yyyyMMdd]这种，以后要考虑兼容性
         */
        if (valueParts.length > 1) {
            String part = valueParts[valueParts.length - 1];
            return part.replace("]", "");
        }
        return uniqueName;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.baidu.rigel.biplatform.ma.report.service.QueryActionBuildService#
     * generateQueryContext(java.util.Map)
     */
    @Override
    public QueryContext generateQueryContext(String areaId, Map<String, String[]> contextParams) {
        
        QueryContext context = new QueryContext();
        context.setExtendAreaId(areaId);
        Map<String, Object> params = Maps.newHashMap();
        context.setParams(params);
        return context;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.baidu.rigel.biplatform.ma.report.service.QueryBuildService#
     * parseToPivotTable(com.baidu.rigel.biplatform.ma.report.query.DataModel)
     */
    @Override
    public PivotTable parseToPivotTable(Cube cube, DataModel dataModel) throws PivotTableParseException {
        
        PivotTable table = DataModelUtils.transDataModel2PivotTable(cube, dataModel, false, 0, false);
        // TODO Auto-generated method stub
        return table;
    }

    private ExtendArea getRealExtendArea(ReportDesignModel model, String areaId,
            Map<String, Object> contextParams) {
        
        ExtendArea targetArea = model.getExtendById(areaId);
        if (targetArea.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            /**
             * 针对liteOlap表的查询
             */
            targetArea = model.getExtendById(targetArea.getReferenceAreaId());
        } 
        return targetArea;
    }
    
}