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
package com.baidu.rigel.biplatform.ma.model.utils;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Maps;

/**
 *Description:
 * @author david.wang
 *
 */
public class HttpUrlUtilsTest {
    
    @Test
    public void testGetBaseUrlWithNull () {
        try {
            HttpUrlUtils.getBaseUrl (null);
            Assert.fail ();
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testGetBaseUrlWithEmpty () {
        try {
            String rs = HttpUrlUtils.getBaseUrl ("");
            Assert.assertEquals ("", rs);
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testGetBaseUrlWithQuestionMark () {
        try {
            HttpUrlUtils.getBaseUrl ("http://localhost/abc?a?b");
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetBaseUrl () {
        try {
            String rs = HttpUrlUtils.getBaseUrl ("http://localhost/abc?a?b");
            Assert.assertEquals ("http://localhost/abc", rs);
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGetParamsWithNull () {
        try {
            HttpUrlUtils.getParams (null);
            Assert.fail ();
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testGetParamsWithUrl () {
        try {
            Assert.assertEquals (0, HttpUrlUtils.getParams ("http://localhpost").size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void tetGetParams () {
        try {
            Assert.assertEquals (1, HttpUrlUtils.getParams ("http://localhpost?a=b").size ());
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void tetGetParamsFailed () {
        try {
            HttpUrlUtils.getParams ("http://localhpost?a==b");
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void tetGetParamsFailed2 () {
        try {
            HttpUrlUtils.getParams ("http://localhpost??a=b").size ();
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGenerateTotalUrlWithNullParam () {
        try {
             HttpUrlUtils.generateTotalUrl ("http://", null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGenerateTotalUrlWithNullUrl () {
        try {
             HttpUrlUtils.generateTotalUrl (null, Maps.newHashMap ());
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testGenerateTotalUrl () {
        try {
            Map<String, String> params = Maps.newHashMap ();
            params.put ("a", "b");
             String rs = HttpUrlUtils.generateTotalUrl ("http://localhost/abc", params);
            String realUrl = "http://localhost/abc?a=b";
            Assert.assertEquals (realUrl, rs);
            rs = HttpUrlUtils.generateTotalUrl ("http://localhost/abc?", params);
            Assert.assertEquals (realUrl, rs);
            params.put ("c", "d");
            rs = HttpUrlUtils.generateTotalUrl ("http://localhost/abc?", params);
            Assert.assertEquals (realUrl + "&c=d", rs);
            params.put ("e", "");
            rs = HttpUrlUtils.generateTotalUrl ("http://localhost/abc?", params);
            Assert.assertEquals (realUrl + "&c=d", rs);
        } catch (Exception e) {
        }
    }
}
