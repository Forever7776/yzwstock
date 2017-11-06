/***
 * 判断浏览器类型和版本，温馨提示用户
 * @returns {{ie: boolean, opera: (boolean|*), webkit: boolean, mac: boolean, quirks: boolean}}
 */
var client= function (){

	var agent = navigator.userAgent.toLowerCase(),
		opera = window.opera,
		browser = {
			//检测当前浏览器是否为IE
			ie: /(msie\s|trident.*rv:)([\w.]+)/.test(agent),
			//检测当前浏览器是否为Opera
			opera: (!!opera && opera.version),
			//检测当前浏览器是否是webkit内核的浏览器
			webkit: (agent.indexOf(' applewebkit/') > -1),
			//检测当前浏览器是否是运行在mac平台下
			mac: (agent.indexOf('macintosh') > -1),
			//检测当前浏览器是否处于“怪异模式”下
			quirks: (document.compatMode == 'BackCompat')
		};
	//检测当前浏览器内核是否是gecko内核
	browser.gecko = (navigator.product == 'Gecko' && !browser.webkit && !browser.opera && !browser.ie);
	var version = 0;
	// Internet Explorer 6.0+
	if (browser.ie) {
		var v1 = agent.match(/(?:msie\s([\w.]+))/);
		var v2 = agent.match(/(?:trident.*rv:([\w.]+))/);
		if (v1 && v2 && v1[1] && v2[1]) {
			version = Math.max(v1[1] * 1, v2[1] * 1);
		} else if (v1 && v1[1]) {
			version = v1[1] * 1;
		} else if (v2 && v2[1]) {
			version = v2[1] * 1;
		} else {
			version = 0;
		}
		//检测浏览器模式是否为 IE11 兼容模式
		if(browser.ie11Compat = document.documentMode == 11){
			layer.msg('您的浏览器是IE，版本为11.0', {
				offset: 0,
				shift: 6
			});
		}
		//检测浏览器模式是否为 IE9 兼容模式
		if(browser.ie9Compat = document.documentMode == 9){

					layer.msg('您的浏览器是IE，版本为9.0,请升级浏览器或者换Chrome，360', {
						offset: 0,
						shift: 6,
						time:1000*60*60
					})

		}
		//检测浏览器模式是否为 IE10 兼容模式
		if(browser.ie10Compat = document.documentMode == 10){
			layer.msg('您的浏览器是IE，版本为10.0', {
				offset: 0,
				shift: 6
			});
		}
		//检测浏览器是否是IE8浏览器
		if(browser.ie8 = !!document.documentMode) {
			//检测浏览器模式是否为 IE8 兼容模式
			if (browser.ie8Compat = document.documentMode == 8) {
				layer.msg('您的浏览器是IE，版本为8.0,请升级浏览器或者换Chrome，360', {
					offset: 0,
					shift: 6,
					time:1000*60*60
				});
			} else if (browser.ie7Compat = ((version == 7 && !document.documentMode) || document.documentMode == 7)) {
				//检测浏览器模式是否为 IE7 兼容模式
				layer.msg('您的浏览器是IE，版本为7.0,请升级浏览器或者换Chrome，360', {
					offset: 0,
					shift: 6,
					time:1000*60*60
				});
			}

		}
		//检测浏览器模式是否为 IE6 模式 或者怪异模式
		if(agent.indexOf("msie 6.0")&&(browser.ie6Compat = (version < 7 || browser.quirks))){
			layer.msg('您的浏览器是IE，版本为6.0,请升级浏览器或者换Chrome，360', {
				offset: 0,
				shift: 6,
				time:1000*60*60
			});
		}
		browser.ie9above = version > 8;
		browser.ie9below = version < 9;
	}
	// Gecko.
	if (browser.gecko) {
		var geckoRelease = agent.match(/rv:([\d\.]+)/);
		if (geckoRelease) {
			geckoRelease = geckoRelease[1].split('.');
			version = geckoRelease[0] * 10000 + (geckoRelease[1] || 0) * 100 + (geckoRelease[2] || 0) * 1;
		}
	}
	//检测当前浏览器是否为firebox, 如果是，则返回Chrome的大版本号
	if (agent.indexOf("firefox")>=0) {
		layer.msg('您的浏览器是firebox,版本为'+agent.substring(agent.length-4,agent.length), {
			offset: 0,
			shift: 6
		});
	}
	//检测当前浏览器是否为Chrome, 如果是，则返回Chrome的大版本号
	if (/chrome\/(\d+\.\d)/i.test(agent)) {
		browser.chrome = +RegExp['\x241'];
		layer.msg('您的浏览器是Chrome', {
			offset: 0,
			shift: 6
		});
	}
	//检测当前浏览器是否为Safari, 如果是，则返回Safari的大版本号
	if (/(\d+\.\d)?(?:\.\d)?\s+safari\/?(\d+\.\d+)?/i.test(agent) && !/chrome/i.test(agent)) {
		browser.safari = +(RegExp['\x241'] || RegExp['\x242']);
		layer.msg('您的浏览器是Safari', {
			offset: 0,
			shift: 6
		});
	}
	// Opera 9.50+
	if (browser.opera)
		version = parseFloat(opera.version());
	// WebKit 522+ (Safari 3+)
	if (browser.webkit)
		version = parseFloat(agent.match(/ applewebkit\/(\d+)/)[1]);
	//检测当前浏览器版本号
	browser.version = version;
	return browser;
}

