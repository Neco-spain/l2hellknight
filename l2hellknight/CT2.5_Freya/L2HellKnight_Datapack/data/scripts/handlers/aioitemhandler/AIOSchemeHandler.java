package handlers.aioitemhandler;

import java.util.logging.Logger;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

import l2.hellknight.Config;
import l2.hellknight.gameserver.cache.HtmCache;
import l2.hellknight.gameserver.datatables.AIOItemTable;
import l2.hellknight.gameserver.handler.IAIOItemHandler;
import l2.hellknight.gameserver.model.L2ItemInstance;
import l2.hellknight.gameserver.model.L2Skill;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.network.serverpackets.NpcHtmlMessage;

public class AIOSchemeHandler implements IAIOItemHandler
{
	private static final Logger _log = Logger.getLogger(AIOSchemeHandler.class.getName());
	private static final String BYPASS = "scheme";
	
	private static enum Action
	{
		CREATE_PROFILE,
		GET_BUFFS
	}
	
	
	@Override
	public String getBypass() 
	{
		return BYPASS;
	}

	@Override
	public void onBypassUse(L2PcInstance player, String command) 
	{
	    if(!AIOItemTable.getInstance().checkPlayerConditions(player))
		{
			return;
		}
		String[] subCommands = command.split(" ");
		String actualCmd = subCommands[0];
		
		
		/*
		 * Send main's scheme page
		 */
		if(actualCmd.equalsIgnoreCase("main"))
		{
			sendSchemePageToPlayer(player);
		}
		/*
		 * Create scheme profile
		 */
		else if(actualCmd.equalsIgnoreCase("createprofile"))
		{
			if(player.getProfileNames().size() == Config.AIOITEM_SCHEME_MAX_PROFILES)
			{
				player.sendMessage("You already reached the max allowed profiles per char!");
				return;
			}
			if(subCommands.length == 1)
			{
				player.sendMessage("You need to enter a NAME for the scheme profile!");
				return;
			}
			String secondCmd = subCommands[1];
			if(secondCmd == null || secondCmd.isEmpty())
			{
				_log.severe("Null Scheme profile from: "+player.getName());
				return;
			}
			if(player.getProfileNames().contains(secondCmd))
			{
				player.sendMessage("The given profile alredy exist in your list!");
				return;
			}
			
			if(!paymentDone(player, Action.CREATE_PROFILE))
			{
				return;
			}
			
			player.addNewProfile(secondCmd);
			player.needSaveSchemes();
			sendSchemePageToPlayer(player);
		}
		/*
		 * Manage profile
		 */
		else if(actualCmd.equalsIgnoreCase("manage"))
		{
			String secondCmd = subCommands[1];
			
			if(secondCmd != null)
			{
				sendManageWindow(player, secondCmd);
			}
			else
			{
				_log.severe("Player "+player.getName()+" has a empty AIOItem scheme profile!");
				return;
			}
		}
		/*
		 * Get buffs
		 */
		else if(actualCmd.equalsIgnoreCase("buff"))
		{
			if(!paymentDone(player, Action.GET_BUFFS))
			{
				return;
			}
			
			String secondCmd = subCommands[1];
			if(secondCmd == null || secondCmd.isEmpty())
			{
				_log.severe("Null Scheme profile for player: "+player.getName());
				return;
			}
			
			FastList<L2Skill> buffs = player.getProfileBuffs(secondCmd);
			if(buffs != null)
			{
				for(L2Skill sk : buffs)
				{
					sk.getEffects(player, player);
				}
			}
		}
		/*
		 * Erase profile
		 */
		else if(actualCmd.equalsIgnoreCase("del"))
		{
			String secondCmd = subCommands[1];
			if(secondCmd == null || secondCmd.isEmpty())
			{
				_log.severe("Null scheme profile for: "+player.getName());
				return;
			}
			player.deleteProfile(secondCmd);
			player.needSaveSchemes();
			sendSchemePageToPlayer(player);
		}
		/*
		 * Add buffs to profile page
		 */
		else if(actualCmd.equalsIgnoreCase("editadd"))
		{
			String category = subCommands[1];
			String profile = subCommands[2];
			
			if(category == null || profile == null)
				return;
			
			sendAddBuffWindow(category, profile, player);
			player.setLastAIOBufferCategory(category);
		}
		/*
		 * Add a buff
		 */
		else if(actualCmd.equalsIgnoreCase("addbuff"))
		{
			String profile = subCommands[1];
			
			if(profile == null)
				return;
			
			int buffId = 0;
			try
			{
				buffId = Integer.parseInt(subCommands[2]);
			}
			catch(NumberFormatException nfe)
			{
				nfe.printStackTrace();
			}
			
			L2Skill buff = null;
			
			if((buff = AIOItemTable.getInstance().getBuff(buffId)) != null)
			{
				player.addNewBuff(profile, buff);
				player.needSaveSchemes();
				sendAddBuffWindow(player.getLastCategory(), profile, player);
			}
		}
		/*
		 * Delete buff window
		 */
		else if(actualCmd.equalsIgnoreCase("editdel"))
		{
			if(subCommands[1] == null)
				return;
			
			sendDelBuffWindow(subCommands[1], player);
		}
		/*
		 * Delete a buff
		 */
		else if(actualCmd.equalsIgnoreCase("delbuff"))
		{
			String profile = subCommands[1];
			
			if(profile == null)
				return;
			
			int buffId = 0;
			try
			{
				buffId = Integer.parseInt(subCommands[2]);
			}
			catch(NumberFormatException nfe)
			{
				nfe.printStackTrace();
			}
			
			L2Skill deletedBuff = AIOItemTable.getInstance().getBuff(buffId);
			player.deleteBuff(profile, deletedBuff);
			player.needSaveSchemes();
			sendDelBuffWindow(profile, player);
		}
	}
	
