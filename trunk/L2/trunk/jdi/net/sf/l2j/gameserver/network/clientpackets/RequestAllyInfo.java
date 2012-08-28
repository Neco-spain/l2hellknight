package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.AllyInfo;

public final class RequestAllyInfo extends L2GameClientPacket
{
  private static final String _C__8E_REQUESTALLYINFO = "[C] 8E RequestAllyInfo";

  public void readImpl()
  {
  }

  protected void runImpl()
  {
    AllyInfo ai = new AllyInfo(((L2GameClient)getClient()).getActiveChar());
    sendPacket(ai);
  }

  public String getType()
  {
    return "[C] 8E RequestAllyInfo";
  }
}