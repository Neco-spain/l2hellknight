package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.RankPrivs;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.PledgePowerGradeList;

public final class RequestPledgePowerGradeList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    L2Clan clan = player.getClan();
    if (clan != null)
    {
      L2Clan.RankPrivs[] privs = clan.getAllRankPrivs();
      player.sendPacket(new PledgePowerGradeList(privs));
    }
  }

  public String getType()
  {
    return "C.PledgePowerGradeList";
  }
}