package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.RankPrivs;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.PledgePowerGradeList;

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