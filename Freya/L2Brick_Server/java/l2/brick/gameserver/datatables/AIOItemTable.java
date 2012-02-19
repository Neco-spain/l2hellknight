package l2.brick.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Logger;

import l2.brick.Config;
import l2.brick.L2DatabaseFactory;
import l2.brick.gameserver.handler.AIOItemHandler;
import l2.brick.gameserver.handler.IAIOItemHandler;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.entity.DM;
import l2.brick.gameserver.model.entity.LMEvent;
import l2.brick.gameserver.model.entity.TvTEvent;
import l2.brick.gameserver.network.serverpackets.NpcHtmlMessage;

import javolution.util.FastList;
import javolution.util.FastMap;



public final class AIOItemTable
{
	private static final Logger _log = Logger.getLogger(AIOItemTable.class.getName());
	
	private static class SingletonHolder
	{
		private static final AIOItemTable _instance = new AIOItemTable();
	}

	private static class BufferPageHolder
	{
		private static NpcHtmlMessage mainPage = null;
		private static FastMap<String, NpcHtmlMessage> categoryPages = new FastMap<String, NpcHtmlMessage>();
	}
	
	private static class TeleportPageHolder
	{
		private static NpcHtmlMessage mainPage = null;
		private static FastMap<Integer, NpcHtmlMessage> categoryPages = new FastMap<Integer, NpcHtmlMessage>();
		private static FastMap<Integer, FastMap<Integer, Integer[]>> teleInfo = new FastMap<Integer, FastMap<Integer, Integer[]>>();
	}
	
	private class TopInfo
	{
		private String name = "";
		private int amount = 0;
	}
	
	// Holds each category and his one-use buffs
	private static FastMap<String, CategoryBuffHolder> _buffs = new FastMap<String, CategoryBuffHolder>();
	// Holds all skills
	private static FastMap<Integer, L2Skill> _allBuffs = new FastMap<Integer,L2Skill>();
	// Holds pvp rank
	private static String _pvpList;
	private static int _minPvp;
	private FastMap<String, Integer> _allPvps = new FastMap<String, Integer>();
	// Holds pk rank
	private static String _pkList;
	private static int _minPk;
	private FastMap<String, Integer> _allPks = new FastMap<String, Integer>();
	
	private AIOItemTable()
	{
	}
	
	public static AIOItemTable getInstance()
	{
		return SingletonHolder._instance;
	}
		
	public class SpawnPointInfo
	{
		public String _name;
		public int _x;
		public int _y;
		public int _z;
	}
	
	public class CategoryBuffHolder
	{
		FastMap<Integer, L2Skill> _categoryBuffs;
		String _categoryName;
		
		CategoryBuffHolder(String name)
		{
			if(name != null)
			{
				_categoryName = name;
				_categoryBuffs = new FastMap<Integer, L2Skill>();
			}
		}
		
		void loadMyData()
		{
			Connection con = null;
			
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("SELECT buff_id, buff_lvl FROM aio_buffs WHERE category = ?");
				statement.setString(1, _categoryName);				
				ResultSet rset = statement.executeQuery();
				
				StringBuilder catSb = new StringBuilder();
				catSb.append("<html><body>");
				catSb.append("<html><body><center>");
				catSb.append("<br><font color=LEVEL>Choose what you want!</font><br>");
				catSb.append("<table width = 240 height 32>");
				
				int b = 2;
				
				while(rset.next())
				{
					int id = rset.getInt("buff_id");
					int lvl = rset.getInt("buff_lvl");
					
					L2Skill buff = SkillTable.getInstance().getInfo(id, lvl);
					_categoryBuffs.put(id, buff);
					_allBuffs.put(id, buff);
					
					if(b % 2 == 0)
					{
						catSb.append("<tr>");
						catSb.append("<td><a action=\"bypass -h Aioitem_buffer_buff "+_categoryName+" "+id+"\">"+buff.getName()+"</a></td>");
					}
					else
					{
						catSb.append("<td><a action=\"bypass -h Aioitem_buffer_buff "+_categoryName+" "+id+"\">"+buff.getName()+"</a></td>");
						catSb.append("</tr>");
					}
					b++;
				}
				
				catSb.append("</table><br><a action=\"bypass -h Aioitem_buffer_main\"><font color=LEVEL>Main</font></a>");
				catSb.append("</center></body></html>");
				
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setHtml(catSb.toString());

				BufferPageHolder.categoryPages.put(_categoryName, msg);
				rset.close();
				statement.close();
			}
			catch(Exception e)
			{
				_log.severe("Couldnt load buffs table for AIOItem\n"+e.getMessage());
			}
			try
			{
				con.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public FastMap<Integer, L2Skill> getCategoryBuffs()
		{
			return _categoryBuffs;
		}
	}
		
