window.name ="NG_DEFER_BOOTSTRAP!";
init =false;


var  dg = $('#dg').datagrid({
    url:PATH+'/stock/supplier/list',
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
		width : 150,
		checkbox : true
	}, {
		field : 'code',
		title : '供应商编号',
		width : 80,
		sortable : true
	} ] ],
     columns:[[
        {
           field : 'name',
           title : '供应商名称',
           width : 60
       },
       {
           field : 'contact',
           title : '联系人',
           width : 150
       },
         {
             field : 'phone',
             title : '联系电话',
             width : 150
         },
        {
            field:'fax',
            title:'传真',
            width:200
        },
        {field:'insert_date',title:'添加时间',width:150},
        {field:'last_date',title:'更新时间',width:50,hidden:true},
        {field:'status',title:'状态',width:50,formatter:function(value,row){
        	if(value=='1') return '正常';
        	if(value=='2') return '冻结';
        }},
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




MainApp.controller('SupplierCtrls', [ '$scope', function($scope) {


$scope.freeze=function(id,status){
 
	
	
	
	dg.datagrid('unselectAll').datagrid('uncheckAll');
 
	parent.$.messager.confirm('询问', '是否冻结或解冻供应商？', function(b) {
 		if(b){
 			var currentUserId = '${session.user.id}';/*当前登录用户的ID*/
			if (alertSelf(currentUserId,id)) {
				parent.$.messager.progress({
					title : '提示',
					text : '数据处理中，请稍后....'
				});
				$.post(PATH+'/stock/supplier/freeze', {
					id : id,
					status:status
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
	parent.$.messager.confirm('询问', '您是否要删除当前供应商？', function(b) {
		if (b) {
			var currentUserId = '${session.user.id}';/*当前登录用户的ID*/
			if (alertSelf(currentUserId,id)) {
				parent.$.messager.progress({
					title : '提示',
					text : '数据处理中，请稍后....'
				});
				$.post(PATH+'/stock/supplier/delete', {
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
		
		$('#username').attr('readonly','readonly');
		
		$('#icon').attr('src',node.icon);
		if(node.des)$('#des').text(node.des);
		$('#pwd').val('');
		showDialog('#dlg','编辑供应商');
		url=PATH+'/stock/supplier/edit';
	}
};

$scope.addFun=function() {
	  $('#username').attr('readonly',false);
		$('#icon').attr('src','');
	  $('#fm').form('clear');
	  showDialog('#dlg','添加供应商');
	  url=PATH+'/stock/supplier/add';
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
				$.getJSON(PATH+'/stock/supplier/batchDelete', {
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

