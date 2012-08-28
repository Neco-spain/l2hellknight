package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ShowMiniMap;

public final class RequestShowMiniMap extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected final void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    player.sendPacket(new ShowMiniMap(1665));
  }

  public String getType()
  {
    return "C.ShowMiniMap";
  }
}