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
package com.baidu.rigel.biplatform.ma.model.service;

import java.util.List;



import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.Schema;
import com.baidu.rigel.biplatform.ma.auth.bo.CalMeasureViewBo;

/**
 * 
 * {@link SchemaManageService} :<p>design for manage {@link Schema} which description 
 * the logic model of business subject areas.
 *    Already know sub class:
 *      {@link SchemaManageServiceImpl}
 * </p>
 * @see com.baidu.rigel.biplatform.ac.model.Dimension
 * @see com.baidu.rigel.biplatform.ac.model.Measure
 * @see com.baidu.rigel.biplatform.ac.model.Schema 
 * @since jdk1.8.5 or after
 * @version Silkroad1.0.1
 * @author david.wang
 *
 */
public interface SchemaManageService {
    
    /**
     * modify {@link Dimension}:
     * <p>
     *  modify the {@link Schema} define with the newest {@link Dimension} 
     *  {@link Dimension} is part of {@link Schema}'s @{link Cube},
     *  Which is an perspective for make an assay of specifically business topic.
     *  For example: Time is an particular dimension for sales and so on.
     * </p>
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param dim -- the newest dimension
     * @return the modified Schema, if error happened, return null
     * 
     */
    public Schema modifyDimension(Schema schema, String cubeId, Dimension dim);
    
    /**
     * 
     * modify @{link Measure} : <p>
     *      modify the {@link Schema} define with the newest {@link Measure}.
     *      @{link Measure} is the metrics of the business topic. 
     *      for example: you want get the Top N best income from districts, the income count is a measure.
     * </p>
     * 
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param measure -- the newest measure
     * @return the modified Schema, if error happened, return null
     */
    public Schema modifyMeasure(Schema schema, String cubeId, Measure measure);
    
    /**
     * 
     * convert {@link Measure} to {@link Dimension} :</p>
     * this is design for modify the business topic metrics to properties.
     * </p>
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param measure -- source
     * @return the modified Schema, if error happened, return null
     * 
     */
    public Schema converMeasure2Dim(Schema schema, String cubeId, Measure measure);
    
    /**
     * 
     * convert {@link Dimension} to {@link Measure} :</p>
     * this is design for modify the business topic properties to metrics.
     * 
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param dim -- source
     * @return the modified Schema, if error happened, return null
     * 
     */
    public Schema convertDim2Measure(Schema schema, String cubeId, Dimension dim);
    
    /**
     * 删除schem中定义的维度
     * 
     * @param schema -- business topic's logic model
     * @param dimId -- cube's id
     * @param cubeId -- cube's id
     * 
     * @return the modified Schema, if error happened, return null
     */
    public Schema removeDimention(Schema schema, String cubeId, String dimId);
    
    /**
     * add {@link Dimension} into dimension group:<p>
     *  Dimension group is a special {@link Dimension}, which reference some already exists {@Dimension}'s {@link Level}.
     *  It provide a flexible way for analyze some business topic from one perspective to another. 
     *  In OLAP area, that's called drill down or up, in fact, that implements the logic model (table relation) 's rotate action.
     * </p>
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param groupDimId -- dimension group id 
     * @param dimId -- dimension id
     * @return the modified Schema, if error happened, return null
     * 
     */
    public Schema addDimIntoDimGroup(Schema schema, String cubeId, String groupDimId,
        String... dimId);
    
    /**
     * delete {@link Dimension} reference from dimension group
     * 
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param groupDimId -- dimension group id
     * @param dimIds -- Id list which will be deleted from dimension group
     * @return the modified Schema, if error happened, return null
     */
    public Schema removeDimFromGroup(Schema schema, String cubeId, String groupDimId,
        String... dimIds);
    
    /**
     * 
     * create new dimension group
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param name -- dimension group name
     * @return the modified Schema, if error happened, return null
     */
    public Schema createDimGroup(Schema schema, String cubeId, String name);
    
    /**
     * 
     * batch update the visibility property of the dimensions or measures. if the dimension or measure visibility values is false,
     * you can't not use if for query.
     * 
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param dims -- dimension list
     * @param measures -- measure list
     * @return the modified Schema, if error happened, return null
     */
    public Schema modifyIndAndDimVisibility(Schema schema, String cubeId, List<Dimension> dims,
        List<Measure> measures);
    
    /**
     * change the dimension group's levels order
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param dimId -- dimension id
     * @param before -- the target level's before level
     * @param target -- the level which will be moved
     * @return the modified Schema, if error happened, return null
     */
    public Schema modifyDimOrder(Schema schema, String cubeId, String dimId, String before,
        String target);
    
    /**
     * delete the extend measure from schema's cube define.<p>
     *  Extend measure called Calculate member in OLAP, it from already exists {@link Measure} based on a formula.
     *  That can describe some complex measures. 
     * </p>
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param measureId -- measure's id
     * @return the modified Schema, if error happened, return null
     */
    Schema delExtendMeasure(Schema schema, String cubeId, String measureId);

    /**
     * add or modify extend measure for schema's cube
     * @param schema -- business topic's logic model
     * @param cubeId -- cube's id
     * @param extendMeasure -- new extend measure list
     * @return the modified Schema, if error happened, return null
     */
    Schema addOrModifyExtendMeasure(Schema schema, String cubeId, CalMeasureViewBo extendMeasure);

}
