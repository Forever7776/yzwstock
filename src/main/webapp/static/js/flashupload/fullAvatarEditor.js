function fullAvatarEditor() {
	var id				= 'fullAvatarEditor'			//flash�ļ���ID
	var file			= 'fullAvatarEditor.swf';		//flash�ļ���·��
	var	version			= "10.1.0";						//���Ÿ�flash�������Ͱ汾
	var	expressInstall	= 'expressInstall.swf';			//expressInstall.swf��·��
	var	width			= 630;							//flash�ļ��Ŀ��
	var	height			= 430;							//flash�ļ��ĸ߶�
	var container		= id;							//װ��flash�ļ�������(��div)��id
	var flashvars		= {};
	var callback		= function(){};
	var heightChanged	= false;
	//���ܻ�ȡ�������ַ�����Ϊװ��flash�ļ�������(��div)��id����һ���������͵�Ϊ�߶ȣ��ڶ���Ϊ��ȣ���һ��object���͵�Ϊ�����������4��������˳������⡣
	for(var i = 0; i < arguments.length; i++)
	{
		var a = arguments[i];
		if(typeof a == 'string')
		{
			if (a.substring(a.length - 'fullAvatarEditor.swf'.length) == 'fullAvatarEditor.swf')
			{
				file = a;
			}
			else if (a.substring(a.length - 'expressInstall.swf'.length) == 'expressInstall.swf')
			{
				expressInstall = a;
			}
			else
			{
				container = a;
			}
		}
		else if(typeof a == 'number')
		{
			if(heightChanged)
			{
				width = a;
			}
			else
			{
				height = a;
				heightChanged = true;
			}
		}
		else if(typeof a == 'function')
		{
			callback = a;
		}
		else
		{
			flashvars = a;
		}
	}
	var vars = {
		id : id
	};
	//�ϲ�����
	for (var name in flashvars)
	{
		if(flashvars[name] != null)
		{
			if(name == 'upload_url' || name == 'src_url')
			{
				vars[name] = encodeURIComponent(flashvars[name]);
			}
			else
			{
				vars[name] = flashvars[name];
			}
		}
	}
	var params = {
		menu				: 'true',
		scale				: 'noScale',
		allowFullscreen		: 'true',
		allowScriptAccess	: 'always',
		wmode				: 'transparent'
	};
	var attributes = {
		id	: vars.id,
		name: vars.id
	};
	var swf = null;
	var	callbackFn = function (e) {
		swf = e.ref;
		swf.eventHandler = function(json){
			callback.call(swf, json);
		};
	};
	swfobject.embedSWF(
		file,
		container,
		width,
		height,
		version,
		expressInstall,
		vars,
		params,
		attributes,
		callbackFn
	);
	return swf;
}