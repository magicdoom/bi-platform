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

import org.junit.Assert;
import org.junit.Test;

import com.baidu.rigel.biplatform.ac.model.LevelType;
import com.baidu.rigel.biplatform.ac.model.TimeType;

/**
 *Description:
 * @author david.wang
 *
 */
public class TimeTypeAdapterUtilsTest {
    
    @Test
    public void testParseToTimeTypeWithNull () {
        try {
            TimeTypeAdaptorUtils.parseToTimeType (null);
            Assert.fail ();
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testParseToTimeTypeWithEmpty () {
        try {
            TimeTypeAdaptorUtils.parseToTimeType ("");
            Assert.fail ();
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testParseToTimeTypeWithAnyStr () {
        try {
            TimeTypeAdaptorUtils.parseToTimeType ("abc");
            Assert.fail ();
        } catch (Exception e) {
            
        }
    }
    
    @Test
    public void testParseToTimeType () {
        try {
            TimeType rs = TimeTypeAdaptorUtils.parseToTimeType ("TimeDay");
            Assert.assertEquals (TimeType.TimeDay, rs);
        } catch (Exception e) {
            Assert.fail ();
        }
    }
    
    @Test
    public void testParseToLevelTypeWithNull () {
        try {
            TimeTypeAdaptorUtils.parseToLevelType (null);
            Assert.fail ();
        } catch (Exception e) {
        }
    }
    
    @Test
    public void testParseToLevelType () {
        try {
            Assert.assertEquals (LevelType.TIME_DAYS, TimeTypeAdaptorUtils.parseToLevelType (TimeType.TimeDay));
            Assert.assertEquals (LevelType.TIME_WEEKS, TimeTypeAdaptorUtils.parseToLevelType (TimeType.TimeWeekly));
            Assert.assertEquals (LevelType.TIME_MONTHS, TimeTypeAdaptorUtils.parseToLevelType (TimeType.TimeMonth));
            Assert.assertEquals (LevelType.TIME_QUARTERS, 
                    TimeTypeAdaptorUtils.parseToLevelType (TimeType.TimeQuarter));
            Assert.assertEquals (LevelType.TIME_YEARS, TimeTypeAdaptorUtils.parseToLevelType (TimeType.TimeYear));
            Assert.assertEquals (LevelType.REGULAR, TimeTypeAdaptorUtils.parseToLevelType (TimeType.TimeHalfYear));
        } catch (Exception e) {
            Assert.fail ();
        }
    }
}
