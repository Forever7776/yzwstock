var dg = $('#dg').datagrid({
    url:PATH+'/stock/finance/queryDailyStockFinance',
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
        field : 'stock_id',
        title : '编号',
        width : 50,
        hidden:true
    } ] ],
    columns:[[
        {field : 'stock_name',title : '仓库名称',width : 100},
        {field : 'in_num',title : '入库单总数',width : 100},
        {field : 'out_num',title : '出库单总数',width : 100},
        {field : 'in_amount',title : '入库货品总金额',width : 100},
        {field : 'out_amount',title : '出库货品总金额',width : 100},
        {field : 'date',title : '日期',width : 100}
    ]],
    toolbar : '#toolbar',
    onLoadSuccess : function() {
        $('#searchForm table').show();
        parent.$.messager.progress('close');
        $(this).datagrid('tooltip');
    },
    onRowContextMenu : function(e, rowIndex, rowData) {
        e.preventDefault();
        $(this).datagrid('unselectAll').datagrid('uncheckAll');
        $(this).datagrid('selectRow', rowIndex);
    }
});

var queryTotalStock=function() {
    $('#fm').form('clear');
    showDialog('#dlg','汇总结果');
    url=PATH+'/stock/finance/queryTotalStock';
};

var searchFun = function() {
    dg.datagrid('load', $.serializeObject($('#searchForm')));
};
var cleanFun = function(){
    $('#searchForm input').val('');
    dg.datagrid('load', {});
}

var printFinanceFun=function() {
    $("#ts_message").hide();
    $('#print-Financefm4').form('clear');
    showDialog('#print-Finance-dlg','打印仓库财务统计');
};


var printFinanceExcel = function(obj) {
    var start=$('#printDateStart').datebox('getValue');
    var end=$('#printDateEnd').datebox('getValue');
    var url = PATH + '/stock/finance/printStockFinanceChoose?start='+start+'&end='+end;
    if (start == "" || end == "") {
        $("#ts_message").show();
    }else{
        window.location.href=url;
        $('#print-instorage-dlg').dialog('close')
    }
}



