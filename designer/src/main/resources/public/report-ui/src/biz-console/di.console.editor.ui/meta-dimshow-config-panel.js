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
    var META_DIMSHOW_CONFIG_PANEL = $namespace().MetaDimShowConfigPanel =
        inheritsObject(
            BASE_CONFIG_PANEL,
            function(options) {
                this._mModel = options.model;
                this.DATASOURCE_ID_MAPPING = {
                    INIT: 'DIMSHOW_CONFIG_INIT',
                    SUBMIT: 'DIMSHOW_CONFIG_SUBMIT'
                };
            }
        );
    var META_DIMSHOW_CONFIG_PANEL_CLASS = META_DIMSHOW_CONFIG_PANEL.prototype;

    //------------------------------------------
    // 方法
    //------------------------------------------

    /** 
     * @override
     */
    META_DIMSHOW_CONFIG_PANEL_CLASS.$doDispose = function() {
        this.getContentEl().innerHTML = '';
        // FIXME
    };

    /** 
     * @override
     */
    META_DIMSHOW_CONFIG_PANEL_CLASS.$doOpen = function(mode, options) {
    };

    /** 
     * @override
     */
    META_DIMSHOW_CONFIG_PANEL_CLASS.$doRender = function(contentEl, data) {
        // 首先dispose
        contentEl.innerHTML = ''

        var css = 'meta-candidate';
        var childrenShowConfig = data.childrenShowConfig || {};
        var fatherShowConfig = data.fatherShowConfig || {};
        var childrenType = data.childrenType;
        var fatherType = data.fatherType;

        var tplMain = [
            '<div>',
                '<div class="#{css}-dimshow-label">请选择每一个行表头的展开方式：</div>',
                '<div>#{dimShowConfigShow}</div>',
            '</div>'
        ].join('');

        var dimShowOptionItem = [
            '<option class="#{css}-showTypeItem" value="#{value}" #{selected}>#{name}',
            '</option>'
        ].join('');

        var tplItem = [
            '<span class="#{css}-item">',
                '<p>行的第#{index}个表头: </p>',
                '<span>是否包含选中节点本身<select class="#{css}-dimShowFatherSelect" name="fatherShow" >',
                '#{fatherShow}',
                '</select></span>',
                '<span>是否展示子节点<select class="#{css}-dimShowChildrenSelect" name="childrenShow" >',
                '#{childrenShow}',
                '</select></span>',
            '</span>'
        ].join('');

        var dimShowConfigShow = [];
        for(var key in childrenShowConfig){
            var dimFatherShowTypes = [];
            var dimChildrenShowTypes = [];
            for(var i in childrenType){
                var candicate = childrenType[i];
                var selected;
                if(candicate==childrenShowConfig[key]){
                    selected = 'selected';
                }else{
                    selected = '';
                }
                var name;
                if(candicate=='SINGLE_NODE'){
                    name = '不展示孩子节点';
                }else{
                    name = '展示孩子节点';
                }
                dimChildrenShowTypes.push(template(
                    dimShowOptionItem,
                    {
                        css: css,
                        value: candicate,
                        name: name,
                        selected: selected
                    }
                ));
            }
            for(var i in fatherType){
                var candicate = fatherType[i];
                var selected;
                if(candicate==fatherShowConfig[key]){
                    selected = 'selected';
                }else{
                    selected = '';
                }
                var name;
                if(candicate=='INCLUDE'){
                    name = '包含选中节点本身';
                }else{
                    name = '不包含选中节点';
                }
                dimFatherShowTypes.push(template(
                    dimShowOptionItem,
                    {
                        css: css,
                        value: candicate,
                        name: name,
                        selected: selected
                    }
                ));
            }
            dimShowConfigShow.push(template(
                tplItem,
                {
                    css: css,
                    index: (parseInt(key)+1),
                    fatherShow: dimFatherShowTypes,
                    childrenShow: dimChildrenShowTypes
                }
            ));
        }

        contentEl.innerHTML = template(
            tplMain, 
            { 
                css: css,
                dimShowConfigShow: dimShowConfigShow.join('')
            }
        );

        // 挂事件
        this.$bindEvent();
    };

    /**
     * @private
     */
    META_DIMSHOW_CONFIG_PANEL_CLASS.$bindEvent = function() {
    };

    /** 
     * @override
     */
    META_DIMSHOW_CONFIG_PANEL_CLASS.$doGetSubmitArgs = function() {
        var css = 'meta-candidate';
        var contentEl = this.getContentEl();
        
        var fatherShowConfig = [];
        var childrenShowConfig = [];

        fillSel(css + '-dimShowChildrenSelect', childrenShowConfig);
        fillSel(css + '-dimShowFatherSelect', fatherShowConfig);
       
        return {
            fatherShowConfig: fatherShowConfig,
            childrenShowConfig: childrenShowConfig
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