package l2p.gameserver.clientpackets;

import l2p.gameserver.cache.Msg;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.ItemInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.network.GameClient;

@Deprecated
public class RequestUnEquipItem extends L2GameClientPacket
{
  private int _slot;

  protected void readImpl()
  {
    _slot = readD();
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

    if (activeChar.isFishing())
    {
      activeChar.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
      return;
    }

    if (((_slot == 128) || (_slot == 256) || (_slot == 16384)) && ((activeChar.isCursedWeaponEquipped()) || (activeChar.getActiveWeaponFlagAttachment() != null))) {
      return;
    }
    if (_slot == 128)
    {
      ItemInstance weapon = activeChar.getActiveWeaponInstance();
      if (weapon == null)
        return;
      activeChar.abortAttack(true, true);
      activeChar.abortCast(true, true);
      activeChar.sendDisarmMessage(weapon);
    }

    activeChar.getInventory().unEquipItemInBodySlot(_slot);
  }
}