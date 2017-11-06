//document.onkeydown = function(e){
//    if($(".bac").length==0)
//    {
//        if(!e) e = window.event;
//        if((e.keyCode || e.which) == 13){
//            var obtnLogin=document.getElementById("submit_btn")
//            obtnLogin.focus();
//        }
//    }
//}


var login = function(obj){
    var reg = /^[a-zA-Z0-9_]+$/;
    show_loading();
    if($('#username').val() == '请输入用户名'){
        show_err_msg('帐号还没填呢！');
        $('#username').focus();
    }else if($('#username').val().length<5){
        show_err_msg('用户名长度为5~30个字符之间！');
        $('#username').focus();
    }else if(!reg.test($('#username').val())){
        show_err_msg('用户名只能为大小写英文字母或数字！');
        $('#username').focus();
    }else if($('#password').val() == 'Password'){
        show_err_msg('密码还没填呢！');
        $('#password').focus();
    }else if($('#password').val().length<6){
        show_err_msg('密码长度为6~30个字符之间！');
        $('#password').focus();
    }else{
        $("#loginform").ajaxSubmit({
            type:"get",  //提交方式
            dataType:"json", //数据类型
            success:function(data){ //提交成功的回调函数
                if(data.result){
                    window.location.href="/admin/login";
                    show_msg('登录成功咯！  正在为您跳转...','/');
                }else{
                    show_err_msg(data.msg);
                }
            }
        });
    }
}


