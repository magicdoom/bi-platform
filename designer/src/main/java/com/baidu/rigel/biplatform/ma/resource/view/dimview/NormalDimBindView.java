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
package com.baidu.rigel.biplatform.ma.resource.view.dimview;

import java.io.Serializable;
import java.util.List;

import com.baidu.rigel.biplatform.ma.resource.view.dimdetail.NormalDimDetail;
import com.google.common.collect.Lists;

/**
 * BO类：普通维度与视图定义的映射关系定义
 *
 * @author zhongyi
 * @version 1.0.0.1
 */
public class NormalDimBindView implements Serializable {
    
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 613929903419138836L;
    
    /**
     * 立方体id
     */
    private String cubeId;
    
    /**
     * 普通维度明细信息
     */
    private List<NormalDimDetail> children;
    
    /**
     * get the children
     * 
     * @return the children
     */
    public List<NormalDimDetail> getChildren() {
        if (children == null) {
            children = Lists.newArrayList();
        }
        return children;
    }
    
    /**
     * set the children
     * 
     * @param children
     *            the children to set
     */
    public void setChildren(List<NormalDimDetail> children) {
        this.children = children;
    }
    
    /**
     * get the cubeId
     * 
     * @return the cubeId
     */
    public String getCubeId() {
        return cubeId;
    }
    
    /**
     * set the cubeId
     * 
     * @param cubeId
     *            the cubeId to set
     */
    public void setCubeId(String cubeId) {
        this.cubeId = cubeId;
    }
}