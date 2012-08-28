package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceiveWarList;

public final class RequestPledgeWarList extends L2GameClientPacket
{
  private static final String _C__D0_1E_REQUESTPLEDGEWARLIST = "[C] D0:1E RequestPledgeWarList";
  private int _unk1;
  private int _tab;

  protected void readImpl()
  {
    _unk1 = readD();
    _tab = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if (activeChar.getClan() == null) return;

    activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(), _tab));
  }

  public String getType()
  {
    return "[C] D0:1E RequestPledgeWarList";
  }
}