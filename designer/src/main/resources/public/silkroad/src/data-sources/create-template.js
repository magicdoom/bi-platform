/**
 * @file:    数据源新建模块Html Template
 * @author:  lizhantong(lztlovely@126.com)
 */

define(['template'], function (template){

    //------------------------------------------
    // 常量 
    //------------------------------------------

    var CLASS = {
        J_CREATE_CLASS: 'j-data-sources-create',
        J_DATA_SOURCES_NAME: 'j-input-datasource-name',
        J_DATA_SOURCES_ADDRESS: 'j-input-datasource-address',
        J_ADD_ADDRESS: 'j-add-address',
        J_DELETE_ADDRESS: 'j-delete-address',
        J_DATASOURCE_USERNAME_BOX: 'j-datasource-userName-box',
        J_DATASOURCE_DATABASE_BOX: 'j-datasource-database-box',//
        J_INPUT_DATASOURCE_USERNAME: 'j-input-datasource-userName',
        J_INPUT_PASSWORD: 'j-input-password',
        J_INPUT_DATASOURCE_DATABASE: 'j-input-database',
        J_DATA_SOURCES_PART: 'j-data-sources-part',
        J_DATASOURCE_RESERVEADDRESS_MOUDLE: 'j-datasource-reserveAddress-moudle',
        J_DATA_SOURCES_TYPE: 'j-input-datasource-type',
        J_DATA_SOURCES_ENCODING: 'j-input-datasource-encoding'
    };

    var STRING = {
        'ADDRESS_PLACEHOLDER': '例如：172.21.217.74:8801 或 a.baidu.com',
        'ADDRESS_VALIDATE': '请输入正确格式的数据库地址(ip+端口 或 域名)'

    };

    //------------------------------------------
    // html模版
    //------------------------------------------
    // 其他的模板语法 doc\syntax-simple.md
    var html = '<!--模板-添加数据源-->'
        + '<div class="form-common data-sources-create '+ CLASS.J_CREATE_CLASS+ '">'
        +     '<div class="form-common-line">'
        +         '<span class="form-common-label">数据源名称：</span>'
        +         '<div class="form-common-text form-common-text-big">'
        +             '<input type="text" class="'+ CLASS.J_DATA_SOURCES_NAME +'" name="name" value="{{name}}"/>'
        +             '<span class="form-common-text-validation hide">数据源名称不能为空</span>'
        +         '</div>'
        +     '</div>'
        +     '<div class="form-common-line">'
        +         '<span class="form-common-label">数据源类型：</span>'
        +         '<select class="form-common-select ' + CLASS.J_DATA_SOURCES_TYPE + '" name="type">'
        +             '<option value="MYSQL" {{if type== "MYSQL"}} selected="true"{{/if}}>MySQL</option>'
        +             '<option value="MYSQL" {{if type== "MYSQL-DBPROXY"}} selected="true"{{/if}}>MySQL-DBPROXY</option>'
        +         '<select>'
        +     '</div>'
        +     '<div class="form-common-line">'
        +         '<span class="form-common-label">数据库地址：</span>'
        +         '<div class="form-common-text form-common-text-big">'
        +             '<input type="text" class="' + CLASS.J_DATA_SOURCES_ADDRESS + '"name="hostAndPort" value="{{hostAndPort}}" placeholder="' + STRING.ADDRESS_PLACEHOLDER + '"/>'
        +             '<span class="form-common-text-validation hide">' + STRING.ADDRESS_VALIDATE + '</span>'
        +             '<span class="form-common-btn-extend form-common-btn-extend-absolute ' + CLASS.J_ADD_ADDRESS + '" title="添加备用数据库">+</span>'
        +         '</div>'
        +     '</div>'
        +     '{{each reserveAddress as $value}}'
        +     '{{if $value}}'
        +     '<div class="form-common-line">'
        +         '<span class="form-common-label">备库地址：</span>'
        +         '<div class="form-common-text form-common-text-big">'
        +             '<input type="text" class="' + CLASS.J_DATA_SOURCES_ADDRESS + '" placeholder="' + STRING.ADDRESS_PLACEHOLDER + '" value="{{$value}}"/>'
        +             '<span class="form-common-text-validation hide">' + STRING.ADDRESS_VALIDATE + '</span>'
        +             '<span class="form-common-btn-extend form-common-btn-extend-absolute ' + CLASS.J_DELETE_ADDRESS + '" title="删除">×</span>'
        +         '</div>'
        +     '</div>'
        +     '{{/if}}'
        +     '{{/each}}'
        +     '<div class="form-common-line ' + CLASS.J_DATASOURCE_DATABASE_BOX + '">'
        +         '<span class="form-common-label">数据库：</span>'
        +         '<div class="form-common-text form-common-text-big">'
        +             '<input type="text" class="' + CLASS.J_INPUT_DATASOURCE_DATABASE + '"name="dbInstance" value="{{dbInstance}}" placeholder="请输入数据库名"/>'
        +             '<span class="form-common-text-validation hide">数据库不能为空</span>'
        +         '</div>'
        +     '</div>'
        +     '<div class="form-common-line ' + CLASS.J_DATASOURCE_USERNAME_BOX + '">'
        +         '<span class="form-common-label">用户名：</span>'
        +         '<div class="form-common-text form-common-text-big">'
        +             '<input type="text" class="' + CLASS.J_INPUT_DATASOURCE_USERNAME + '" name="dbUser" value="{{dbUser}}" placeholder="请输入正确格式的用户名"/>'
        +             '<span class="form-common-text-validation hide">用户名不能为空</span>'
        +         '</div>'
        +     '</div>'
        +     '<div class="form-common-line">'
        +         '<span class="form-common-label">密码：</span>'
        +         '<div class="form-common-text form-common-text-big">'
        +             '<input type="password" class="' + CLASS.J_INPUT_PASSWORD + '" name="dbPwd" value="{{dbPwd}}" placeholder="请输入正确格式的密码"/>'
        +             '<span class="form-common-text-validation hide">密码不能为空</span>'
        +         '</div>'
        +     '</div>'
        +     '<div class="form-common-line c-p c-link">'
        +         '<span class="form-common-label j-extend-line-link">'
        +             '高级选项'
        +             '<span class="icon-arrow icon-arrow-down j-icon-arrow"></span>'
        +         '</span>'
        +     '</div>'
        +     '<div class="form-common-line j-extend-line hide">'
        +         '<span class="form-common-label">数据库编码：</span>'
        +         '<select class="form-common-select ' + CLASS.J_DATA_SOURCES_ENCODING + '" name="encoding">'
        +             '<option value="">请选择</option>'
        +             '<option value="GBK" {{if encoding== "GBK"}} selected="true"{{/if}}>GBK</option>'
        +             '<option value="UTF-8" {{if encoding== "UTF-8"}} selected="true"{{/if}}>UTF-8</option>'
        +         '<select>'
        +     '</div>'

        +     '<div class="form-common-line ta-c">'
        +         '<span class="button button-flat-primary m-20 j-button-submit">提交</span>'
        +         '<span class="button button-flat m-20 j-button-cancel">取消</span>'
        +     '</div>'
        + '</div>'
        // 小片段
        + '<div class="hide ' + CLASS.J_DATA_SOURCES_PART + '">'
              // 备用数据库地址
        +        '<div class="form-common-line ' + CLASS.J_DATASOURCE_RESERVEADDRESS_MOUDLE + '">'
        +            '<span class="form-common-label w-100">备库地址：</span>'
        +           '<div class="form-common-text form-common-text-big">'
        +                '<input type="text" class="' + CLASS.J_DATA_SOURCES_ADDRESS + '" placeholder="' + STRING.ADDRESS_PLACEHOLDER + '" />'
        +                '<span class="form-common-text-validation hide">' + STRING.ADDRESS_VALIDATE + '</span>'
        +                '<span class="form-common-btn-extend form-common-btn-extend-absolute ' + CLASS.J_DELETE_ADDRESS + '" title="删除">×</span>'
        +           '</div>'
        +           '</div>'
        +      '</div>';

    return {
        render: template.compile(html),
        CLASS: CLASS
    };
});