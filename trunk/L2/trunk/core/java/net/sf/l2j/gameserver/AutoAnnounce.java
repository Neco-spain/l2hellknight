package net.sf.l2j.gameserver;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import net.sf.l2j.Config;

public class AutoAnnounce
{
	   
          private static List<String> _autoannouncements = new FastList<String>();
          private static Logger _log = Logger.getLogger(AutoAnnounce.class.getName());

	


        public static void load()
        {
              loadAutoAnnouncements();
               ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoAnnouncements(), Config.AUTO_ANNOUNCE_DELAY*500, Config.AUTO_ANNOUNCE_DELAY*1000);
        }

        public static class AutoAnnouncements implements Runnable
        {

            public void run() {
              announce();
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }
    public static void loadAutoAnnouncements()
	{
		_autoannouncements.clear();
		File file = new File(Config.DATAPACK_ROOT, "data/autoannouncements.txt");
		if (file.exists())
		{
			readFromDisk(file);
		}
		else
		{
			_log.config("data/autoannouncements.txt doesn't exist");
		}
	 }
        public static void readFromDisk(File file) {

       // File file = new File(Config.DATAPACK_ROOT, "data/announcements.txt");
        LineNumberReader lnr = null;
		try
		{
			int i=0;
			String line = null;
			lnr = new LineNumberReader(new FileReader(file));
			while ( (line = lnr.readLine()) != null)
			{
				StringTokenizer st = new StringTokenizer(line,"\n\r");
				if (st.hasMoreTokens())
				{
					String announcement = st.nextToken();
					_autoannouncements.add(announcement);

					i++;
				}
			}

			_log.config("AutoAnnouncements: Loaded " + i + " Announcements.");
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Error reading Autoannouncements", e);
		}
		finally
		{
			try
			{
				lnr.close();
			}
			catch (Exception e2)
			{
				// nothing
			}
		}


  }
    public static void announce(){
       for (int i = 0; i < _autoannouncements.size(); i++)
		{
			 Announcements.getInstance().announceToAll(_autoannouncements.get(i));
		}
    }
  
 }

