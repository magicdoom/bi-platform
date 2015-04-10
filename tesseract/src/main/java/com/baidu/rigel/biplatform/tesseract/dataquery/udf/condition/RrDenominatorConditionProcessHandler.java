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
package com.baidu.rigel.biplatform.tesseract.dataquery.udf.condition;

import java.util.Calendar;
import java.util.Date;

import com.baidu.rigel.biplatform.ac.minicube.TimeDimension;
import com.baidu.rigel.biplatform.ac.model.TimeType;
import com.baidu.rigel.biplatform.ac.query.model.DimensionCondition;
import com.baidu.rigel.biplatform.ac.query.model.MetaCondition;
import com.baidu.rigel.biplatform.ac.util.TimeRangeDetail;
import com.baidu.rigel.biplatform.ac.util.TimeUtils;
import com.baidu.rigel.biplatform.tesseract.qsservice.query.vo.QueryContext;

/**
 * Description: RrDenominatorConditionProcessHandler
 * @author david.wang
 *
 */
class RrDenominatorConditionProcessHandler  extends RateConditionProcessHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public QueryContext processCondition(QueryContext context) {
        /*
         * 环比分母计算条件，修改时间条件为上一个统计周期
         * 如果时间粒度是天，则上一个统计周期为昨天
         * 如果时间粒度是周，则上一个统计周期为上周
         * 如果时间粒度是月，则上一个统计周期为上月
         * 如果时间粒度是季度，则上一个统计周期为上季度
         * 如果时间粒度是年，则上一个统计周期为去年
         * 需要QueryContext提供：时间维度定义，当前时间维度过滤条件，此处修改时间范围后，重新设置到查询上下文
         */
        if (!(context instanceof QueryContextAdapter)) {
            throw new IllegalArgumentException("参数必须为QueryContextAdapter类型");
        }
        QueryContextAdapter adapter = (QueryContextAdapter) context;
        TimeDimension dimension = getTimeDimension(adapter);
        if (dimension == null) {
            throw new IllegalStateException("计算同环比必须包含时间维度，请确认查询结果");
        }
        TimeType timeType = dimension.getDataTimeType();
        Date firstDayOfTimeRange = null;
        try {
            firstDayOfTimeRange = getFirstDayOfTimeDim(dimension, adapter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(firstDayOfTimeRange);
        // 将时间调整为上一统计周期最后一天
        cal.add(Calendar.DAY_OF_YEAR, -1);
        TimeRangeDetail timeRange = null;
        switch (timeType) {
            case TimeDay:
                MetaCondition condition = adapter.getQuestionModel().getQueryConditions().get(dimension.getName());
                int size = 0;
                if (condition instanceof DimensionCondition) {
                    DimensionCondition dimCondition = (DimensionCondition) condition;
                    size = dimCondition.getQueryDataNodes().size();
                }
                timeRange = TimeUtils.getDays(cal.getTime(), 0, size - 1);
                break;
            case TimeWeekly:
                timeRange = TimeUtils.getWeekDays(cal.getTime());
                break;
            case TimeMonth:
                timeRange = TimeUtils.getMonthDays(cal.getTime());
                break;
            case TimeQuarter:
                timeRange = TimeUtils.getQuarterDays(cal.getTime());
                break;
            case TimeYear:
                timeRange = TimeUtils.getYearDays(cal.getTime());
                break;
            default:
                throw new RuntimeException("未知的时间维度类型：" + timeType.name());
        }
        if (timeRange == null) {
            throw new RuntimeException("未知的时间维度类型：" + timeType.name());
        }
        String[] days = timeRange.getDays();
        QueryContext rs = createOrModifyNewContext(days, dimension, adapter);
        return rs;
    }

}
