package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.Map;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Broadcast;

public class SpiritShot
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5790, 2509, 2510, 2511, 2512, 2513, 2514 };
  private static final int[] SKILL_IDS = { 2061, 2155, 2156, 2157, 2158, 2159 };

  public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;

    L2PcInstance activeChar = (L2PcInstance)playable;
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    L2Weapon weaponItem = activeChar.getActiveWeaponItem();
    int itemId = item.getItemId();

    if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
    {
      if (!activeChar.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SPIRITSHOTS));
      return;
    }

    if (weaponInst.getChargedSpiritshot() != 0) return;

    int weaponGrade = weaponItem.getCrystalType();
    if (((weaponGrade == 0) && (itemId != 5790) && (itemId != 2509)) || ((weaponGrade == 1) && (itemId != 2510)) || ((weaponGrade == 2) && (itemId != 2511)) || ((weaponGrade == 3) && (itemId != 2512)) || ((weaponGrade == 4) && (itemId != 2513)) || ((weaponGrade == 5) && (itemId != 2514)))
    {
      if (!activeChar.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
        activeChar.sendPacket(new SystemMessage(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH));
      return;
    }

    if (!Config.NOT_CONSUME_SHOTS)
    {
      if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
      {
        if (activeChar.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
        {
          activeChar.removeAutoSoulShot(itemId);
          activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));

          SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
          sm.addString(item.getItem().getName());
          activeChar.sendPacket(sm);
        } else {
          activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS));
        }return;
      }
    }

    if (Config.NOT_CONSUME_SHOTS)
    {
      if (activeChar.getActiveTradeList() != null) {
        activeChar.cancelActiveTrade();
      }
    }
    weaponInst.setChargedSpiritshot(1);

    activeChar.sendPacket(new SystemMessage(SystemMessageId.ENABLED_SPIRITSHOT));
    Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponGrade], 1, 0, 0), 360000L);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}