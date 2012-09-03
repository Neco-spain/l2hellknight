package items;

import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.handler.IItemHandler;
import l2rt.gameserver.handler.ItemHandler;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;

public abstract class SpiritLake
    implements IItemHandler, ScriptFile
{

    public SpiritLake()
    {
    }

    @SuppressWarnings("unchecked")
	public void useItem(L2Playable playable, L2ItemInstance item)
    {
        if(playable == null || !playable.isPlayer())
            return;
        L2Player activeChar = (L2Player)playable;
        if(activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2NpcInstance))
            return;
        L2NpcInstance target = (L2NpcInstance)activeChar.getTarget();
        if(target.getNpcId() != 18482)
        {
            activeChar.sendPacket(new L2GameServerPacket[] {
                new SystemMessage(144)
            });
            return;
        }
        if(item.getItemId() == 9689)
        {
            L2Skill sl_skill = SkillTable.getInstance().getInfo(2368, 1);
            if(sl_skill != null)
            {
                activeChar.getInventory().destroyItemByItemId(9689, 1L, false);
                GArray targets = new GArray();
                targets.add(target);
                activeChar.broadcastPacket(new L2GameServerPacket[] {
                    new MagicSkillUse(activeChar, 1011, 1, 1000, 500L)
                });
                activeChar.broadcastPacket(new L2GameServerPacket[] {
                    new MagicSkillLaunched(activeChar.getObjectId(), 1011, 1, targets, false)
                });
                sl_skill.useSkill(activeChar, targets);
                if(target.hasAI())
                    target.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, sl_skill, activeChar);
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

    @SuppressWarnings("unused")
	private static final int FAFURION = 18482;
    @SuppressWarnings("unused")
	private static final int SPIRIT_OF_THE_LAKE = 9689;
    @SuppressWarnings("unused")
	private static final int SPIRIT_OF_THE_LAKE_SKILL = 2368;
    private static final int ITEM_IDS[] = {
        9689
    };

}
