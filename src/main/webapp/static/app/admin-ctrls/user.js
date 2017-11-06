/**
 *
 */
 function edit(userID){
    console.log("进入");
    var data = {userID: userID};
    console.log(userID);
    $("#userForm").show();
    $.ajax({
        url: PATH+"/admin/user/edit",
        method: "post",
        data:{userID:userID},
        success: function (resultData) {
            var html='';
            console.log(resultData);
            if(resultData){
                html+='<html lang="en">' +
                    '<fieldset><legend>修改您的资料</legend><form action="/admin/user/update">'+
                    '<input type="hidden" id="userID" name="userID" width="15%" value="'+resultData.id+'"></br>'+
                    '电话:<input type="text" id="tel" name="tel" width="15%" placeholder="'+resultData.tel+'"></br>'+
                    '传真:<input type="text" id="fax" name="fax" width="15%" placeholder="'+resultData.fax+'"></br>' +
                    '地址:<input type="text" id="address" name="address" width="15%" placeholder="'+resultData.address+'"></br>'+
                        '<input type="submit" class="button grey"  value="确定">'
                    ;
                html += '</form></fieldset></html>';
                /*layer.open({
                    type: 1,
                    title: false,
                    closeBtn: false,
                    area: '516px',
                    skin: 'layui-layer-nobg', //没有背景色
                    shadeClose: true,
                    content: html
                });*/
                layer.open({
                    type: 1,
                    area: ['400px', '300px'],
                    fix: false, //不固定
                    maxmin: true,
                    content: html
                });
            }
        },
        error: function () {
            alert("系统异常");
        }
    });
}

function add(userID){
    var data={userID:userID};
    console.log("进入");
    $.ajax({
        url: PATH+"/admin/user/edit",
        method: "post",
        data:data,
        success: function (resultData) {
            var html='';
            console.log(resultData);
            if(resultData){
                html+='<fieldset><legend>新增用户</legend><form action="/admin/user/add" ">'+
                    '姓名:<input type="text" id="nickname" name="nickname" width="15%" ></br>'+
                    '密码:<input type="text" id="pwd" name="pwd" width="15%" ></br>'+
                    '电话:<input type="text" id="tel" name="tel" width="15%" ></br>'+
                    '传真:<input type="text" id="fax" name="fax" width="15%" ></br>' +
                    '地址:<input type="text" id="address" name="address" width="15%" ></br>'+
                    '<input type="submit" style="text-align: right" value="确定">'
                ;
                html += '</form></fieldset>';
                /*layer.open({
                    type: 1,
                    title: false,
                    closeBtn: false,
                    area: '516px',
                    skin: 'layui-layer-nobg', //没有背景色
                    shadeClose: true,
                    content: html
                });*/
                layer.open({
                    type: 1,
                    area: ['400px', '300px'],
                    fix: false, //不固定
                    maxmin: true,
                    content: html
                });
            }
        },
        error: function () {
            alert("系统异常");
        }
    });


}