package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.MercTicketManager;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;

public class MercTicket
  implements IItemHandler
{
  private static final String[] MESSAGES = { "To arms!.", "I am ready to serve you my lord when the time comes.", "You summon me." };

  public void useItem(L2PlayableInstance playable, L2ItemInstance item)
  {
    int itemId = item.getItemId();
    L2PcInstance activeChar = (L2PcInstance)playable;
    Castle castle = CastleManager.getInstance().getCastle(activeChar);
    int castleId = -1;
    if (castle != null) castleId = castle.getCastleId();

    if (MercTicketManager.getInstance().getTicketCastleId(itemId) != castleId)
    {
      switch (castleId) {
      case 1:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Gludio."); return;
      case 2:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Dion."); return;
      case 3:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Giran."); return;
      case 4:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Oren."); return;
      case 5:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Aden."); return;
      case 6:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Heine."); return;
      case 7:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Goddard."); return;
      case 8:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Rune."); return;
      case 9:
        activeChar.sendMessage("This Mercenary Ticket can only be used in Schuttgart."); return;
      }
      activeChar.sendMessage("Mercenary Tickets can only be used in a castle."); return;
    }

    if (!activeChar.isCastleLord(castleId))
    {
      activeChar.sendMessage("You are not the lord of this castle!");
      return;
    }

    if (castle.getSiege().getIsInProgress())
    {
      activeChar.sendMessage("You cannot hire mercenary while siege is in progress!");
      return;
    }

    if (MercTicketManager.getInstance().isAtCasleLimit(item.getItemId()))
    {
      activeChar.sendMessage("You cannot hire any more mercenaries");
      return;
    }
    if (MercTicketManager.getInstance().isAtTypeLimit(item.getItemId()))
    {
      activeChar.sendMessage("You cannot hire any more mercenaries of this type.  You may still hire other types of mercenaries");
      return;
    }

    int npcId = MercTicketManager.getInstance().addTicket(item.getItemId(), activeChar, MESSAGES);
    activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
    activeChar.sendMessage("Hired mercenary (" + itemId + "," + npcId + ") at coords:" + activeChar.getX() + "," + activeChar.getY() + "," + activeChar.getZ() + " heading:" + activeChar.getHeading());
  }

  public int[] getItemIds()
  {
    return MercTicketManager.getInstance().getItemIds();
  }
}