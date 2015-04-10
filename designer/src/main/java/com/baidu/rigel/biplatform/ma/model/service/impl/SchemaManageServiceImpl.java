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
package com.baidu.rigel.biplatform.ma.model.service.impl;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baidu.rigel.biplatform.ac.minicube.CallbackMeasure;
import com.baidu.rigel.biplatform.ac.minicube.ExtendMinicubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeDimension;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeLevel;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.Level;
import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ac.util.DeepcopyUtils;
import com.baidu.rigel.biplatform.ma.auth.bo.CalMeasureViewBo;
import com.baidu.rigel.biplatform.ma.model.consts.Constants;
import com.baidu.rigel.biplatform.ma.model.service.SchemaManageService;
import com.baidu.rigel.biplatform.ma.model.utils.HttpUrlUtils;
import com.baidu.rigel.biplatform.ma.model.utils.UuidGeneratorUtils;

/**
 * 
 * the {@link SchemaManageService} sub class, implement all the abstract method defined in {@link SchemaManageService}.
 * 
 *  <p>design for manage {@link Schema} which description 
 * the logic model of business subject areas
 *      Already know sub class :
 *          None
 *  </p>
 * @see com.baidu.rigel.biplatform.ac.model.Dimension
 * @see com.baidu.rigel.biplatform.ac.model.Measure
 * @see com.baidu.rigel.biplatform.ac.model.Schema 
 * @since jdk1.8.5 or after
 * @version Silkroad1.0.1
 * @author david.wang
 * 
 * 
  */
@Service("schemaManageService")
public class SchemaManageServiceImpl implements SchemaManageService {
    
