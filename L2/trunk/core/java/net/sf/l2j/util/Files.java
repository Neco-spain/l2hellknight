package net.sf.l2j.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.logging.Logger;

import net.sf.l2j.Config;

public class Files
{
	@SuppressWarnings("unused")
	private static Logger _log = Logger.getLogger(Strings.class.getName());

	private static HashMap<String, String> cache = new HashMap<String, String>();

	public static String read(String name)
	{
		if(name == null)
			return null;

		if(Config.USE_FILE_CACHE && cache.containsKey(name))
			return cache.get(name);

		File file = new File("./" + name);

		//		_log.info("Get file "+file.getPath());

		if(!file.exists())
			return null;

		String content = null;

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String s = "";
			while((s = br.readLine()) != null)
				sb.append(s).append("\n");
			content = sb.toString();
			sb = null;
		}
		catch(Exception e)
		{ /* problem are ignored */}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(Exception e1)
			{ /* problems ignored */}
		}
		
		if(Config.USE_FILE_CACHE)
			cache.put(name, content);

		return content;
	}

	public static String read(File file)
	{
		if(!file.exists())
			return null;

		String content = null;

		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String s = "";
			while((s = br.readLine()) != null)
				sb.append(s).append("\n");
			content = sb.toString();
			sb = null;
		}
		catch(Exception e)
		{ /* problem are ignored */}
		finally
		{
			try
			{
				if(br != null)
					br.close();
			}
			catch(Exception e1)
			{ /* problems ignored */}
		}
		
		return FilesCrypt.getInstance().decrypt(content);
	}

	public static void cacheClean()
	{
		cache = new HashMap<String, String>();
	}

	public static long lastModified(String name)
	{
		if(name == null)
			return 0;

		return new File(name).lastModified();
	}

	



	
}
