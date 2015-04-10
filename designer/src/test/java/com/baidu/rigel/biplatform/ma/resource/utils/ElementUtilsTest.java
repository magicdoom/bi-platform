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

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.baidu.rigel.biplatform.ac.minicube.MiniCube;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeDimension;
import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.minicube.StandardDimension;
import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.Dimension;
import com.baidu.rigel.biplatform.ac.model.DimensionType;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.Item;
import com.baidu.rigel.biplatform.ma.report.model.LogicModel;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.google.common.collect.Maps;

/**
 *Description:
 * @author david.wang
 *
 */
public class ElementUtilsTest {
    
    @Test
    public void testGetChangableIndNamesWithEmptyModel () {
        try {
            ElementUtils.getChangableIndNames (null, null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChangableIndNamesWithEmptyCube () {
        try {
            ElementUtils.getChangableIndNames (new ReportDesignModel (), null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChanbleIndNamesWithCmmInd () {
        try {
            MiniCube cube = new MiniCube();
            MiniCubeMeasure m = new MiniCubeMeasure ("test");
            m.setType (MeasureType.COMMON);
            cube.getMeasures ().put (m.getId (), m);
            Set<String> rs = ElementUtils.getChangableIndNames (new ReportDesignModel (), cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChanbleIndNamesWithArea () {
        try {
            MiniCube cube = new MiniCube();
            MiniCubeMeasure m = new MiniCubeMeasure ("test");
            m.setType (MeasureType.COMMON);
            cube.getMeasures ().put (m.getId (), m);
            ReportDesignModel model = new ReportDesignModel();
            ExtendArea area = new ExtendArea ();
            LogicModel logicModel = new LogicModel();
            Item column = new Item();
            column.setOlapElementId ("test2");
            // TODO mock 静态方法
//            OlapElement element = new StandardDimension("test") ;
            logicModel.addColumn (column);
            area.setLogicModel (logicModel);
            model.getExtendAreas ().put (area.getId (), area);
            Set<String> rs = ElementUtils.getChangableIndNames (model, cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChanbleIndNamesWithCal () {
        try {
            MiniCube cube = new MiniCube();
            MiniCubeMeasure m = new MiniCubeMeasure ("test");
            m.setType (MeasureType.CAL);
            cube.getMeasures ().put (m.getId (), m);
            Set<String> rs = ElementUtils.getChangableIndNames (new ReportDesignModel (), cube);
            Assert.assertEquals (0, rs.size ());
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChanbleIndNamesWithRr () {
        try {
            MiniCube cube = new MiniCube();
            MiniCubeMeasure m = new MiniCubeMeasure ("test");
            m.setType (MeasureType.RR);
            cube.getMeasures ().put (m.getId (), m);
            Set<String> rs = ElementUtils.getChangableIndNames (new ReportDesignModel (), cube);
            Assert.assertEquals (0, rs.size ());
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChanbleIndNamesWithSr () {
        try {
            MiniCube cube = new MiniCube();
            MiniCubeMeasure m = new MiniCubeMeasure ("test");
            m.setType (MeasureType.SR);
            cube.getMeasures ().put (m.getId (), m);
            Set<String> rs = ElementUtils.getChangableIndNames (new ReportDesignModel (), cube);
            Assert.assertEquals (0, rs.size ());
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChangableIndNamesWithEmptyInd () {
        try {
            MiniCube cube = new MiniCube ();
            cube.setMeasures (null);
            ElementUtils.getChangableIndNames (new ReportDesignModel (), cube);
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithEmptyModel () {
        try {
            ElementUtils.getChangableDimNames (null, null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithEmptyCube () {
        try {
            ElementUtils.getChangableDimNames (new ReportDesignModel (), null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithEmptyDim () {
        try {
            MiniCube cube = new MiniCube ();
            cube.setDimensions (null);
            ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithTimeDim () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            MiniCubeDimension dimension = new TimeDimension("test");
            dimensions.put (dimension.getId (), dimension);
            cube.setDimensions (dimensions);
            Set<String> rs = ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
            Assert.assertTrue (rs.isEmpty ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithCallbackDim () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.CALLBACK);
            dimensions.put (dimension.getId (), dimension);
            cube.setDimensions (dimensions);
            Set<String> rs = ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
            Assert.assertTrue (rs.isEmpty ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithStdDim () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.STANDARD_DIMENSION);
            dimension.setId ("test2");
            dimensions.put (dimension.getId (), dimension);
            cube.setDimensions (dimensions);
            Set<String> rs = ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithMixDim () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.STANDARD_DIMENSION);
            dimension.setId ("test2");
            dimensions.put (dimension.getId (), dimension);
            TimeDimension timeDim = new TimeDimension("test1");
            timeDim.setId ("test");
            dimensions.put (timeDim.getId (), timeDim);
            cube.setDimensions (dimensions);
            Set<String> rs = ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithMixDim2 () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension group = new StandardDimension("test3");
            group.setType (DimensionType.GROUP_DIMENSION);
            group.setId ("test3");
            dimensions.put (group.getId (), group);
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.STANDARD_DIMENSION);
            dimension.setId ("test2");
            dimensions.put (dimension.getId (), dimension);
            TimeDimension timeDim = new TimeDimension("test1");
            timeDim.setId ("test");
            dimensions.put (timeDim.getId (), timeDim);
            cube.setDimensions (dimensions);
            Set<String> rs = ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithDimGroup () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.GROUP_DIMENSION);
            dimension.setId ("test2");
            dimensions.put (dimension.getId (), dimension);
            TimeDimension timeDim = new TimeDimension("test1");
            timeDim.setId ("test");
            dimensions.put (timeDim.getId (), timeDim);
            cube.setDimensions (dimensions);
            Set<String> rs = ElementUtils.getChangableDimNames (new ReportDesignModel (), cube);
            Assert.assertEquals (0, rs.size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithEptArea () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension group = new StandardDimension("test3");
            group.setType (DimensionType.GROUP_DIMENSION);
            group.setId ("test3");
            dimensions.put (group.getId (), group);
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.STANDARD_DIMENSION);
            dimension.setId ("test2");
            dimensions.put (dimension.getId (), dimension);
            TimeDimension timeDim = new TimeDimension("test1");
            timeDim.setId ("test");
            dimensions.put (timeDim.getId (), timeDim);
            cube.setDimensions (dimensions);
            ReportDesignModel model = new ReportDesignModel ();
            model.setExtendAreas (Maps.newHashMap ());
            Set<String> rs = ElementUtils.getChangableDimNames (model, cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetChangableDimNamesWithArea () {
        try {
            MiniCube cube = new MiniCube ();
            Map<String, Dimension> dimensions = Maps.newHashMap ();
            StandardDimension group = new StandardDimension("test3");
            group.setType (DimensionType.GROUP_DIMENSION);
            group.setId ("test3");
            dimensions.put (group.getId (), group);
            StandardDimension dimension = new StandardDimension("test");
            dimension.setType (DimensionType.STANDARD_DIMENSION);
            dimension.setId ("test2");
            dimensions.put (dimension.getId (), dimension);
            TimeDimension timeDim = new TimeDimension("test1");
            timeDim.setId ("test");
            dimensions.put (timeDim.getId (), timeDim);
            cube.setDimensions (dimensions);
            ReportDesignModel model = new ReportDesignModel ();
            ExtendArea area = new ExtendArea ();
            LogicModel logicModel = new LogicModel();
            Item column = new Item();
            column.setOlapElementId ("test2");
            // TODO mock 静态方法
//            OlapElement element = new StandardDimension("test") ;
            logicModel.addColumn (column);
            area.setLogicModel (logicModel);
            model.setExtendAreas (Maps.newHashMap ());
            Set<String> rs = ElementUtils.getChangableDimNames (model, cube);
            Assert.assertEquals (1, rs.size ());
        } catch (Exception e) {
            e.printStackTrace ();
            Assert.fail ();
        }
    }
}
