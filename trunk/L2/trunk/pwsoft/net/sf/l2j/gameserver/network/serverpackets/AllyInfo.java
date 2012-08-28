package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastTable;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class AllyInfo extends L2GameServerPacket
{
  private L2PcInstance _cha;

  public AllyInfo(L2PcInstance cha)
  {
    _cha = cha;
  }

  protected final void writeImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) {
      return;
    }
    if (activeChar.getAllyId() == 0)
    {
      _cha.sendPacket(Static.NO_CURRENT_ALLIANCES);
      return;
    }

    _cha.sendPacket(Static.ALLIANCE_INFO_HEAD);

    _cha.sendPacket(SystemMessage.id(SystemMessageId.ALLIANCE_NAME_S1).addString(_cha.getClan().getAllyName()));
    int online = 0;
    int count = 0;
    int clancount = 0;

    FastTable cn = new FastTable();
    for (L2Clan clan : ClanTable.getInstance().getClans())
    {
      if (clan.getAllyId() == _cha.getAllyId())
      {
        clancount++;
        online += clan.getOnlineMembers("").length;
        count += clan.getMembers().length;
        cn.add(clan);
      }
    }

    _cha.sendPacket(SystemMessage.id(SystemMessageId.CONNECTION_S1_TOTAL_S2).addString("" + online).addString("" + count));
    L2Clan leaderclan = ClanTable.getInstance().getClan(_cha.getAllyId());
    _cha.sendPacket(SystemMessage.id(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1).addString(leaderclan.getName()).addString(leaderclan.getLeaderName()));

    _cha.sendPacket(SystemMessage.id(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1).addString("" + clancount));

    _cha.sendPacket(Static.CLAN_INFO_HEAD);

    for (L2Clan clan : cn)
    {
      if (clan == null)
      {
        continue;
      }
      _cha.sendPacket(SystemMessage.id(SystemMessageId.CLAN_INFO_NAME).addString(clan.getName()));

      _cha.sendPacket(SystemMessage.id(SystemMessageId.CLAN_INFO_LEADER).addString(clan.getLeaderName()));

      _cha.sendPacket(SystemMessage.id(SystemMessageId.CLAN_INFO_LEVEL).addNumber(clan.getLevel()));

      _cha.sendPacket(SystemMessage.id(SystemMessageId.CONNECTION_S1_TOTAL_S2).addString("" + clan.getOnlineMembers("").length).addString("" + clan.getMembers().length));

      _cha.sendPacket(SystemMessage.id(SystemMessageId.CLAN_INFO_SEPARATOR));
    }

    _cha.sendPacket(Static.CLAN_INFO_FOOT);

    cn.clear();
    cn = null;
  }
}