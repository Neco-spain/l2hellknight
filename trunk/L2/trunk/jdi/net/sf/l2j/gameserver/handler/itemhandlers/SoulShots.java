package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.util.Broadcast;

public class SoulShots
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5789, 1835, 1463, 1464, 1465, 1466, 1467 };
  private static final int[] SKILL_IDS = { 2039, 2150, 2151, 2152, 2153, 2154 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) return;

    L2PcInstance activeChar = (L2PcInstance)playable;
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    L2Weapon weaponItem = activeChar.getActiveWeaponItem();
    int itemId = item.getItemId();

    if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
    {
      if (!activeChar.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
        activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_USE_SOULSHOTS));
      return;
    }

    int weaponGrade = weaponItem.getCrystalType();
    if (((weaponGrade == 0) && (itemId != 5789) && (itemId != 1835)) || ((weaponGrade == 1) && (itemId != 1463)) || ((weaponGrade == 2) && (itemId != 1464)) || ((weaponGrade == 3) && (itemId != 1465)) || ((weaponGrade == 4) && (itemId != 1466)) || ((weaponGrade == 5) && (itemId != 1467)))
    {
      if (!activeChar.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
        activeChar.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_GRADE_MISMATCH));
      return;
    }

    activeChar.soulShotLock.lock();
    try
    {
      if (weaponInst.getChargedSoulshot() != 0)
        return;
      int saSSCount = (int)activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0.0D, null, null);
      int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;

      if (!Config.NOT_CONSUME_SHOTS)
      {
        if (!activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), SSCount, null, false))
        {
          if (activeChar.getAutoSoulShot().containsKey(Integer.valueOf(itemId)))
          {
            activeChar.removeAutoSoulShot(itemId);
            activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));

            SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
            sm.addString(item.getItem().getName());
            activeChar.sendPacket(sm);
          } else {
            activeChar.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS));
          }return;
        }
      }
      weaponInst.setChargedSoulshot(1);
    }
    finally
    {
      activeChar.soulShotLock.unlock();
    }

    activeChar.sendPacket(new SystemMessage(SystemMessageId.ENABLED_SOULSHOT));
    if (Config.NOT_CONSUME_SHOTS)
    {
      if (activeChar.getActiveTradeList() != null) {
        activeChar.cancelActiveTrade();
      }
    }
    Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponGrade], 1, 0, 0), 360000L);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}