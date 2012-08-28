package scripts.items.itemhandlers;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.util.Rnd;
import scripts.items.IItemHandler;

public class PaganKeys
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 8273, 8274, 8275 };
  public static final int INTERACTION_DISTANCE = 100;

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    int itemId = item.getItemId();
    if (!playable.isPlayer()) {
      return;
    }
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2Object target = activeChar.getTarget();

    if ((target == null) || (!target.isL2Door())) {
      activeChar.sendPacket(Static.INCORRECT_TARGET);
      activeChar.sendActionFailed();
      return;
    }
    L2DoorInstance door = (L2DoorInstance)target;

    if (!activeChar.isInsideRadius(door, 100, false, false)) {
      activeChar.sendMessage("Too far.");
      activeChar.sendActionFailed();
      return;
    }
    if ((activeChar.getAbnormalEffect() > 0) || (activeChar.isInCombat())) {
      activeChar.sendMessage("You cannot use the key now.");
      activeChar.sendActionFailed();
      return;
    }

    int openChance = 35;

    if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) {
      return;
    }

    switch (itemId) {
    case 8273:
      if (door.getDoorName().startsWith("Anteroom")) {
        if ((openChance > 0) && (Rnd.get(100) < openChance)) {
          activeChar.sendMessage("You opened Anterooms Door.");
          door.openMe();
          door.onOpen();
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
        }
        else {
          activeChar.sendMessage("You failed to open Anterooms Door.");
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          PlaySound playSound = new PlaySound("interfacesound.system_close_01");
          activeChar.sendPacket(playSound);
        }
      }
      else activeChar.sendMessage("Incorrect Door.");

      break;
    case 8274:
      if (door.getDoorName().startsWith("Altar_Entrance")) {
        if ((openChance > 0) && (Rnd.get(100) < openChance)) {
          activeChar.sendMessage("You opened Altar Entrance.");
          door.openMe();
          door.onOpen();
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
        } else {
          activeChar.sendMessage("You failed to open Altar Entrance.");
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          PlaySound playSound = new PlaySound("interfacesound.system_close_01");
          activeChar.sendPacket(playSound);
        }
      }
      else activeChar.sendMessage("Incorrect Door.");

      break;
    case 8275:
      if (door.getDoorName().startsWith("Door_of_Darkness")) {
        if ((openChance > 0) && (Rnd.get(100) < openChance)) {
          activeChar.sendMessage("You opened Door of Darkness.");
          door.openMe();
          door.onOpen();
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
        } else {
          activeChar.sendMessage("You failed to open Door of Darkness.");
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          PlaySound playSound = new PlaySound("interfacesound.system_close_01");
          activeChar.sendPacket(playSound);
        }
      }
      else activeChar.sendMessage("Incorrect Door.");
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}