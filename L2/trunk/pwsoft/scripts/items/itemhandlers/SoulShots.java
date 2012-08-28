package scripts.items.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.actor.stat.PcStat;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.gameserver.templates.L2Weapon;
import scripts.items.IItemHandler;

public class SoulShots
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5789, 1835, 1463, 1464, 1465, 1466, 1467 };
  private static final int[] SKILL_IDS = { 2039, 2150, 2151, 2152, 2153, 2154 };

  private boolean incorrectGrade(int weaponGrade, int itemId)
  {
    return ((weaponGrade == 0) && (itemId != 5789) && (itemId != 1835)) || ((weaponGrade == 1) && (itemId != 1463)) || ((weaponGrade == 2) && (itemId != 1464)) || ((weaponGrade == 3) && (itemId != 1465)) || ((weaponGrade == 4) && (itemId != 1466)) || ((weaponGrade == 5) && (itemId != 1467));
  }

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }

    L2PcInstance activeChar = playable.getPlayer();
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    L2Weapon weaponItem = activeChar.getActiveWeaponItem();
    int itemId = item.getItemId();

    if ((weaponInst == null) || (weaponItem.getSoulShotCount() == 0))
    {
      return;
    }

    if (weaponInst.getChargedSoulshot() != 0) {
      return;
    }

    if (incorrectGrade(weaponItem.getCrystalType(), itemId)) {
      return;
    }

    int saSSCount = (int)activeChar.getStat().calcStat(Stats.SOULSHOT_COUNT, 0.0D, null, null);
    int SSCount = saSSCount == 0 ? weaponItem.getSoulShotCount() : saSSCount;
    if ((Config.USE_SOULSHOTS) && (!activeChar.destroyItemByItemId("Consume", itemId, SSCount, activeChar, false))) {
      activeChar.removeAutoSoulShot(itemId);
      activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
      activeChar.sendPacket(Static.NOT_ENOUGH_SOULSHOTS);
      return;
    }

    if (activeChar.showSoulShotsAnim()) {
      activeChar.sendPacket(Static.ENABLED_SOULSHOT);
    }

    weaponInst.setChargedSoulshot(1);

    activeChar.broadcastSoulShotsPacket(new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponItem.getCrystalType()], 1, 0, 0));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}