package ccpGuard.commons.utils;


import l2rt.gameserver.model.L2Character;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;



public class Log
{
	private static final Logger _log = Logger.getLogger(Log.class.getName());

	public static void add(String text, String cat)
	{
		if(cat.equals("items") || cat.equals("chat") || cat.equals("CommunityBoard"))
			cat = cat + (new SimpleDateFormat("yyyy.MM.dd")).format(new Date());
		add(text, cat, "yy.MM.dd HH:mm:ss", null);
	}

	public static void add(String text, String cat, L2Character player)
	{
		add(text, cat, "yy.MM.dd HH:mm:ss", player);
	}

	public static void add(String text, String cat, String DateFormat)
	{
		add(text, cat, DateFormat, null);
	}

	public static synchronized void add(String text, String cat, String DateFormat, L2Character player)
	{
		new File("logs/game").mkdirs();
		File file = new File("logs/game/" + (cat != null ? cat : "_all") + ".txt");

		if(!file.exists())
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				_log.warning("saving " + (cat != null ? cat : "all") + " log failed, can't create file: " + e);
				return;
			}

		FileWriter save = null;
		StringBuffer msgb = new StringBuffer();

		try
		{
			save = new FileWriter(file, true);
			if(!DateFormat.equals(""))
			{
				String date = (new SimpleDateFormat(DateFormat)).format(new Date());
				msgb.append("[" + date + "]: ");
			}

			if(player != null)
				msgb.append(player.getName() + " ");

			msgb.append(text + "\n");
			save.write(msgb.toString());
		}
		catch(IOException e)
		{
			_log.warning("saving " + (cat != null ? cat : "all") + " log failed: " + e);
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(save != null)
					save.close();
			}
			catch(Exception e1)
			{}
		}
	}

}