	public void loadAioItemData()
	{
		if(!Config.AIOITEM_ENABLEME)
		{
			_log.config("AIOItem: I'm disabled");
			return;
		}
		
		Connection con = null;
		_log.config("Loading AIOItem Data...");
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			try
			{
				PreparedStatement statement = con.prepareStatement("SELECT category_id, category FROM aio_teleports_categories");			
				ResultSet rset = statement.executeQuery();
				
				StringBuilder sbMain = new StringBuilder();
				sbMain.append("<html><body><br><center>Choose any category to teleport:<br>");
				while(rset.next())
				{
					final int id = rset.getInt("category_id");
					final String name = rset.getString("category");
					
					sbMain.append("<a action=\"bypass -h Aioitem_teleport_categorypage "+id+"\">"+name+"</a><br1>");
					
					PreparedStatement categoryStatement = con.prepareStatement("SELECT * FROM aio_teleports WHERE category = ?");
					categoryStatement.setString(1, name);
					ResultSet catSet = categoryStatement.executeQuery();
					
					StringBuilder sbCategory = new StringBuilder();
					FastMap<Integer, Integer[]> spawnInfo = new FastMap<Integer, Integer[]>();
					sbCategory.append("<html><body><br><center>Choose between my teleports!:<br>");
					
					while(catSet.next())
					{
						final int teleId = catSet.getInt("id");
						final String teleName = catSet.getString("tpname");
						final int x = catSet.getInt("x");
						final int y = catSet.getInt("y");
						final int z = catSet.getInt("z");
						
						final Integer[] array = {x,y,z};
						spawnInfo.put(teleId, array);
						
						sbCategory.append("<a action=\"bypass -h Aioitem_teleport_goto "+id+" "+teleId+"\">"+teleName+"</a><br1>");
					}
					sbCategory.append("</center></body></html>");
					NpcHtmlMessage msg = new NpcHtmlMessage(5);
					msg.setHtml(sbCategory.toString());
					TeleportPageHolder.categoryPages.put(id, msg);
					TeleportPageHolder.teleInfo.put(id, spawnInfo);
					catSet.close();
					categoryStatement.close();
				}
				sbMain.append("</center></body></html>");
				NpcHtmlMessage main = new NpcHtmlMessage(5);
				main.setHtml(sbMain.toString());
				TeleportPageHolder.mainPage = main;
				
				rset.close();
				statement.close();
				_log.config("AIOItemTable: Loaded "+TeleportPageHolder.categoryPages.size() +" teleport categories!");
			}
			catch(Exception e)
			{
				_log.warning("AIOItemTable: Couldnt load AIO Item teleports: "+e.getMessage());
				e.printStackTrace();
			}
			
			try
			{
				PreparedStatement buffStatement = con.prepareStatement("SELECT category FROM aio_buffs");
				ResultSet buffSet = buffStatement.executeQuery();
				
				StringBuilder mainSb = new StringBuilder();
				mainSb.append("<html><body><br><center>Choose any category to get Buffs:</center><br>");
				FastList<String> alredyGathered = new FastList<String>();
				while(buffSet.next())
				{
					final String name = buffSet.getString("category");
					if(alredyGathered.contains(name))
						continue;
					alredyGathered.add(name);
					mainSb.append("<a action=\"bypass -h Aioitem_buffer_category "+name+"\">"+name+"</a><br1>");
					
					CategoryBuffHolder holder = new CategoryBuffHolder(name);
					holder.loadMyData();
					_buffs.put(name, holder);
				}
				mainSb.append("</body></html>");
				NpcHtmlMessage msg = new NpcHtmlMessage(5);
				msg.setHtml(mainSb.toString());
				BufferPageHolder.mainPage = msg;
				
				buffSet.close();
				buffStatement.close();			
				_log.config("Loaded "+_buffs.size()+" buffs categories for the AIOItem");
			}
			catch(Exception e)
			{
				_log.warning("AIOItem: Couldnt load AIO Item buffs: "+e.getMessage());
				e.printStackTrace();
			}
			// PvP Rank 
			loadPvps();
			// Pk Rank
			loadPks();
		}
		catch(Exception e)
		{
			_log.severe("Couldnt load data for the AIOItem\n"+e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}
	
	private void loadPvps()
	{	
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 and accesslevel=0");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				final String name = rset.getString("char_name");
				final int pvp = rset.getInt("pvpkills");
				
				_allPvps.put(name, pvp);
			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning("AIOItemTable: Couldnt gather needed info from database for PvP Top:");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		buildPvpRank(getTop25Pvps());
	}
	
	private FastMap<String, Integer> getTop25Pvps()
	{
		Map<String, Integer> pvps = new FastMap<String, Integer>();
		pvps.putAll(_allPvps);
		FastMap<String, Integer> toReturn = new FastMap<String, Integer>();
		for(int i = 0; i < 25; i++)
		{
			final TopInfo info = getBest(pvps);
			final int amount = info.amount;
			if(amount == 0)
				continue;
			toReturn.put(info.name, amount);
			pvps.remove(info.name);
			_minPvp = amount;
		}
		return toReturn;
	}
	
	private TopInfo getBest(Map<String, Integer> pvps)
	{
		TopInfo info = new TopInfo();
		for(Map.Entry<String, Integer> entry : pvps.entrySet())
		{
			if(entry.getValue() > info.amount)
			{
				info.amount = entry.getValue();
				info.name = entry.getKey();
			}
		}
		return info;
	}
	
	private void buildPvpRank(final FastMap<String, Integer> data)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("");
		final String colored = "<table width=270 bgcolor=\"66CCFF\"><tr>";
		final String nonColored = "<table width=270><tr>";
		int counter = 1;
		
		for(Map.Entry<String, Integer> entry : getTop25Pvps().entrySet())
		{
			if(counter % 2 == 0)
				sb.append(colored);
			else
				sb.append(nonColored);
			
			sb.append("<td width=90>"+counter+"</td>");
			sb.append("<td width=90>"+entry.getKey()+"</td>");
			sb.append("<td width=90>"+entry.getValue()+"</td>");
			sb.append("</tr></table>");
		
			++counter;
		}
		_pvpList = sb.toString();
	}
	
