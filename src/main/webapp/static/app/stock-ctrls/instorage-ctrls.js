window.name ="NG_DEFER_BOOTSTRAP!";
init =false;
inId=0;
in_Code = 0;
access = false;

initProduct("input[name='child.product_id']");

var  dg = $('#dg').datagrid({
    url:PATH+'/stock/instorage/list',
    fit:true,
    fitColumns : true,
    idField : 'id',
    sortName : 'insert_date',
    sortOrder : 'desc',
    striped: true,
    singleSelect:true,
    checkOnSelect:true,
    selectOnCheck:true,
    pagination : true,
    pageSize : 10,
    pageList : [10,15, 20, 30, 40, 50],
    frozenColumns : [ [ {
        field : 'id',
        title : '编号',
        width : 150,
        checkbox : true
    }, {
        field : 'code',
        title : '入库单号',
        width : 100,
        sortable : true
    }] ],
    columns:[[
        {field:'supplier_name',title:'供应商',width:50},
        {field:'input_name',title:'录入人',width:50},
        {field:'deliverer',title:'送货人',width:50,hidden:true},
        {field:'review',title:'审批结果',width:50,formatter:function(value,row){
            if(row.status>1){
                switch(value*1){
                    case 0:
                        return '不通过';
                    case 1:
                        return "审批通过";
                    default :
                        return "";
                }
            }else{
                return "";
            }
        }},
        {field:'review_text',title:'审批详情',width:100,formatter:function(value,row){
            if(row.status>1)
                return "审批人[" + row.review_user + "]<br>审批意见["+row.review_text+"]";
            else
                return "";
        }},
        {field:'check',title:'验收结果',width:50,formatter:function(value,row){
            if(row.status>2){
                switch(value*1){
                    case 0:
                        return '异常';
                    case 1:
                        return '正常';
                    default:
                        return "待验收";
                }
            }else
                return "";
        }},
        {field:'check_user',title:"验收详情",width:80,formatter:function(value,row){
            if(row.status>2)
                return $.formatString("验收人[{0}]<br>验收备注[{1}]",value,row.check_text);
            else
                return "";
        }},
        {field:'status',title:'状态',width:80,formatter:function(value,row){
            switch(value*1){
                case 0:
                    return "已删除";
                case 1:
                    return "待审批";
                case 2:
                    return "待核验";
                case 3:
                    return "已完成";
                case 6:
                    return "出库完成";
            }
        }},
        {field:'last_date',title:'修改时间',width:100},
        {field:'insert_date',title:'提交时间',width:100},
        {field:'action',title:'操作',width:80,
            formatter:formatterFun
        }
    ]],
    toolbar : '#toolbar',
    onLoadSuccess : function(data) {
        $('#searchForm table').show();
        parent.$.messager.progress('close');
        $(this).datagrid('tooltip');

        for(var i=0;i<data.rows.length;i++){
            $('#btn-edit'+data.rows[i].id).menubutton({
                iconCls: 'icon-edit',
                menu: '#mm'+data.rows[i].id
            });
        }
        if(!init){
            angular.resumeBootstrap();
            init=true;
        }
    },
    onRowContextMenu : function(e, rowIndex, rowData) {
        e.preventDefault();
        $(this).datagrid('unselectAll').datagrid('uncheckAll');
        $(this).datagrid('selectRow', rowIndex);
        $('#menu').menu('show', {
            left : e.pageX,
            top : e.pageY
        });
    },
    onClickRow:function(index,row){
        if(li_south == 0){
            $('#main').layout('expand','south');
        }

        inId = row.id;
        in_Code = row.code;
        if(row.status == 0){
            var url = "/stock/instorage/detail";
        }else{
            var url = "/stock/instorage/detail?inId="+inId;
        }

        if(row.status == 3){
            access= true;
        }else{
            access = false;
        }
        if(access){
            url = "/stock/instorage/detail1?inId="+inId+"&code="+in_Code;
        }

        $('#instorageDetail').panel("refresh", url);
    }
});



