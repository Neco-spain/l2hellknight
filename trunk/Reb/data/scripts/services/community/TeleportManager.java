package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Zone.ZoneType;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.scripts.ScriptFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KilRoy
 * Community Board v2.0 Teleport MOD
 */
public class TeleportManager implements ScriptFile, ICommunityBoardHandler
{

	private static final Logger _log = LoggerFactory.getLogger(TeleportManager.class);
	
	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.BBS_PVP_TELEPORT_ENABLED)
		{
			_log.info("CommunityBoard: Manage Teleport service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED && Config.BBS_PVP_TELEPORT_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsteleport;", "_bbsteleport;delete;", "_bbsteleport;save; ", "_bbsteleport;teleport;" };
	}
	
	
	public class CBteleport
	{
		public int TpId = 0; // Teport loc ID
		public String TpName = ""; // Loc name
		public int PlayerId = 0; // charID
		public int xC = 0; // Location coords
		public int yC = 0; //
		public int zC = 0; //
	}

	@Override
	public void onBypassCommand(Player player, String command)
	{

		player.setSessionVar("add_fav", null);

		if(command.equals("_bbsteleport;"))
		{
			showTp(player);
		}
		else if(command.startsWith("_bbsteleport;delete;"))
		{
			StringTokenizer stDell = new StringTokenizer(command, ";");
			stDell.nextToken();
			stDell.nextToken();
			int TpNameDell = Integer.parseInt(stDell.nextToken());
			delTp(player, TpNameDell);
			showTp(player);
		}
		else if(command.startsWith("_bbsteleport;save; "))
		{
			StringTokenizer stAdd = new StringTokenizer(command, ";");
			stAdd.nextToken();
			stAdd.nextToken();
			String TpNameAdd = stAdd.nextToken();
			AddTp(player, TpNameAdd);
			showTp(player);
		}
		else if(command.startsWith("_bbsteleport;teleport;"))
		{
			StringTokenizer stGoTp = new StringTokenizer(command, " ");
			stGoTp.nextToken();
			int xTp = Integer.parseInt(stGoTp.nextToken());
			int yTp = Integer.parseInt(stGoTp.nextToken());
			int zTp = Integer.parseInt(stGoTp.nextToken());
			int priceTp = Integer.parseInt(stGoTp.nextToken());
			goTp(player, xTp, yTp, zTp, priceTp);
			showTp(player);
		}
		else
		{
			ShowBoard.separateAndSend("<html><body><br><br><center>Функция: " + command + " пока не реализована</center><br><br></body></html>", player);
		}
	}

	private void goTp(Player player, int xTp, int yTp, int zTp, int priceTp)
	{
		if(player.isCursedWeaponEquipped()/* ||player.isInJail() */||player.getReflectionId() != 0 || player.getPvpFlag() != 0 || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying() || player.isTerritoryFlagEquipped() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.SIEGE) || player.isInZone(ZoneType.epic))
		{
			player.sendMessage(player.isLangRus() ? "Телепортация невозможна!" : "Teleportation is not possible!");
			return;
		}
		if(priceTp > 0 && player.getAdena() < priceTp)
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_ENOUGH_ADENA));
			return;
		}
		if(priceTp > 0)
		{
			player.reduceAdena((long) priceTp, true);
		}
		player.teleToLocation(xTp, yTp, zTp);
	}

	private void showTp(Player player)
	{
		CBteleport tp;
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM comteleport WHERE charId=?;");
			st.setLong(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			TextBuilder html = new TextBuilder();
			html.append("<table width=220>");
			while(rs.next())
			{
				tp = new CBteleport();
				tp.TpId = rs.getInt("TpId");
				tp.TpName = rs.getString("name");
				tp.PlayerId = rs.getInt("charId");
				tp.xC = rs.getInt("xPos");
				tp.yC = rs.getInt("yPos");
				tp.zC = rs.getInt("zPos");
				html.append("<tr>");
				html.append("<td>");
				html.append("<button value=\"" + tp.TpName + "\" action=\"bypass _bbsteleport;teleport; " + tp.xC + " " + tp.yC + " " + tp.zC + " " + Config.BBS_PVP_TELEPORT_POINT_PRICE + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("<td>");
				html.append("<button value=\"Удалить\" action=\"bypass _bbsteleport;delete;" + tp.TpId + "\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				html.append("</td>");
				html.append("</tr>");
			}
			html.append("</table>");
			
			DbUtils.closeQuietly(st, rs);

			String content = HtmCache.getInstance().getNotNull("scripts/services/communityPVP/pages/teleport/teleport.htm", player);
			content = content.replace("%tp%", html.toString());
			ShowBoard.separateAndSend(content, player);
			return;

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}

	}

	private void delTp(Player player, int TpNameDell)
	{
		Connection conDel = null;
		try
		{
			conDel = DatabaseFactory.getInstance().getConnection();
			PreparedStatement stDel = conDel.prepareStatement("DELETE FROM comteleport WHERE charId=? AND TpId=?;");
			stDel.setInt(1, player.getObjectId());
			stDel.setInt(2, TpNameDell);
			stDel.execute();
			DbUtils.closeQuietly(stDel);
		}
		catch(Exception e)
		{
			_log.warn("data error on Delete Teleport: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(conDel);
		}

	}

	private void AddTp(Player player, String TpNameAdd)
	{
		if(player.isCursedWeaponEquipped() /*|| player.isInJail()*/ ||player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isAttackingNow() || player.isInZone(ZoneType.no_escape) || player.isInZone(ZoneType.SIEGE) || player.isOlympiadGameStart() || player.isInZone(ZoneType.epic))
		{
			player.sendMessage(player.isLangRus() ? "Сохранить закладку в вашем состоянии нельзя!" : "Bookmark in your condition can not be!");

			return;
		}

		if(player.isInCombat() || player.getPvpFlag() != 0)
		{
			player.sendMessage(player.isLangRus() ? "Сохранение закладок в бою невозможно!" : "Bookmark in combat can not be!");
			return;
		}

		if(TpNameAdd.equals("") || TpNameAdd.equals(null))
		{
			player.sendMessage(player.isLangRus() ? "Вы не ввели имя закладки!" : "You have not entered the name of a bookmark!");
			return;
		}
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=?;");
			st.setLong(1, player.getObjectId());
			ResultSet rs = st.executeQuery();
			rs.next();
			if(rs.getInt(1) <= Config.BBS_PVP_TELEPORT_MAX_POINT_COUNT - 1)
			{
				PreparedStatement st1 = con.prepareStatement("SELECT COUNT(*) FROM comteleport WHERE charId=? AND name=?;");
				st1.setLong(1, player.getObjectId());
				st1.setString(2, TpNameAdd);
				ResultSet rs1 = st1.executeQuery();
				rs1.next();
				if(rs1.getInt(1) == 0)
				{
					PreparedStatement stAdd = con.prepareStatement("INSERT INTO comteleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
					stAdd.setInt(1, player.getObjectId());
					stAdd.setInt(2, player.getX());
					stAdd.setInt(3, player.getY());
					stAdd.setInt(4, player.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
					DbUtils.closeQuietly(stAdd);
				}
				else
				{
					PreparedStatement stAdd = con.prepareStatement("UPDATE comteleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
					stAdd.setInt(1, player.getObjectId());
					stAdd.setInt(2, player.getX());
					stAdd.setInt(3, player.getY());
					stAdd.setInt(4, player.getZ());
					stAdd.setString(5, TpNameAdd);
					stAdd.execute();
					DbUtils.closeQuietly(stAdd);
				}
			}
			else
			{
			player.sendMessage(player.isLangRus() ? "Вы не можете сохранить более " + Config.BBS_PVP_TELEPORT_MAX_POINT_COUNT + " закладок" : "You can not save more than "+ Config.BBS_PVP_TELEPORT_MAX_POINT_COUNT +" bookmarks ");
				return;
			}
			DbUtils.closeQuietly(st, rs);

		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}
}