package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.PcInventory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.templates.L2Item;
import net.sf.l2j.gameserver.templates.L2Weapon;

public final class RequestAutoSoulShot extends L2GameClientPacket
{
  private static final String _C__CF_REQUESTAUTOSOULSHOT = "[C] CF RequestAutoSoulShot";
  private int _itemId;
  private int _type;

  protected void readImpl()
  {
    _itemId = readD();
    _type = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (activeChar.getActiveTradeList() != null) {
      return;
    }
    if ((activeChar.getPrivateStoreType() != 0) && (activeChar.getActiveRequester() != null) && (activeChar.isDead()))
    {
      return;
    }
    L2ItemInstance item = activeChar.getInventory().getItemByItemId(_itemId);

    if (item != null)
    {
      if ((_itemId >= 3947) && (_itemId <= 3952) && (activeChar.isInOlympiadMode()))
      {
        SystemMessage sm = new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
        sm.addString(item.getItemName());
        activeChar.sendPacket(sm);
        sm = null;
        return;
      }

      if (_type == 1)
      {
        if ((_itemId < 6535) || (_itemId > 6540))
        {
          activeChar.addAutoSoulShot(_itemId);
          ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
          activeChar.sendPacket(atk);
          SystemMessage sm = new SystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO);
          sm.addString(item.getItemName());
          activeChar.sendPacket(sm);
          sm = null;

          if ((_itemId == 6645) || (_itemId == 6646) || (_itemId == 6647))
          {
            activeChar.rechargeAutoSoulShot(true, true, true);
          }

          if ((activeChar.getActiveWeaponItem() != activeChar.getFistsWeaponItem()) && (item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType()))
          {
            activeChar.rechargeAutoSoulShot(true, true, false);
          }
        }
      }
      else if (_type == 0)
      {
        activeChar.removeAutoSoulShot(_itemId);
        ExAutoSoulShot atk = new ExAutoSoulShot(_itemId, _type);
        activeChar.sendPacket(atk);
        SystemMessage sm = new SystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
        sm.addString(item.getItemName());
        activeChar.sendPacket(sm);
        sm = null;
      }
    }
  }

  public String getType()
  {
    return "[C] CF RequestAutoSoulShot";
  }
}