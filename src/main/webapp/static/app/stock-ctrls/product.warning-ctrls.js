
var inId = $("#inId").val();
var  inStorageDetailDg = $('#productWarning').datagrid({
    url:PATH+'/stock/manager/detailJson?inId='+inId,
    fit:true,
    fitColumns : true,
    idField : 'id',
    sortName : 'stockWorning.insert_date',
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
        {field : 'product_name',title : '货物名称',width : 100},
        {field : 'spec',title : '货物规格',width : 100},
        {field : 'stock_name',title : '仓库',width : 100},
        {field : 'warning_first',title : '一级预警',width : 100},
        {field : 'warning_secend',title:'二级预警',width:100},
        {field : 'worning_third',title:'三级预警',width:100},
        {field : 'action',title:'操作',width:100,formatter:detailFormatterFun}
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

var outStockDetailFun = function(id,stockid,productid) {
    if (id != undefined){
        $('#productid').val(productid);
        $('#stockid').val(inId);
        inStorageDetailDg.datagrid('selectRecord', id);
    }else{
        $('#yj_form').form('clear');
        $('#productid').val(productid);
        $('#stockid').val(inId);
        $('#warning-dlg').dialog('open');
        return false;
    }
    var node = inStorageDetailDg.datagrid('getSelected');
    if (node) {
        loadFrom('#yj_form',node);
        if(node.content)$('#content').text(node.content);
        $('#warning-dlg').dialog('open');
        console.log($("#productid").val());
        console.log($("#stockid").val());
    }
};
var submitWar = function(){
    var id1= $("input[name='waraing.id']").val();
    var id = $("#warid").val();
    if(id != undefined && id != "" && id != null){
        url=PATH+'/stock/manager/etidWar';
    }else{
        url=PATH+'/stock/manager/addWar';
    }
    $('#yj_form').form('submit',{
        url: url,
        onSubmit:function(){
            console.log(this);
        },
        success: function(result){
            result= $.parseJSON(result);
            if(result.code==200){
                $('#warning-dlg').dialog('close');
                inStorageDetailDg.datagrid('reload');
                $('#layout_west_tree').tree('reload');
            }else {
                $.messager.alert('提示',result.msg);
            }
        }
    });
}