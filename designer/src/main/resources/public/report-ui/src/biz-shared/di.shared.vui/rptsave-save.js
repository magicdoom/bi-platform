/**
 * di.shared.vui.SaveButton
 * Copyright 2012 Baidu Inc. All rights reserved.
 *
 * @file:    文字区
 * @author:  lizhantong(lztlovely@126.com)
 * @depend:  xui, xutil
 */

$namespace('di.shared.vui');
    
(function () {


    //------------------------------------------
    // 引用 
    //------------------------------------------


    var inheritsObject = xutil.object.inheritsObject;
    var addClass = xutil.dom.addClass;
    var removeClass = xutil.dom.removeClass;
    var domChildren = xutil.dom.children;
    var getParent = xutil.dom.getParent;
    var hasClass = xutil.dom.hasClass;
    var confirm = di.helper.Dialog.confirm;
    var alert = di.helper.Dialog.alert;
    var domQ = xutil.dom.q;
    var extend = xutil.object.extend;
    var encodeHTML = xutil.string.encodeHTML;
    var isObject = xutil.lang.isObject;
    var isArray = xutil.lang.isArray;
    var template = xutil.string.template;
    var textLength = xutil.string.textLength;
    var XOBJECT = xui.XObject;


    //------------------------------------------
    // 类型声明 
    //------------------------------------------


    /**
     * 文字区
     * 直接指定文字，或者html，
     * 或者模板（模板形式参见xutil.string.template）
     * 初始dom中的内容被认为是初始模板。
     * 也可以用参数传入模板。
     * 
     * @class
     * @extends xui.XView
     * @param {Object} options
     * @param {HTMLElement} options.el 容器元素
     */
    var SAVE_BUTTON = $namespace().SaveButton =
            inheritsObject(XOBJECT, constructor);
    var SAVE_BUTTON_CLASS = SAVE_BUTTON.prototype;
    
    
    //------------------------------------------
    // 常量 
    //------------------------------------------


    // 显示错误提示，验证镜像名称时使用
    var SHOW_ERROR_TIPS = true;
    // 隐藏错误提示，验证镜像名称时使用
    var HIDE_ERROR_TIPS = false;
    var ADD_MODE = true;
    var UPDATE_MODE = false;
    // 镜像名称能保存的最大字符长度（一个中文两个英文）
    var TAB_NAME_MAX_LENGTH = 50;
    
    // 样式
    var SAVE_CLASS = {
        SAVE_CLASS_NAME: 'ui-reportSave-save',
        // 保存按钮一般样式
        SAVE_BUTTON_CLASS_NAME: 'ui-reportSave-save-saveButton',
        // 保存按钮hover样式
        SAVE_BUTTON_HOVER_CLASS_NAME: 'ui-reportSave-save-saveButton-hover',
        // 按钮操作项的样式(ul)
        OPERATE_BUTTONS_CLASS_NAME: 'ui-reportSave-save-operateButtons',
        // 隐藏样式
        HIDE: 'di-o_o-hide',
        // 弹出框中的一般样式
        DIALOG_ITEM_CLASS_NAME: 'ui-reportSave-save-dialog-form-item',
        // 弹出框中的错误提示样式
        DIALOG_ERROR_CLASS_NAME: 'ui-reportSave-save-dialog-form-error'
    }
    
    // 提示信息
    var MESSAGE = {
        // 镜像名称验证失败提示
        NAME_WARN: '请您输入正确格式的名称',
        // 镜像名称placehoder
        NAME_PLACE_HOLDER: '请您输入正确格式的名称',
        // 如果是默认项不能被编辑提示
        TAB_UPDATE_DEFAULT_WARN: '默认项不能编辑',
        // 镜像名称超过最大个数提示
        TAB_MAX_NUM_WARN: '保存报表个数已达上限，不能继续添加'
    };
    
    
    //------------------------------------------
    // 方法
    //------------------------------------------

    
    /**
     * 构造函数
     *
     * @private
     * @param {Object} options 参数
     * @param {Object} options.el 容器元素
     */
    function constructor(options) { 
        var el = this._eMain = options.el;
        var html = [
            '<div class="', SAVE_CLASS.SAVE_BUTTON_CLASS_NAME, '">保存报表</div>',
            '<ul class="', 
                SAVE_CLASS.OPERATE_BUTTONS_CLASS_NAME, ' ', 
                SAVE_CLASS.HIDE, '">',
                '<li>新增个人报表 </li>',
                '<li>更新当前报表</li>',
            '</ul>'
        ].join('');
        var elChildrens;
        var btnOperates;
        
        addClass(el, SAVE_CLASS.SAVE_CLASS_NAME);
        el.innerHTML = html;

        //设置最外层父亲z-Index
        //resetContainParentZIndex(el);

        // 获取保存按钮并挂载上
        elChildrens = domChildren(el);
        this._btnSave = elChildrens[0];
        this._btnOperates = elChildrens[1];
        
        btnOperates = domChildren(elChildrens[1]);
        this._btnAdd = btnOperates[0];
        this._btnUpdate = btnOperates[1];
    }

    /**
     * 初始化，把component中可通信的方法挂在到当前，绑定事件
     *
     * @public
     * @param {Object} options 参数对象
     * @param {Function} options.saveImageName 描述如下：
     * 保存镜像校验通过，就执行component中的saveImageName方法
     * @param {Function} options.getCurrentTabName 描述如下：
     * 通过执行component中的getCurrentTabName时时的获取当前选中tab的名称
     * @param {number} options.maxTabNum 最大可增添tab个数
     * 在component描述文件中设置，在component中挂载到vui-save下
     * @param {Function} options.getTabsNums 描述如下：
     * 通过执行component中的getTabsNums时时的获取当前tab的总个数
     */
    SAVE_BUTTON_CLASS.init = function (options) {
        this._saveImageNameCallBack = options.saveImageName;
        this._getCurrentTabName = options.getCurrentTabName;
        this._maxTabNum = options.maxTabNum;
        this._getTabsNums = options.getTabsNums;
        // 绑定事件
        bindEvent.call(this);
    };
    
    /**
     * 解禁操作
     *
     * @protected
     */
    SAVE_BUTTON_CLASS.disable = function () {
        mask(true);
    }
    
    /**
     * 启用操作
     *
     * @protected
     */
    SAVE_BUTTON_CLASS.enable = function () {
        mask(false);  
    }

    /**
     * 绑定事件
     *
     * @private
     */
    function bindEvent() {
        var me = this;

        // 绑定保存按钮click与mouseleave事件
        me._btnSave.onclick = function () {
            removeClass(me._btnOperates, SAVE_CLASS.HIDE);
        }
        me._btnSave.onmouseover = function () {
            addClass(this, SAVE_CLASS.SAVE_BUTTON_HOVER_CLASS_NAME);
        }
        me._btnSave.onmouseout = function () {
            removeClass(this, SAVE_CLASS.SAVE_BUTTON_HOVER_CLASS_NAME);
        }
        me._eMain.onmouseleave = function () {
            addClass(me._btnOperates, SAVE_CLASS.HIDE);
        }

        // 绑定新增按钮点击事件
        me._btnAdd.onclick = function (ev) {
            var oEv = ev || window.event;

            // 隐藏按钮选项
            hideOperates(me._btnOperates, oEv);
            
            if (me._getTabsNums() > me._maxTabNum) {
                alert(MESSAGE.TAB_MAX_NUM_WARN);
                return; 
            }
            // 保证this指向
            dialog.call(me, 
                        HIDE_ERROR_TIPS, 
                        '', 
                        dialogCallback, 
                        ADD_MODE);
        }

        // 绑定更新按钮点击事件
        me._btnUpdate.onclick = function (ev) {
            var oEv = ev || window.event;

            // 隐藏按钮选项
            hideOperates(me._btnOperates, oEv);
            
            dialog.call(me, 
                        HIDE_ERROR_TIPS,
                        me._getCurrentTabName(), 
                        dialogCallback, 
                        UPDATE_MODE);
        }
    };

    /**
     * 设置父亲包含块的z-Index
     * 
     * @private
     * @param {HTMLElement} el vui-save的容器
     */
    function resetContainParentZIndex(el) {
    	 var parentClassName = 'di-o_o-block';
         var parent = el.parentNode;

         while (parent) {
             parent.style.zIndex = 100;
             if (hasClass(parent, parentClassName)) {
                 break;
             }
             parent = getParent(parent);
         }
    }

    /**
     * 隐藏按钮操作项
     * 
     * @private
     * @param {HTMLElement} el 按钮操作项
     * @param {Event} ev 事件
     */
    function hideOperates(el, ev) {
        // 隐藏按钮选项
        addClass(el, SAVE_CLASS.HIDE);

        // 阻止事件冒泡
        ev.stopPropagation 
        ? (ev.stopPropagation()) 
        : (ev.cancelBubble = true);
    }

    /**
     * 弹出框事件
     * 
     * @private
     * @param {string} showErrorTips 显示错误提示的方式：是否显示
     * @param {string} value 用户输入的名称
     * @param {function} callback 弹出框点击确定后的回调事件
     * @param {boolean} isAdd 新增或者更新
     */
    function dialog(showErrorTips, value, callback, isAdd) {
        var me = this;
        // 默认项不能编辑，这块的实现不是很好
        if (value == '默认') {
            alert(MESSAGE.TAB_UPDATE_DEFAULT_WARN);
            return;
        }
        
        var html = [
           '<div class="', SAVE_CLASS.DIALOG_ERROR_CLASS_NAME, '">',
                showErrorTips ? MESSAGE.NAME_WARN : '',
            '</div>',
            '<div class="', SAVE_CLASS.DIALOG_ITEM_CLASS_NAME, '">',
                '<label>',
                    '名称',
                '</label>',
                '<input type="text" id="reportSaveName" ',
                   'value="',
                    value,
                    '"',
                    isAdd ? '' : 'disabled="disabled"',
                    ' placeholder="', MESSAGE.NAME_PLACE_HOLDER, '" />',
            '</div>'
        ].join('');
        
        confirm(
            html,
            function () {
                var name = document.getElementById('reportSaveName').value;
                // 传递this指向
                callback.call(me, isAdd, name);
            }
        );
    }

    /**
     * 弹出框中点击确定后的回调事件
     * 
     * 如果校验成功，就执行component中的saveImageName事件
     * $handleGetAllImagesSuccess中初始化vui-save时传入的saveImageName方法
     * 在saveImageName中区分新增和更新，分别去请求后端操作
     * 
     * 如果校验失败，继续执行dialog进行弹框（把当前值以及错误提示带进去）
     * 
     * @private
     * @param {string} isAdd true表示新增，反之为更新
     * @param {string} name 用户输入的名称
     */
    function dialogCallback(isAdd, name) {
        
        if(!validate(name)) {
            dialog.call(this, 
                        SHOW_ERROR_TIPS, 
                        name , 
                        dialogCallback, 
                        isAdd);
        }
        else {
            this._saveImageNameCallBack(isAdd, name);
        } 
    }
    
    /**
     * 验证名称
     * 
     * @private
     * @param {string} name 名称
     */
    function validate(name) {
        var l = textLength(name);
        
        if (name === '' 
            || l > TAB_NAME_MAX_LENGTH
        ) {
            return false;
        } 
        
        return true;
    }
    
    /**
     * 遮罩层，防止二次点击
     * 如果启用，先判断body里面是否已经生成遮罩
     * 如果已经生成，就不做处理，如果没有生成，就生成一个
     * 如果禁用，就删除掉遮罩层
     * 其实，在body里面始终只存在一个遮罩层
     * 缺陷：创建删除dom操作，感觉不是很理想
     * 不过ajax请求不会很多，性能应该不会影响很大
     * 
     * @private
     * @param {boolean} status 状态：启用还是禁用遮罩
     */
    function mask(status) {
        var oLayerMasks = domQ('ui-reportSave-layerMask', 
                               document.body);
        var oLayerMask;
        
        // oLayerMasks为一个数组
        if (oLayerMasks.length === 1){
            oLayerMask = oLayerMasks[0];
        }
        
        // 启用
        if (status) {
            // 如果 遮罩层不存在就创建一个
            // 这里用nodeType判断是否为element元素,实现不是很好
            if (!oLayerMask 
                || (oLayerMask && !oLayerMask.nodeType)
            ) {
                oLayerMask = document.createElement('div');
                
                var maskCss = [
                    'background-color: #e3e3e3;',
                    'position: absolute;',
                    'z-index: 1000;',
                    'left: 0;',
                    'top: 0;',
                    'width: 100%;',
                    'height: 100%;',
                    'opacity: 0;',
                    'filter: alpha(opacity=0);',
                    '-moz-opacity: 0;'
                    ].join('');
                
                oLayerMask.style.cssText = maskCss;
                oLayerMask.style.width = document.documentElement.scrollWidth 
                                         + "px";
                oLayerMask.className = 'ui-reportSave-layerMask';
                document.body.appendChild(oLayerMask);
            }
        }
        // 禁用
        else {
              if (oLayerMask && oLayerMask.nodeType) {
                document.body.removeChild(oLayerMask);
            }
        }
    }

})();