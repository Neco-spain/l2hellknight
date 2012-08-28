package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.L2GameClient;

public class GameGuardReply extends L2GameClientPacket
{
  private int[] _reply = new int[4];

  protected void readImpl()
  {
    _reply[0] = readD();
    _reply[1] = readD();
    _reply[2] = readD();
    _reply[3] = readD();
  }

  protected void runImpl()
  {
    if (getClient() == null) {
      return;
    }

    if (!((L2GameClient)getClient()).checkGameGuardReply(_reply)) {
      return;
    }

    ((L2GameClient)getClient()).setGameGuardOk(true);
  }
}