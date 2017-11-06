window.name ="NG_DEFER_BOOTSTRAP!";
init =false;

initStock("input[name='child.stock_id']");

var  dg = $('#dg').datagrid({
    url:PATH+'/stock/movestorage/list',
    fit:true,
    fitColumns : true,
    idField : 'id',
    sortName : 'yzwstock_movestore.insert_date',
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
        field : 'move_code',
        title : '调库单号',
        width : 100,
        sortable : true
    } ] ],
    columns:[[
        {field:'input_name',title:'录入人',width:50},
        {field:'review_status',title:'审批结果',width:50,formatter:function(value,row){
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
        {field:'check',title:'核对结果',width:50,formatter:function(value,row){
            switch(value*1){
                case 0:
                    return '待核对';
                case 1:
                    return '正常';
                default:
                    return "待验收";
            }
        }},
        {field:'check_user',title:"核对详情",width:150,formatter:function(value,row){
            if(row.status>2)
                return $.formatString("核对人[{0}]<br>验收备注[{1}]",value,row.check_text);
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
                    return "待核验";
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
});

var movedetailpro = function(id){
    $('#movedetailProducts').dialog('open');
    $("#detailProducts").datagrid({
        url:PATH+'/stock/movestorage/detailJson?id='+id,
        fit:true,
        fitColumns : true,
        idField : 'yw.id',
        sortName : 'yw.id',
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
            field : 'move_code',
            title : '调库编号',
            width : 120,
        } ] ],
        columns:[[
            {field : 'name',title : '产品名称',width : 100},
            {field : 'spec',title : '产品规格',width : 80},
            {field : 'nums',title : '产品数量',width : 80},
            {field : 'price',title : '出库单价',width : 80},
            {field : 'stock_out',title:'出库仓库',width:100},
            {field : 'stock_in',title:'入库仓库',width:100}
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




MainApp.controller('MoveStorageCtrls', [ '$scope', function($scope) {
    $scope.searchFun=function() {
        dg.datagrid('load', $.serializeObject($('#searchForm')));
    };
    $scope.cleanFun=function() {
        $('#searchForm input').val('');
        dg.datagrid('load', {});
    };
    $scope.printMoveStorageFun=function() {
        $("#ts_message").hide();
        $('#print-MoveStoragefm4').form('clear');
        showDialog('#print-MoveStorage-dlg','打印调库单');
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
                    $.post(PATH+'/stock/movestorage/delete', {
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
            showDialog('#dlg','编辑调库单');
         }
    };

    $scope.addFun=function() {
        $('#fm').form('clear');
        showDialog('#dlg','新增调库单');
        url=PATH+'/stock/movestorage/add';
    };

    $scope.submit=function(fm,dlg){
        var stock_id_in = $("input[name='child.stock_id_in']").val();
        var stock_id = $("input[name='child.stock_id']").val();
        if(stock_id != "" && stock_id_in != "" && stock_id_in == stock_id){
            $.messager.alert('提示',"入库仓库不能和出库仓库相同！");
            return false;
        }
        $(fm).form('submit',{
            url: url,
            onSubmit: function(){
                console.log("出库单提交");
                parent.$.messager.progress({
                    title : '提示',
                    text : '入库数据处理中，请稍后....'
                });
                movebtonclick_true();
            },
            success: function(result){
                parent.$.messager.progress('close');
                movebtonclick_false();
                result= $.parseJSON(result);
                //处理位置浏览器问题，返回result为空的问题。
                if(result){
                    if(result.code==200){
                        $(dlg).dialog('close');
                        movebtonclick_false();
                        dg.datagrid('reload');
                        $('#layout_west_tree').tree('reload');
                    }
                    else {
                        movebtonclick_false();
                        $.messager.alert('提示',result.msg);
                    }
                }else{
                    $(dlg).dialog('close');
                    movebtonclick_false();
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
            url=PATH+'/stock/movestorage/review';
        }
    }
    $scope.check = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            $('#fm3').form('clear');
            loadFrom('#fm3',node);
            showDialog('#dlg-check','出库核对');
            url=PATH+'/stock/movestorage/check';
        }
    }
    $scope.print = function(id){
        var url=PATH+'/stock/outstorage/print?outid='+id;
        window.location.href=url;
    }

} ]);

var printMoveStorageExcel = function(obj) {
    var start=$('#printDateStart').datebox('getValue');
    var end=$('#printDateEnd').datebox('getValue');
    var url = PATH + '/stock/movestorage/printMoveStorageChoose?start='+start+'&end='+end;
    if (start == "" || end == "") {
        $("#ts_message").show();
    }else{
        window.location.href=url;
        $('#print-MoveStorage-dlg').dialog('close')
    }
}