package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Weapon;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.gameserver.util.Broadcast;

public class FishShots
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 6535, 6536, 6537, 6538, 6539, 6540 };
  private static final int[] SKILL_IDS = { 2181, 2182, 2183, 2184, 2185, 2186 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    if (!(playable instanceof L2PcInstance)) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
    L2Weapon weaponItem = activeChar.getActiveWeaponItem();

    if ((weaponInst == null) || (weaponItem.getItemType() != L2WeaponType.ROD))
    {
      return;
    }

    if (weaponInst.getChargedFishshot())
    {
      return;
    }

    int FishshotId = item.getItemId();
    int grade = weaponItem.getCrystalType();
    int count = item.getCount();

    if (((grade == 0) && (FishshotId != 6535)) || ((grade == 1) && (FishshotId != 6536)) || ((grade == 2) && (FishshotId != 6537)) || ((grade == 3) && (FishshotId != 6538)) || ((grade == 4) && (FishshotId != 6539)) || ((grade == 5) && (FishshotId != 6540)))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.WRONG_FISHINGSHOT_GRADE));
      return;
    }

    if (count < 1)
    {
      return;
    }

    weaponInst.setChargedFishshot(true);
    activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
    L2Object oldTarget = activeChar.getTarget();
    activeChar.setTarget(activeChar);

    MagicSkillUser MSU = new MagicSkillUser(activeChar, SKILL_IDS[grade], 1, 0, 0);
    Broadcast.toSelfAndKnownPlayers(activeChar, MSU);
    activeChar.setTarget(oldTarget);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}