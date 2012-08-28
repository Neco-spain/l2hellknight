package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.data.tables.ClanTable;

public class RequestDismissAlly extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Clan clan = activeChar.getClan();
    if (clan == null)
    {
      activeChar.sendActionFailed();
      return;
    }

    Alliance alliance = clan.getAlliance();
    if (alliance == null)
    {
      activeChar.sendPacket(Msg.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
      return;
    }

    if (!activeChar.isAllyLeader())
    {
      activeChar.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
      return;
    }

    if (alliance.getMembersCount() > 1)
    {
      activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE);
      return;
    }

    ClanTable.getInstance().dissolveAlly(activeChar);
  }
}