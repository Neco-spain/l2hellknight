package l2m.gameserver.network.clientpackets;

import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.L2GameServerPacket;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.network.serverpackets.components.CustomMessage;
import l2m.gameserver.data.tables.ClanTable;

public class RequestOustAlly extends L2GameClientPacket
{
  private String _clanName;

  protected void readImpl()
  {
    _clanName = readS(32);
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    Clan leaderClan = activeChar.getClan();
    if (leaderClan == null)
    {
      activeChar.sendActionFailed();
      return;
    }
    Alliance alliance = leaderClan.getAlliance();
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

    if (_clanName == null) {
      return;
    }
    Clan clan = ClanTable.getInstance().getClanByName(_clanName);

    if (clan != null)
    {
      if (!alliance.isMember(clan.getClanId()))
      {
        activeChar.sendActionFailed();
        return;
      }

      if (alliance.getLeader().equals(clan))
      {
        activeChar.sendPacket(Msg.YOU_HAVE_FAILED_TO_WITHDRAW_FROM_THE_ALLIANCE);
        return;
      }

      clan.broadcastToOnlineMembers(new L2GameServerPacket[] { new SystemMessage("Your clan has been expelled from " + alliance.getAllyName() + " alliance."), new SystemMessage(468) });
      clan.setAllyId(0);
      clan.setLeavedAlly();
      alliance.broadcastAllyStatus();
      alliance.removeAllyMember(clan.getClanId());
      alliance.setExpelledMember();
      activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.RequestOustAlly.ClanDismissed", activeChar, new Object[0]).addString(clan.getName()).addString(alliance.getAllyName()));
    }
  }
}