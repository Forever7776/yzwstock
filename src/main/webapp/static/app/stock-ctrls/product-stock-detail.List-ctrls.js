var soninit = false;
var productid = $("#productid").val();
var  stockDetaildg = $('#stockDetaildg').datagrid({
    url:PATH+'/stock/stockquery/getstockList?productid='+productid,
    fit:true,
    fitColumns : true,
	idField : 'id',
	sortName : 'stock_id',
	sortOrder : 'asc',
	striped: true, 
	border : false,
	nowrap:false,
	rownumbers:true,
	singleSelect:true,
    pagination : true,
    checkOnSelect : false,
	selectOnCheck : false,
	pageSize : 10,
    pageList : [10,5, 20, 30, 40, 50],
     columns:[[
       {
           field : 'stockName',
           title : '仓库',
           width : 100
       },
         {
             field : 'stocksum',
             title : '入库总数',
             width : 100
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
         {field : 'action',title:'操作',width:100,formatter:formatterFun}
      ]],
    toolbar : '#stockDetailtoolbar',
    onLoadSuccess : function() {
        $('#detailSearchForm table').show();
		$('#sonsearchForm table').show();
		parent.$.messager.progress('close');
		$(this).datagrid('tooltip');

	},
	onRowContextMenu : function(e, rowIndex, rowData) {
		e.preventDefault();
		$(this).datagrid('unselectAll').datagrid('uncheckAll');
		$(this).datagrid('selectRow', rowIndex);
	}
});

var sonsearchFun=function() {
    stockDetaildg.datagrid('load', $.serializeObject($('#sonsearchForm')));
};
var soncleanFun=function() {
    $('#sonsearchForm input').val('');
    stockDetaildg.datagrid('load', {});
};

var getStockDetailFun = function(stockid){
    $('#ProductList-dlg').dialog('open');
    console.log(stockid);
    console.log(productid);
    $("#outProductCode-dg").datagrid({
        url:PATH+'/stock/stockquery/stockDetailListQuery?stockid='+stockid+'&productid='+productid,
        fitColumns : true,
        idField : 'stockid',
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
        columns:[[
            {
                field : 'code',
                title : '货物编号',
                width : 100,
            },
            {
                field : 'incode',
                title : '入库批次',
                width : 100,
            },
        ]],
    });

}
