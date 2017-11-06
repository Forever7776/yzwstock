window.name ="NG_DEFER_BOOTSTRAP!";
init =false;


var  dg = $('#dg').datagrid({
    url:PATH+'/stock/stockquery/list',
    fit:true,
    fitColumns : true,
	idField : 'id',
	sortName : 'yzwstock_product.insert_date',
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
     columns:[[
         {
             field : 'id',
             title : '编号',
             width : 50,
             checkbox : true
         }, {
             field : 'name',
             title : '产品名称',
             width : 70,
             sortable : true
         } , {
             field : 'spec',
             title : '产品规格',
             width : 70,
             sortable : true
         },
         {
           field : 'category_name',
           title : '所属分类',
           width : 70
       },
       {
           field : 'code',
           title : '货物代码',
           width : 70
       },
         {
             field : 'stock_nums',
             title : '总数',
             width : 70
         },
		 {
			 field : 'lockingnums',
			 title : '冻结数',
			 width : 70
		 },
		 {
			 field : 'enablenums',
			 title : '可出库数',
			 width : 70
		 },
         {
             field : 'unit',
             title : '计量单位',
             width : 70
         },
      ]],
    toolbar : '#toolbar',
    onClickRow:function(index,row){
        if(li_south == 0){
            $('#stockquery_main_list').layout('expand','south');
        }
        $('#productStockDetail').panel("refresh", "/stock/stockquery/detailList?productid="+row.id);
    },
    onLoadSuccess : function() {
		$('#searchForm table').show();
		parent.$.messager.progress('close');
		$(this).datagrid('tooltip');

		if(!init){
			angular.resumeBootstrap();
			init=true;
		}
	},
	onRowContextMenu : function(e, rowIndex, rowData) {
		e.preventDefault();
		$(this).datagrid('unselectAll').datagrid('uncheckAll');
	}
});

var li_south = 0;



MainApp.controller('ProductStockCtrls', [ '$scope', function($scope) {


$scope.searchFun=function() {
	dg.datagrid('load', $.serializeObject($('#searchForm')));
};
$scope.cleanFun=function() {
	$('#searchForm input').val('');
	dg.datagrid('load', {});
};


$scope.deleteFun=function(id) {
	if (id == undefined) {//点击右键菜单才会触发这个
		var rows = dg.datagrid('getSelections');
		id = rows[0].id;
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
				$.post(PATH+'/stock/stockquery/delete', {
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
		showDialog('#dlg','编辑货物');
		url=PATH+'/stock/stockquery/edit';
	}
};

$scope.addFun=function() {
	  $('#fm').form('clear');
	  showDialog('#dlg','添加货物');
	  url=PATH+'/stock/stockquery/add';
};


$scope.submit=function(fm,dlg){
      $(fm).form('submit',{
                url: url,
                success: function(result){
                 result= $.parseJSON(result);
                 if(result.code==200){
                  $(dlg).dialog('close'); 
                  dg.datagrid('reload');
                   $('#layout_west_tree').tree('reload');
                  }
                else {
                  $.messager.alert('提示',result.msg);
                }
             }
       });		
};

$scope.batchDeleteFun=function() {
	var rows = dg.datagrid('getChecked');
	var ids = [];
	if (rows.length > 0) {
		parent.$.messager.confirm('确认', '您是否要删除当前选中的项目？', function(r) {
			if (r) {
				parent.$.messager.progress({
					title : '提示',
					text : '数据处理中，请稍后....'
				});
				var currentUserId = '${session.user.id}';/*当前登录用户的ID*/
				var flag = false;
				for ( var i = 0; i < rows.length; i++) {
					if (currentUserId != rows[i].id) {
						ids.push(rows[i].id);
					} else {
						flag = true;
					}
				}h
				$.getJSON(PATH+'/stock/stockquery/batcDelete', {
					ids : ids.join(',')
				}, function(result) {
					if (result.code==200) {
						dg.datagrid('load');
						dg.datagrid('uncheckAll').dataGrid('unselectAll').datagrid('clearSelections');
					}
					if (flag) {
						parent.$.messager.show({
							title : '提示',
							msg : '不可以删除自己！'
						});
					} else {
						parent.$.messager.alert('提示', result.msg, 'info');
					}
					parent.$.messager.progress('close');
				});
			}
		});
	} else {
		parent.$.messager.show({
			title : '提示',
			msg : '请勾选要删除的记录！'
		});
	}
};

} ]);

