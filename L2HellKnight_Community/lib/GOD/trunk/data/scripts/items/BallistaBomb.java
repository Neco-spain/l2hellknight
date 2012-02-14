package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.tables.SkillTable;
import npc.model.L2BallistaInstance;

public abstract class BallistaBomb
    implements IItemHandler, ScriptFile
{

    public BallistaBomb()
    {
    }

    public void useItem(L2Playable playable, L2ItemInstance item)
    {
        if(playable.isCastingNow() || playable.isAttackingNow())
            return;
        if(playable != null)
        {
            L2Player player = null;
            if(playable instanceof L2Summon)
            {
                player = ((L2Summon)playable).getPlayer();
                player.sendPacket(new L2GameServerPacket[] {
                    Msg.YOU_CANNOT_SUMMON_A_BASE_BECAUSE_YOU_ARE_NOT_IN_BATTLE
                });
            } else
            if(playable instanceof L2Player)
            {
                player = (L2Player)playable;
                if(player.getSiegeState() == 0)
                    player.sendActionFailed();
                else
                if(player.getTarget() instanceof L2BallistaInstance)
                {
                    L2BallistaInstance trg = (L2BallistaInstance)player.getTarget();
                    if(trg.isDead())
                        return;
                    l2rt.gameserver.model.L2Skill skill = SkillTable.getInstance().getInfo(2342, 1);
                    player.doCast(skill, (L2Character)player.getTarget(), false);
                } else
                {
                    player.sendPacket(new L2GameServerPacket[] {
                        Msg.THAT_IS_THE_INCORRECT_TARGET
                    });
                }
            }
        }
    }

    public int[] getItemIds()
    {
        return ITEM_IDS;
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

    private static final int ITEM_IDS[] = {
        9688
    };
    @SuppressWarnings("unused")
	private static final int BALLISTA_BOMB_SKILL_ID = 2342;
    @SuppressWarnings("unused")
	private static final int BALLISTA_BOMB_SKILL_LEVEL = 1;

}