var initDetailDg = function(selector,dataid){
    $('#'+selector).datagrid({
        url:PATH+'/stock/instorage/detailJson?inId='+dataid,
        fit:true,
        fitColumns : true,
        idField : 'id',
        sortName : 'insert_date',
        sortOrder : 'desc',
        striped: true,
        border : false,
        nowrap:false,
        rownumbers:true,
        singleSelect:true,
        pagination : true,
        checkOnSelect : false,
        selectOnCheck : false,
        pageSize : 15,
        pageList : [15, 20, 30, 40, 50],
        frozenColumns : [ [ {
            field : 'id',
            title : '编号',
            width : 50,
            checkbox : true
        } ] ],
        columns:[[
            {field : 'product_name',title : '货物产品',width : 100},
            {field : 'spec',title : '货物规格',width : 100},
            {field : 'nums',title : '货物数量',width : 100},
            {field : 'price',title : '入库单价',width : 100},
            {field : 'stock_name',title:'仓库',width:100}
        ]],
        onLoadSuccess : function() {
            $('#detailSearchForm table').show();
            parent.$.messager.progress('close');
            $(this).datagrid('tooltip');



        },
        onRowContextMenu : function(e, rowIndex, rowData) {
            e.preventDefault();
            $(this).datagrid('unselectAll').datagrid('uncheckAll');
            $(this).datagrid('selectRow', rowIndex);
        }
    });

}

var detailPRO = function(selector,dataid){
    $('#'+selector).datagrid({
        url:PATH+'/stock/instorage/detailJson?inId='+dataid,
        fit:true,
        fitColumns : true,
        idField : 'id',
        sortName : 'insert_date',
        sortOrder : 'desc',
        striped: true,
        border : false,
        nowrap:false,
        rownumbers:true,
        singleSelect:true,
        pagination : true,
        checkOnSelect : false,
        selectOnCheck : false,
        pageSize : 15,
        pageList : [15, 20, 30, 40, 50],
        frozenColumns : [ [ {
            field : 'id',
            title : '编号',
            width : 50,
            //checkbox : true
        } ] ],
        columns:[[
            {field : 'product_name',title : '货物产品',width : 100},
            {field : 'spec',title : '货物规格',width : 100},
            {field : 'nums',title : '货物数量',width : 100},
            {field : 'price',title : '入库单价',width : 100},
            {field : 'stock_name',title:'仓库',width:100}
        ]],
        onLoadSuccess : function() {
            $('#detailSearchForm table').show();
            parent.$.messager.progress('close');
            $(this).datagrid('tooltip');



        },
        onRowContextMenu : function(e, rowIndex, rowData) {
            e.preventDefault();
            $(this).datagrid('unselectAll').datagrid('uncheckAll');
            $(this).datagrid('selectRow', rowIndex);
        }
    });

}


MainApp.controller('InStorageCtrls', [ '$scope', function($scope) {
    $scope.searchFun=function() {
        dg.datagrid('load', $.serializeObject($('#searchForm')));
    };
    $scope.cleanFun=function() {
        $('#searchForm input').val('');
        dg.datagrid('load', {});
    };
    $scope.importDowloadFun = function(){
        var url=PATH+'/stock/instorage/importDowload';
        window.location.href=url;
    };
    $scope.importFun=function() {
        $('#fm4').form('clear');
        showDialog('#import-dlg','批量导入');
        url=PATH+'/stock/instorage/importExcel';
    };

    $scope.printInstorageFun=function() {
        $("#ts_message").hide();
        $('#print-instoragefm4').form('clear');
        showDialog('#print-instorage-dlg','打印入库单');
    };

    $scope.printInStorage = function(id){
        var url=PATH+'/stock/instorage/printInStorage?inid='+id;
        window.location.href=url;
    }

    $scope.printFun=function(id){
        var url=PATH+'/stock/instorage/print?id='+id;
        window.location.href=url;
        dg.datagrid('reload');
    };


    $scope.deleteFun=function(id) {
        if (id == undefined) {//点击右键菜单才会触发这个
            var rows = dg.datagrid('getSelections');
            id = rows[0].id;
            if(rows[0].type == 2){
                return false;
            }
        } else {//点击操作里面的删除图标会触发这个
            dg.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.messager.confirm('询问', '您是否要删除当前产品？', function(b) {
            if (b) {
                var currentUserId = '${session.user.id}';/*当前登录用户的ID*/
                if (alertSelf(currentUserId,id)) {
                    parent.$.messager.progress({
                        title : '提示',
                        text : '数据处理中，请稍后....'
                    });
                    $.post(PATH+'/stock/instorage/delete', {
                        id : id
                    }, function(result) {
                        if (result.code==200) {
                            dg.datagrid('reload');
                        }
                        parent.$.messager.progress('close');
                    }, 'JSON');

                }
            }
        });
    };

    $scope.editFun=function(id) {
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            loadFrom('#fm',node);
            if(node.content)$('#content').text(node.content);
            showDialog('#dlg','编辑入库单');
            url=PATH+'/stock/instorage/edit';
        }
    };
    $scope.addFun=function() {

        $('#fm').form('clear');
        showDialog('#dlg','新增入库单');
        url=PATH+'/stock/instorage/add';
    };

    $scope.submit=function(fm,dlg){
        $(fm).form('submit',{
            url: url,
            onSubmit:function(){
                parent.$.messager.progress({
                    title : '提示',
                    text : '入库数据处理中，请稍后....'
                });
                inbtonclick_true();
            },
            success: function(result){
                parent.$.messager.progress('close');
                inbtonclick_false();
                result= $.parseJSON(result);
                //处理位置浏览器问题，返回result为空的问题。
                if(result){
                    if(result.code==200){
                        $(dlg).dialog('close');
                        inbtonclick_false();
                        dg.datagrid('reload');
                        $('#layout_west_tree').tree('reload');
                    }else {
                        inbtonclick_false();
                        $.messager.alert('提示',result.msg);
                    }
                }else{
                    $(dlg).dialog('close');
                    inbtonclick_false();
                    dg.datagrid('reload');
                    $('#layout_west_tree').tree('reload');
                }


            }
        });

    };
    $scope.review = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            initDetailDg("review-inStorageDetailDg",id);
            $('#fm2').form('clear');
            loadFrom('#fm2',node);
            showDialog('#dlg-review','审批');
            url=PATH+'/stock/instorage/review';
        }
    }

    $scope.detailPro = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            detailPRO("detailProducts",id);
            showDialog('#detailPro','详情');
            url=PATH+'/stock/instorage/detailPro';
        }
    }

    $scope.check = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            initDetailDg("check-inStorageDetailDg",id);
            $('#fm3').form('clear');
            loadFrom('#fm3',node);
            showDialog('#dlg-check','验收');
            url=PATH+'/stock/instorage/check';
        }
    }
} ]);

