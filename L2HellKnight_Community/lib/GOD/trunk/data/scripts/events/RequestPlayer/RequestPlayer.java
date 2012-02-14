package events.RequestPlayer;


import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map.Entry;

import javolution.util.FastMap;

import l2rt.Config;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.database.mysql;
import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.util.Files;


public class RequestPlayer extends Functions implements ScriptFile
{


	
	public String DialogAppend_31215(Integer val)
	{
		if(val != 0)
			return "";
		return OutDia();
	}
	
	public String OutDia()
	{
		L2Player activeChar = (L2Player) getSelf();
		String append = Files.read("data/scripts/events/RequestPlayer/index.htm");
		append = append.replaceFirst("%Name%", activeChar.getName());
		append = append.replaceFirst("%list%", returnList());
		
		return append;
	}
	
	public String returnList()
	{
		String html = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM requestplayer ORDER BY time DESC LIMIT 5;");
			rs = statement.executeQuery();
			html+=("");
			html+=("<center>ТОП 5 заказов</center>");
			html+=("<img src=L2UI.SquareWhite width=300 height=1>");
			html+=("<table height=20 width=300 bgcolor=CCCCCC>");
			html+=("<tr>");
			html+=("<td width=100>Цель</td>");
			html+=("<td width=100>Награда</td>");
			html+=("<td width=100>Дата заявки</td>");
			html+=("</tr>");
			html+=("</table>");
			html+=("<img src=L2UI.SquareWhite width=300 height=1>");
			html+=("<table width=300>");
			while (rs.next())
			{
				Calendar testStartTime = null;
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				testStartTime.setTimeInMillis(rs.getLong("time"));

				html+=("<tr>");
				html+=("<td width=100>" + rs.getString("Goal_name") + "</td>");
				html+=("<td width=100>" + rs.getInt("price") + "</td>");
				html+=("<td width=100>" +  testStartTime.get(Calendar.DATE) + "." + (testStartTime.get(Calendar.MONTH)+1) + "." + testStartTime.get(Calendar.YEAR) + "</td>");
				html+=("</tr>");

			}
			html+=("</table>");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return html;
	}
	
	public void ShowAddOrder()
	{
		L2Player activeChar = (L2Player) getSelf();
		String append = Files.read("data/scripts/events/RequestPlayer/order.htm");
		show(append,activeChar);
		
	}
	
	public void addOrder(String[] args)
	{	
		L2Player player = (L2Player) getSelf();
		String name;
		int price;
		int countKill;
		try
		{
			name = args[0];
			price = Integer.valueOf(args[1]);
			
		}
		catch(Exception e)
		{
			show("Некорректные данные", player);
			return;
		}
		if (player.getAdena() <= price)  
		{
			player.sendPacket(new L2GameServerPacket[] { Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA });
			return;
		}
		
		mysql.set("INSERT INTO requestplayer (char_id,Goal_name,price,count_kill,time) VALUES (" + player.getObjectId() + ",'" + name.toLowerCase() + "'," + price + "," + 1 + "," + System.currentTimeMillis() + ")");
		player.reduceAdena(price, true);
		Announcements.getInstance().announceToAll("Заказ на убийство: " + name +" в исполнении.");
		if (RewarPlayer.get(name) != null)
			RewarPlayer.put(name,price+RewarPlayer.get(name));
		else
		RewarPlayer.put(name, (long)price);
		
	}
	
	public static void removeSqlOrder(String name)
	{
		 ThreadConnection con = null;
	        try {
	            con = L2DatabaseFactory.getInstance().getConnection();
	            con.prepareStatement("DELETE FROM `requestplayer` where `Goal_name`=" + name).execute();
	        } catch (final Exception e) {
	            e.printStackTrace();
	        } finally {
	            DatabaseUtils.closeConnection(con);
	        }
	}
	
