package l2rt.gameserver.tables;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.*;
import l2rt.Config;
import l2rt.gameserver.templates.L2Henna;
import l2rt.gameserver.templates.StatsSet;

import java.util.logging.Logger;

@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public class HennaTable
{
	private static final Logger _log = Logger.getLogger(HennaTable.class.getName());

	private static HennaTable _instance;

	private TIntObjectHashMap<L2Henna> _henna;
	
	private boolean _initialized = true;

	public static HennaTable getInstance()
	{
		if(_instance == null)
			_instance = new HennaTable();
		return _instance;
	}

	private HennaTable()
	{
		_henna = new TIntObjectHashMap<L2Henna>();
		RestoreHennaData();

	}

	private void RestoreHennaData()
	{
		int id = 0;
		LineNumberReader lnr = null;
		try
		{
			File rsData = new File(Config.DATAPACK_ROOT, "data/pts/dyedata.txt");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(rsData)));

			String line = null;
			while((line = lnr.readLine()) != null)
			{				
				if(line.trim().length() == 0 || line.startsWith("#"))
					continue;
					
				String args[] = line.split("\t",-1);
				
				StatsSet hennaDat = new StatsSet();
				
				id = getInt(args[2]);
				hennaDat.set("symbol_id", id);
				hennaDat.set("dye", getInt(args[3]));
				
				hennaDat.set("stat_STR", getInt(args[5]));
				hennaDat.set("stat_CON", getInt(args[6]));
				hennaDat.set("stat_DEX", getInt(args[7]));				
				hennaDat.set("stat_INT", getInt(args[8]));				
				hennaDat.set("stat_MEN", getInt(args[9]));				
				hennaDat.set("stat_WIT", getInt(args[10]));
				
				hennaDat.set("amount", getInt(args[11]));				
				hennaDat.set("price", getInt(args[12]));
				
				L2Henna template = new L2Henna(hennaDat);
				_henna.put(id, template);
			}			
			_log.config("HennaTable: Loaded " + _henna.size() + " Templates.");
		}
		catch(FileNotFoundException e)
		{
			_log.info("data/pts/dyedata.txt is missing in data folder");
		}
		catch(Exception e)
		{
			_log.info("error while creating HennaTable dyeId:  "+id+" "+e);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception e1)
			{}
		}
		
	}
	
	public L2Henna getTemplate(int id)
	{
		return _henna.get(id);
	}

	public boolean isInitialized()
	{
		return _initialized;
	}
	
	private static int getInt(String name)
	{
		String[]args = name.split("=",-1);
		return Integer.parseInt(args[1]);
	}
}
