package l2m.gameserver.network.clientpackets;

import java.util.Set;
import l2p.commons.math.SafeMath;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.data.dao.MailDAO;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.model.mail.Mail;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExShowSentPostList;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.templates.item.ItemTemplate;
import l2m.gameserver.utils.Log;

public class RequestExCancelSentPost extends L2GameClientPacket
{
  private int postId;

  protected void readImpl()
  {
    postId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.isActionsDisabled())
    {
      activeChar.sendActionFailed();
      return;
    }

    if (activeChar.isInStoreMode())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_BECAUSE_THE_PRIVATE_SHOP_OR_WORKSHOP_IS_IN_PROGRESS);
      return;
    }

    if (activeChar.isInTrade())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_DURING_AN_EXCHANGE);
      return;
    }

    if (activeChar.getEnchantScroll() != null)
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_DURING_AN_ITEM_ENHANCEMENT_OR_ATTRIBUTE_ENHANCEMENT);
      return;
    }

    if (!activeChar.isInPeaceZone())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_CANCEL_IN_A_NON_PEACE_ZONE_LOCATION);
      return;
    }

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
      return;
    }

    Mail mail = MailDAO.getInstance().getSentMailByMailId(activeChar.getObjectId(), postId);
    if (mail != null)
    {
      if (mail.getAttachments().isEmpty())
      {
        activeChar.sendActionFailed();
        return;
      }
      activeChar.getInventory().writeLock();
      try
      {
        int slots = 0;
        long weight = 0L;
        for (ItemInstance item : mail.getAttachments())
        {
          weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getTemplate().getWeight()));
          if ((!item.getTemplate().isStackable()) || (activeChar.getInventory().getItemByItemId(item.getItemId()) == null)) {
            slots++;
          }
        }
        if (!activeChar.getInventory().validateWeight(weight)) {
          sendPacket(Msg.YOU_COULD_NOT_CANCEL_RECEIPT_BECAUSE_YOUR_INVENTORY_IS_FULL);
          return;
        }
        if (!activeChar.getInventory().validateCapacity(slots))
        {
          sendPacket(Msg.YOU_COULD_NOT_CANCEL_RECEIPT_BECAUSE_YOUR_INVENTORY_IS_FULL);
          return;
        }
        Set attachments = mail.getAttachments();
        ItemInstance[] items;
        synchronized (attachments)
        {
          items = (ItemInstance[])mail.getAttachments().toArray(new ItemInstance[attachments.size()]);
          attachments.clear();
        }

        for (ItemInstance item : items)
        {
          activeChar.sendPacket(new SystemMessage(3073).addItemName(item.getItemId()).addNumber(item.getCount()));
          Log.LogItem(activeChar, "PostCancel", item);
          activeChar.getInventory().addItem(item);
        }

        mail.delete();

        activeChar.sendPacket(Msg.MAIL_SUCCESSFULLY_CANCELLED);
      }
      catch (ArithmeticException ae)
      {
      }
      finally
      {
        activeChar.getInventory().writeUnlock();
      }
    }
    activeChar.sendPacket(new ExShowSentPostList(activeChar));
  }
}