	private void loadPks()
	{	
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT char_name,pkkills FROM characters WHERE pkkills>0 and accesslevel=0");
			ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				final String name = rset.getString("char_name");
				final int pk = rset.getInt("pkkills");
				
				_allPks.put(name, pk);

			}
			rset.close();
			statement.close();
		}
		catch(Exception e)
		{
			_log.warning("AIOItemTable: Couldnt gather needed info from database for Pk Top:");
			e.printStackTrace();
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		buildPkRank(getTop25Pks());
	}
	
	private FastMap<String, Integer> getTop25Pks()
	{
		Map<String, Integer> pks = new FastMap<String, Integer>();
		pks.putAll(_allPks);
		FastMap<String, Integer> toReturn = new FastMap<String, Integer>();
		for(int i = 0; i < 25; i++)
		{
			final TopInfo info = getBest(pks);
			final int amount = info.amount;
			if(amount == 0)
				continue;
			toReturn.put(info.name, amount);
			pks.remove(info.name);
			_minPk = amount;
		}
		return toReturn;
	}
		
	private void buildPkRank(final FastMap<String, Integer> data)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("");
		final String colored = "<table width=270 bgcolor=\"FF9999\"><tr>";
		final String nonColored = "<table width=270><tr>";
		int counter = 1;
		
