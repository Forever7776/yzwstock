var outId = $("#outId").val();
var  outStorageDetailDg = $('#outStorageDetailDg').datagrid({
    url:PATH+'/stock/outstorage/detailJson?outId='+outId,
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
        {field : 'product_name',title : '货物名称',width : 100},
        {field : 'product_num',title : '货物数量',width : 100},
        {field:'stock_name',title:'出库仓库',width:100},
        {field:'action',title:'操作',width:100,formatter:detailFormatterFun}
    ]],
    toolbar : '#outStorageDetailtoolbar',
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
    outStorageDetailDg.datagrid('load', $.serializeObject($('#detailSearchForm')));
};
var detailCleanFun = function(){
    $('#detailSearchForm input').val('');
    outStorageDetailDg.datagrid('load', {});
}

var outStockDetailFun = function(outDetailId){
    $('#outProductCode-dlg').dialog('open');

    $("#outProductCode-dg").datagrid({
        url:PATH+'/stock/outstorage/loadOutProductStockData?outDetailId='+outDetailId,
        fitColumns : true,
        idField : 'stockid',
        sortName : 'insert_date',
        sortOrder : 'asc',
        striped: true,
        border : true,
        nowrap:false,
        pagination : true,
        rownumbers:false,
        singleSelect:true,
        checkOnSelect:true,
        selectOnCheck:true,
        pageSize : 15,
        columns:[[
            {
                field : 'id',
                title : '编号',
                width : "50px",
                checkbox : true
            },
            {
                field : 'code',
                title : '货物编号',
                width : "100px"
            },
            {
                field : 'status',
                title : '状态',
                width : "200px",
                formatter:function(value,row){
                    switch(value*1){
                        case 0:
                            return "删除";
                        case 1:
                            return "未出库";
                        case 2:
                            return "【已扫码】-->>已经成功进行出库扫码，等待出库完成中……";
                        case 3:
                            return "已出库";
                    }
                }

            }
        ]],
        onCheck:function(index,row){
        },
        onUncheck:function(index,row){
        }
    });

}


