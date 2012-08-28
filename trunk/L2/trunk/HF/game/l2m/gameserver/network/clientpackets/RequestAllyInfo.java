package l2m.gameserver.network.clientpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.cache.Msg;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SystemMessage;
import l2m.gameserver.data.tables.ClanTable;

public class RequestAllyInfo extends L2GameClientPacket
{
  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    Alliance ally = player.getAlliance();
    if (ally == null) {
      return;
    }
    int clancount = 0;
    Clan leaderclan = player.getAlliance().getLeader();
    clancount = ClanTable.getInstance().getAlliance(leaderclan.getAllyId()).getMembers().length;
    int[] online = new int[clancount + 1];
    int[] count = new int[clancount + 1];
    Clan[] clans = player.getAlliance().getMembers();
    for (int i = 0; i < clancount; i++)
    {
      online[(i + 1)] = clans[i].getOnlineMembers(0).size();
      count[(i + 1)] = clans[i].getAllSize();
      online[0] += online[(i + 1)];
      count[0] += count[(i + 1)];
    }

    List packets = new ArrayList(7 + 5 * clancount);
    packets.add(Msg._ALLIANCE_INFORMATION_);
    packets.add(new SystemMessage(492).addString(player.getClan().getAlliance().getAllyName()));
    packets.add(new SystemMessage(493).addNumber(online[0]).addNumber(count[0]));
    packets.add(new SystemMessage(494).addString(leaderclan.getName()).addString(leaderclan.getLeaderName()));
    packets.add(new SystemMessage(495).addNumber(clancount));
    packets.add(Msg._CLAN_INFORMATION_);
    for (int i = 0; i < clancount; i++)
    {
      packets.add(new SystemMessage(497).addString(clans[i].getName()));
      packets.add(new SystemMessage(498).addString(clans[i].getLeaderName()));
      packets.add(new SystemMessage(499).addNumber(clans[i].getLevel()));
      packets.add(new SystemMessage(493).addNumber(online[(i + 1)]).addNumber(count[(i + 1)]));
      packets.add(Msg.__DASHES__);
    }
    packets.add(Msg.__EQUALS__);

    player.sendPacket(packets);
  }
}