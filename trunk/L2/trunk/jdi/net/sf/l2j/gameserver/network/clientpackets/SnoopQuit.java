package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class SnoopQuit extends L2GameClientPacket
{
  private static final String _C__AB_SNOOPQUIT = "[C] AB SnoopQuit";
  private int _snoopID;

  protected void readImpl()
  {
    _snoopID = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = (L2PcInstance)L2World.getInstance().findObject(_snoopID);

    if (player == null)
      return;
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    player.removeSnooper(activeChar);
    activeChar.removeSnooped(player);
  }

  public String getType()
  {
    return "[C] AB SnoopQuit";
  }
}