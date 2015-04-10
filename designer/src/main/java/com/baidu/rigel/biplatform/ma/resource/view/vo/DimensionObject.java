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
package com.baidu.rigel.biplatform.ma.resource.view.vo;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.google.common.collect.Lists;

/**
 * Dimension View Object
 * 
 * @author zhongyi
 *
 */
public class DimensionObject implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 8595021379045609897L;
    
    /**
     * id
     */
    private String id;
    
    /**
     * name meta name
     */
    private String name;
    
    /**
     * caption meta caption
     */
    private String caption;
    
    /**
     * visible
     */
    private boolean visible;
    
    /**
     * type
     */
    private DimensionType type;
    
    /**
     * canToDim
     */
    private boolean canToInd;
    
    /**
     * levels
     */
    private List<DimensionObject> levels = Lists.newArrayList();
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public boolean isCanToInd() {
        return canToInd;
    }
    
    public void setCanToInd(boolean canToInd) {
        this.canToInd = canToInd;
    }
    
    public DimensionType getType() {
        return type;
    }
    
    public void setType(DimensionType type) {
        this.type = type;
    }
    
    public List<DimensionObject> getLevels() {
        return levels;
    }
    
    public void setLevels(List<DimensionObject> levels) {
        this.levels = levels;
    }
}
