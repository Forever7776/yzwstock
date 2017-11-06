window.name ="NG_DEFER_BOOTSTRAP!";
init =false;

initStock("input[name='child.stock_id']");

var  dg = $('#dg').datagrid({
    url:PATH+'/stock/outstorage/list',
    fit:true,
    fitColumns : true,
    idField : 'id',
    sortName : 't1.insert_date',
    sortOrder : 'desc',
    striped: true,
    border : false,
    nowrap:false,
    rownumbers:true,
    singleSelect:true,
    checkOnSelect:true,
    selectOnCheck:true,
    pagination : true,
    pageSize : 10,
    pageList : [10,15, 20, 30, 40, 50],
    frozenColumns : [ [ {
        field : 'id',
        title : '编号',
        width : 150,
        checkbox : true
    }, {
        field : 'code',
        title : '出库单号',
        width : 100,
        sortable : true
    } ] ],
    columns:[[
        {field:'customer_name',title:'客户',width:50,hidden:true},
        {field:'out_type',title:'出库类型',width:80,formatter:function(value,row){
            switch(value*1){
                //1：普通 2：报损 3：调库 4：订单出库(扫码出库)，5:订单出库（随机出库）
                case 1:
                    return '普通（扫码出库）';
                case 2:
                    return "报损（扫码出库）";
                case 3:
                    return "调库（扫码出库）";
                case 4:
                    return "订单出库(扫码出库)";
                case 5:
                    return "订单出库(随机出库)";
                case 6:
                    return "普通（随机出库）";
                case 7:
                    return "报损（随机出库）";
                case 8:
                    return "调库（随机出库）"
                default :
                    return "未知异常";
            }
        }},
        {field:'input_name',title:'录入人',width:40},
        {field:'deliverer',title:'送货人',width:50,hidden:true},
        {field:'review',title:'审批结果',width:40,formatter:function(value,row){
            if(row.status>1){
                switch(value*1){
                    case 0:
                        return '不通过';
                    case 1:
                        return "审批通过";
                    case 2:
                        return "系统自动";
                    default :
                        return "";
                }
            }else{
                return "";
            }
        }},
        {field:'review_text',title:'审批详情',width:60,formatter:function(value,row){
            if(row.status>1)
                if(row.out_type==5){
                    return "系统自动审批";
                }else{
                    return "审批人[" + row.review_user + "]<br>审批意见["+row.review_text+"]";
                }
            else
                return "";
        }},
        {field:'check',title:'核对结果',width:40,formatter:function(value,row){
            switch(value*1){
                case 0:
                    return '待核对';
                case 1:
                    return '正常';
                case 2:
                    return "系统自动";
                default:
                    return "待验收";
            }
        }},
        {field:'check_user',title:"核对详情",width:60,formatter:function(value,row){
            if(row.status>2)
                if(row.out_type==5){
                    return "系统自动随机出库";
                }else{
                    return $.formatString("核对人[{0}]<br>验收备注[{1}]",value,row.check_text);
                }

            else
                return "";
        }},
        {field:'status',title:'状态',width:80,formatter:function(value,row){
            switch(value*1){
                case -1:
                    return "已删除";
                case 0:
                    return "订单生成中（等待支付）";
                case 1:
                    return "待审批";
                case 2:
                    return "待核验";
                case 3:
                    return "审批通过,准备出库";
                case 6:
                    return "出库完成";
            }
        }},
        {field:'last_date',title:'修改时间',width:100},
        {field:'insert_date',title:'提交时间',width:100},
        {field:'action',title:'操作',width:80,
            formatter:formatterFun
        }
    ]],
    toolbar : '#toolbar',
    onLoadSuccess : function(data) {
        $('#searchForm table').show();
        parent.$.messager.progress('close');
        $(this).datagrid('tooltip');

        for(var i=0;i<data.rows.length;i++){
            $('#btn-edit'+data.rows[i].id).menubutton({
                iconCls: 'icon-edit',
                menu: '#mm'+data.rows[i].id
            });
        }
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
    },
    onDblClickRow:function(index,row){
        if(li_south == 0){
            $('#main').layout('expand','south');
        }
        $('#outstorageDetail').panel("refresh", "/stock/outstorage/detail?outId="+row.id);
    },
    ProductCheck:function(obj){
        var $that = obj.val();
    }
});



