package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;

public final class RequestPledgeMemberList extends L2GameClientPacket
{
  private static final String _C__3C_REQUESTPLEDGEMEMBERLIST = "[C] 3C RequestPledgeMemberList";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    L2Clan clan = activeChar.getClan();
    if (clan != null)
    {
      PledgeShowMemberListAll pm = new PledgeShowMemberListAll(clan, activeChar);
      activeChar.sendPacket(pm);
    }
  }

  public String getType()
  {
    return "[C] 3C RequestPledgeMemberList";
  }
}