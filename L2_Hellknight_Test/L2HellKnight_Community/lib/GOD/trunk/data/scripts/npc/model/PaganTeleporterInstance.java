package npc.model;

import l2rt.extensions.scripts.Functions;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.templates.L2NpcTemplate;
import l2rt.util.Location;

public class PaganTeleporterInstance extends L2NpcInstance
    implements ScriptFile
{

    public PaganTeleporterInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onBypassFeedback(L2Player player, String command)
    {
        if(command.startsWith("PaganTeleport "))
        {
            String xyz = command.replaceFirst("PaganTeleport ", "");
            Location loc;
            try
            {
                loc = new Location(xyz);
            }
            catch(Exception e)
            {
                System.out.println((new StringBuilder()).append("PaganTeleport [").append(xyz).append("] Error! NPC: ").append(getNpcId()).append(" | ").append(player).toString());
                e.printStackTrace();
                return;
            }
            if(player.getInventory().getCountOf(8067) > 0L)
            {
                Functions.removeItem(player, 8067, 0L);
                player.teleToLocation(loc);
                return;
            } else
            {
                showChatWindow(player, 1);
                return;
            }
        } else
        {
            super.onBypassFeedback(player, command);
            return;
        }
    }

    public void onLoad()
    {
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    public static final int Pagan_Mark = 8067;
}
