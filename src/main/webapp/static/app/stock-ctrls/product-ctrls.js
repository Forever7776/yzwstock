window.name ="NG_DEFER_BOOTSTRAP!";
init =false;


var  dg = $('#dg').datagrid({
    url:PATH+'/stock/product/list',
    fit:true,
    fitColumns : true,
	idField : 'id',
	sortName : 'yzwstock_product.wx_goods_code,yzwstock_product.insert_date',
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
          {
             field : 'name',
             title : '产品名称',
             width : 150,
             sortable : true
         },
        {
           field : 'category_name',
           title : '所属分类',
           width : 60
       },
       {
           field : 'code',
           title : '货物代码',
           width : 80
       },
        {field:'spec',title:'规格',width:80},
        {field:'price',title:'价格',width:50,hidden:true},
        {field:'unit',title:'单位',width:50,hidden:true},
        {field:'status',title:'状态',width:50,formatter:function(value,row){
        	if(value=='1'){
				if(row.specCode != null && row.wx_goods_code != null){
					return '正常(与商城同步)';
				}else{
					return '正常';
				}
			}
        	if(value=='2'){
				if(row.specCode != null && row.wx_goods_code != null){
					return '冻结(与商城同步)';
				}else{
					return '冻结';
				}
			}
        }},
        {field:'last_date',title:'修改时间',width:80},
        {field:'insert_date',title:'创建日期',width:80},
        {field:'action',title:'操作',width:100,
         formatter:formatterFun
	    
        }
      ]],
    toolbar : '#toolbar',
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
		$(this).datagrid('selectRow', rowIndex);
		$('#menu').menu('show', {
			left : e.pageX,
			top : e.pageY
		});
	}
});



MainApp.controller('ProductCtrls', [ '$scope', function($scope) {


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
				$.post(PATH+'/stock/product/delete', {
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
		url=PATH+'/stock/product/edit';
	}
};

$scope.addFun=function() {
	  $('#fm').form('clear');
	  showDialog('#dlg','添加货物');
	  url=PATH+'/stock/product/add';
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
				}
				$.getJSON(PATH+'/stock/product/batchDelete', {
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

