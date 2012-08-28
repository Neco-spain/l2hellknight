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

public class BlessedSpiritShot
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 3947, 3948, 3949, 3950, 3951, 3952 };
  private static final int[] SKILL_IDS = { 2061, 2160, 2161, 2162, 2163, 2164 };

  private boolean incorrectGrade(int weaponGrade, int itemId)
  {
    return ((weaponGrade == 0) && (itemId != 3947)) || ((weaponGrade == 1) && (itemId != 3948)) || ((weaponGrade == 2) && (itemId != 3949)) || ((weaponGrade == 3) && (itemId != 3950)) || ((weaponGrade == 4) && (itemId != 3951)) || ((weaponGrade == 5) && (itemId != 3952));
  }

  public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!playable.isPlayer()) {
      return;
    }

    L2PcInstance activeChar = playable.getPlayer();
    if (activeChar.isInOlympiadMode()) {
      activeChar.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
      return;
    }

    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    L2Weapon weaponItem = activeChar.getActiveWeaponItem();

    if ((weaponInst == null) || (weaponItem.getSpiritShotCount() == 0))
    {
      return;
    }
    int itemId = item.getItemId();

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

    weaponInst.setChargedSpiritshot(2);

    activeChar.broadcastSoulShotsPacket(new MagicSkillUser(activeChar, activeChar, SKILL_IDS[weaponItem.getCrystalType()], 1, 0, 0));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}