/***
 * 把form 表单  转化为 url 参数
 * @param param
 * @param key
 * @return
 */
var parseParam = function(param, key) {
	var paramStr = "";
	if (param instanceof String || param instanceof Number || param instanceof Boolean) {
		paramStr += "&" + key + "=" + encodeURIComponent(param);
	} else {
		$.each(param, function(i) {
			var k = key == null ? i : key + (param instanceof Array ? "[" + i + "]" : "." + i);
			paramStr += '&' + parseParam(this, k);
		});
	}
	return paramStr.substr(1);
};

/***
 * 判断文件后缀名
 * @param value
 * @param param
 * @return
 */

function isEnd(value, param) {

	if (value.indexOf(param.toLocaleUpperCase()) == -1 && value.indexOf(param.toLocaleLowerCase()) == -1) {
		return false;
	}
	return true;
}

/***
 * 验证是否为空  传递 id 即可
 * @param param
 * user isEmpty(['name','pwd'])
 * @return
 */

function isEmpty(param) {

	for (var i in param) {

		var val = $('#' + param[i]).val();

		if (val == '' || val == undefined) {
			return true;
		}

	}
	return false;
}


function trim(str) { //删除左右两端的空格
	return str.replace(/(^\s*)|(\s*$)/g, "");
}

/**
 * 验证 url
 * @param str_url
 * @return
 */

function isURL(str) {
	str = str.match(/http:\/\/.+/);
	if (str == null) {
		return false;
	} else {
		return true;
	}
}


/**
 * 获得 url 里面的参数
 * @param paras
 * @return
 */

function request(paras) {
	var url = location.href;
	var paraString = url.substring(url.indexOf("?") + 1, url.length).split("&");
	var paraObj = {}
	for (i = 0; j = paraString[i]; i++) {
		paraObj[j.substring(0, j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=") + 1, j.length);
	}
	var returnValue = paraObj[paras.toLowerCase()];
	if (typeof(returnValue) == "undefined") {
		return "";
	} else {
		return returnValue;
	}
}



/**
 * 打印log 到 console
 * @param msg
 * @return
 */

function log(msg) {
	console.log(msg);

}


/***
 * 判断 2个日期的相差的天数
 * @param sDate1
 * @param sDate2
 * @return
 */

function dateDiff(sDate1, sDate2) { // sDate1和sDate2是2004-10-18格式
	var aDate, oDate1, oDate2, iDays
	aDate = sDate1.split("-")
	oDate1 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0]) // 转换为10-18-2004格式
	aDate = sDate2.split("-")
	oDate2 = new Date(aDate[1] + '-' + aDate[2] + '-' + aDate[0])
	iDays = parseInt(Math.abs(oDate1 - oDate2) / 1000 / 60 / 60 / 24) // 把相差的毫秒数转换为天数
	return iDays
}