	public void showSerachOrder(String[] args)
	{
		L2Player activeChar = (L2Player) getSelf();
		String name = args[0];
		String html = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM requestplayer where `Goal_name`='" + name + "'");
			rs = statement.executeQuery();
			html+=("");
			html+=("<img src=L2UI.SquareWhite width=300 height=1>");
			html+=("<table height=20 width=300 bgcolor=CCCCCC>");
			html+=("<tr>");
			html+=("<td width=100>Цель</td>");
			html+=("<td width=100>Награда</td>");
			html+=("<td width=100>Дата заявки</td>");
			html+=("</tr>");
			html+=("</table>");
			html+=("<img src=L2UI.SquareWhite width=300 height=1>");
			html+=("<table width=300>");
			while (rs.next())
			{
				Calendar testStartTime = null;
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				testStartTime.setTimeInMillis(rs.getLong("time"));

				html+=("<tr>");
				html+=("<td width=100>" + rs.getString("Goal_name") + "</td>");
				html+=("<td width=100>" + rs.getInt("price") + "</td>");
				html+=("<td width=100>" +  Calendar.getInstance().get(Calendar.DATE) + "." + (Calendar.getInstance().get(Calendar.MONTH)+1) + "." + Calendar.getInstance().get(Calendar.YEAR) + "</td>");
				html+=("</tr>");

			}
			html+=("</table>");
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		show(html,activeChar);	
	}
	
	public static void OnDie(final L2Character self, L2Character killer)
	{
		String name = self.getName().toLowerCase();
		int priceadd = 0;
		if (RewarPlayer.containsKey(name) && killer.isPlayer() && self.isPlayer())
		{
			for (Entry<String, Long> e : RewarPlayer.entrySet())
			{
				Announcements.getInstance().announceToAll("Заказ на убийство: "+ name +" выполнен!");
				String nameFast = e.getKey();
				Long price = e.getValue();
				if (nameFast.equalsIgnoreCase(name))
					priceadd+=price;	
			}
			removeSqlOrder(name);
			addItem(killer.getPlayer(),57,priceadd);
			RewarPlayer.remove(name);
			killer.sendMessage("Поздравляем вы выполнили заказ.");
		}
	}
	
	public void returnAllList()
	{
		L2Player activeChar = (L2Player) getSelf();
		String html = "";
		html = Files.read("data/scripts/events/RequestPlayer/AllOrder.htm");
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM requestplayer");
			rs = statement.executeQuery();
			html+=("");
			html+=("<img src=L2UI.SquareWhite width=300 height=1>");
			html+=("<table height=20 width=300 bgcolor=CCCCCC>");
			html+=("<tr>");
			html+=("<td width=100>Цель</td>");
			html+=("<td width=100>Награда</td>");
			html+=("<td width=100>Дата заявки</td>");
			html+=("</tr>");
			html+=("</table>");
			html+=("<img src=L2UI.SquareWhite width=300 height=1>");
			html+=("<table width=300>");
			while (rs.next())
			{
				Calendar testStartTime = null;
				testStartTime = Calendar.getInstance();
				testStartTime.setLenient(true);
				testStartTime.setTimeInMillis(rs.getLong("time"));

				html+=("<tr>");
				html+=("<td width=100>" + rs.getString("Goal_name") + "</td>");
				html+=("<td width=100>" + rs.getInt("price") + "</td>");
				html+=("<td width=100>" +  Calendar.getInstance().get(Calendar.DATE) + "." + (Calendar.getInstance().get(Calendar.MONTH)+1) + "." + Calendar.getInstance().get(Calendar.YEAR) + "</td>");
				html+=("</tr>");

			}
			html+=("</table>");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		show(html,activeChar);
	}
	
	private static FastMap<String,Long> RewarPlayer =  new FastMap <String,Long>().setShared(true);
	
	public void loadSQL()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM requestplayer");
			rs = statement.executeQuery();
			while (rs.next())
			{
				String name = rs.getString("Goal_name");
				long price = rs.getLong("price");
				if (RewarPlayer.get(name) != null)
					RewarPlayer.put(name,price+RewarPlayer.get(name));
				else
				RewarPlayer.put(name, (long)price);
			}
			
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
	
	@Override
	public void onLoad() {
		loadSQL();
		
	}

	@Override
	public void onReload() {
		RewarPlayer.clear();
		
	}

	@Override
	public void onShutdown() {
		onReload();		
	}

	
}