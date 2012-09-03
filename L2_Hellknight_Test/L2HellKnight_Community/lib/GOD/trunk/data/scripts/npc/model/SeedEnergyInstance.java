package npc.model;

import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.templates.L2NpcTemplate;

public final class SeedEnergyInstance extends L2NpcInstance
{

    public SeedEnergyInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    public void onAction(L2Player player, boolean dontMove)
    {
        player.setLastNpc(this);
        if(this != player.getTarget())
        {
            player.setTarget(this);
            player.sendPacket(new L2GameServerPacket[] {
                new MyTargetSelected(getObjectId(), player.getLevel() - getLevel())
            });
            if(isAutoAttackable(player))
            {
                StatusUpdate su = new StatusUpdate(getObjectId());
                su.addAttribute(9, (int)getCurrentHp());
                su.addAttribute(10, getMaxHp());
                player.sendPacket(new L2GameServerPacket[] {
                    su
                });
            }
            player.sendPacket(new L2GameServerPacket[] {
                new ValidateLocation(this)
            });
            player.sendActionFailed();
            return;
        } else
        {
            player.sendActionFailed();
            return;
        }
    }
}