///////////////////////////JQUERY -SETTING  JQEURY EXTENDS//////////////////////////////////

/**
 * 设置 ajax  缓存为 false 并且设置默认 错误提示
 */

$.ajaxSetup({
	cache: false,
	error: function(XMLHttpRequest, status, errorThrown) {
		$.messager.progress('close');
		parent.$.messager.progress('close');

		if (status == 'parsererror' && XMLHttpRequest.responseText.indexOf('login') != -1) window.open('jump', '_top');
		else if (typeof(errorThrown) != "undefined") $.messager.alert('error', "调用服务器失败<br />" + errorThrown, 'error');
		else {
			var error = "<b style='color: #f00'>" + XMLHttpRequest.status + "  " + XMLHttpRequest.statusText + "</b>";
			var start = XMLHttpRequest.responseText.indexOf("<title>");
			var end = XMLHttpRequest.responseText.indexOf("</title>");
			if (start > 0 && end > start) error += "<br /><br />" + XMLHttpRequest.responseText.substring(start + 7, end);
			$.messager.alert("error", "调用服务器失败<br />" + error, 'error');
		}
	}
});





/***
 * Jquery 扩展
 */
$.extend({
	getUrlVars: function() {
		var vars = [],
			hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for (var i = 0; i < hashes.length; i++) {
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	},
	getUrlVar: function(name) {
		return $.getUrlVars()[name];
	}

});




/**
 * map
 * @returns
 */

function Map() {
	this.container = new Object();
}


Map.prototype.put = function(key, value) {
	this.container[key] = value;
}


Map.prototype.get = function(key) {
	return this.container[key];
}


Map.prototype.keySet = function() {
	var keyset = new Array();
	var count = 0;
	for (var key in this.container) {
		// 跳过object的extend函数
		if (key == 'extend') {
			continue;
		}
		keyset[count] = key;
		count++;
	}
	return keyset;
}


Map.prototype.size = function() {
	var count = 0;
	for (var key in this.container) {
		// 跳过object的extend函数
		if (key == 'extend') {
			continue;
		}
		count++;
	}
	return count;
}


Map.prototype.remove = function(key) {
	delete this.container[key];
}


Map.prototype.toString = function() {
	var str = "";
	for (var i = 0, keys = this.keySet(), len = keys.length; i < len; i++) {
		str = str + keys[i] + "=" + this.container[keys[i]] + ";\n";
	}
	return str;
}

function uploadInit(selector,callback){
	var cafun = function (msg) {
		switch(msg.code)
		{
			case 1 : break;
			case 2 : break;
			case 3 :
				if(msg.type == 0)
				{
					alert("摄像头已准备就绪且用户已允许使用。");
				}
				else if(msg.type == 1)
				{
					alert("摄像头已准备就绪但用户未允许使用！");
				}
				else
				{
					alert("摄像头被占用！");
				}
				break;
			case 5 :
				callback(msg);
				break;
		}
	}
	var option = {
		id : "swf",
		upload_url : "/common/file/flashUploadNew",
		method : "post",
		src_upload : 2
	};
	new fullAvatarEditor('/static/js/flashupload/fullAvatarEditor.swf', '/static/js/flashupload/expressInstall.swf', selector,option, cafun);
}


jQuery.ajaxpost = function(url,data,callback){
    return $.ajax({
        'url':url,
        'type':'post',
        'data':data,
        'dataType':"json",
        'timeout':6000,
        'success':function(resp){
            if(resp.code==200){
                callback(resp);
            }else{
                $.messager.alert("提示",resp.msg);
                callback(resp);
            }
        },'error':function(){
            $.messager.alert('系统处理异常或超时！');
        }
    })
};

Number.prototype.rate=function(){
	var oStr=this.toString();
	if(oStr.indexOf(".")==-1)
		return 1;
	else
		return Math.pow(10,parseInt(oStr.length-oStr.indexOf(".")-1));
}
function tran(){
	args=tran.arguments;
	var temp=1;
	for(i=0;i<args.length;i++)
		temp*=args[ i ]*args[ i ].rate();
	for(i=0;i<args.length;i++)
		temp/=args[ i ].rate();
	return temp
}