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
package com.baidu.rigel.biplatform.tesseract.qsservice.query.impl;

import org.springframework.stereotype.Service;

import com.baidu.rigel.biplatform.ac.minicube.MiniCubeMeasure;
import com.baidu.rigel.biplatform.ac.model.Aggregator;
import com.baidu.rigel.biplatform.ac.model.Cube;
import com.baidu.rigel.biplatform.ac.model.MeasureType;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.MeasureParseService;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.MeasureParseResult;

/**
 * 指标解析实现
 * 
 * @author xiaoming.chen
 *
 */
@Service
public class MeasureParseServiceImpl implements MeasureParseService {
    // TODO 需要注入关键字相关服务，获取关键字和解析关键字

    @Override
    public MeasureParseResult parseMeasure(Cube cube, String measureKey) {
        if (cube.getMeasures().containsKey(measureKey)) {
            MiniCubeMeasure measure = (MiniCubeMeasure) cube.getMeasures().get(measureKey);
            if (!measure.getType().equals(MeasureType.CAL)
                    && !measure.getAggregator().equals(Aggregator.DISTINCT_COUNT)) {
                return MeasureParseResult.createBaseMeasure(measureKey);
            } else if (measure.getType().equals(MeasureType.CAL)) {
                // 计算列解析，需要获取基础指标
                throw new UnsupportedOperationException("not implement..");
            }
            // 需要添加计算指标解析
        }
        // 不在cube中，解析表达式，需要支持自定义的关键字
        throw new UnsupportedOperationException("not implement..");
    }

}
