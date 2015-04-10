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

import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import com.baidu.rigel.biplatform.ma.report.model.ExtendArea;
import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;
import com.baidu.rigel.biplatform.ma.resource.ResponseResult;
import com.baidu.rigel.biplatform.ma.resource.view.vo.ExtendAreaViewObject;

/**
 * Rest 结果封装工具测试类
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class ResourceUtilsTest {
    
    /**
     * 
     */
    @Test
    public void testGetResultWithNullData() {
        ResponseResult rs = ResourceUtils.getResult("success", "failed", null);
        Assert.assertNotNull(rs);
        Assert.assertEquals(ResponseResult.FAILED, rs.getStatus());
        Assert.assertEquals("failed", rs.getStatusInfo());
    }
    
    /**
     * 
     */
    @Test
    public void testGetResult() {
        ResponseResult rs = ResourceUtils.getResult("success", "failed", "data");
        Assert.assertNotNull(rs);
        Assert.assertEquals(ResponseResult.SUCCESS, rs.getStatus());
        Assert.assertEquals("success", rs.getStatusInfo());
        Assert.assertEquals("data", rs.getData());
    }
    
    /**
     * 
     */
    @Test
    public void testGetErrorResult() {
        ResponseResult rs = ResourceUtils.getErrorResult("failed", ResponseResult.FAILED);
        Assert.assertNotNull(rs);
        Assert.assertEquals(ResponseResult.FAILED, rs.getStatus());
        Assert.assertEquals("failed", rs.getStatusInfo());
    }
    
    /**
     * 
     */
    @Test
    public void testGetCorrectResult() {
        ResponseResult rs = ResourceUtils.getCorrectResult("success", "data");
        Assert.assertNotNull(rs);
        Assert.assertEquals(ResponseResult.SUCCESS, rs.getStatus());
        Assert.assertEquals("success", rs.getStatusInfo());
        Assert.assertEquals("data", rs.getData());
    }
    
    @Test
    public void testBuildValueObjeWithNullArea () {
        try {
            ExtendAreaViewObject rs = ResourceUtils.buildValueObject (null, null);
            Assert.assertNull (rs.getxAxis ());
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testBuildValueObjeWithNullModel () {
        try {
            ExtendArea area = new ExtendArea();
            area.setCubeId ("test");
            ResourceUtils.buildValueObject (null, area);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testBuildValueObjeWithNullCubeId () {
        try {
            ExtendArea area = new ExtendArea();
            area.setCubeId (null);
            ExtendAreaViewObject rs = ResourceUtils.buildValueObject (null, area);
            Assert.assertNull (rs.getxAxis ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testBuildValueObjeWithNullLogicModel () {
        try {
            ExtendArea area = new ExtendArea();
            area.setCubeId ("test");
            area.setLogicModel (null);
            ResourceUtils.buildValueObject (null, area);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testBuildValueObje () {
        InputStream is = null;
        ObjectInputStream ois = null;
        try {
            is = ResourceUtils.class.getClassLoader ().getResourceAsStream ("report_model");
             ois = new ObjectInputStream(is);
             ReportDesignModel model = (ReportDesignModel) ois.readObject ();
             ExtendAreaViewObject rs = ResourceUtils.buildValueObject (model, model.getExtendAreaList ()[0]);
             Assert.assertEquals (1, rs.getxAxis ().size());
             Assert.assertEquals (1, rs.getyAxis ().size());
        } catch (Exception e) {
            e.printStackTrace ();
            Assert.fail ();
        } finally {
            IOUtils.closeQuietly (is);
            IOUtils.closeQuietly (ois);
        }
    }
}
