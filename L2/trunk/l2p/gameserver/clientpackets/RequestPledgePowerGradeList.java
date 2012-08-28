package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.RankPrivs;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.serverpackets.PledgePowerGradeList;

public class RequestPledgePowerGradeList extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    Clan clan = activeChar.getClan();
    if (clan != null)
    {
      RankPrivs[] privs = clan.getAllRankPrivs();
      activeChar.sendPacket(new PledgePowerGradeList(privs));
    }
  }
}