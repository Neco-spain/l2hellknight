package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.GMAudit;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

 public class AdminHwid implements IAdminCommandHandler
 {
   	 private static Log _log = LogFactory.getLog("AdminHwid");
	 private static String[] ADMIN_COMMANDS = 
	 { 
		 "admin_hwid","admin_hwidlist","admin_hwidban","admin_hwidunban"
	 };
	 
	@SuppressWarnings("unused")
	public boolean useAdminCommand(String command, L2PcInstance activeChar) 
	{
		StringTokenizer st = new StringTokenizer(command);
		st.nextToken();
		String account_name = "";
		String player = "";
		L2PcInstance plyr = null;
		
		if (command.equals("admin_hwid"))
		{
			 String html = HtmCache.getInstance().getHtm("data/html/protect/main.htm");
			 NpcHtmlMessage msg = new NpcHtmlMessage(0);
			 msg.setHtml(html);
			 activeChar.sendPacket(msg);
		}
		else if (command.startsWith("admin_hwidlist")) 
		{
			String html = HtmCache.getInstance().getHtm("data/html/protect/list.htm");
		    String list = "";
		    String next = "";
		    String prev = "";
		    Connection con = null;
		    try 
		    {
		    	
		    	int startwith = 0;
		    	if (command.indexOf(" ") != -1)
		    		startwith = Integer.parseInt(command.split(" ")[1]);
		    	con = L2DatabaseFactory.getInstance().getConnection();
		    	PreparedStatement preparedstatement1 = con.prepareStatement("SELECT c.char_name, s.hwid FROM ban_hwid AS s INNER JOIN characters c on c.obj_id = s.charId");
		    	ResultSet resultset1 = preparedstatement1.executeQuery();
		    	int i = 0;
		    	while (resultset1.next()) 
		    	{
		    		if (i++ < startwith)
		    			continue;
		    		list = list + "<tr><td>" + ((resultset1.getString(1) != null) ? resultset1.getString(1) : "") + "</td><td>" + resultset1.getString(2) + "</td><td>" + 
		    		"<button action=\"bypass -h admin_hwidunban " + resultset1.getString(2) + " \" value=\"Unban\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td></tr>";
		    		if (i - startwith > 30) 
		    		{
		    			next = "<button action=\"bypass -h admin_hwidban " + i + "\" value=\"Next\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">";
		    			break;
		    		}
		    	}
		    	if (startwith != 0) 
		    	{
		    		startwith -= 30;
		    		if (startwith < 0)
		    			startwith = 0;
		    		prev = "<button action=\"bypass -h admin_hwidban " + startwith + "\" value=\"Prev\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\">";
		    	}
		    } 
		    catch (SQLException e) 
		    {
		    	_log.warn("Protect: Unable load banned list :" + e);
		    }
		    finally
		    {
		    	//DatabaseUtils.closeDatabaseCSR(con, stm, rs);
		    }
 
	   NpcHtmlMessage msg = new NpcHtmlMessage(0);
       msg.setHtml(html);
       msg.replace("%list%", list);
       msg.replace("%next%", next);
       msg.replace("%prev%", prev);
       activeChar.sendPacket(msg);
     }
	else if (command.startsWith("admin_hwidban"))
	{
		Connection con = null;
		try
		{
			player = st.nextToken();
			L2PcInstance playerObj = L2World.getInstance().getPlayer(player);

			if (playerObj != null)
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement preparedstatement1 = con.prepareStatement("INSERT INTO ban_hwid (hwid, charId) values(?,?)");
				preparedstatement1.setString(1, String.format("%X", playerObj.getClient().getSessionId().clientKey));
				preparedstatement1.setInt(2, playerObj.getObjectId());
				preparedstatement1.execute();
				playerObj.sendMessage("Вы получили бан по HWID");
				activeChar.sendMessage("Вы дали игроку " + player + " Бан по HWID ");
				playerObj.closeNetConnection(false);
			}
			else
			{
				activeChar.sendMessage("Персонажа нет в игре");
			}
		}
		catch (NoSuchElementException nsee)
		{
			activeChar.sendMessage("Specify a character name.");
		}
		catch (Exception e)
		{
		}
		GMAudit.auditGMAction(activeChar.getName(), command, player, "");
		return true;
	}
	else if (command.startsWith("admin_hwidunban"))
	{
		Connection con = null;
		try
		{
				player = st.nextToken();
				con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement preparedstatement1 = con.prepareStatement("DELETE FROM ban_hwid WHERE hwid=?");
				preparedstatement1.setString(1,player);
				preparedstatement1.execute();
				preparedstatement1.close();
				con.close();
		} 
		catch (SQLException localSQLException) 
		{
									  
		}
		finally
		{
						
		} 	
	}
		return false;
		
	}
	  public String[] getAdminCommandList() 
	 {
		  return ADMIN_COMMANDS;
	 }


	
	
	 
 }