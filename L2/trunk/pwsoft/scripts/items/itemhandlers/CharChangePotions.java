package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.appearance.PcAppearance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUser;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import scripts.items.IItemHandler;

public class CharChangePotions
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 5235, 5236, 5237, 5238, 5239, 5240, 5241, 5242, 5243, 5244, 5245, 5246, 5247, 5248 };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    L2PcInstance player = null;
    if (playable.isPlayer())
      player = (L2PcInstance)playable;
    else if (playable.isPet()) {
      player = ((L2PetInstance)playable).getOwner();
    }
    if (player == null) {
      return;
    }
    if (player.isAllSkillsDisabled())
    {
      player.sendActionFailed();
      return;
    }

    switch (item.getItemId())
    {
    case 5235:
      player.getAppearance().setFace(0);
      break;
    case 5236:
      player.getAppearance().setFace(1);
      break;
    case 5237:
      player.getAppearance().setFace(2);
      break;
    case 5238:
      player.getAppearance().setHairColor(0);
      break;
    case 5239:
      player.getAppearance().setHairColor(1);
      break;
    case 5240:
      player.getAppearance().setHairColor(2);
      break;
    case 5241:
      player.getAppearance().setHairColor(3);
      break;
    case 5242:
      player.getAppearance().setHairStyle(0);
      break;
    case 5243:
      player.getAppearance().setHairStyle(1);
      break;
    case 5244:
      player.getAppearance().setHairStyle(2);
      break;
    case 5245:
      player.getAppearance().setHairStyle(3);
      break;
    case 5246:
      player.getAppearance().setHairStyle(4);
      break;
    case 5247:
      player.getAppearance().setHairStyle(5);
      break;
    case 5248:
      player.getAppearance().setHairStyle(6);
    }

    player.broadcastPacket(new MagicSkillUser(playable, player, 2003, 1, 1, 0));

    player.store();

    player.destroyItem("Consume", item.getObjectId(), 1, null, false);

    player.broadcastPacket(new UserInfo(player));
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}