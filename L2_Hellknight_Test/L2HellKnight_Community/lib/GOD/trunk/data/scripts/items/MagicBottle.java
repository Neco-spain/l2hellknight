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

public class MagicBottle
    implements IItemHandler, ScriptFile
{

    public MagicBottle()
    {
    }

    @SuppressWarnings("unchecked")
	public void useItem(L2Playable playable, L2ItemInstance item)
    {
        if(playable == null || !playable.isPlayer())
            return;
        L2Player activeChar = (L2Player)playable;
        if(activeChar.getTarget() == null || !(activeChar.getTarget() instanceof L2NpcInstance))
        {
            activeChar.sendPacket(new L2GameServerPacket[] {
                new SystemMessage(144)
            });
            return;
        }
        L2NpcInstance targetNPC = (L2NpcInstance)activeChar.getTarget();
        int npcId = targetNPC.getNpcId();
        if(npcId < 22349 && npcId > 22353)
        {
            activeChar.sendPacket(new L2GameServerPacket[] {
                new SystemMessage(144)
            });
            return;
        }
        if(item.getItemId() == 9672)
        {
            L2Skill sl_skill = SkillTable.getInstance().getInfo(2359, 1);
            if(sl_skill != null)
            {
                activeChar.getInventory().destroyItemByItemId(9672, 1L, false);
                GArray targets = new GArray();
                targets.add(targetNPC);
                sl_skill.useSkill(activeChar, targets);
                activeChar.broadcastPacket(new L2GameServerPacket[] {
                    new MagicSkillUse(activeChar, 1085, 1, 1000, 500L)
                });
                if(targetNPC.hasAI())
                    targetNPC.getAI().notifyEvent(CtrlEvent.EVT_SEE_SPELL, sl_skill, activeChar);
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

    public void useItem(L2Playable l2playable, L2ItemInstance l2iteminstance, Boolean boolean1)
    {
    }

    private static final int MAGIC_BOTTLE_ITEM = 9672;
    private static final int ITEM_IDS[] = {
        9672
    };
    private static final int MAGIC_BOTTLE_SKILL = 2359;

}
