package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.protection.nProtect;

public class GameGuardReply extends L2GameClientPacket
{
  private static final String _C__CA_GAMEGUARDREPLY = "[C] CA GameGuardReply";
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
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();

    if (activeChar == null) {
      return;
    }
    if (!nProtect.getInstance().checkGameGuardReply((L2GameClient)getClient(), _reply)) {
      return;
    }
    ((L2GameClient)getClient()).setGameGuardOk(true);
  }

  public String getType()
  {
    return "[C] CA GameGuardReply";
  }
}