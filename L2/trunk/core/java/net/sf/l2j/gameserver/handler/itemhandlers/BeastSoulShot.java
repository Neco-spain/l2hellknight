package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Summon;
import net.sf.l2j.gameserver.model.actor.instance.L2BabyPetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Broadcast;

public class BeastSoulShot implements IItemHandler
{
    // All the item IDs that this handler knows.
    private static final int[] ITEM_IDS = { 6645 };

    public void useItem(L2PlayableInstance playable, L2ItemInstance item)
    {
    	if (playable == null) return;

        L2PcInstance activeOwner = null;
        if (playable instanceof L2Summon)
        {
            activeOwner = ((L2Summon)playable).getOwner();
            activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_USE_ITEM));
            return;
        } else if (playable instanceof L2PcInstance)
        {
        	activeOwner = (L2PcInstance)playable;
        }

        if (activeOwner == null)
        	return;
        L2Summon activePet = activeOwner.getPet();

        if (activePet == null)
        {
            activeOwner.sendPacket(new SystemMessage(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME));
            return;
        }

        if (activePet.isDead())
        {
            activeOwner.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET));
            return;
        }

        int itemId = 6645;
        int shotConsumption = 1;
        L2ItemInstance weaponInst = null;
        L2Weapon weaponItem = null;

        if ((activePet instanceof L2PetInstance) && !(activePet instanceof L2BabyPetInstance))
        {
            weaponInst = ((L2PetInstance)activePet).getActiveWeaponInstance();
            weaponItem = ((L2PetInstance)activePet).getActiveWeaponItem();

            if (weaponInst == null)
            {
                activeOwner.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SOULSHOTS));
                return;
            }

            if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
            {
                // SoulShots are already active.
                return;
            }

            int shotCount = item.getCount();
            shotConsumption = weaponItem.getSoulShotCount();

            if (shotConsumption == 0)
            {
                activeOwner.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SOULSHOTS));
                return;
            }

            if (!(shotCount > shotConsumption))
            {
                // Not enough Soulshots to use.
                activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
                return;
            }

            weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
        }
        else
        {
            if (activePet.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
                return;

            activePet.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT);
        }

        // If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!Config.NOT_CONSUME_SHOTS)
		{
			if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
			{
				if (activeOwner.getAutoSoulShot().containsKey(itemId))
				{
					activeOwner.removeAutoSoulShot(itemId);
					activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));

					SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
					sm.addString(item.getItem().getName());
					activeOwner.sendPacket(sm);
					return;
				}

				activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS));
				return;
			}
			if (Config.NOT_CONSUME_SHOTS)
		    {
		      if (activeOwner.getActiveTradeList() != null) {
		        activeOwner.cancelActiveTrade();
		      }
		    }
		}

        // Pet uses the power of spirit.
        activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT));

        Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUser(activePet, activePet, 2033, 1, 0, 0), 360000/*600*/);
    }

    public int[] getItemIds()
    {
        return ITEM_IDS;
    }
}
