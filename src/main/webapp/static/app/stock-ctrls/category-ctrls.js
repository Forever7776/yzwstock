window.name ="NG_DEFER_BOOTSTRAP!";
init =false;

//init
$('#iconCls').combobox({
    data : $.iconData,
    formatter : function(v) {
        return $.formatString('<span class="{0}" style="display:inline-block;vertical-align:middle;width:16px;height:16px;"></span>{1}', v.value, v.value);
    }
});

$('#pid').combotree({
    url : PATH+'/stock/category/tree',
    parentField : 'pid',
    lines : true,
    panelHeight : 'auto'
});


var treeGrid = $('#treeGrid').treegrid({
    url : PATH+'/stock/category/list',
    idField : 'id',
    treeField : 'name',
    parentField : 'pid',
    fit : true,
    fitColumns : false,
    border : false,
    frozenColumns : [ [ {
        title : '编号',
        field : 'id',
        width : 150,
        hidden : true
    } ] ],
    columns : [ [ {
        field : 'name',
        title : '分类名称',
        width : 200
    },{
            field : 'pid',
            title : '上级资源ID',
            width : 150,
            hidden : true
        }, {
            field : 'remarks',
            title : '备注',
            width : 250
        } ,{
        field : 'action',
        title : '操作',
        width : 50,
        formatter : formatterFun
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
    }
});


MainApp.controller('CategoryCtrls', [ '$scope', function($scope) {

    $scope.deleteFun=function(id) {
        if (id != undefined)treeGrid.treegrid('select', id);

        var node = treeGrid.treegrid('getSelected');
        if (node) {
            parent.$.messager.confirm('询问', '您是否要删除当前资源？', function(b) {
                if (b) {
                    parent.$.messager.progress({
                        title : '提示',
                        text : '数据处理中，请稍后....'
                    });
                    $.post(PATH+'/stock/category/delete', {
                        id : node.id
                    }, function(result) {
                        if (result.code==200) {
                            treeGrid.treegrid('reload');
                            $('#layout_west_tree').tree('reload');
                            $('#pid').combotree('reload');
                        }
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
            if(node.remarks)$('#des').text(node.remarks);
            $('#pid').combotree('reload',PATH+'/stock/category/tree?passId='+node.id);
            showDialog('#dlg','编辑资源');
            url=PATH+'/stock/category/edit';
        }
    };

    $scope.addFun=function() {
        $('#fm').form('clear');
        showDialog('#dlg','添加分类');
        url=PATH+'/stock/category/add';
    };


    $scope.submit=function(){
        $('#fm').form('submit',{
            url: url,
            success: function(result){
                result= $.parseJSON(result);
                if(result.code==200){
                    $('#dlg').dialog('close');
                    treeGrid.treegrid('reload');
                    $('#layout_west_tree').tree('reload');
                    $('#pid').combotree('reload');
                }
                else {
                    $.messager.alert('提示',result.msg);
                }
            }
        });
    };
} ]);