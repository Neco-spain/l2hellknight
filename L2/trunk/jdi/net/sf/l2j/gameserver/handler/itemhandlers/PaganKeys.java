package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Object;
import net.sf.l2j.gameserver.model.actor.instance.L2DoorInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.util.Rnd;

public class PaganKeys
  implements IItemHandler
{
  private static final int[] ITEM_IDS = { 8273, 8274, 8275 };
  public static final int INTERACTION_DISTANCE = 100;

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    int itemId = item.getItemId();
    if (!(playable instanceof L2PcInstance)) return;
    L2PcInstance activeChar = (L2PcInstance)playable;
    L2Object target = activeChar.getTarget();

    if (!(target instanceof L2DoorInstance))
    {
      activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    L2DoorInstance door = (L2DoorInstance)target;

    if (!activeChar.isInsideRadius(door, 100, false, false))
    {
      activeChar.sendMessage("Too far.");
      activeChar.sendPacket(new ActionFailed());
      return;
    }
    if ((activeChar.getAbnormalEffect() > 0) || (activeChar.isInCombat()))
    {
      activeChar.sendMessage("You cannot use the key now.");
      activeChar.sendPacket(new ActionFailed());
      return;
    }

    int openChance = 35;

    if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, false)) return;

    switch (itemId) {
    case 8273:
      if (door.getDoorName().startsWith("Anteroom")) {
        if ((openChance > 0) && (Rnd.get(100) < openChance)) {
          activeChar.sendMessage("You opened Anterooms Door.");
          door.openMe();
          door.onOpen();
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
        }
        else
        {
          activeChar.sendMessage("You failed to open Anterooms Door.");
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          PlaySound playSound = new PlaySound("interfacesound.system_close_01");
          activeChar.sendPacket(playSound);
        }
      }
      else {
        activeChar.sendMessage("Incorrect Door.");
      }
      break;
    case 8274:
      if (door.getDoorName().startsWith("Altar_Entrance")) {
        if ((openChance > 0) && (Rnd.get(100) < openChance)) {
          activeChar.sendMessage("You opened Altar Entrance.");
          door.openMe();
          door.onOpen();
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
        }
        else {
          activeChar.sendMessage("You failed to open Altar Entrance.");
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          PlaySound playSound = new PlaySound("interfacesound.system_close_01");
          activeChar.sendPacket(playSound);
        }
      }
      else {
        activeChar.sendMessage("Incorrect Door.");
      }
      break;
    case 8275:
      if (door.getDoorName().startsWith("Door_of_Darkness")) {
        if ((openChance > 0) && (Rnd.get(100) < openChance)) {
          activeChar.sendMessage("You opened Door of Darkness.");
          door.openMe();
          door.onOpen();
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 3));
        }
        else {
          activeChar.sendMessage("You failed to open Door of Darkness.");
          activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), 13));
          PlaySound playSound = new PlaySound("interfacesound.system_close_01");
          activeChar.sendPacket(playSound);
        }
      }
      else
        activeChar.sendMessage("Incorrect Door.");
    }
  }

  public int[] getItemIds()
  {
    return ITEM_IDS;
  }
}