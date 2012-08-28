package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ShowMiniMap;

public final class RequestShowMiniMap extends L2GameClientPacket
{
  private static final String _C__cd_REQUESTSHOWMINIMAP = "[C] cd RequestShowMiniMap";

  protected void readImpl()
  {
  }

  protected final void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    activeChar.sendPacket(new ShowMiniMap(1665));
  }

  public String getType()
  {
    return "[C] cd RequestShowMiniMap";
  }
}