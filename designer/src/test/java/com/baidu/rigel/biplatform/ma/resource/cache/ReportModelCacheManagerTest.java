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
package com.baidu.rigel.biplatform.ma.resource.cache;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.baidu.rigel.biplatform.ma.report.model.ReportDesignModel;

/**
 * test class
 *
 * @author david.wang
 * @version 1.0.0.1
 */
public class ReportModelCacheManagerTest {
    
    /**
     * ReportModelCacheManager
     */
    ReportModelCacheManager cacheManager = new ReportModelCacheManager();
    
    /**
     * 
     */
    @Test
    public void testGetReportModelWithUnexistKey() {
        CacheManagerForResource resource = Mockito.mock(CacheManagerForResource.class); 
        Mockito.doReturn(null).when(resource).getFromCache("report_null_test");
        cacheManager.setCacheManagerForResource(resource);
        try {
            this.cacheManager.getReportModel("test");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }
    
    /**
     * 
     */
    @Test
    public void testGetReportModel() {
        CacheManagerForResource resource = Mockito.mock(CacheManagerForResource.class); 
        Mockito.doReturn(new ReportDesignModel()).when(resource).getFromCache("report_null_test");
        cacheManager.setCacheManagerForResource(resource);
        try {
            Assert.assertNotNull(this.cacheManager.getReportModel("test"));
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }
    
    
}
