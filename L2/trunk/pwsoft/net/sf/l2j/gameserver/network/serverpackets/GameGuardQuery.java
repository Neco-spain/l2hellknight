package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.network.L2GameClient;

public class GameGuardQuery extends L2GameServerPacket
{
  public void runImpl()
  {
    ((L2GameClient)getClient()).setGameGuardOk(false);
  }

  public void writeImpl()
  {
    writeC(249);
  }
}