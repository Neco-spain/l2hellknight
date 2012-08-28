package l2p.gameserver.clientpackets;

import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.SendStatus;

public final class RequestStatus extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    ((GameClient)getClient()).close(new SendStatus());
  }
}