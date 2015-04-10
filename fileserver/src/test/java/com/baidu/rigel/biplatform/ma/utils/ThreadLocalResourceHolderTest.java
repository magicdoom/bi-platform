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
package com.baidu.rigel.biplatform.ma.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 *Description:
 * @author david.wang
 *
 */
public class ThreadLocalResourceHolderTest {
    
    @Test
    public void testGetThreadMap () {
        Assert.assertNotNull(ThreadLocalResourceHolder.getThreadMap ());
    }
    
    @Test
    public void testGetPropertiesWithNull () {
        try {
            ThreadLocalResourceHolder.getProperty (null);
            Assert.fail ();
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testGetPropertiesWithUnExistKey () {
        try {
            Assert.assertNull(ThreadLocalResourceHolder.getProperty ("test"));
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testGetPropertiesWithExistKey () {
        try {
            ThreadLocalResourceHolder.getThreadMap ().put ("test", "test");
            Assert.assertNotNull(ThreadLocalResourceHolder.getProperty ("test"));
            Assert.assertEquals ("test", ThreadLocalResourceHolder.getProperty ("test"));
            ThreadLocalResourceHolder.unbindProperty ("test");
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testBindPropertiesWithNullKey () {
        try {
            ThreadLocalResourceHolder.bindProperty (null, null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testBindPropertiesWithExistKey () {
        try {
            ThreadLocalResourceHolder.getThreadMap ().put ("test", "test");
            ThreadLocalResourceHolder.bindProperty ("test", null);
            Assert.fail ();
        } catch (Exception e) {
            ThreadLocalResourceHolder.unbindProperty ("test");
        }
    }
    
    @Test
    public void testBindProperties () {
        try {
            ThreadLocalResourceHolder.bindProperty ("test", "test");
            ThreadLocalResourceHolder.unbindProperty ("test");
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testUnbindPropertyWithNull () {
        try {
            ThreadLocalResourceHolder.unbindProperty (null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testUnbindPropertyWithUnexistKey () {
        try {
            ThreadLocalResourceHolder.unbindProperty ("test");
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testUnbindProperty () {
        try {
            ThreadLocalResourceHolder.bindProperty ("test", "test");
            ThreadLocalResourceHolder.unbindProperty ("test");
            Assert.assertNull (ThreadLocalResourceHolder.getProperty ("test"));
            ThreadLocalResourceHolder.bindProperty ("test", "test");
            Assert.assertNotNull (ThreadLocalResourceHolder.getProperty ("test"));
            ThreadLocalResourceHolder.unbindProperty ("test");
            Assert.assertNull (ThreadLocalResourceHolder.getProperty ("test"));
        } catch (Exception e) {
            Assert.fail ();
        }
    }
}
