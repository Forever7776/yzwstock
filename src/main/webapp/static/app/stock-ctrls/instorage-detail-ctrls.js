var inId = $("#inId").val();
var inId1 = $("#inId1").val();
var in_code = $("#in_code").val();

var  inStorageDetailDg = $('#inStorageDetailDg').datagrid({
    url:PATH+'/stock/instorage/detailJson?inId='+inId,
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
        {field : 'nums',title : '货物数量',width : 100},
        {field : 'price',title : '入库单价',width : 100},
        {field:'stock_name',title:'仓库',width:100},
        {field:'action',title:'操作',width:100,
         formatter:function(value, row, index) {

         }
        }
    ]],
    toolbar : '#inStorageDetailtoolbar',
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
var detailSearchFun = function() {
    inStorageDetailDg.datagrid('load', $.serializeObject($('#detailSearchForm')));
};
var detailCleanFun = function(){
    $('#detailSearchForm input').val('');
    inStorageDetailDg.datagrid('load', {});
}


var  inStorageDetailDg1 = $('#inStorageDetailDg1').datagrid({
    url:PATH+'/stock/instorage/detailJson1?inId='+inId1+'&code='+in_code,
    fit:true,
    fitColumns : true,
    idField : 'id',
    sortName : 'insert_date',
    sortOrder : 'asc',
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
        {field : 'code',title : '货物代码',width : 100},
        {field : 'name',title : '货物名称',width : 100},
        {field : 'spec',title : '货物规格',width : 100},
        {field : 'stock_name',title:'仓库',width:100},
        {field : 'status',title:'状态',width:100,
            formatter:function(value) {
                switch(value*1){
                    case 1:
                        return "正常";
                    case 2:
                        return "已扫码";
                    case 3:
                        return "出库";
                    case 0:
                        return "删除";
                }
            }
        },
        {field:'action',title:'操作',width:100,
         formatter:function(value, row, index) {
            var code = row.code;
            var str = '';

            str += $.formatString('<img style="float:left;" onclick="qrCodeDialog(\'#qrCode_dlg\',\'二维码\',\'{0}\')" src="{1}" title="查看二维码"/>',code,PATH + '/static/js/ext/style/images/extjs_icons/search.png');
            return  str;
        }}
    ]],
    toolbar : '#inStorageDetailtoolbar1',
    onLoadSuccess : function() {
        $('#detailSearchForm1 table').show();
        parent.$.messager.progress('close');
        $(this).datagrid('tooltip');
    },
    onRowContextMenu : function(e, rowIndex, rowData) {
        e.preventDefault();
        $(this).datagrid('unselectAll').datagrid('uncheckAll');
        $(this).datagrid('selectRow', rowIndex);
    }
});
var detailSearchFun1 = function() {
    inStorageDetailDg1.datagrid('load', $.serializeObject($('#detailSearchForm1')));
};
var detailCleanFun1 = function(){
    $('#detailSearchForm1 input').val('');
    inStorageDetailDg1.datagrid('load', {});
}

var qrCodeDialog = function(id,title,code){
    $("#qrCode").empty();
    $("#qrCode").qrcode(code);
    $(id).show().dialog('open').dialog('setTitle',title);
}