var changeprice = function(obj){
    var $that = $(obj);
    var temp=/^\d+(\.\d+)?$/;
    var price = $that.parent().parent().find("input[name='child.price']").val();
    var sum =  $that.parent().parent().find("input[name='child.nums']").val();
    if(!temp.test(price)){
        $.messager.alert('提示',"请输入正确的单价！");
        return false;
    }  else if(price != "" && sum != "" ){
        $that.parent().parent().find("input[name='priceSum']").val(parseFloat(price) * parseFloat(Math.round(sum*100)/100));
    } else {
        $that.parent().parent().find("input[name='priceSum']").val("");
        return false;
    }
    var t = 0;
    var result = $("#content input[name='priceSum']");
    for(var i=0;i<result.length;i++){
        t =t + parseFloat(result[i].value);
        $("input[name='instorage.total_fee']").val(t);
    }
}


var changenum = function(obj){
    var $that = $(obj);
    var temp=/^\d+(\.\d+)?$/;
    var num =  $that.parent().parent().find("input[name='child.nums']").val();
    var price = $that.parent().parent().find("input[name='child.price']").val();
    if(!temp.test(num)) {
        $.messager.alert('提示', "请输入正确的数量！");
        return false;
    }
    if(num == "") {
        $.messager.alert('提示', "请输入入库数量！");
        return false;
    } else if(price != "" && sum != "" ){
        $that.parent().parent().find("input[name='priceSum']").val(parseFloat(price) * parseFloat(ngNonBindable(sum*100)/100));
    }

}

var importExcel = function(obj) {
    url = PATH + "/stock/instorage/importExcel";
    var filename = $("#file").val();
    var fileType = filename.substring(filename.lastIndexOf(".") + 1);
    if (filename == "") {
        $.messager.alert('提示', "请选择要上传的文件！");
        return false;
    }
    if (fileType != "xls") {
        $.messager.alert('提示', "请选择Excel文件！");
        return false;
    }
    $.ajaxFileUpload({
        url: url,
        secureuri: false,
        fileElementId: 'file',
        dataType: 'json',
        success: function (result) {
            if (result) {
                $.messager.alert('提示', result.msg);
            }
            $('#import-dlg').dialog('close')
        },
        error: function (data, status, e) {
            $.messager.alert('提示', "导入文件发生错误！");
        }
    });

}

var printInstorageExcel = function(obj) {
    var start=$('#printDateStart').datebox('getValue');
    var end=$('#printDateEnd').datebox('getValue');
    var url = PATH + '/stock/instorage/printInStorageChoose?start='+start+'&end='+end;
    if (start == "" || end == "") {
       $("#ts_message").show();
    }else{
          window.location.href=url;
        $('#print-instorage-dlg').dialog('close')
    }
}