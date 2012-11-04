package services.community;

import javolution.text.TextBuilder;
import l2r.gameserver.Config;
import l2r.gameserver.ServicesConfig;
import l2r.gameserver.data.htm.HtmCache;
import l2r.gameserver.handler.bbs.CommunityBoardManager;
import l2r.gameserver.handler.bbs.ICommunityBoardHandler;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.base.Experience;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.components.SystemMsg;
import l2r.gameserver.scripts.Functions;
import l2r.gameserver.scripts.ScriptFile;
import l2r.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServicesCommunity extends Functions implements ScriptFile, ICommunityBoardHandler {

    static final Logger _log = LoggerFactory.getLogger(ServicesCommunity.class);
    String NameItemPice = ItemFunctions.createItem(ServicesConfig.get("LevelUpItemPice", 4357)).getName();

    @Override
    public void onLoad() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            _log.info("ServicesCommunity: Services Community service loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public void onReload() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            CommunityBoardManager.getInstance().removeHandler(this);
        }
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]
                {
                        "_bbsservices",
                        "_bbsservices:level",
                        "_bbsservices:level:up"
                };
    }

    @Override
    public void onBypassCommand(Player player, String bypass) 
    {
		
    	if(!checkCondition(player))
			return;
    	
        if (!ServicesConfig.get("LevelUpEnable", false)) 
        {
            show("Сервис отключен.", player);
            return;
        }
        

        if (bypass.startsWith("_bbsservices:level")) {
            String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "pages/pages/content.htm", player);

            TextBuilder _content = new TextBuilder();
            _content.append("<table width=400><tr><td align=center> Сервис повышения уровня. </td></tr></table>");
            _content.append("<table border=0 width=400><tr>");
            int LvList[] = ServicesConfig.get("LevelUpList", new int[]{});
            int LvPiceList[] = ServicesConfig.get("LevelUpPiceList", new int[]{});
            for (int i = 0; i < LvList.length; i++) {
                if (LvList[i] > player.getLevel()) {
                    if (i % 4 == 0)
                        _content.append("</tr><tr>");
                    _content.append("<td><center><button value=\"На " + LvList[i] + " (Цена:" + LvPiceList[i] + " " + NameItemPice + ")\" action=\"bypass _bbsservices:level:up:" + LvList[i] + ":" + LvPiceList[i] + "\" width=180 height=20 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></center></td>");
                }
            }
            _content.append("</tr></table>");
            html = html.replace("%content%", _content.toString());
            ShowBoard.separateAndSend(html, player);
        }
        if (bypass.startsWith("_bbsservices:level:up")) {
            String var[] = bypass.split(":");
            if (player.getInventory().destroyItemByItemId(ServicesConfig.get("LevelUpItemPice", 4357), Integer.parseInt(var[4])))
                player.addExpAndSp(Experience.LEVEL[Integer.parseInt(var[3])] - player.getExp(), 0);
            else
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            onBypassCommand(player, "_bbsservices:level");
        }
    }


	public boolean checkCondition(Player player)
	{
		if(/*player.isInJail() ||*/player.getReflectionId() != 0 || player.isDead() || player.isAlikeDead() || player.isCastingNow() || player.isInCombat() || player.isAttackingNow() || player.isInOlympiadMode() || player.isFlying())
		{
			player.sendMessage("Повышение уровня не возможно");
			return false;
		}
		return false;
	}
	

	
    @Override
    public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5) {
    }

}