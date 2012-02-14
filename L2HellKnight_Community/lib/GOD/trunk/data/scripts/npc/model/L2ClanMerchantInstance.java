package npc.model;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2ClanMerchantInstance extends L2NpcInstance
{

    public L2ClanMerchantInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void showChatWindow(L2Player player, int val)
    {
        if(player.getClanId() != 0)
            showChatWindow(player, (new StringBuilder()).append("data/html/default/").append(getNpcId()).append(".htm").toString());
        else
            showChatWindow(player, (new StringBuilder()).append("data/html/default/").append(getNpcId()).append("-noclan.htm").toString());
    }
}
