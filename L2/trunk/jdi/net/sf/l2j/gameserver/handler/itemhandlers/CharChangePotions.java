package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public class CharChangePotions
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5235, 5236, 5237, 5238, 5239, 5240, 5241, 5242, 5243, 5244, 5245, 5246, 5247, 5248 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    int itemId = item.getItemId();
    L2PcInstance activeChar;
    if ((playable instanceof L2PcInstance)) {
      activeChar = (L2PcInstance)playable;
    }
    else
    {
      L2PcInstance activeChar;
      if ((playable instanceof L2PetInstance))
        activeChar = ((L2PetInstance)playable).getOwner();
      else
        return;
    }
    L2PcInstance activeChar;
    if (activeChar.isAllSkillsDisabled())
    {
      ActionFailed af = new ActionFailed();
      activeChar.sendPacket(af);
      return;
    }

    switch (itemId) {
    case 5235:
      activeChar.getAppearance().setFace(0);
      break;
    case 5236:
      activeChar.getAppearance().setFace(1);
      break;
    case 5237:
      activeChar.getAppearance().setFace(2);
      break;
    case 5238:
      activeChar.getAppearance().setHairColor(0);
      break;
    case 5239:
      activeChar.getAppearance().setHairColor(1);
      break;
    case 5240:
      activeChar.getAppearance().setHairColor(2);
      break;
    case 5241:
      activeChar.getAppearance().setHairColor(3);
      break;
    case 5242:
      activeChar.getAppearance().setHairStyle(0);
      break;
    case 5243:
      activeChar.getAppearance().setHairStyle(1);
      break;
    case 5244:
      activeChar.getAppearance().setHairStyle(2);
      break;
    case 5245:
      activeChar.getAppearance().setHairStyle(3);
      break;
    case 5246:
      activeChar.getAppearance().setHairStyle(4);
      break;
    case 5247:
      activeChar.getAppearance().setHairStyle(5);
      break;
    case 5248:
      activeChar.getAppearance().setHairStyle(6);
    }

    MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, 2003, 1, 1, 0);
    activeChar.broadcastPacket(MSU);

    activeChar.store();

    activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);

    UserInfo ui = new UserInfo(activeChar);
    activeChar.broadcastPacket(ui);
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}