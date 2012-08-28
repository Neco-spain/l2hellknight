package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.GmListTable;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestGmList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveChar() == null)
      return;
    GmListTable.getInstance().sendListToPlayer(((L2GameClient)getClient()).getActiveChar());
  }
}