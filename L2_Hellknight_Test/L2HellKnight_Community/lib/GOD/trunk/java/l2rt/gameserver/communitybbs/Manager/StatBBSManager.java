package l2rt.gameserver.communitybbs.Manager;

import javolution.text.TextBuilder;
import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.ShowBoard;
import l2rt.util.Files;

import java.sql.Date;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class StatBBSManager extends BaseBBSManager
{
	public static StatBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void parsecmd(String command, L2Player player)
	{
		if (command.equals("_bbsstat;"))
			showPvp(player);
		else if (command.startsWith("_bbsstat;pk"))
			showPK(player);
		else if (command.startsWith("_bbsstat;clan"))
			showClan(player);
		else if (command.startsWith("_bbsstat;castle"))
			showCastle(player);
		else
			ShowBoard.separateAndSend("<html><body><br><br><center>В bbsstat функция: " + command + " пока не реализована</center><br><br></body></html>", player);
	}
	
	private void showPvp(L2Player player)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pvpkills DESC LIMIT 20;");
			rs = statement.executeQuery();

			TextBuilder html = new TextBuilder();
			html.append("<center>ТОП 20 PVP</center>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=250>Ник</td>");
			html.append("<td width=50>Пол</td>");
			html.append("<td width=100>Время в игре</td>");
			html.append("<td width=50>PK</td>");
			html.append("<td width=50><font color=00CC00>PVP</font></td>");
			html.append("<td width=100>Статус</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450>");
			while (rs.next())
			{
				CBStatMan tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = rs.getInt("online");

				String sex = tp.ChSex == 1 ? "Ж" : "М";
				String color;
				String OnOff;
				if (tp.ChOnOff == 1)
				{
					OnOff = "Онлайн";
					color = "00CC00";
				}
				else
				{
					OnOff = "Оффлайн";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=250>" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(tp.ChGameTime) + "</td>");
				html.append("<td width=50>" + tp.ChPk + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPvP + "</font></td>");
				html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");

			String content = Files.read("data/html/CommunityBoardPVP/200.htm", player);
			content = content.replace("%stat%", html.toString());
			separateAndSend(content, player);
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void showPK(L2Player player)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM characters WHERE accesslevel = '0' ORDER BY pkkills DESC LIMIT 20;");
			rs = statement.executeQuery();

			TextBuilder html = new TextBuilder();
			html.append("<center>ТОП 20 PK</center>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450 bgcolor=CCCCCC>");
			html.append("<tr>");
			html.append("<td width=250>Ник</td>");
			html.append("<td width=50>Пол</td>");
			html.append("<td width=100>Время в игре</td>");
			html.append("<td width=50><font color=00CC00>PK</font></td>");
			html.append("<td width=50>PVP</td>");
			html.append("<td width=100>Статус</td>");
			html.append("</tr>");
			html.append("</table>");
			html.append("<img src=L2UI.SquareWhite width=450 height=1>");
			html.append("<table width=450>");
			while (rs.next())
			{
				CBStatMan tp = new CBStatMan();
				tp.PlayerId = rs.getInt("obj_Id");
				tp.ChName = rs.getString("char_name");
				tp.ChSex = rs.getInt("sex");
				tp.ChGameTime = rs.getInt("onlinetime");
				tp.ChPk = rs.getInt("pkkills");
				tp.ChPvP = rs.getInt("pvpkills");
				tp.ChOnOff = rs.getInt("online");

				String sex = tp.ChSex == 1 ? "Ж" : "М";
				String color;
				String OnOff;
				if (tp.ChOnOff == 1)
				{
					OnOff = "Онлайн";
					color = "00CC00";
				}
				else
				{
					OnOff = "Оффлайн";
					color = "D70000";
				}
				html.append("<tr>");
				html.append("<td width=250>" + tp.ChName + "</td>");
				html.append("<td width=50>" + sex + "</td>");
				html.append("<td width=100>" + OnlineTime(tp.ChGameTime) + "</td>");
				html.append("<td width=50><font color=00CC00>" + tp.ChPk + "</font></td>");
				html.append("<td width=50>" + tp.ChPvP + "</td>");
				html.append("<td width=100><font color=" + color + ">" + OnOff + "</font></td>");
				html.append("</tr>");
			}
			html.append("</table>");

			String content = Files.read("data/html/CommunityBoardPVP/200.htm", player);
			content = content.replace("%stat%", html.toString());
			separateAndSend(content, player);
			return;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void showCastle(L2Player player) 
	{ 
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm"); 
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
	    try 
	    { 
	    	con = L2DatabaseFactory.getInstance().getConnection(); 
	    	statement = con.prepareStatement("SELECT * FROM castle ORDER BY id DESC LIMIT 10;"); 
	    	rs = statement.executeQuery(); 
	    	TextBuilder html = new TextBuilder(); 
	    	html.append("<center>Статистика Замков</center>"); 
	    	html.append("<img src=L2UI.SquareWhite width=450 height=1>");
	    	html.append("<table width=450 bgcolor=CCCCCC>"); 
	    	html.append("<tr>"); 
	    	html.append("<td width=150>Замок</td>"); 
	    	html.append("<td width=100>Налог</td>"); 
	    	html.append("<td width=200>Владелец</td>"); 
	    	html.append("<td width=150>Дата осады</td>"); 
	    	html.append("</tr>"); 
	    	html.append("</table>"); 
	    	html.append("<img src=L2UI.SquareWhite width=450 height=1>"); 
	    	html.append("<table width=450>");
	    	String Owner = null; 
	    	String color = "FFFFFF";
	    	while (rs.next())
	    	{
	    		CBStatMan tp = new CBStatMan(); 
	    	   tp.id = rs.getInt("id");
	    	   tp.NameCastl = rs.getString("name");
	    	   tp.Percent = (rs.getString("taxPercent") + "%"); 
	    	   if (Config.ALT_SIEGE_MOD) 
	    		   tp.siegeDate = CastleManager.getInstance().getAltSiegeTime(rs.getInt("id")); 
	    	   else 
	    		   tp.siegeDate = sdf.format(new Date(rs.getLong("siegeDate") * 1000L)); 
	    	   		Owner = CastleManager.getInstance().getOwner(tp.id); 
	    	   		if (Owner != null) 
	    	   		{ 
	    	   			color = "00CC00"; 
	    	   		} 
	    	   		else 
	    	   		{
	    	   			color = "FFFFFF"; 
	    	   			Owner = "Нет владельца"; 
	    	   		} 
	    	   	html.append("<tr>"); 
	    	   	html.append("<td width=150>" + tp.NameCastl + "</td>"); 
	    	   	html.append("<td width=100>" + tp.Percent + "</td>"); 
	    	   	html.append("<td width=200><font color=" + color + ">" + Owner + "</font></td>"); 
	    	   	html.append("<td width=150>" + tp.siegeDate + "</td>");
	    	   	html.append("</tr>"); 
	    	} 
	    	html.append("</table>"); 
	    	String content = Files.read("data/html/CommunityBoardPVP/200.htm", player);
	    	content = content.replace("%stat%", html.toString());
	    	separateAndSend(content, player);
	    	return;
	    }
	    catch (Exception e) 
		{ 
			e.printStackTrace(); 
		} 
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		} 
	} 
	
	
	private void showClan(L2Player player) 
		{ 
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			ResultSet rs = null;
			try 
			{ 
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT clan_data.clan_name,clan_data.clan_level,clan_data.reputation_score,ally_data.ally_name FROM clan_data LEFT JOIN ally_data ON clan_data.ally_id = ally_data.ally_id WHERE clan_data.clan_level>0 order by clan_data.clan_level desc limit 10;"); 
				rs = statement.executeQuery(); 
				
				TextBuilder html = new TextBuilder(); 
				html.append("<center>Топ 10 Кланов</center>"); 
				html.append("<img src=L2UI.SquareWhite width=450 height=1>");
		    	html.append("<table width=450 bgcolor=CCCCCC>"); 
		    	html.append("<tr>"); 
				html.append("<td width=200>Клан</td>"); 
				html.append("<td width=150>Альянс</td>"); 
				html.append("<td width=100>Репутация</td>"); 
				html.append("<td width=150>Уровень</td>"); 
				html.append("</tr>"); 
		    	html.append("</table>"); 
		    	html.append("<img src=L2UI.SquareWhite width=450 height=1>"); 
		    	html.append("<table width=450>"); 
				while (rs.next())
				{
					CBStatMan tp = new CBStatMan();
					tp.ClanName = rs.getString("clan_name"); 
					tp.AllyName = rs.getString("ally_name"); 
					tp.ReputationClan = rs.getInt("reputation_score"); 
					tp.ClanLevel = rs.getInt("clan_level"); 
					html.append("<tr>"); 
					html.append("<td width=200>" + tp.ClanName + "</td>"); 
					if (tp.AllyName != null) 
					{
						html.append("<td width=150>" + tp.AllyName + "</td>"); 
					}
					else 
					{
						html.append("<td width=150>Нет альянса</td>"); 
					}
					html.append("<td width=100>" + tp.ReputationClan + "</td>"); 
					html.append("<td width=150>" + tp.ClanLevel + "</td>");
					html.append("</tr>"); 
				}
				html.append("</table>"); 
						
			String content = Files.read("data/html/CommunityBoardPVP/200.htm", player);
			content = content.replace("%stat%", html.toString()); 
			separateAndSend(content, player);
			return;
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace(); 
		} 
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}
		  
	String OnlineTime(int time)
	{
		long onlinetimeH;
		if (time / 60 / 60 - 0.5 <= 0)
			onlinetimeH = 0L;
		else
			onlinetimeH = Math.round(time / 60 / 60 - 0.5);
		int onlinetimeM = Math.round((float)((time / 60 / 60 - onlinetimeH) * 60));
		return "" + onlinetimeH + " ч. " + onlinetimeM + " м.";
	}

	public void parsewrite(String ar1, String ar2, String ar3, String ar4, String ar5, L2Player player)
	{
	}

	private static class SingletonHolder
	{
		protected static final StatBBSManager _instance = new StatBBSManager();
	}

	public class CBStatMan
	{
		public String NameCastl;
		public Object siegeDate;
		public String Percent;
		public Object id2;
		public int id;
		public int ClanLevel;
		public int ReputationClan;
		public String AllyName;
		public String ClanName;
		public int PlayerId = 0;
		public String ChName = "";
		public int ChGameTime = 0;
		public int ChPk = 0;
		public int ChPvP = 0;
		public int ChOnOff = 0;
		public int ChSex = 0;

		public CBStatMan()
		{
		}
	}
}