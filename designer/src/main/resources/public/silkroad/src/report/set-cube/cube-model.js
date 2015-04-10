/**
 * @file 设置报表cube的view
 * @author 赵晓强(longze_xq@163.com)
 * @date: 2014-7-17
 */
define(['url', 'data-sources/list/main-model'], function (Url, DataSourcesModel) {

    return Backbone.Model.extend({

        /**
         * 构造函数
         *
         * @constructor
         */
        initialize: function () {
            var that = this;

            that.dataSourcesModel = new DataSourcesModel();
            that.listenToOnce(
                that.dataSourcesModel,
                'change:dataSourcesList',
                function (model, data) {
                    that._mergeDataSourcesList(data);
                }
            );
        },

        /**
         * 加载选中的数据源（主要用于编辑时的数据还原）
         * @param {Function} success 加载成功后的回调函数
         * @public
         */
        loadSelectedDataSources: function (success) {
            var that = this;

            $.ajax({
                url: Url.loadSelectedDataSources(that.id),
                success: function (data) {
                    that.selectedDsId = data.data.selected;
                    success();
                }
            });
        },

        /**
         * 加载实体表列表
         *
         * @param {boolean} isEdit 是否是编辑状态（如果是编辑状态需要还原之前选中的表）
         * @public
         */
        loadFactTableList: function (isEdit) {
            var that = this;
            var dsId = this.selectedDsId;
            var factTableList = {};

            that.dataSourcesModel.loadTables(dsId, function (data) {
                if (isEdit) {
                    that.loadReportFactTableList(data);
                }
                else if (data.length > 0) {
                    factTableList = {
                        factTables: data
                    };
                    // 为了始终触发数据重新渲染
                    that.set(
                        { 'factTableList': factTableList },
                        { silent: true }
                    );
                    that.trigger(
                        'change:factTableList',
                        that,
                        factTableList
                    );
                }
            });
        },

        /**
         * 加载报表所对应的已经选中的cube的id数组
         * @param {Array} dsFactTablesList
         *                某数据源的实体表对象数组，用于还原选中
         * @public
         */
        loadReportFactTableList: function (dsFactTablesList) {
            var that = this;
            var factTableList = {};

            $.ajax({
                url: Url.loadReportFactTableList(that.id),
                success: function (data) {
                    factTableList.prefixs = data.data.prefixs;
                    factTableList.factTables = that._mergeFactTablesList(
                        dsFactTablesList,
                        data.data.selected
                    );
                    // 为了始终触发数据重新渲染
                    that.set(
                        { 'factTableList': factTableList }
                        //{ 'silent': true } // 阻止change事件
                    );

                    // 修改日期 11.7
                    //that.trigger(
                    //    'change:factTableList',
                    //    that,
                    //    factTableList
                    //);
                }
            });
        },

        /**
         * 合并报表选中的实体表与数据源实体表，用于还原cube的选中与不选中
         *
         * @param {Array} dsFactTables 数据源实体表
         * @param {Array} reportFactTables 报表实体体表
         * @private
         */
        _mergeFactTablesList: function (dsFactTables, reportFactTables) {
            for (var i = 0, len = dsFactTables.length; i < len; i++) {
                for (var j = 0, jLen = reportFactTables.length; j < jLen; j++) {
                    if (dsFactTables[i].id == reportFactTables[j]) {
                        dsFactTables[i].selected = true;
                    }
                }
            }
            return dsFactTables;
        },

        /**
         * 合并数据源列表（主要是选中状态）
         *
         * @param {Object} data 数据源列表
         * @private
         */
        _mergeDataSourcesList: function (data) {
            var that = this;
            var opt_selectedId = that.selectedDsId;

            if (opt_selectedId !== undefined) {
                for (var i = 0, len = data.length; i < len; i++) {
                    if (data[i].id == opt_selectedId) {
                        data[i].selected = true;
                        break;
                    }
                }
            }
            // 新建报表走的逻辑，且数据源列表不为空
            else if (data.length > 0) {
                that.set('dsId', data[0].id);
                data[0].selected = true;
                this.selectedDsId = data[0].id;
                that.loadFactTableList();
            }

            that.set('dataSourcesList', data);
        },

        /**
         * 提交报表设置
         * @param {Object} data 由view整理过的提交数据
         * @param {Function} success 提交成功后的回调函数
         * @public
         */
        submit: function (data, success) {
            var that = this;

            $.ajax({
                url: Url.submitCubeSetInfo(that.id),
                type: 'PUT',
                data: data,
                success: function (data) {
                    success();
                }
            });
        }
    });

});