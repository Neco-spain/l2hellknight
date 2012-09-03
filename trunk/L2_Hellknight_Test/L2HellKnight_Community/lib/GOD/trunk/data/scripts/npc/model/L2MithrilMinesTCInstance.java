package npc.model;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2MithrilMinesTCInstance extends L2NpcInstance
{

    public L2MithrilMinesTCInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void showChatWindow(L2Player player, int val)
    {
        String htmltext = "";
        if(isInRange(0x2a45b, 0xfffd593e, 200))
            htmltext = "-01";
        else
        if(isInRange(0x2c6b5, 0xfffd55ea, 200))
            htmltext = "-02";
        else
        if(isInRange(0x2bd68, 0xfffd3554, 200))
            htmltext = "-03";
        showChatWindow(player, (new StringBuilder()).append("data/html/teleporter/").append(getNpcId()).append(htmltext).append(".htm").toString());
    }

    private boolean isInRange(int i, int j, int interactionDistance)
    {
        return false;
    }
}
