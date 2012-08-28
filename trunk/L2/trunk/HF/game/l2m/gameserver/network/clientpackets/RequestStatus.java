package l2m.gameserver.network.clientpackets;

import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SendStatus;

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