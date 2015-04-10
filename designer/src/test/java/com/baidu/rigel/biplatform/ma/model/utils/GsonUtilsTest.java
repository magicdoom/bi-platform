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
public class GsonUtilsTest {
    
    @Test
    public void testFromJsonWithNull () {
        Object obj = GsonUtils.fromJson (null, null);
        Assert.assertNull (obj);
    }
    
    @Test
    public void testFromJsonWithEmpty () {
        Object obj = GsonUtils.fromJson ("", null);
        Assert.assertNull (obj);
    }
    
    @Test
    public void testFromJsonWithEmptyClass () {
        try {
            GsonUtils.fromJson ("{test:test}", null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testFromJsonWithMap () {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> rs = GsonUtils.fromJson ("{test:test}", Map.class);
            Assert.assertNotNull (rs.get ("test"));
            Assert.assertEquals ("test", rs.get ("test"));
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testToJsonWithNull () {
        String rs = GsonUtils.toJson (null);
        Assert.assertNull (rs);
    }
    
    @Test
    public void testToJson () {
        Map<String, String> data = Maps.newHashMap ();
        data.put ("test", "test");
        Assert.assertEquals ("{\"test\":\"test\"}", GsonUtils.toJson (data));
    }
}
