/**
 * 
 * @file:    
 * @author:  
 * @depend:  xui, xutil
 */
$namespace('di.console.editor.ui');

(function() {

    //------------------------------------------
    // 引用 
    //------------------------------------------

    var DICT = di.config.Dict;
    var UTIL = di.helper.Util;
    var DIALOG = di.helper.Dialog;
    var LANG = di.config.Lang;
    var AJAX = di.config.Ajax;
    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var removeClass = xutil.dom.removeClass;
    var extend = xutil.object.extend;
    var objKey = xutil.object.objKey;
    var q = xutil.dom.q;
    var children = xutil.dom.children;
    var textSubstr = xutil.string.textSubstr;
    var encodeHTML = xutil.string.encodeHTML;
    var htmlText = xutil.string.htmlText;
    var bind = xutil.fn.bind;
    var trim = xutil.string.trim;
    var template = xutil.string.template;
    var preInit = UTIL.preInit;
    var stringifyParam = xutil.url.stringifyParam;
    var cmptCreate4Console = UTIL.cmptCreate4Console;
    var cmptSync4Console = UTIL.cmptSync4Console;
    var ecuiCreate = UTIL.ecuiCreate;
    var $fastCreate = ecui.$fastCreate;
    var isString = xutil.lang.isString;
    var PANEL_PAGE = di.shared.ui.PanelPage;
    var strToBoolean = UTIL.strToBoolean;
    var UI_CONTROL = ecui.ui.Control;
    var alert = di.helper.Dialog.alert;
    var UI_BUTTON = ecui.ui.Button;
    var BASE_CONFIG_PANEL = di.shared.ui.BaseConfigPanel;
    var DI_FACTORY;
    var COMMON_PARAM_FACTORY;

    $link(function() {
        DI_FACTORY = di.shared.model.DIFactory;
        COMMON_PARAM_FACTORY = di.shared.model.CommonParamFactory;
    });

    //------------------------------------------
    // 类型声明 
    //------------------------------------------

    /**
     * 元数据候选项组件
     *
     * @class
     * @extends di.shared.ui.PanelPage
     * @param {Object} options
     */
    var META_ROWHEAD_CONFIG_PANEL = $namespace().MetaRowHeadConfigPanel =
        inheritsObject(
            BASE_CONFIG_PANEL,
            function(options) {
                this._mModel = options.model;
                this.DATASOURCE_ID_MAPPING = {
                    INIT: 'ROWHEAD_CONFIG_INIT',
                    SUBMIT: 'ROWHEAD_CONFIG_SUBMIT'
                };
            }
        );
    var META_ROWHEAD_CONFIG_PANEL_CLASS = META_ROWHEAD_CONFIG_PANEL.prototype;

    //------------------------------------------
    // 方法
    //------------------------------------------

    /** 
     * @override
     */
    META_ROWHEAD_CONFIG_PANEL_CLASS.$doDispose = function() {
        this.getContentEl().innerHTML = '';
        // FIXME
    };

    /** 
     * @override
     */
    META_ROWHEAD_CONFIG_PANEL_CLASS.$doOpen = function(mode, options) {
    };

    /** 
     * @override
     */
    META_ROWHEAD_CONFIG_PANEL_CLASS.$doRender = function(contentEl, data) {
        // 首先dispose
        contentEl.innerHTML = ''

        var css = 'meta-candidate';
        var drillTypeConfig = data.drillTypeConfig || {};
        var candicates = data.candicates;

        var tplMain = [
            '<div>',
                '<div class="#{css}-rowhead-label">请选择每一个行表头的下钻方式：</div>',
                '<div>#{drillTypeConfig}</div>',
            '</div>'
        ].join('');

        var drillTypeOptionItem = [
            '<option class="#{css}-drillTypeItem" value="#{value}" #{selected}>#{name}',
            '</option>'
        ].join('');

        var tplItem = [
            '<span class="#{css}-item">',
                '<p>行的第#{index}个表头<select class="#{css}-drillTypeSelect" name="drillTypes">',
                '#{drillTypes}',
                '</select></p>',
            '</span>'
        ].join('');

        var drillTypeConfigShow = [];
        for(var key in drillTypeConfig){
            var drillTypes = [];
            for(var i in candicates){
                var candicate = candicates[i];
                var selected;
                if(candicate==drillTypeConfig[key]){
                    selected = 'selected';
                }else{
                    selected = '';
                }
                var name;
                if(candicate=='LINK'){
                    name = '链接下钻';
                }else if(candicate=='PLUS_MINUS'){
                    name = '加减号展开';
                }else{
                    name = '不下钻';
                }
                drillTypes.push(template(
                    drillTypeOptionItem,
                    {
                        css: css,
                        value: candicate,
                        name: name,
                        selected: selected
                    }
                ));
            }
            drillTypeConfigShow.push(template(
                tplItem,
                {
                    css: css,
                    index: (parseInt(key)+1),
                    drillTypes: drillTypes.join('')
                }
            ));
        }

        contentEl.innerHTML = template(
            tplMain, 
            { 
                css: css,
                drillTypeConfig: drillTypeConfigShow.join('')
            }
        );

        // 挂事件
        this.$bindEvent();
    };

    /**
     * @private
     */
    META_ROWHEAD_CONFIG_PANEL_CLASS.$bindEvent = function() {
    };

    /** 
     * @override
     */
    META_ROWHEAD_CONFIG_PANEL_CLASS.$doGetSubmitArgs = function() {
        var css = 'meta-candidate';
        var contentEl = this.getContentEl();
        
        var drillTypeConfig = [];
        fillSel(css + '-drillTypeSelect', drillTypeConfig);
       
        return {
            drillTypeCfg: drillTypeConfig
        };

        function fillSel(cssSelectName, list) {
            var selects = q(cssSelectName, contentEl);
            for (var i = 0, select; select = selects[i]; i ++) {
                list.push(select.value);
            }
        }
    };

    function makeSet(list) {
        list = list || [];
        var ret = {};
        for (var i = 0; i < list.length; i ++) {
            ret[list[i]] = 1;
        }
        return ret;
    }

    function fmtInput(value) {
        if (value == null) {
            return '';
        }
        return trim(String(value));
    }

})();