package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgeInfo;

public final class RequestPledgeInfo extends L2GameClientPacket
{
  private static final String _C__66_REQUESTPLEDGEINFO = "[C] 66 RequestPledgeInfo";
  private static Logger _log = Logger.getLogger(RequestPledgeInfo.class.getName());
  private int _clanId;

  protected void readImpl()
  {
    _clanId = readD();
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    L2Clan clan = ClanTable.getInstance().getClan(_clanId);

    if (clan == null)
    {
      return;
    }

    PledgeInfo pc = new PledgeInfo(clan);
    if (activeChar != null)
    {
      activeChar.sendPacket(pc);
    }
  }

  public String getType()
  {
    return "[C] 66 RequestPledgeInfo";
  }
}