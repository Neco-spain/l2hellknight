package services.community;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

import l2r.commons.dbutils.DbUtils;
import l2r.gameserver.Config;
import l2r.gameserver.cache.Msg;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.data.xml.holder.BuyListHolder;
import l2r.gameserver.data.xml.holder.BuyListHolder.NpcTradeList;
import l2r.gameserver.data.xml.holder.MultiSellHolder;
import l2r.gameserver.database.DatabaseFactory;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.scripts.Scripts;
import l2r.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunityBoard implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBoard.class);

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: service loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbshome", "_bbslink", "_bbsmultisell", "_bbssell", "_bbsaugment", "_bbsdeaugment", "_bbspage", "_bbsscripts" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";
		if("bbshome".equals(cmd))
		{
			StringTokenizer p = new StringTokenizer(Config.BBS_DEFAULT, "_");
			String dafault = p.nextToken();
			if(dafault.equals(cmd))
			{
				html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/index.htm", player);

				int favCount = 0;
				Connection con = null;
				PreparedStatement statement = null;
				ResultSet rset = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("SELECT count(*) as cnt FROM `bbs_favorites` WHERE `object_id` = ?");
					statement.setInt(1, player.getObjectId());
					rset = statement.executeQuery();
					if(rset.next())
						favCount = rset.getInt("cnt");
				}
				catch(Exception e)
				{}
				finally
				{
					DbUtils.closeQuietly(con, statement, rset);
				}

				html = html.replace("<?fav_count?>", String.valueOf(favCount));
				html = html.replace("<?clan_count?>", String.valueOf(ClanTable.getInstance().getClans().length));
				html = html.replace("<?market_count?>", String.valueOf(CommunityBoardManager.getInstance().getIntProperty("col_count")));
			}
			else
			{
				onBypassCommand(player, Config.BBS_DEFAULT);
				return;
			}
		}
		else if("bbslink".equals(cmd))
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/index.htm", player);
		else if(bypass.startsWith("_bbspage"))
		{
			String[] b = bypass.split(":");
			String page = b[1];
			html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/" + page + ".htm", player);
		}
		else if(Config.BBS_PVP_ALLOW_BUY && bypass.startsWith("_bbsmultisell"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String[] mBypass = st2.nextToken().split(":");
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			int listId = Integer.parseInt(mBypass[1]);
			MultiSellHolder.getInstance().SeparateAndSend(listId, player, 0);
			return;
		}
		else if(Config.BBS_PVP_ALLOW_SELL && bypass.startsWith("_bbssell"))
		{
            StringTokenizer st2 = new StringTokenizer(bypass, ";");
            st2.nextToken();
            String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
            if(pBypass != null)
            {
                ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
                if(handler != null)
                    handler.onBypassCommand(player, pBypass);
            }
			NpcTradeList list = BuyListHolder.getInstance().getBuyList(-1);
			player.sendPacket(new ExBuySellList.BuyList(list, player, 0.), new ExBuySellList.SellRefundList(player, false));
			return;
		}
		else if (bypass.startsWith("_bbsaugment")) 
		{
		if(Config.BBS_PVP_ALLOW_AUGMENT)
                player.sendPacket(Msg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
            else
			player.sendMessage(player.isLangRus() ? "Функция переплавления отключена администрацией." : "Augmentation function disabled by an administrator.!");
            return;
        } 
		else if (bypass.startsWith("_bbsdeaugment")) 
		{
		if(Config.BBS_PVP_ALLOW_AUGMENT)
                player.sendPacket(Msg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
            else
			player.sendMessage(player.isLangRus() ? "Функция переплавления отключена администрацией." : "Augmentation function disabled by an administrator.!");
            return;
        }
		else if(bypass.startsWith("_bbsscripts"))
		{
			StringTokenizer st2 = new StringTokenizer(bypass, ";");
			String sBypass = st2.nextToken().substring(12);
			String pBypass = st2.hasMoreTokens() ? st2.nextToken() : null;
			if(pBypass != null)
			{
				ICommunityBoardHandler handler = CommunityBoardManager.getInstance().getCommunityHandler(pBypass);
				if(handler != null)
					handler.onBypassCommand(player, pBypass);
			}

			String[] word = sBypass.split("\\s+");
			String[] args = sBypass.substring(word[0].length()).trim().split("\\s+");
			String[] path = word[0].split(":");
			if(path.length != 2)
				return;

			Scripts.getInstance().callScripts(player, path[0], path[1], word.length == 1 ? new Object[] {} : new Object[] { args });
			return;
		}

		ShowBoard.separateAndSend(html, player);
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}
}