	/**
	 * Will return true if the player made the payment for the
	 * service, otherwise, will return false
	 * @param player
	 * @param action
	 * @return boolean
	 */
	private boolean paymentDone(L2PcInstance player, Action action)
	{
		L2ItemInstance coin = null;
		
		if((coin = player.getInventory().getItemByItemId(Config.AIOITEM_SCHEME_COIN)) == null)
		{
			player.sendMessage("You dont have the required items to proceed!");
			return false;
		}
		
		switch(action)
		{
			case CREATE_PROFILE:
			{
				if(coin.getCount() < Config.AIOITEM_SCHEME_PROFILE_PRICE)
				{
					player.sendMessage("You dont have enought "+coin.getName()+" to create a new scheme!");
					return false;
				}
				else
				{
					player.destroyItemByItemId("AIOItem", coin.getItemId(), Config.AIOITEM_SCHEME_PROFILE_PRICE, player, true);
					return true;
				}
			}
			case GET_BUFFS:
			{
				if(coin.getCount() < Config.AIOITEM_SCHEME_PRICE)
				{
					player.sendMessage("You dont have enought "+coin.getName()+" to create a new scheme!");
					return false;
				}
				else
				{
					player.destroyItemByItemId("AIOItem", coin.getItemId(), Config.AIOITEM_SCHEME_PRICE, player, true);
					return true;
				}
			}
			default:
				return false;
		}
	}
	
	/**
	 * Will send to the player the scheme page with his info
	 * @param player
	 */
	private void sendSchemePageToPlayer(L2PcInstance player)
	{
		String schemeMain = HtmCache.getInstance().getHtm(null, "data/html/aioitem/scheme.htm");
		StringBuilder profiles = new StringBuilder();
		for(String prof : player.getProfileNames())
		{
			profiles.append("<a action=\"bypass -h Aioitem_scheme_manage "+prof+"\">"+prof+"<br1>");
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(schemeMain);
		msg.replace("%profList%", profiles.toString());
		player.sendPacket(msg);
	}
	
	/**
	 * Will send the profile edition page to the given player
	 * @param player
	 * @param profile
	 */
	private void sendManageWindow(L2PcInstance player, String profile)
	{
		String html = HtmCache.getInstance().getHtm(null, "data/html/aioitem/schememanage.htm");
		if(html == null)
		{
			_log.severe("The file schememanage.htm for the AIOItem desn't exist or is corrupted!");
			return;
		}
		
		StringBuilder categories = new StringBuilder();
		for(String cat : AIOItemTable.getInstance().getBuffs().keySet())
		{
			categories.append("<a action=\"bypass -h Aioitem_scheme_editadd "+cat+" "+profile+"\">Add "+cat+" buffs</a>");
		}
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(html);
		msg.replace("%profile%", profile);
		msg.replace("%addList%", categories.toString());
		player.sendPacket(msg);
	}
	
	/**
	 * Will show a window with the buffs from the given category
	 * to be added to the player schemes profile
	 * @param category
	 * @param profile
	 * @param player
	 */
	private void sendAddBuffWindow(String category, String profile, L2PcInstance player)
	{
		int b = 2;
		FastMap<Integer, L2Skill> temp = AIOItemTable.getInstance().getBuffCategory(category).getCategoryBuffs();
		
		TextBuilder tb = new TextBuilder();
		tb.append("<html><body><center>");
		tb.append("<br><font color=LEVEL>Choose what you want!</font><br>");
		tb.append("<table width = 240 height 32>");
		for(int i : temp.keySet())
		{
			L2Skill sk = temp.get(i);
			if(player.getProfileBuffs(profile).contains(sk))
				continue;
			
			if(b % 2 == 0)
			{
				tb.append("<tr>");
				tb.append("<td><a action=\"bypass -h Aioitem_scheme_addbuff "+profile+" "+i+"\">"+sk.getName()+"</a></td>");
			}
			else
			{
				tb.append("<td><a action=\"bypass -h Aioitem_scheme_addbuff "+profile+" "+i+"\">"+sk.getName()+"</a></td>");
				tb.append("</tr>");
			}
			b++;
		}
		tb.append("</table><br><a action=\"bypass -h Aioitem_scheme_main\"><font color=LEVEL>Main</font></a>");
		tb.append("</center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
	
	private void sendDelBuffWindow(String profile, L2PcInstance player)
	{
		if(player.getProfileBuffs(profile) == null)
			return;
		
		int b = 2;
		TextBuilder tb = new TextBuilder();
		tb.append("<html><body><center>");
		tb.append("<br><font color=LEVEL>Choose what you want!</font><br>");
		tb.append("<table width = 240 height 32>");
		for(L2Skill sk : player.getProfileBuffs(profile))
		{
			int id = sk.getId();
			if(player.getProfileBuffs(profile).contains(sk))
				continue;
			
			if(b % 2 == 0)
			{
				tb.append("<tr>");
				tb.append("<td><a action=\"bypass -h Aioitem_scheme_delbuff "+profile+" "+id+"\">"+sk.getName()+"</a></td>");
			}
			else
			{
				tb.append("<td><a action=\"bypass -h Aioitem_scheme_delbuff "+profile+" "+id+"\">"+sk.getName()+"</a></td>");
				tb.append("</tr>");
			}
			b++;
		}
		tb.append("</table><br><a action=\"bypass -h Aioitem_scheme_main\"><font color=LEVEL>Main</font></a>");
		tb.append("</center></body></html>");
		
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setHtml(tb.toString());
		player.sendPacket(msg);
	}
}