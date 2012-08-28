package scripts.items.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.templates.L2Weapon;
import scripts.items.IItemHandler;

public class SpiritShot
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5790, 2509, 2510, 2511, 2512, 2513, 2514 };
  private static final int[] SKILL_IDS = { 2061, 2155, 2156, 2157, 2158, 2159 };

  private boolean incorrectGrade(int weaponGrade, int itemId)
  {
    return ((weaponGrade == 0) && (itemId != 5790) && (itemId != 2509)) || ((weaponGrade == 1) && (itemId != 2510)) || ((weaponGrade == 2) && (itemId != 2511)) || ((weaponGrade == 3) && (itemId != 2512)) || ((weaponGrade == 4) && (itemId != 2513)) || ((weaponGrade == 5) && (itemId != 2514));
  }

  public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }

    L2PcInstance activeChar = playable.getPlayer();
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    L2Weapon weaponItem = activeChar.getActiveWeaponItem();
    int itemId = item.getItemId();

    if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
    {
      return;
    }

    if (weaponInst.getChargedSpiritshot() != 0) {
      return;
    }

    if (incorrectGrade(weaponItem.getCrystalType(), itemId)) {
      return;
    }

    if ((Config.USE_SOULSHOTS) && (!activeChar.destroyItemByItemId("Consume", itemId, weaponItem.getSpiritShotCount(), activeChar, false))) {
      activeChar.removeAutoSoulShot(itemId);
      activeChar.sendPacket(new ExAutoSoulShot(itemId, 0));
      activeChar.sendPacket(Static.NOT_ENOUGH_SPIRITSHOTS);
      return;
    }

    if (activeChar.showSoulShotsAnim()) {
      activeChar.sendPacket(Static.ENABLED_SPIRITSHOT);
    }

    weaponInst.setChargedSpiritshot(1);

    activeChar.broadcastSoulShotsPacket(new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponItem.getCrystalType()], 1, 0, 0));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}