    /**
     * logger
     */
    private Logger logger = LoggerFactory.getLogger(SchemaManageServiceImpl.class);
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema modifyDimension(Schema schema, String cubeId, Dimension dim) {
        // check input make sure all inputs validate
        logger.info("begin modify dimension operation");
        if (!checkValidate(schema, cubeId, dim)) {
            return null;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.error("can not get cube with id : " + cubeId);
            return null;
        }
        Dimension oldDim = cube.getDimensions().get(dim.getId());
        if (oldDim == null) {
            logger.error("can not get ori dim with id : " + dim.getId());
            return null;
        }
        if (StringUtils.isEmpty(dim.getCaption())) {
            logger.error("dim caption is empty");
            return null;
        }
        // only can modify the caption
        ((MiniCubeDimension) oldDim).setCaption(dim.getCaption());
        
        logger.info("modify dim successfully successfully. Schema {} ", schema);
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema modifyMeasure(Schema schema, String cubeId, Measure measure) {
        logger.info("begin modify schema measures");
        if (!checkValidate(schema, cubeId, measure)) {
            return null;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.error("can not get cube with id : " + cubeId);
            return null;
        }
        MiniCubeMeasure oldMeasure = (MiniCubeMeasure) cube.getMeasures().get(measure.getId());
        if (oldMeasure == null) {
            logger.error("can not get ori measure with id : " + measure.getId());
            return null;
        }
        if (!StringUtils.isEmpty(measure.getCaption())) {
            oldMeasure.setCaption(measure.getCaption());
        }
        if (measure.getAggregator() != null) {
            oldMeasure.setAggregator(measure.getAggregator());
        }
        logger.info("successfully modify schema measure. Measure {} ", schema);
        return schema;
    }
    
    /**
     * check the input is empty or not 
     * 
     * @param schema -- schema
     * @param cubeId -- cube id
     * @param measure -- measure
     * @return if all inputs validate return true or false
     */
    private boolean checkValidate(Schema schema, String cubeId, OlapElement measure) {
        if (schema == null) {
            logger.error("schema is null");
            return false;
        }
        if (StringUtils.isEmpty(cubeId)) {
            logger.error("cube id is null");
            return false;
        }
        if (measure == null) {
            logger.error("measure is null");
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema converMeasure2Dim(Schema schema, String cubeId, Measure measure) {
        if (!checkValidate(schema, cubeId, measure)) {
            return null;
        }
        MiniCube cube = (MiniCube) schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can not get cube with id : " + cubeId);
            return null;
        }
        Measure oldMeasure = cube.getMeasures().get(measure.getId());
        if (oldMeasure == null) {
            logger.info("can not get measure with id : " + measure.getId());
            return null;
        }
        
        // remove the measure from cube's measures
        Map<String, Measure> newMeasures = new LinkedHashMap<String, Measure>();
        newMeasures.putAll(cube.getMeasures());
        measure = newMeasures.remove(measure.getId());
        cube.setMeasures(newMeasures);
        
        StandardDimension dim = new StandardDimension(oldMeasure.getName());
        dim.setCaption(oldMeasure.getCaption());
        dim.setId(oldMeasure.getId());
        dim.setPrimaryKey(oldMeasure.getName());
        dim.setTableName(cube.getSource());
        dim.setVisible(true);
        dim.setFacttableColumn(oldMeasure.getName());
        MiniCubeLevel level = new MiniCubeLevel(measure.getName());
        level.setId(UuidGeneratorUtils.generate());
        level.setCaption(measure.getCaption());
        level.setDimension(dim);
        level.setDimTable(cube.getSource());
        level.setPrimaryKey(measure.getDefine());
        level.setVisible(true);
        level.setType(LevelType.REGULAR);
        level.setSource(measure.getDefine());
        level.setFactTableColumn(measure.getDefine());
        dim.addLevel(level);
        Map<String, Dimension> newDims = new LinkedHashMap<String, Dimension>();
        newDims.putAll(cube.getDimensions());
        newDims.put(dim.getId(), dim);
        cube.setDimensions(newDims);
        logger.info("conver measure to dimension operation successfully : " + schema);
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema convertDim2Measure(Schema schema, String cubeId, Dimension dim) {
        if (!checkValidate(schema, cubeId, dim)) {
            return null;
        }
        MiniCube cube = (MiniCube) schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can not get cube with id : " + cubeId);
            return null;
        }
        Dimension oldDim = cube.getDimensions().get(dim.getId());
        if (oldDim == null) {
            logger.info("can not get dim define with id : " + dim.getId());
            return null;
        }
        Map<String, Dimension> newDims = new LinkedHashMap<String, Dimension>();
        newDims.putAll(cube.getDimensions());
        newDims.remove(oldDim.getId());
        cube.setDimensions(newDims);
        
        Map<String, Measure> newMeasures = new LinkedHashMap<String, Measure>();
        newMeasures.putAll(cube.getMeasures());
        MiniCubeMeasure measure = new MiniCubeMeasure(oldDim.getFacttableColumn());
        measure.setAggregator(Aggregator.SUM);
        measure.setCaption(oldDim.getCaption());
        measure.setDefine(oldDim.getFacttableColumn());
        measure.setId(oldDim.getId());
        measure.setType(MeasureType.COMMON);
        measure.setCube(cube);
        measure.setVisible(true);
        newMeasures.put(measure.getId(), measure);
        cube.setMeasures(newMeasures);
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema removeDimention(Schema schema, String cubeId, String dimId) {
        if (!checkValid(schema, cubeId)) {
            return null;
        }
        if (StringUtils.isEmpty(dimId)) {
            return null;
        }
        MiniCube cube = (MiniCube) schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can not get cube with id : " + cubeId);
            return null;
        }
        Dimension dimension = cube.getDimensions().get(dimId);
        if (dimension == null) {
            logger.info("can't get dimension with id : " + dimId);
            return null;
        }
        Map<String, Dimension> newDims = new LinkedHashMap<String, Dimension>();
        newDims.putAll(cube.getDimensions());
        newDims.remove(dimId);
        cube.setDimensions(newDims);
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema addDimIntoDimGroup(Schema schema, String cubeId, String groupDimId,
            String... dimId) {
        Map<String, Dimension> dims = getDimensions(schema, cubeId, groupDimId, dimId);
        if (dims == null) {
            logger.info("current cube's dim is empty");
            return null;
        }
        StandardDimension dimGroup = (StandardDimension) dims.get(groupDimId);
        if (dimGroup == null) {
            logger.info("can not get dim from cube with dim id : " + groupDimId);
            return null;
        }
        
        for (String id : dimId) {
            if (groupDimId.equals(id)) {
                continue;
            }
            Dimension dim = dims.get(id);
            if (dim.getLevels() == null) {
                continue;
            }
            for (Map.Entry<String, Level> entry : dim.getLevels().entrySet()) {
                dimGroup.addLevel(entry.getValue());
            }
        }
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema removeDimFromGroup(Schema schema, String cubeId, String groupDimId,
            String... dimIds) {
        Map<String, Dimension> dims = getDimensions(schema, cubeId, groupDimId, dimIds);
        if (dims == null) {
            StringBuilder info = new StringBuilder();
            info.append("current cube's dim is empty : cubeId = " + cubeId);
            info.append(", groupDimId = " + groupDimId);
            info.append(", dimIds = [");
            for (String dimId : dimIds) {
                info.append(dimId + ";");
            }
            info.append("]");
            logger.info(info.toString());
            return null;
        }
        StandardDimension dimGroup = (StandardDimension) dims.get(groupDimId);
        if (dimGroup == null) {
            logger.info("can not get dim from cube with dim id : " + groupDimId);
            return null;
        }
        Map<String, Level> levels = dimGroup.getLevels();
        if (levels == null) {
            logger.info("level is empty");
            return null;
        }
        Map<String, Level> tmp = new LinkedHashMap<String, Level>();
        tmp.putAll(levels);
        for (String id : dimIds) {
            tmp.remove(id);
        }
        dimGroup.clearLevels();
        for (Map.Entry<String, Level> entry : tmp.entrySet()) {
            dimGroup.addLevel(entry.getValue());
        }
        return schema;
    }
    
    /**
     * get {@link Dimension} define from schems's cubes with cube id
     * @param schema -- schema
     * @param cubeId -- cubeId
     * @param groupDimId -- group dimension id
     * @param dimIds -- dimension id list
     * @return if all input validate return  dimensions else null
     * 
     */
    private Map<String, Dimension> getDimensions(Schema schema, String cubeId, String groupDimId,
            String... dimIds) {
        if (!this.checkValid(schema, cubeId)) {
            return null;
        }
        if (StringUtils.isEmpty(groupDimId)) {
            logger.info("dim group id is null");
            return null;
        }
        if (dimIds == null) {
            logger.info("dim id is null");
            return null;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can not get cube with id : " + cubeId);
            return null;
        }
        Map<String, Dimension> dims = cube.getDimensions();
        return dims;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema createDimGroup(Schema schema, String cubeId, String name) {
        if (!checkValid(schema, cubeId)) {
            return null;
        }
        String id = UuidGeneratorUtils.generate();
        StandardDimension dim = new StandardDimension(name);
        dim.setCaption(name);
        dim.setId(id);
        dim.setVisible(true);
        dim.setType(DimensionType.GROUP_DIMENSION);
        MiniCube cube = (MiniCube) schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can't get cube with id : " + cubeId);
            return null;
        }
        Map<String, Dimension> newDims = new LinkedHashMap<String, Dimension>();
        newDims.putAll(cube.getDimensions());
        newDims.put(dim.getId(), dim);
        cube.setDimensions(newDims);
        return schema;
    }
    
    /**
     * check input either empty or not
     * 
     * @param schema -- schema
     * @param cubeId -- cube id
     * @return if not empty return true else false
     */
    private boolean checkValid(Schema schema, String cubeId) {
        if (schema == null) {
            logger.info("schema is null");
            return false;
        }
        if (StringUtils.isEmpty(cubeId)) {
            logger.info("cube id is empty");
            return false;
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema modifyIndAndDimVisibility(Schema schema, String cubeId, List<Dimension> dims,
            List<Measure> measures) {
        if (!checkValid(schema, cubeId)) {
            return null;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can not get cube with cubeId : " + cubeId);
            return null;
        }
        if (dims != null) {
            Map<String, Dimension> oldDims = cube.getDimensions();
            for (Dimension tmp : dims) {
                Dimension dim = oldDims.get(tmp.getId());
                if (dim == null) {
                    continue;
                }
                ((MiniCubeDimension) dim).setVisible(!dim.isVisible());
            }
        }
        if (measures != null) {
            Map<String, Measure> oldMeasures = cube.getMeasures();
            for (Measure measure : measures) {
                Measure m = oldMeasures.get(measure.getId());
                if (m == null) {
                    continue;
                }
                ((MiniCubeMeasure) m).setVisible(!m.isVisible());
            }
        }
        return schema;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Schema modifyDimOrder(Schema schema, String cubeId, String dimId, String before,
            String target) {
        if (!checkValid(schema, cubeId)) {
            return null;
        }
        Cube cube = schema.getCubes().get(cubeId);
        if (cube == null) {
            logger.info("can not get cube with cubeId : " + cubeId);
            return null;
        }
        StandardDimension dim = (StandardDimension) cube.getDimensions().get(dimId);
        if (dim == null) {
            logger.info("can not get dimension with id : " + dimId);
            return null;
        }
        Map<String, Level> levels = dim.getLevels();
        if (levels == null || levels.isEmpty()) {
            logger.info("current dimension's levels is empty");
            return null;
        }
        Level targetLevel = levels.get(target);
        if (targetLevel == null) {
            logger.info("can not get target level ");
            return null;
        }
        dim.clearLevels();
        if (StringUtils.isEmpty(before) || "-1".equals(before)) {
            /**
             * -1 and "" means the first level
             */
            dim.addLevel(targetLevel);
        }
        Iterator<Entry<String, Level>> it = levels.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Level> entry = it.next();
            if (entry.getKey().equals(target)) { // if equals target, ignore current loop
                continue;
            }
            dim.addLevel(entry.getValue());
            if (entry.getKey().equals(before)) { // find the correct place, add it
                dim.addLevel(targetLevel);
            }
            
        }
        return schema;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema delExtendMeasure(Schema schema, String cubeId, String measureId) {
        Cube cube = getCubeWithId(schema, cubeId);
        // remove the measure
        final Measure m = cube.getMeasures().remove(measureId);
        // remove the reference, include extend area and other extend measures
        removeRelatedMeasuers(schema, cube, m);
        return schema;
    }

    /**
     * 
     * get {@link Cube} from {@link Schema} with cube id
     * @param schema -- schema
     * @param cubeId -- cube id
     * @return if exist return cube throw IllegalStateException exception
     * 
     */
    private Cube getCubeWithId(Schema schema, String cubeId) {
        Cube cube = null;
        try {
            cube = schema.getCubes().get(cubeId);
        } catch (Throwable e) {
            logger.error("something happend when get cube(cubeId : {}) "
                    + "from report schema[{}]", cubeId, schema);
            throw new IllegalStateException(e);
        }
        
        if (cube == null) {
            logger.error("can not get cube with id {} from report model {}", cubeId, schema);
            throw new IllegalStateException("can not get cube with id " + cubeId);
        }
        
        if (cube.getMeasures() == null || cube.getMeasures().isEmpty()) {
            throw new IllegalStateException("cube's measures is empty");
        }
        return cube;
    }

    /**
     * delete the measure form schema's cube
     * @param schema -- schema
     * @param cube -- cube
     * @param m -- measure
     */
    private void removeRelatedMeasuers(Schema schema, Cube cube, Measure m) {
        
        Map<String, Measure> measures = DeepcopyUtils.deepCopy(cube.getMeasures());
        measures.values().stream().filter(measure -> {
            return measure instanceof ExtendMinicubeMeasure 
                    && !((ExtendMinicubeMeasure) measure).getRefIndNames().contains(m.getName())
                        && measure.getId().equals(m.getId());
        }).forEach(measure -> {
            cube.getMeasures().remove(measure.getId());
        });
                
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema addOrModifyExtendMeasure(Schema schema, String cubeId,
            CalMeasureViewBo extendMeasure) {
        MiniCube cube = (MiniCube) getCubeWithId(schema, cubeId);
        if (extendMeasure == null) {
            return null;
        }
        extendMeasure.getCals().stream().filter(cal -> {
            return !StringUtils.isEmpty(cal.getCaption()) && !StringUtils.isEmpty(cal.getFormula());
        }).forEach(cal -> {
            if (StringUtils.isEmpty(cal.getId())) {
                ExtendMinicubeMeasure m = new ExtendMinicubeMeasure(cal.getCaption());
                m.setAggregator(Aggregator.SUM);
                m.setCaption(cal.getCaption());
                m.setCube(cube);
                m.setDefine(m.getName());
                m.setFormula(cal.getFormula());
                m.setRefIndNames(cal.getReferenceNames());
                m.setType(MeasureType.CAL);
//                m.setName(cal.getCaption());
                m.setId(UuidGeneratorUtils.generate());
                cube.getMeasures().put(m.getId(), m);
            } else {
                Measure m = cube.getMeasures().get(cal.getId());
                if (m != null) {
                    ((ExtendMinicubeMeasure) m).setCaption(cal.getCaption());
                    ((ExtendMinicubeMeasure) m).setName(cal.getCaption());
                    ((ExtendMinicubeMeasure) m).setFormula(cal.getFormula());
                    ((ExtendMinicubeMeasure) m).setRefIndNames(cal.getReferenceNames());
                }
                cube.getMeasures().put(m.getId(), m);
            }
        });
        
        extendMeasure.getCallback().forEach(measureBo -> {
            CallbackMeasure m = new CallbackMeasure(measureBo.getName());
            if (!StringUtils.isEmpty(measureBo.getId())) {
                m = (CallbackMeasure) cube.getMeasures().get(measureBo.getId());
            } else {
                m.setId(UuidGeneratorUtils.generate());
            }
            if (m != null) {
//                m.setCallbackUrl(measureBo.getUrl());
            	// 回调指标参数问题
                m.setCallbackUrl(HttpUrlUtils.getBaseUrl(measureBo.getUrl()));
                m.setCallbackParams(HttpUrlUtils.getParams(measureBo.getUrl()));
                m.setName(measureBo.getName());
                m.setDefine(measureBo.getName());
                m.setCaption(measureBo.getCaption());
                String timeOut = measureBo.getProperties().get(Constants.SOCKET_TIME_OUT_KEY);
                if (StringUtils.isEmpty(timeOut)) {
                    timeOut = "3000";
                }
                m.setType(MeasureType.CALLBACK);
                m.setAggregator(Aggregator.CALCULATED);
                m.setSocketTimeOut(Long.valueOf(timeOut));
                m.setCube(cube);
                cube.getMeasures().put(m.getId(), m);
            }
        });
        
        extendMeasure.getTbs().stream().forEach(tb -> {
            ExtendMinicubeMeasure m = new ExtendMinicubeMeasure(tb.getName());
            if (StringUtils.isEmpty(tb.getId())) {
                m.setId(UuidGeneratorUtils.generate());
            } else {
                m.setId(tb.getId());
            }
            m.setAggregator(Aggregator.SUM);
            m.setCaption(tb.getCaption());
            m.setCube(cube);
            m.setDefine(m.getName());
            m.setFormula(tb.getFormula());
            m.setRefIndNames(tb.getReferenceNames());
            m.setType(MeasureType.SR);
            cube.getMeasures().put(m.getId(), m);
        });
        
        extendMeasure.getHbs().stream().forEach(hb -> {
            ExtendMinicubeMeasure m = new ExtendMinicubeMeasure(hb.getName());
            m.setAggregator(Aggregator.SUM);
            m.setCaption(hb.getCaption());
            m.setCube(cube);
            m.setDefine(m.getName());
            m.setFormula(hb.getFormula());
            m.setRefIndNames(hb.getReferenceNames());
            m.setType(MeasureType.RR);
            if (StringUtils.isEmpty(hb.getId())) {
                m.setId(UuidGeneratorUtils.generate());
            } else {
                m.setId(hb.getId());
            }
            cube.getMeasures().put(m.getId(), m);
        });
        return schema;
    }

}
