package l2p.gameserver.clientpackets;

import l2p.gameserver.serverpackets.ExSendManorList;

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