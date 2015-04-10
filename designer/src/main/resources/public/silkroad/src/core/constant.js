/**
 * @file:    常量
 * @author:  lzt(lztlovely@126.com)
 * @date:    2014/11/11
 */
define(function () {
    /**
     * 数据格式全部选项
     *
     * @const
     * @type {string}
     */
    var DATA_FORMAT_OPTIONS = {
        'I,III': '千分位整数（18,383）',
        'I,III.DD': '千分位两位小数（18,383.88）',
        'I.DD%': '百分比两位小数（34.22%）',
        'HH:mm:ss': '时间（13:23:22）',
        'D天HH:mm:ss': '时间格式（2天1小时23分45秒）'
    };
    /**
     * 图类型
     *
     * @const
     * @type {string}
     */
    var CHART_TYPES = {
        'bar': false,
        'pie': false,
        'line': false,
        'column': false,
        'map': false
    };
    /**
     * 单个图类型
     * 只能是单种图，不能是组合
     *
     * @const
     * @type {string}
     */
    var SINGLE_CHART = ['bar', 'map', 'pie'];

    /**
     * 组合图类型
     *
     * @const
     * @type {string}
     */
    var COMBINATION_CHART = ['column', 'line'];

    /**
     * 组件id后缀
     *
     * @const
     * @type {string}
     */
    var COMPONENT_ID_SUFFIX = {
        SELECT: '-vu-form-select',
        CALENDAR: '-vu-form-calendar',
        TEXT: '-vu-form-text',
        MULTISELECT: '-vu-form-multiselect',
        EUUI_INPUT_TREE: '-vu-form-ecui-input-tree',
        CHART: '-vu-chart',
        TABLE: '-vu-table',
        H_BUTTON: '-vu-confirm'
    };

    return {
        DATA_FORMAT_OPTIONS: DATA_FORMAT_OPTIONS,
        CHART_TYPES: CHART_TYPES,
        COMPONENT_ID_SUFFIX: COMPONENT_ID_SUFFIX,
        SINGLE_CHART: SINGLE_CHART,
        COMBINATION_CHART: COMBINATION_CHART
    };
});