		for(Map.Entry<String, Integer> entry : getTop25Pks().entrySet())
		{
			if(counter % 2 == 0)
				sb.append(colored);
			else
				sb.append(nonColored);
			
			sb.append("<td width=90>"+counter+"</td>");
			sb.append("<td width=90>"+entry.getKey()+"</td>");
			sb.append("<td width=90>"+entry.getValue()+"</td>");
			sb.append("</tr></table>");
		
			++counter;
		}
		_pkList = sb.toString();
	}
	
	public String getPvpRank()
	{
		return _pvpList;
	}
	
	public String getPkRank()
	{
		return _pkList;
	}
	
	public void onPvpIncrease(L2PcInstance pc)
	{
		final int pvps = pc.getPvpKills();
		_allPvps.put(pc.getName(), pvps);
		if(pvps > _minPvp)
			buildPvpRank(getTop25Pvps());
	}
	
	public void onPkIncrease(L2PcInstance pc)
	{
		final int pks = pc.getPkKills();
		_allPks.put(pc.getName(), pks);
		if(pks > _minPk)
			buildPkRank(getTop25Pks());
	}
	
	public NpcHtmlMessage getTeleportMain()
	{
		return TeleportPageHolder.mainPage;
	}
	
	public NpcHtmlMessage getTeleportCategoryPage(int category)
	{
		return TeleportPageHolder.categoryPages.get(category);
	}
	
	public Integer[] getTelportCoordinates(int category, int teleId)
	{
		try
		{
			return TeleportPageHolder.teleInfo.get(category).get(teleId);
		}
		catch(Exception e)
		{
			_log.warning("AIOItemTable: Teleport Category ["+category+"] or spawn point ["+teleId+"] does not exist");
		}
		return null;
	}
	
	public NpcHtmlMessage getBufferMain()
	{
		return BufferPageHolder.mainPage;
	}
	
	public NpcHtmlMessage getBufferCategoryPage(String category)
	{
		return BufferPageHolder.categoryPages.get(category);
	}
	
	public FastMap<String, CategoryBuffHolder> getBuffs()
	{
		return _buffs;
	}
	
	/**
	 * Will return the holder of buffs of a given category
	 * @param cat [String]
	 * @return CategoryBuffHolder
	 */
	public CategoryBuffHolder getBuffCategory(String cat)
	{
		if(!_buffs.containsKey(cat))
			return null;
		
		return _buffs.get(cat);
	}
	
	/**
	 * Will return a skill which must be contained in the
	 * given category with the given id
	 * @param category
	 * @param id
	 * @return L2Skill
	 */
	public L2Skill getBuff(String category, int id)
	{
		if(getBuffCategory(category) != null)
		{
			L2Skill buff = null;
			if((buff = getBuffCategory(category).getCategoryBuffs().get(id)) != null)
			{
				return buff;
			}
		}
		return null;
	}
	
	/**
	 * Return the skill linked to the given
	 * id
	 * @param id
	 * @return L2Skill
	 */
	public L2Skill getBuff(int id)
	{
		return _allBuffs.get(id);
	}
	
	/**
	 * Check the general requirements for the player to be able to send
	 * a aio item bypass and use it
	 * @param player [L2PcInstance]
	 * @return boolean
	 */
	public boolean checkPlayerConditions(L2PcInstance player)
	{
		if(player.getPvpFlag() > 0)
		{
			player.sendMessage("Cannot use AIOItem while flagged!");
			return false;
		}
		if(player.getKarma() > 0 || player.isCursedWeaponEquipped())
		{
			player.sendMessage("Cannot use AIOItem while chaotic!");
			return false;
		}
		if(player.isInOlympiadMode() || TvTEvent.isPlayerParticipant(player.getObjectId()) || LMEvent.isPlayerParticipant(player.getObjectId()) || DM.isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("Cannot use while in events!");
			return false;
		}
		if(player.isEnchanting())
		{
			player.sendMessage("Cannot use while enchanting!");
			return false;
		}
		if(player.isInJail())
		{
			player.sendMessage("Cannot use while in Jail!");
			return false;
		}
		return true;
	}
	
	public void handleBypass(L2PcInstance activeChar, String command)
	{
		if(!Config.AIOITEM_ENABLEME)
			return;
		
		if(!checkPlayerConditions(activeChar))
			return;
		
		activeChar.setTarget(activeChar);
		
		String[] subCmd = command.split("_");
		
		IAIOItemHandler handler = AIOItemHandler.getInstance().getAIOHandler(subCmd[1]);
		
		if(handler != null)
			handler.onBypassUse(activeChar, subCmd[2]);
	}	
}