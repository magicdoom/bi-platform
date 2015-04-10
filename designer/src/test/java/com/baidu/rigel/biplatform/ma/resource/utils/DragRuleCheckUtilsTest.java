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
package com.baidu.rigel.biplatform.ma.resource.utils;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.Measure;
import com.baidu.rigel.biplatform.ac.model.OlapElement;
import com.baidu.rigel.biplatform.ma.model.service.PositionType;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;

/**
 *Description:
 * @author david.wang
 *
 */
public class DragRuleCheckUtilsTest {
    
    @Test
    public void testCheckIllegalWithNullElement () {
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (null, null, null));
    }
    
    @Test
    public void testCheckIllegalWithNullArea () {
        OlapElement element = Mockito.mock (OlapElement.class);
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, null, null));
    }
    
    @Test
    public void testCheckIllegalWithEmptyPosition () {
        OlapElement element = Mockito.mock (OlapElement.class);
        ExtendArea area = Mockito.mock (ExtendArea.class);
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, null, area));
    }
    
    @Test
    public void testCheckIllegalWithMeasuer () {
        OlapElement element = Mockito.mock (Measure.class);
        ExtendArea area = Mockito.mock (ExtendArea.class);
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, null, area));
        Assert.assertTrue (DragRuleCheckUtils.checkIllegal (element, PositionType.Y, area));
        Assert.assertTrue (DragRuleCheckUtils.checkIllegal (element, PositionType.CAND_IND, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.X, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.S, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.CAND_DIM, area));
    }
    
    @Test
    public void testCheckIllegalWithDimension () {
        OlapElement element = Mockito.mock (Dimension.class);
        ExtendArea area = Mockito.mock (ExtendArea.class);
        Assert.assertFalse(DragRuleCheckUtils.checkIllegal (element, null, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.Y, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.CAND_IND, area));
        Assert.assertTrue (DragRuleCheckUtils.checkIllegal (element, PositionType.X, area));
        Assert.assertTrue (DragRuleCheckUtils.checkIllegal (element, PositionType.S, area));
        Assert.assertTrue (DragRuleCheckUtils.checkIllegal (element, PositionType.CAND_DIM, area));
    }
    
    @Test
    public void testCheckIllegalWithOthers () {
        OlapElement element = Mockito.mock (Cube.class);
        ExtendArea area = Mockito.mock (ExtendArea.class);
        Assert.assertFalse(DragRuleCheckUtils.checkIllegal (element, null, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.Y, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.CAND_IND, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.X, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.S, area));
        Assert.assertFalse (DragRuleCheckUtils.checkIllegal (element, PositionType.CAND_DIM, area));
    }
}
