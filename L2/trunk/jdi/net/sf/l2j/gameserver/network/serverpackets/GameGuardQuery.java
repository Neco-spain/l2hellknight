package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.protection.nProtect;

public class GameGuardQuery extends L2GameServerPacket
{
  private static final String _S__F9_GAMEGUARDQUERY = "[S] F9 GameGuardQuery";

  public void runImpl()
  {
    ((L2GameClient)getClient()).setGameGuardOk(false);
  }

  public void writeImpl()
  {
    writeC(249);
    nProtect.getInstance().sendGameGuardQuery(this);
  }

  public String getType()
  {
    return "[S] F9 GameGuardQuery";
  }
}