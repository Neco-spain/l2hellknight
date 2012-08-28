package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Creature;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Creature activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    activeChar.sendPacket(new ExCursedWeaponList());
  }
}