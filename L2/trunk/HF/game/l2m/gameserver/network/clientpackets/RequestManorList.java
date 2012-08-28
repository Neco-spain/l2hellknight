package l2m.gameserver.network.clientpackets;

import l2m.gameserver.network.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    sendPacket(new ExSendManorList());
  }
}