package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.Map;
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
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Broadcast;

public class BeastSpiritShot
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 6646, 6647 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (playable == null) return;

    L2PcInstance activeOwner = null;
    if ((playable instanceof L2Summon))
    {
      activeOwner = ((L2Summon)playable).getOwner();
      activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_USE_ITEM));
      return;
    }if ((playable instanceof L2PcInstance))
    {
      activeOwner = (L2PcInstance)playable;
    }

    if (activeOwner == null)
      return;
    L2Summon activePet = activeOwner.getPet();

    if (activeOwner.isInOlympiadMode())
    {
      if (item.getItemId() == 6647)
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
        sm.addString(item.getItemName());
        activeOwner.sendPacket(sm);
        sm = null;
        return;
      }
    }

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

    int itemId = item.getItemId();
    boolean isBlessed = itemId == 6647;
    int shotConsumption = 1;

    L2ItemInstance weaponInst = null;
    L2Weapon weaponItem = null;

    if (((activePet instanceof L2PetInstance)) && (!(activePet instanceof L2BabyPetInstance)))
    {
      weaponInst = ((L2PetInstance)activePet).getActiveWeaponInstance();
      weaponItem = ((L2PetInstance)activePet).getActiveWeaponItem();

      if (weaponInst == null)
      {
        activeOwner.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
        return;
      }

      if (weaponInst.getChargedSpiritshot() != 0)
      {
        return;
      }

      int shotCount = item.getCount();
      shotConsumption = weaponItem.getSpiritShotCount();

      if (shotConsumption == 0)
      {
        activeOwner.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
        return;
      }

      if (shotCount <= shotConsumption)
      {
        activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET));
        return;
      }

      if (isBlessed)
        weaponInst.setChargedSpiritshot(2);
      else
        weaponInst.setChargedSpiritshot(1);
    }
    else
    {
      if (activePet.getChargedSpiritShot() != 0) {
        return;
      }
      if (isBlessed)
        activePet.setChargedSpiritShot(2);
      else {
        activePet.setChargedSpiritShot(1);
      }
    }
    if (!Config.NOT_CONSUME_SHOTS)
    {
      if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
      {
        if (activeOwner.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
        {
          activeOwner.removeAutoSoulShot(itemId);
          activeOwner.sendPacket(new ExAutoSoulShot(itemId, 0));

          SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
          sm.addString(item.getItem().getName());
          activeOwner.sendPacket(sm);
          return;
        }

        activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
        return;
      }
    }
    if (Config.NOT_CONSUME_SHOTS)
    {
      if (activeOwner.getActiveTradeList() != null) {
        activeOwner.cancelActiveTrade();
      }
    }
    activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT));

    Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUser(activePet, activePet, isBlessed ? 2009 : 2008, 1, 0, 0), 360000L);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}