package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.SevenSigns;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SSQStatus;

public final class RequestSSQStatus extends L2GameClientPacket
{
  private static final String _C__C7_RequestSSQStatus = "[C] C7 RequestSSQStatus";
  private int _page;

  protected void readImpl()
  {
    _page = readC();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (((SevenSigns.getInstance().isSealValidationPeriod()) || (SevenSigns.getInstance().isCompResultsPeriod())) && (_page == 4)) {
      return;
    }
    SSQStatus ssqs = new SSQStatus(activeChar, _page);
    activeChar.sendPacket(ssqs);
  }

  public String getType()
  {
    return "[C] C7 RequestSSQStatus";
  }
}