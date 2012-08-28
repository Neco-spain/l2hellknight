package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
  private static Logger _log = Logger.getLogger(RequestAutoSoulShot.class.getName());
  private int _itemId;
  private int _type;

  protected void readImpl()
  {
    _itemId = readD();
    _type = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

    if (player == null) {
      return;
    }
    if ((player.getPrivateStoreType() == 0) && (player.getTransactionRequester() == null) && (!player.isDead()))
    {
      L2ItemInstance item = player.getInventory().getItemByItemId(_itemId);

      if (item != null)
      {
        if (_type == 1)
        {
          if ((_itemId < 6535) || (_itemId > 6540))
          {
            if ((_itemId == 6645) || (_itemId == 6646) || (_itemId == 6647))
            {
              if (player.getPet() != null)
              {
                player.addAutoSoulShot(_itemId);
                player.sendPacket(new ExAutoSoulShot(_itemId, _type));

                player.sendPacket(SystemMessage.id(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addString(item.getItemName()));
                player.rechargeAutoSoulShot(true, true, true);
              }
              else
              {
                player.sendPacket(Static.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
                player.sendPacket(Static.NO_SERVITOR_CANNOT_AUTOMATE_USE);
              }

            }
            else if ((_itemId >= 3947) && (_itemId <= 3952) && (player.isInOlympiadMode()))
            {
              player.sendPacket(Static.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            }
            else
            {
              player.addAutoSoulShot(_itemId);
              player.sendPacket(new ExAutoSoulShot(_itemId, _type));

              player.sendPacket(SystemMessage.id(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addString(item.getItemName()));
              player.rechargeAutoSoulShot(true, true, false);
            }

          }

        }
        else if (_type == 0)
        {
          player.removeAutoSoulShot(_itemId);
          player.sendPacket(new ExAutoSoulShot(_itemId, _type));

          player.sendPacket(SystemMessage.id(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addString(item.getItemName()));
        }
      }
    }
  }
}