MainApp.controller('OutStorageCtrls', [ '$scope', function($scope) {
    $scope.searchFun=function() {
        dg.datagrid('load', $.serializeObject($('#searchForm')));
    };
    $scope.cleanFun=function() {
        $('#searchForm input').val('');
        dg.datagrid('load', {});
    };

    $scope.printOutstorageFun=function() {
        $("#ts_message").hide();
        $('#print-Outstoragefm4').form('clear');
        showDialog('#print-Outstorage-dlg','打印出库单');
    };

    $scope.deleteFun=function(id) {
        if (id == undefined) {//点击右键菜单才会触发这个
            var rows = dg.datagrid('getSelections');
            id = rows[0].id;
            if(rows[0].out_type == 2){
                return false;
            }
        } else {//点击操作里面的删除图标会触发这个
            dg.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.messager.confirm('询问', '您是否要删除当前出库单？', function(b) {
            if (b) {
                var currentUserId = '${session.user.id}';/*当前登录用户的ID*/
                if (alertSelf(currentUserId,id)) {
                    parent.$.messager.progress({
                        title : '提示',
                        text : '数据处理中，请稍后....'
                    });
                    $.post(PATH+'/stock/outstorage/delete', {
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
            showDialog('#dlg','编辑出库单');
            url=PATH+'/stock/outstorage/edit';
        }
    };

    $scope.addFun=function() {
        $('#fm').form('clear');
        showDialog('#dlg','新增出库单');
        url=PATH+'/stock/outstorage/add';
    };

    $scope.submit=function(fm,dlg){
        $(fm).form('submit',{
            url: url,
            onSubmit: function(){
                parent.$.messager.progress({
                    title : '提示',
                    text : '入库数据处理中，请稍后....'
                });
                outbtonclick_true();
            },
            success: function(result){
                parent.$.messager.progress('close');
                outbtonclick_false();
                result= $.parseJSON(result);
                //处理位置浏览器问题，返回result为空的问题。
                if(result){
                    if(result.code==200){
                        $(dlg).dialog('close');
                        outbtonclick_false();
                        dg.datagrid('reload');
                        $('#layout_west_tree').tree('reload');
                    }else {
                        outbtonclick_false();
                        $.messager.alert('提示',result.msg);
                    }
                }else{
                    $(dlg).dialog('close');
                    outbtonclick_false();
                    dg.datagrid('reload');
                    $('#layout_west_tree').tree('reload');
                }
            }
        });
    };
    $scope.review = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            $('#fm2').form('clear');
            loadFrom('#fm2',node);
            showDialog('#dlg-review','审批');
            url=PATH+'/stock/outstorage/review';
        }
    }
    $scope.check = function(id){
        if (id != undefined)dg.datagrid('selectRecord', id);
        var node = dg.datagrid('getSelected');
        if (node) {
            $('#fm3').form('clear');
            loadFrom('#fm3',node);
            showDialog('#dlg-check','出库核对');
            url=PATH+'/stock/outstorage/check';
        }
    }
    $scope.print = function(id){
        var url=PATH+'/stock/outstorage/print?outid='+id;
        window.location.href=url;
    }
    $scope.qrCodeDialog = function(id,title,code){
        $("#qrCode").empty();
        $("#qrCode").qrcode(code);
        $(id).show().dialog('open').dialog('setTitle',title);
    }
    $scope.outstorageAddressDialog = function(id,title,outid){
        var url=PATH+'/stock/outstorage/getOutAddress';
        $.ajax({
            url:url,
            data:{outid:outid},
            success:function(json){
                $(id).show().dialog('open').dialog('setTitle',title);
                if(json.address){
                    var html ='<tr>';
                    html +='<th width="30%" style="text-align: right;">收件人:</th>';
                    html +='<td>'+json.address.name+'</td>';
                    html +='</tr>';
                    html +='<th style="text-align: right;">手机号:</th>';
                    html +='<td>'+json.address.telephone+'</td>';
                    html +='</tr>';
                    html +='<tr>';
                    html +='<th  style="text-align: right;">地址:</th>';
                    html +='<td>'+json.address.loc_province+" "+json.address.loc_city+" "+json.address.loc_town+" "+json.address.detail_address+'</td>';
                    html +='</tr>';
                    $("#outstorageAddress").html(html);
                }else if(json.customer){
                    $(id).show().dialog('open').dialog('setTitle',title);
                    var html ='<tr>';
                    html +='<th width="30%" style="text-align: right;">客户名称:</th>';
                    html +='<td>'+json.customer.name+'</td>';
                    html +='</tr>';
                    html +='<th style="text-align: right;">联系人:</th>';
                    html +='<td>'+json.customer.contact+'</td>';
                    html +='</tr>';
                    html +='<tr>';
                    html +='<th  style="text-align: right;">地址:</th>';
                    html +='<td>'+json.customer.address+'</td>';
                    html +='</tr>';
                    $("#outstorageAddress").html(html);
                }else {
                    $("#outstorageAddress").html("<tr><td>无数据</td></tr>");
                }


            },
            error:function(){
                $.messager.alert('提示',"系统错误！");
            }
        });

    }

} ]);

var printOutstorageExcel = function(obj) {
    var start=$('#printDateStart').datebox('getValue');
    var end=$('#printDateEnd').datebox('getValue');
    var url = PATH + '/stock/outstorage/printOutStorageChoose?start='+start+'&end='+end;
    if (start == "" || end == "") {
        $("#ts_message").show();
    }else{
        window.location.href=url;
        $('#print-Outstorage-dlg').dialog('close')
    }
}

