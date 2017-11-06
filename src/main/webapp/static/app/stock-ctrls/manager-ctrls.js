window.name ="NG_DEFER_BOOTSTRAP!";
init =false;

$('#pid').combotree({
    url : PATH+'/stock/manager/tree',
    parentField : 'pid',
    lines : true,
    panelHeight : 'auto'
});



var treeGrid = $('#treeGrid').treegrid({
    url : PATH+'/stock/manager/list',
    idField : 'id',
    treeField : 'name',
    parentField : 'pid',
    fit : true,
    fitColumns : false,
    border : false,
    nowrap : true,
    pageSize : 10,
    pageList : [10,15, 20, 30, 40, 50],
    columns : [ [
    {
        title : '编号',
        field : 'id',
        width : 150,
        checkbox : true
    }, {
        field : 'name',
        title : '名称',
        width : 150
    },
    {
    field : 'pid',
    title : '上级ID',
    width : 150,
    hidden : true
    },{
        field : 'pname',
        title : '上级名称',
        width : 150,
        hidden:true
    },{
        field : 'code',
        title : '代码',
        width : 150
    },{
        field : 'address',
        title : '位置',
        width : 300
    }, {
        title : '经纬度',
        field : 'address_point',
        width : 150

       },
        {
        field : 'user_name',
        title : '管理人',
        width : 80
    },
        {field:'is_total',title:'是否总仓',width:50,formatter:function(value,row){
            if(value=='1') return '是';
            if(value=='0') return '否';
        }},
        {
        field : 'action',
        title : '操作',
        width : 70,
        formatter :formatterFun
    } ] ],
    toolbar : '#toolbar',
    onContextMenu : function(e, row) {
        e.preventDefault();
        $(this).treegrid('unselectAll');
        $(this).treegrid('select', row.id);
        $('#menu').menu('show', {
            left : e.pageX,
            top : e.pageY
        });
    },
    onLoadSuccess : function() {
        parent.$.messager.progress('close');
        $(this).treegrid('tooltip');
        if(!init){
            angular.resumeBootstrap();
            init=true;
        }
    },
    onClickRow:function(index){
        var inId = index.id;
        if(li_south == 0){
            $('#main').layout('expand','south');
        }
        $('#instorageDetail').panel("refresh", "/stock/manager/detail?inId="+inId);
    }
});


MainApp.controller('ManagerCtrls', [ '$scope', function($scope) {

    $scope.searchFun=function() {
        console.log(treeGrid);
        treeGrid.treegrid('load', $.serializeObject($('#searchForm')));
    };
    $scope.cleanFun=function() {
        $('#searchForm')[0].reset();
        $('#searchForm input').val('');
        treeGrid.treegrid('load', {});
    };
    $scope.deleteFun=function(id) {
        if (id != undefined)  treeGrid.treegrid('select', id);

        var node = treeGrid.treegrid('getSelected');
        if (node) {
            parent.$.messager.confirm('询问', '您是否要删除当前角色？', function(b) {
                if (b) {
                    parent.$.messager.progress({
                        title : '提示',
                        text : '数据处理中，请稍后....'
                    });
                    $.post(PATH+'/stock/manager/delete', {
                        id : node.id
                    }, function(result) {

                        if (result.code==200) {
                            treeGrid.treegrid('reload');
                            //$('#pid').combotree('reload');
                        }else $.messager.alert('提示',result.msg);
                        parent.$.messager.progress('close');
                    }, 'JSON');
                }
            });
        }
    };

    $scope.editFun=function(id) {
        if (id != undefined)treeGrid.treegrid('select', id);

        var node = treeGrid.treegrid('getSelected');
        if (node) {
            loadFrom('#fm',node);
            showDialog('#dlg','编辑仓库');
            $('#pid').combotree('reload',PATH+'/stock/manager/tree?passId='+node.id);
            url=PATH+'/stock/manager/edit';
        }
    };

    $scope.totalStock=function(id,is_total){
        parent.$.messager.confirm('询问', '是否设置成总仓或取消总仓资格？', function(b) {
            if(b){
                    parent.$.messager.progress({
                        title : '提示',
                        text : '数据处理中，请稍后....'
                    });
                    $.post(PATH+'/stock/manager/total', {
                        id : id,
                        is_total:is_total
                    }, function(result) {
                        if (result.code==200) {
                            treeGrid.treegrid('reload');
                            $('#pid').combotree('reload');
                        }
                        parent.$.messager.progress('close');
                    }, 'JSON');
                }
        });
    };

    $scope.addFun=function() {
        $('#fm').form('clear');
        showDialog('#dlg','添加仓库');
        url=PATH+'/stock/manager/add';
    };

    $scope.submit=function(){
        $('#fm').form('submit',{
            url: url,
            success: function(result){
                result= $.parseJSON(result);
                if(result.code==200){
                    $('#dlg').dialog('close');
                    treeGrid.treegrid('reload');
                    $('#pid').combotree('reload');
                }
                else {
                    $.messager.alert('提示',result.msg);
                }
            }
        });
    };
} ]);

