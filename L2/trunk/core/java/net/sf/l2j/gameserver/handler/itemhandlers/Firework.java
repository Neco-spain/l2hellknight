//L2DDT
package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.util.FloodProtector;

public class Firework implements IItemHandler
{
    private static final int[] ITEM_IDS = { 6403, 6406, 6407 };

    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
    	if(!(playable instanceof L2PcInstance)) return;
        L2PcInstance activeChar = (L2PcInstance)playable;
        int itemId = item.getItemId();

        if (!FloodProtector.getInstance().tryPerformAction(activeChar.getObjectId(), FloodProtector.PROTECTED_FIREWORK))
        {
        	SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
        	sm.addItemName(itemId);
        	activeChar.sendPacket(sm);
        	return;
        }

        if (itemId == 6403) // elven_firecracker, xml: 2023
        {
            MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2023, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            useFw(activeChar, 2023, 1);
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }

        else if (itemId == 6406) // firework, xml: 2024
        {
            MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2024, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            useFw(activeChar, 2024, 1);
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }

        else if (itemId == 6407) // large_firework, xml: 2025
        {
            MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2025, 1, 1, 0);
            activeChar.sendPacket(MSU);
            activeChar.broadcastPacket(MSU);
            useFw(activeChar, 2025, 1);
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }
    }
    public void useFw(L2PcInstance activeChar, int magicId,int level)
    {
        L2Skill skill = SkillTable.getInstance().getInfo(magicId,level);
        if (skill != null) {
            activeChar.useMagic(skill, false, false);
        }
    }
    public int[] getItemIds()
    {
        return ITEM_IDS;
    }
}