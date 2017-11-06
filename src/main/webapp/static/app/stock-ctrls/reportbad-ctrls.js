window.name ="NG_DEFER_BOOTSTRAP!";
init =false;

initStock("input[name='child.stock_id']");

var  dg = $('#dg').datagrid({
    url:PATH+'/stock/reportbad/list',
    fit:true,
    fitColumns : true,
    idField : 'id',
    sortName : 'yzwstock_reportbad.insert_date',
    sortOrder : 'desc',
    striped: true,
    border : false,
    nowrap:false,
    rownumbers:true,
    singleSelect:true,
    checkOnSelect:true,
    selectOnCheck:true,
    pagination : true,
    pageSize : 15,
    pageList : [15, 20, 30, 40, 50],
    frozenColumns : [ [ {
        field : 'id',
        title : '编号',
        width : 150,
        checkbox : true
    }, {
        field : 'code',
        title : '报损单号',
        width : 100,
        sortable : true
    } ] ],
    columns:[[
        {field:'bad_reason',title:'原因',width:50},
        {field:'reporter',title:'报损人',width:50},
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
        {field:'status',title:'状态',width:50,formatter:function(value,row){
            switch(value*1){
                case 0:
                    return "已删除";
                case 1:
                    return "待审批";
                case 2:
                    return "待扫码";
                case 3:
                    return "已完成";
            }
        }},
        {field:'last_date',title:'修改时间',width:140},
        {field:'insert_date',title:'提交时间',width:140},
        {field:'action',title:'操作',width:100,
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
    onDblClickRow:function(index,row){
        if(li_south == 0){
            $('#main').layout('expand','south');
        }
        $('#outstorageDetail').panel("refresh", "/stock/outstorage/detail?outId="+row.id);
    }
});


var repdetailpro = function(id){
    $('#repdetail-dlg').dialog('open');
    $("#repdeatil").datagrid({
        url:PATH+'/stock/reportbad/detaByDataGrid?id='+id,
        fit:true,
        fitColumns : true,
        idField : 'yr.id',
        sortName : 'yr.id',
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
            field : 'code',
            title : '报损编号',
            width : 120,
        } ] ],
        columns:[[
            {field : 'productName',title : '产品名称',width : 100},
            {field : 'spec',title : '产品规格',width : 80},
            {field : 'product_num',title : '产品数量',width : 80},
            {field : 'stockName',title:'出库仓库',width:100},
            {field:'status',title:'状态',width:50,formatter:function(value,row){
                switch(value*1){
                    case 0:
                        return "已删除";
                    case 1:
                        return "待审批";
                    case 2:
                        return "待扫码";
                    case 3:
                        return "已完成";
                }
            }},
        ]],
        onLoadSuccess : function() {
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




MainApp.controller('OutStorageCtrls', [ '$scope', function($scope) {
    $scope.searchFun=function() {
        dg.datagrid('load', $.serializeObject($('#searchForm')));
    };
    $scope.cleanFun=function() {
        $('#searchForm input').val('');
        dg.datagrid('load', {});
    };

    $scope.printReportbadFun=function() {
        $("#ts_message").hide();
        $('#print-Reportbadfm4').form('clear');
        showDialog('#print-Reportbad-dlg','打印报损单');
    };

    $scope.deleteFun=function(id) {
        if (id == undefined) {//点击右键菜单才会触发这个
            var rows = dg.datagrid('getSelections');
            id = rows[0].id;
        } else {//点击操作里面的删除图标会触发这个
            dg.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.messager.confirm('询问', '您是否要删除当前出库单？', function(b) {
            if (b) {
                var currentUserId = '${session.user.id}';/*当前登录用户的ID*/
                if (alertSelf(currentUserId,id)) {
                    parent.$.messager.progress({
                        title : '提示',
                        text : '数据处理中，请稍后....'
                    });
                    $.post(PATH+'/stock/reportbad/delete', {
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
            showDialog('#dlg','编辑出库单');
            url=PATH+'/stock/reportbad/edit';
        }
    };

    $scope.addFun=function() {
        $('#fm').form('clear');
        showDialog('#dlg','新增报损');
        url=PATH+'/stock/reportbad/add';
        window.location.reload();
    };


    $scope.submit=function(fm,dlg){
        $(fm).form('submit',{
            url: url,
            onSubmit: function(){
                parent.$.messager.progress({
                    title : '提示',
                    text : '入库数据处理中，请稍后....'
                });
                badbtonclick_true();
            },
            success: function(result){
                parent.$.messager.progress('close');
                badinbtonclick_false();
                result= $.parseJSON(result);
                //处理位置浏览器问题，返回result为空的问题。
                if(result){
                    if(result.code==200){
                        $(dlg).dialog('close');
                        badinbtonclick_false();
                        dg.datagrid('reload');
                        $('#layout_west_tree').tree('reload');
                    }else {
                        badinbtonclick_false();
                        $.messager.alert('提示',result.msg);
                    }
                }else{
                    $(dlg).dialog('close');
                    badinbtonclick_false();
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
            $('#fm2').form('clear');
            loadFrom('#fm2',node);
            showDialog('#dlg-review','审批');
            url=PATH+'/stock/reportbad/review';
        }
    }
    $scope.check = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            $('#fm3').form('clear');
            loadFrom('#fm3',node);
            showDialog('#dlg-check','出库核对');
            url=PATH+'/stock/reportbad/check';
        }
    }
    $scope.print = function(id){
        var url=PATH+'/stock/reportbad/print?id='+id;
        window.location.href=url;
    }

} ]);

var printReportbadExcel = function(obj) {
    var start=$('#printDateStart').datebox('getValue');
    var end=$('#printDateEnd').datebox('getValue');
    var url = PATH + '/stock/reportbad/printReportbadChoose?start='+start+'&end='+end;
    if (start == "" || end == "") {
        $("#ts_message").show();
    }else{
        window.location.href=url;
        $('#print-Reportbad-dlg').dialog('close')
    }
}