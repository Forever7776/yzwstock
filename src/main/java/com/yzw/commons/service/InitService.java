package com.yzw.commons.service;

import java.io.File;

import com.jfinal.plugin.activerecord.Db;
import com.yzw.base.util.Fs;
import com.yzw.base.util.L;

public class InitService
{

	public void initDb(String path){
		
		try
		{
			String sql = Fs.readFile(new File(path));
			
			String[] sqls = sql.split(";");
			
			for(String s :sqls ){
				Db.update(s);
				L.i(s);
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}
	
	
	
}
