package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.AllyInfo;

public final class RequestAllyInfo extends L2GameClientPacket
{
  public void readImpl()
  {
  }

  protected void runImpl()
  {
    sendPacket(new AllyInfo(((L2GameClient)getClient()).getActiveChar()));
  }
}