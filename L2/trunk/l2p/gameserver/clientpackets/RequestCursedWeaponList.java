package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Creature;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.ExCursedWeaponList;

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