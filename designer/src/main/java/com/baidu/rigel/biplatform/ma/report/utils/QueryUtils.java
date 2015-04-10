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
package com.baidu.rigel.biplatform.ma.report.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackMeasure;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeDimension;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.model.Member;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.query.data.DataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo;
import com.baidu.rigel.biplatform.ac.query.data.impl.SqlDataSourceInfo.DataBase;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta;
import com.baidu.rigel.biplatform.ac.query.model.AxisMeta.AxisType;
import com.baidu.rigel.biplatform.ac.query.model.ConfigQuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.query.model.QueryData;
import com.baidu.rigel.biplatform.ac.query.model.QuestionModel;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord;
import com.baidu.rigel.biplatform.ac.query.model.SortRecord.SortType;
import com.baidu.rigel.biplatform.ac.util.AesUtil;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ac.util.MetaNameUtil;
import com.baidu.rigel.biplatform.ac.util.PlaceHolderUtils;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.ds.DataSourceDefine;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.model.utils.DBUrlGeneratorUtils;
import com.baidu.rigel.biplatform.ma.model.utils.HttpUrlUtils;
import com.baidu.rigel.biplatform.ma.report.exception.QueryModelBuildException;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ExtendAreaType;
import com.baidu.rigel.biplatform.ma.report.model.FormatModel;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LiteOlapExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.MeasureTopSetting;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportParam;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction;
import com.baidu.rigel.biplatform.ma.report.query.QueryAction.MeasureOrderDesc;
import com.baidu.rigel.biplatform.ma.report.query.chart.DIReportChart;
import com.baidu.rigel.biplatform.ma.report.query.chart.SeriesDataUnit;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * 
 * 查询工具类，负责将QueryAction转化城QuestionModel
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public final class QueryUtils {
  
    private static final Logger LOG = LoggerFactory.getLogger (QueryUtils.class);
    /**
     * 构造函数
     */
    private QueryUtils() {
        
    }
    
    /**
     * 
     * 将查询动作转化成问题模型
     * @param dsDefine 
     * 
     * @param queryAction
     *            查询动作
     * @return 问题模型
     * @throws QueryModelBuildException
     *             构建失败异常
     */
    public static QuestionModel convert2QuestionModel(DataSourceDefine dsDefine, ReportDesignModel reportModel,
        QueryAction queryAction, String securityKey) throws QueryModelBuildException {
        if (queryAction == null) {
            throw new QueryModelBuildException("query action is null");
        }
        ConfigQuestionModel questionModel = new ConfigQuestionModel();
        String areaId = queryAction.getExtendAreaId();
        if (StringUtils.isEmpty(areaId)) {
            throw new QueryModelBuildException("area id is empty");
        }
        ExtendArea area = reportModel.getExtendById(areaId);
        if (area == null) {
            throw new QueryModelBuildException("can not get area with id : " + areaId);
        }
        Cube cube = getCubeWithExtendArea(reportModel, area);
        if (cube == null) {
            throw new QueryModelBuildException("can not get cube define in area : " + areaId);
        }
        // 设置轴信息
        questionModel.setAxisMetas(buildAxisMeta(reportModel.getSchema(), area, queryAction));
        // 构建查询信息
        questionModel.setQueryConditions(buildQueryConditions(reportModel, area, queryAction));
        questionModel.setCubeId(area.getCubeId());
        ((MiniCube) cube).setProductLine(dsDefine.getProductLine());
        // TODO 动态更新cube 针对查询过程中动态添加的属性 需要仔细考虑此处逻辑
        Set<Item> tmp = Sets.newHashSet();
        tmp.addAll(queryAction.getSlices().keySet());
        tmp.addAll(queryAction.getRows().keySet());
//        updateLogicCubeWithSlices(cube, tmp,
//                reportModel.getSchema().getCubes().get(area.getCubeId()));
        questionModel.setCube(cube);
        questionModel.setDataSourceInfo(buidDataSourceInfo(dsDefine, securityKey));
        MeasureOrderDesc orderDesc = queryAction.getMeasureOrderDesc();
        if (orderDesc != null) {
            SortType sortType = SortType.valueOf(orderDesc.getOrderType());
            String uniqueName = "[Measure].[" +orderDesc.getName()+ "]";
            SortRecord sortRecord = new SortRecord(sortType, uniqueName , orderDesc.getRecordSize());
            questionModel.setSortRecord(sortRecord);
        }
        // TODO 此处没有考虑指标、维度交叉情况，如后续有指标维度交叉情况，此处需要调整
        questionModel.getQueryConditionLimit().setWarningAtOverFlow(false);
        if (queryAction.isNeedOthers()) {
            // TODO 需要开发通用工具包 将常量定义到通用工具包中
            questionModel.getRequestParams().put("NEED_OTHERS", "1");
        }
        putSliceConditionIntoParams (queryAction, questionModel);
        questionModel.setFilterBlank(queryAction.isFilterBlank());
        return questionModel;
    }

    /**
     * 
     * @param queryAction
     * @param questionModel
     */
    private static void putSliceConditionIntoParams(QueryAction queryAction, QuestionModel questionModel) {
        if (queryAction.getSlices () != null && !queryAction.getSlices ().isEmpty ()) {
            for (Map.Entry<Item, Object> entry : queryAction.getSlices ().entrySet ()) {
                String olapElementId = entry.getKey ().getOlapElementId ();
                Object value = entry.getValue ();
                if (value instanceof String[]) {
                    StringBuilder rs = new StringBuilder();
                    for (String str : (String[]) value) {
                        rs.append (str + ",");
                    }
                    questionModel.getRequestParams().put(olapElementId, rs.toString ());
                } else if (value != null){
                    questionModel.getRequestParams().put(olapElementId, value.toString ());
                }
            }
        }
    }
    
//    /**
//     * 
//     * @param cube
//     * @param keySet
//     * @param defineCube
//     */
//    private static void updateLogicCubeWithSlices(final Cube cube, Set<Item> keySet, final Cube defineCube) {
//        if (keySet == null || keySet.isEmpty()) {
//            return;
//        }
//        
//        keySet.forEach(item -> {
//            Dimension dim = defineCube.getDimensions().get(item.getOlapElementId());
//            if (dim != null) {
//                Dimension tmp = DeepcopyUtils.deepCopy(dim);
//                tmp.getLevels().clear();
//                dim.getLevels().values().forEach(level -> {
//                    tmp.getLevels().put(level.getName(), level);
//                });
//                cube.getDimensions().put(tmp.getName(), tmp);
//            }
//        });
//    }

    /**
     * 
     * @param dsDefine
     * @return DataSourceInfo
     */
    private static DataSourceInfo buidDataSourceInfo(DataSourceDefine dsDefine, String securityKey) {
        SqlDataSourceInfo ds = new SqlDataSourceInfo(dsDefine.getId());
        ds.setDBProxy(true);
        try {
            ds.setPassword(AesUtil.getInstance().decodeAnddecrypt(dsDefine.getDbPwd(), securityKey));
        } catch (Exception e) {
        }
        ds.setUsername(dsDefine.getDbUser());
        ds.setProductLine(dsDefine.getProductLine());
        ds.setInstanceName(dsDefine.getDbInstance());
        List<String> hosts = Lists.newArrayList();
        hosts.add(dsDefine.getHostAndPort());
        ds.setHosts(hosts);
        List<String> jdbcUrls = Lists.newArrayList();
        try {
            jdbcUrls.add(DBUrlGeneratorUtils.getConnUrl(dsDefine));
        } catch (Exception e) {
        }
        ds.setDataBase(DataBase.valueOf(dsDefine.getType().name()));
        ds.setJdbcUrls(jdbcUrls);
        return ds;
    }

    /**
     * 构建查询条件信息
     * 
     * @param reportModel
     * @param area
     * @param queryAction
     * @return
     * @throws QueryModelBuildException
     */
    private static Map<String, MetaCondition> buildQueryConditions(ReportDesignModel reportModel,
        ExtendArea area, QueryAction queryAction) throws QueryModelBuildException {
        Map<String, MetaCondition> rs = new HashMap<String, MetaCondition>();
        Map<Item, Object> items = new HashMap<Item, Object>();
        items.putAll(queryAction.getColumns());
        items.putAll(queryAction.getRows());
        items.putAll(queryAction.getSlices());
        int firstIndex = 0;
        for (Map.Entry<Item, Object> entry : items.entrySet()) {
            Item item = entry.getKey();
            OlapElement olapElement = ReportDesignModelUtils.getDimOrIndDefineWithId(reportModel.getSchema(),
                    area.getCubeId(), item.getOlapElementId());
            if (olapElement == null) {
                Cube cube = com.baidu.rigel.biplatform.ma.report.utils.QueryUtils.getCubeWithExtendArea(reportModel, area);
                for (Dimension dim : cube.getDimensions().values()) {
                    if (dim.getId().equals(item.getOlapElementId())) {
                        olapElement = dim;
                        break;
                    }
                }
            }
            if (olapElement == null) {
                continue;
            }
            if (olapElement instanceof Dimension) {
                DimensionCondition condition = new DimensionCondition(olapElement.getName());
                Object valueObj = entry.getValue();
                if (valueObj != null) {
                    List<String> values = Lists.newArrayList();
                    if (valueObj instanceof String[]) {
                        values = Lists.newArrayList();
                        String[] tmp = resetValues(olapElement.getName(), (String[]) valueObj);
                        CollectionUtils.addAll(values, (String[]) tmp);
                    } else {
                        String tmp = resetValues(olapElement.getName(), valueObj.toString())[0];
                        values.add(tmp);
                    }
                   
                    List<QueryData> datas = Lists.newArrayList();
                    // TODO 需要排查为何多处根节点UniqueName不一致
                    String rootUniqueName = "[" + olapElement.getName() + "].[All_" + olapElement.getName();
                    // TODO QeuryData value如何处理
                    for (String value : values) {
                        if (!queryAction.isChartQuery() && value.indexOf(rootUniqueName) != -1) {
                            datas.clear();
                            break;
                        }
                        QueryData data = new QueryData(value);
                        Object drillValue = queryAction.getDrillDimValues().get(item);
                        String tmpValue = null;
                        if (valueObj instanceof String[]) {
                            tmpValue = ((String[]) valueObj)[0];
                        } else {
                            tmpValue = valueObj.toString();
                        }
                        if (drillValue != null && tmpValue.equals(drillValue)) {
                            data.setExpand(true);
                        } else if ((item.getPositionType() == PositionType.X 
                            || item.getPositionType() == PositionType.S)
                                && queryAction.isChartQuery()) {
                            data.setExpand(true);
                            data.setShow(false);
                        }
                        // 修正展开方式
                        if (item.getParams().get(Constants.LEVEL) != null) {
                            if (item.getParams().get(Constants.LEVEL).equals(1)) {
                                data.setExpand(!queryAction.isChartQuery());
                                data.setShow(true);
                            } else if (item.getParams().get(Constants.LEVEL).equals(2)) {
                                data.setExpand(true);
                                data.setShow(false);
                            } 
                            if (MetaNameUtil.isAllMemberUniqueName(data.getUniqueName()) 
                                    && queryAction.isChartQuery()){
                                data.setExpand(true);
                                data.setShow(false);
                            }
                        }
                        datas.add(data);
                    } 
                    if (values.isEmpty() && queryAction.isChartQuery()) {
                        QueryData data = new QueryData(rootUniqueName + "s]");
                        data.setExpand(true);
                        data.setShow(false);
                        datas.add(data);
                    }
                    condition.setQueryDataNodes(datas);
                } else {
                    List<QueryData> datas = new ArrayList<QueryData>();
                    Dimension dim = (Dimension) olapElement;
                    if ((item.getPositionType() == PositionType.X || item.getPositionType() == PositionType.S)
                            && queryAction.isChartQuery()) {
                        QueryData data = new QueryData(dim.getAllMember().getUniqueName());
                        data.setExpand(true);
                        data.setShow(false);
                        datas.add(data);
                    } else if (dim.getType() == DimensionType.CALLBACK) {
                        QueryData data = new QueryData(dim.getAllMember().getUniqueName());
                        data.setExpand(firstIndex == 0);
                        data.setShow(firstIndex != 0);
                        datas.add(data);
                    }
                    condition.setQueryDataNodes(datas);
                }
                // 时间维度，并且在第一列位置，后续改成可配置方式
                if (item.getPositionType() == PositionType.X 
                    && olapElement instanceof TimeDimension && firstIndex == 0 && !queryAction.isChartQuery()) {
                    condition.setMemberSortType(SortType.DESC);
                    ++firstIndex;
                }
                rs.put(condition.getMetaName(), condition);
            }
        }
        return rs;
    }
    
    private static String[] resetValues(String dimName, String... valueObj) {
        if (valueObj == null) {
            return null;
        }
        String[] rs = new String[valueObj.length];
        int i = 0;
        for (String str : valueObj) {
            if (!MetaNameUtil.isUniqueName(str)) {
                rs[i] = "[" + dimName + "].[" + str + "]";
            } else {
                rs[i] = str;
            }
            ++i;
        }
        return rs;
    }

    /**
     * 通过查询
     * 
     * @param reportModel
     * @param area
     * @param queryAction
     * @return Map<AxisType, AxisMeta>
     */
    private static Map<AxisType, AxisMeta> buildAxisMeta(Schema schema,
        ExtendArea area, QueryAction queryAction) throws QueryModelBuildException {
        Map<Item, Object> columns = queryAction.getColumns();
        Map<AxisType, AxisMeta> rs = new HashMap<AxisType, AxisMeta>();
        AxisMeta columnMeta = buildAxisMeta(schema, area, columns, AxisType.COLUMN);
        rs.put(columnMeta.getAxisType(), columnMeta);
        
        Map<Item, Object> rows = queryAction.getRows();
        AxisMeta rowMeta = buildAxisMeta(schema, area, rows, AxisType.ROW);
        rs.put(rowMeta.getAxisType(), rowMeta);
        
        AxisMeta filterMeta = buildAxisMeta(schema, area, queryAction.getSlices(),
                AxisType.FILTER);
        rs.put(filterMeta.getAxisType(), filterMeta);
        return rs;
    }
    
    /**
     * 
     * @param schema
     * @param area
     * @param items
     * @param axisType
     * @return
     * @throws QueryModelBuildException
     */
    private static AxisMeta buildAxisMeta(Schema schema, ExtendArea area,
        Map<Item, Object> items, AxisType axisType) throws QueryModelBuildException {
        AxisMeta meta = new AxisMeta(axisType);
        for (Map.Entry<Item, Object> entry : items.entrySet()) {
            Item item = entry.getKey();
            OlapElement olapElement = ReportDesignModelUtils.getDimOrIndDefineWithId(schema,
                    area.getCubeId(), item.getOlapElementId());
            if (olapElement == null) {
                continue;
            }
            if (olapElement instanceof Dimension) {
                meta.getCrossjoinDims().add(olapElement.getName());
            } else {
                meta.getQueryMeasures().add(olapElement.getName());
            }
        }
        return meta;
    }
    
    /**
     * 
     * @param reportModel
     * @param area
     * @return
     * @throws QueryModelBuildException
     */
    private static Cube getCubeFromReportModel(ReportDesignModel reportModel, ExtendArea area)
            throws QueryModelBuildException {
        String cubeId = area.getCubeId();
        if (StringUtils.isEmpty(cubeId)) {
            throw new QueryModelBuildException("cube id is empty");
        }
        Schema schema = reportModel.getSchema();
        if (schema == null) {
            throw new QueryModelBuildException("schema is not define");
        }
        Map<String, ? extends Cube> cubes = schema.getCubes();
        if (cubes == null) {
            throw new QueryModelBuildException("can not get cube define from schema : " + schema.getId());
        }
        Cube oriCube = cubes.get(area.getCubeId());
        if (oriCube == null) {
            throw new QueryModelBuildException("can not get cube define from schema : " + area.getCubeId());
        }
        return oriCube;
    }
    
    /**
     * 获取扩展区域包含的立方体定义
     * 
     * @param reportModel
     *            报表模型
     * @param area
     *            扩展区域
     * @return 立方体定义
     * @throws QueryModelBuildException
     */
    public static Cube getCubeWithExtendArea(ReportDesignModel reportModel, ExtendArea area)
        throws QueryModelBuildException {
        Cube oriCube = getCubeFromReportModel(reportModel, area);
        Map<String, List<Dimension>> filterDims = collectFilterDim(reportModel);
        MiniCube cube = new MiniCube(area.getCubeId());
        String areaId = area.getId();
        LogicModel logicModel = area.getLogicModel();
        if (area.getType() == ExtendAreaType.SELECTION_AREA
                || area.getType() == ExtendAreaType.LITEOLAP_CHART 
                || area.getType() == ExtendAreaType.LITEOLAP_TABLE) {
            LiteOlapExtendArea liteOlapArea = (LiteOlapExtendArea) reportModel.getExtendById(area.getReferenceAreaId());
            logicModel = liteOlapArea.getLogicModel();
            areaId = area.getReferenceAreaId();
        }
        if (logicModel == null) {
            throw new QueryModelBuildException("logic model is empty");
        }
        Item[] items = logicModel.getItems(area.getType() != ExtendAreaType.TABLE);
        Map<String, Dimension> dimensions = new HashMap<String, Dimension>();
        Map<String, Measure> measures = new HashMap<String, Measure>();
        
        for (Item item : items) {
            OlapElement olapElement = oriCube.getDimensions().get(item.getOlapElementId());
            if (olapElement == null) { // 维度不存在或者可能是指标信息
                olapElement = oriCube.getMeasures().get(item.getOlapElementId());
                if (olapElement != null) {
                    Measure measure = (Measure) olapElement;
                    measures.put(measure.getName(), measure);
                } 
            } else {
                MiniCubeDimension dim = (MiniCubeDimension) DeepcopyUtils.deepCopy(olapElement);
                dim.setLevels(Maps.newLinkedHashMap());;
                ((Dimension) olapElement).getLevels().values().forEach(level -> {
                    level.setDimension(dim);
                    dim.getLevels().put(level.getName(), level);
                });
                dimensions.put(dim.getName(), dim);
            }
        }
        if (area.getType() == ExtendAreaType.LITEOLAP) {
            /**
             * TODO 把liteOlap中候选的维度和指标加入到items里面
             */
            Map<String, Item> candDims = ((LiteOlapExtendArea) area).getCandDims();
            for (String elementId : candDims.keySet()) {
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(reportModel.getSchema(),
                        area.getCubeId(), elementId);
                MiniCubeDimension dim = (MiniCubeDimension) DeepcopyUtils.deepCopy(element);
                dim.setLevels(Maps.newLinkedHashMap());
                ((Dimension) element).getLevels().values().forEach(level -> {
                    level.setDimension(dim);
                    dim.getLevels().put(level.getName(), level);
                });
                dimensions.put(element.getName(), (Dimension) element);
            }
            Map<String, Item> candInds = ((LiteOlapExtendArea) area).getCandInds();
            for (String elementId : candInds.keySet()) {
                OlapElement element = ReportDesignModelUtils.getDimOrIndDefineWithId(reportModel.getSchema(),
                        area.getCubeId(), elementId);
                if (element instanceof CallbackMeasure) {
                	CallbackMeasure m = DeepcopyUtils.deepCopy((CallbackMeasure) element);
                	String url = ((CallbackMeasure) element).getCallbackUrl();
                	m.setCallbackUrl(HttpUrlUtils.getBaseUrl(url));
                	m.setCallbackParams(HttpUrlUtils.getParams(url));
                	measures.put(m.getName(), m);
                } else {
                	measures.put(element.getName(), (Measure) element);
                }
            }
        }
        if (filterDims != null ) { // && filterDims.get(area.getCubeId()) != null) {
            List<Dimension> dims = filterDims.get(area.getCubeId());
            if (dims != null) {
                for(Dimension dim : dims) {
                    if (dim != null) {
                        dimensions.put(dim.getName(), dim);
                    }
                }
            }
            
            // TODO 处理不同cube共用同一查询条件情况
            filterDims.forEach((key, dimArray) -> {
                if (key != null && !key.equals(area.getCubeId())) {
                    dimArray.stream().filter(dim -> {
                        return dim instanceof TimeDimension;
                    }).forEach(dim -> {
                        for (Dimension tmp : oriCube.getDimensions().values()) {
                            if (dim.getName().equals(tmp.getName())) {
                                MiniCubeDimension tmpDim = (MiniCubeDimension) DeepcopyUtils.deepCopy(dim);
                                tmpDim.setLevels((LinkedHashMap<String, Level>) tmp.getLevels());
                                tmpDim.setFacttableColumn(tmp.getFacttableColumn());
                                tmpDim.setFacttableCaption(tmp.getFacttableCaption());
                                dimensions.put(tmpDim.getName(), tmpDim);
                            }
                        }
                    });
                }
            });
        }
        cube.setDimensions(dimensions);
        modifyMeasures(measures, oriCube);
        cube.setMeasures(measures);
        cube.setSource(((MiniCube) oriCube).getSource());
        cube.setPrimaryKey(((MiniCube) oriCube).getPrimaryKey());
        cube.setId(oriCube.getId() + "_" + areaId);
        return cube;
    }

    /**
     * 修正measure，将measure引用的measure放到cube中
     * @param measures
     * @param oriCube
     */
    private static void modifyMeasures(Map<String, Measure> measures, Cube oriCube) {
        Set<String> refMeasuers = Sets.newHashSet();
        measures.values().stream().filter(m -> {
            return m.getType() == MeasureType.CAL || m.getType() == MeasureType.RR || m.getType() == MeasureType.SR;
        }).forEach(m -> {
            ExtendMinicubeMeasure tmp = (ExtendMinicubeMeasure) m;
            if (m.getType() == MeasureType.CAL) {
                refMeasuers.addAll(PlaceHolderUtils.getPlaceHolderKeys(tmp.getFormula()));
            } else {
                final String refName = m.getName().substring(0, m.getName().length() - 3);
                refMeasuers.add(refName);
                if (m.getType() == MeasureType.RR) {
                    tmp.setFormula("rRate(${" + refName + "})");
                } else if (m.getType() == MeasureType.SR) {
                    tmp.setFormula("sRate(${" + refName + "})");
                }
            }
            tmp.setAggregator(Aggregator.CALCULATED);
        });
        refMeasuers.stream().filter(str -> {
            return !measures.containsKey(str);
        }).map(str -> {
            Set<Map.Entry<String, Measure>> entry = oriCube.getMeasures().entrySet();
            for (Map.Entry<String, Measure> tmp : entry) {
                if (str.equals(tmp.getValue().getName())) {
                    return tmp.getValue();
                }
            }
            return null;
        }).forEach(m -> {
            if (m != null) {
                measures.put(m.getName(), m);
            }
        });
    }

    /**
     * 
     * @param dim -- Dimension
     * @return Dimension
     */
    public static Dimension convertDim2Dim(Dimension dim) {
        StandardDimension rs = new StandardDimension(dim.getName());
        rs.setCaption(dim.getCaption());
        rs.setDescription(dim.getDescription());
        rs.setTableName(dim.getTableName());
        rs.setFacttableCaption(dim.getFacttableCaption());
        rs.setFacttableColumn(dim.getFacttableColumn());
        rs.setPrimaryKey(dim.getPrimaryKey());
        rs.setType(dim.getType());
        rs.setVisible(true);
        rs.setId(dim.getId());
        rs.setName(dim.getName());
        LinkedHashMap<String, Level> levels = Maps.newLinkedHashMap();
        dim.getLevels().values().forEach(level -> {
            level.setDimension(dim);
            levels.put(level.getName(), level);
        });
        rs.setLevels(levels);
        return rs;
    }

    /**
     * 
     * @param model
     * @return Map<String, List<Dimension>>
     */
    private static Map<String, List<Dimension>> collectFilterDim(ReportDesignModel model) {
        Map<String, List<Dimension>> rs = Maps.newHashMap();
        for (ExtendArea area : model.getExtendAreaList()) {
            if (isFilterArea(area.getType())) {
                Cube cube = model.getSchema().getCubes().get(area.getCubeId());
                if (rs.get(area.getCubeId()) == null) {
                    List<Dimension> dims = Lists.newArrayList();
                    area.listAllItems().values().forEach(key -> {
                        MiniCubeDimension dim = (MiniCubeDimension) 
                                DeepcopyUtils.deepCopy(cube.getDimensions().get(key.getId()));
                        dim.setLevels(Maps.newLinkedHashMap());;
                        cube.getDimensions().get(key.getId()).getLevels().values().forEach(level ->{
                            dim.getLevels().put(level.getName(), level);
                        });
                        dims.add(dim);
                    });
                    rs.put(area.getCubeId(), dims);
                } else {
                    area.listAllItems().values().forEach(key -> {
                        MiniCubeDimension dim = (MiniCubeDimension) 
                                DeepcopyUtils.deepCopy(cube.getDimensions().get(key.getId()));
                        dim.setLevels(Maps.newLinkedHashMap());;
                        cube.getDimensions().get(key.getId()).getLevels().values().forEach(level ->{
                            dim.getLevels().put(level.getName(), level);
                        });
                        rs.get(area.getCubeId()).add(dim);
                    });
                }
            } 
        }
        return rs;
    }

    /**
     * 
     * @param type
     * @return boolean
     * 
     */
    public static boolean isFilterArea(ExtendAreaType type) {
        return type == ExtendAreaType.TIME_COMP 
                || type == ExtendAreaType.SELECT 
                || type == ExtendAreaType.MULTISELECT
                || type == ExtendAreaType.SINGLE_DROP_DOWN_TREE;
    }

    /**
     * trans cube
     * @param cube
     * @return new Cube
     */
    public static Cube transformCube(Cube cube) {
        MiniCube newCube = (MiniCube) DeepcopyUtils.deepCopy(cube);
        final Map<String, Measure> measures = Maps.newConcurrentMap();
        cube.getMeasures().values().forEach(m -> {
            measures.put(m.getName(), m);
        });
        newCube.setMeasures(measures);
        final Map<String, Dimension> dimensions = Maps.newLinkedHashMap();
        cube.getDimensions().values().forEach(dim -> {
            MiniCubeDimension tmp = (MiniCubeDimension) DeepcopyUtils.deepCopy(dim);
            LinkedHashMap<String, Level> tmpLevel = Maps.newLinkedHashMap();
            dim.getLevels().values().forEach(level -> {
                level.setDimension (dim);
                tmpLevel.put(level.getName(), level);
            });
            tmp.setLevels(tmpLevel);
            dimensions.put(tmp.getName(), tmp);
        });
        newCube.setDimensions(dimensions);
        return newCube;
    }

    /**
     * decorate chart with extend area
     * @param chart
     * @param area
     * @param index 
     */
    public static void decorateChart(DIReportChart chart, ExtendArea area, Schema schema, int index) {
        if (area.getType() == ExtendAreaType.CHART) {
            assert area.getLogicModel () != null : "当前区域未设置逻辑模型";
            // 设置topN默认设置
            if (area.getLogicModel().getTopSetting() != null) {
                MeasureTopSetting topSetting = area.getLogicModel().getTopSetting();
                chart.setRecordSize(topSetting.getRecordSize());
                chart.setTopedMeasureId(topSetting.getMeasureId());
                chart.setTopType(topSetting.getTopType().name());
                chart.setAreaId(area.getId());
            }
            FormatModel formatModel = area.getFormatModel ();
            if (formatModel != null && formatModel.getDataFormat () != null) {
                addDataFormatInfo(chart, formatModel.getDataFormat ());
                Map<String, String> colorFormat = formatModel.getColorFormat ();
                if (colorFormat != null && !colorFormat.isEmpty () && chart.getSeriesData () != null) {
                    for (SeriesDataUnit data : chart.getSeriesData ()) {
                        if (data == null) {
                            continue;
                        }
                        data.setColorDefine (colorFormat.get (data.getyAxisName ()));
                    }
                }
                Map<String, String> positions = formatModel.getPositions ();
                if (colorFormat != null && !positions.isEmpty () && chart.getSeriesData () != null) {
                    for (SeriesDataUnit data : chart.getSeriesData ()) {
                        if (data == null) {
                            continue;
                        }
                        data.setPosition (positions.get (data.getyAxisName ()));
                    }
                }
            }
            final Map<String, String> dimMap = Maps.newConcurrentMap();
            String[] allDims = area.getLogicModel().getSelectionDims().values().stream().map(item -> {
                OlapElement tmp = getOlapElement(area, schema, item);
                if (tmp != null) {
                    dimMap.put(tmp.getId(), tmp.getName());
                    return tmp.getCaption();
                } else {
                    return null;
                }
            }).filter(x -> x != null).toArray(String[] :: new);
            chart.setDimMap(dimMap);
            chart.setAllDims(allDims);
            String[] allMeasures = area.getLogicModel().getSelectionMeasures().values().stream().map(item -> {
                OlapElement tmp = getOlapElement(area, schema, item);
                if (tmp != null) {
                    chart.getMeasureMap().put(tmp.getId(), tmp.getCaption());
                    return tmp.getCaption();
                } else {
                    return null;
                }
            }).filter(x -> x != null).toArray(String[] :: new);
            chart.setAllMeasures(allMeasures);
            
            final Item[] columns = area.getLogicModel().getColumns();
            List<String> tmp = getOlapElementNames(
                    columns, area.getCubeId(), schema);
            if (tmp.size() > 0) {
                chart.setDefaultMeasures(tmp.toArray(new String[0]));
            }
            for (int i = 0; i < columns.length; ++i) {
                chart.getMeasureMap().put(columns[i].getOlapElementId(), tmp.get(i));
            }
//            List<String>  defaultDims = getOlapElementNames(
//                    area.getLogicModel().getRows(), area.getCubeId(), schema);
            if (index >= 0 && index < chart.getAllMeasures().length) {
            		chart.setDefaultMeasures(new String[]{ chart.getAllMeasures()[index] });
            } 
//            else {
//	            	if (defaultDims.size() > 0) {
//	            		chart.setDefaultDims(defaultDims.toArray(new String[0]));
//	            	}
//            }
        } 
    }

    private static void addDataFormatInfo(DIReportChart chart,
            Map<String, String> dataFormat) {
        if (chart.getSeriesData () == null || chart.getSeriesData ().isEmpty ()) {
            return;
        }
        for (SeriesDataUnit seriesData : chart.getSeriesData ()) {
            if (seriesData == null) {
                continue;
            }
            seriesData.setFormat (dataFormat.get (seriesData.getyAxisName ()));
        }
    }

    /**
     * @param area
     * @param schema
     * @return
     */
    private static List<String> getOlapElementNames(Item[] items, String cubeId, Schema schema) {
        List<String> tmp = Lists.newArrayList();
        if (items == null || items.length == 0) {
            return tmp;
        }
        for (Item item : items) {
            OlapElement olapElement = 
                    ReportDesignModelUtils.getDimOrIndDefineWithId(schema, cubeId, item.getOlapElementId());
            tmp.add(olapElement.getCaption());
        }
        return tmp;
    }

    /**
     * 
     * @param area
     * @param schema
     * @param item
     * @return String
     * 
     */
    private static OlapElement getOlapElement(ExtendArea area, Schema schema,
            Item item) {
        OlapElement olapElement = 
                ReportDesignModelUtils.getDimOrIndDefineWithId(schema, area.getCubeId(), item.getOlapElementId());
        if (olapElement != null) {
            return olapElement;
        }
        return null;
    }

    /**
     * 修正报表区域模型参数
     * @param request
     * @param model
     */
    public static Map<String, Object> resetContextParam(final HttpServletRequest request, ReportDesignModel model) {
        Map<String, Object> rs = Maps.newHashMap();
        Collection<ReportParam> params = DeepcopyUtils.deepCopy(model.getParams()).values();
        if (params.size() == 0) {
            return rs;
        }
        LOG.info ("context params ============== " + ContextManager.getParams ());
        Map<String, String> requestParams = collectRequestParams(params, request);
        rs.putAll(requestParams);
        LOG.info ("current request params ============== " + requestParams);
        params.forEach(param -> {
            LOG.info ("current param define ============== " + param.toString());
            if (param.isNeeded() && StringUtils.isEmpty(requestParams.get(param.getName()))) {
                if (StringUtils.isEmpty(param.getDefaultValue())) {
                    throw new RuntimeException("必要参数未赋值");
                }
                rs.put(param.getElementId(), param.getDefaultValue());
                rs.put(param.getName(), param.getDefaultValue());
            } else if (!StringUtils.isEmpty(requestParams.get(param.getName()))) {
                rs.put(param.getElementId(), requestParams.get(param.getName()));
            } else if (!StringUtils.isEmpty(param.getDefaultValue())) {
                rs.put(param.getElementId(), param.getDefaultValue());
                rs.put(param.getName(), param.getDefaultValue());
            }
        });
        LOG.info ("after reset params is : " + rs);
        return rs;
    }

    /**
     * 
     * @param params
     * @param request
     * @return Map<String, String>
     */
    private static Map<String, String> collectRequestParams(Collection<ReportParam> params,
            HttpServletRequest request) {
        Map<String, String> rs = Maps.newHashMap();
        request.getParameterMap().forEach((k, v) -> {
            rs.put(k, v[0]);
        }); 
        // cookie中如果包含参数值，覆盖url中参数
        if (request.getCookies() != null)  {
            for (Cookie cookie : request.getCookies()) {
                rs.put(cookie.getName(), cookie.getValue());
            }
        }
        
        // 如果当前线程中包含参数值，则覆盖cookie中参数值
        rs.putAll(ContextManager.getParams());
        // 容错，处理其他可能的参数
        rs.remove(Constants.RANDOMCODEKEY);
        rs.remove(Constants.TOKEN);
        rs.remove(Constants.BIPLATFORM_PRODUCTLINE);

        return rs;
    }

    /**
     * TODO:
     * @param members
     * @return List<Map<String, String>>
     */
    public static List<Map<String, String>> getMembersWithChildrenValue(List<Member> members,
            Cube cube, DataSourceInfo dataSource, Map<String, String> params) {
        List<Map<String, String>> rs = Lists.newArrayList();
        if (members == null || members.isEmpty()) {
            return rs;
        }
        members.forEach(m -> {
            Map<String, String> tmp = Maps.newHashMap();
            tmp.put("value", m.getUniqueName());
            tmp.put("text", m.getCaption());
            Member parent = m.getParentMember(cube, dataSource, params);
            if (parent != null) {
                tmp.put("parent", parent.getUniqueName());
            }
            rs.add(tmp);
            List<Member> children = m.getChildMembers(cube, dataSource, params);
            if (children != null) {
                rs.addAll(getMembersWithChildrenValue(children, cube, dataSource, params));
            }
        });
        
        return rs;
    }
    

}
