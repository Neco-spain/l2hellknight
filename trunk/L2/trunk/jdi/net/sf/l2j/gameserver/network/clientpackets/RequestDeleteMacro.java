package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestDeleteMacro extends L2GameClientPacket
{
  private int _id;
  private static final String _C__C2_REQUESTDELETEMACRO = "[C] C2 RequestDeleteMacro";

  protected void readImpl()
  {
    _id = readD();
  }

  protected void runImpl()
  {
    if (((L2GameClient)getClient()).getActiveChar() == null)
      return;
    ((L2GameClient)getClient()).getActiveChar().deleteMacro(_id);
    SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
    sm.addString("Delete macro id=" + _id);
    sendPacket(sm);
    sm = null;
  }

  public String getType()
  {
    return "[C] C2 RequestDeleteMacro";
  }
}