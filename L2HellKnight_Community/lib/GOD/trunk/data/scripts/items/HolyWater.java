package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.L2Playable;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import npc.model.HellboundRemnantInstance;

public class HolyWater
    implements IItemHandler, ScriptFile
{

    public HolyWater()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance _item, Boolean ctrl)
    {
        if(playable == null)
            return;
        if(playable.getTarget() == null || !(playable.getTarget() instanceof HellboundRemnantInstance))
        {
            playable.sendPacket(new L2GameServerPacket[] {
                Msg.THAT_IS_THE_INCORRECT_TARGET
            });
            return;
        }
        HellboundRemnantInstance target = (HellboundRemnantInstance)playable.getTarget();
        if(target.isDead() || target.isDying())
        {
            playable.sendPacket(new L2GameServerPacket[] {
                Msg.THAT_IS_THE_INCORRECT_TARGET
            });
            return;
        } else
        {
            playable.broadcastPacket(new L2GameServerPacket[] {
                new MagicSkillUse(playable, target, 2358, 1, 0, 0L)
            });
            target.onUseHolyWater(playable);
            return;
        }
    }

    public final int[] getItemIds()
    {
        return _itemIds;
    }

    public void onLoad()
    {
        ItemHandler.getInstance().registerItemHandler(this);
    }

    public void onReload()
    {
    }

    public void onShutdown()
    {
    }

    private static final int _itemIds[] = {
        9673
    };
    L2Player player;
    L2MonsterInstance target;

}
