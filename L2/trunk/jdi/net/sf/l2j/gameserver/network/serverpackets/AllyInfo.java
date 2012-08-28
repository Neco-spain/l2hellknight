package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.datatables.ClanTable;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class AllyInfo extends L2GameServerPacket
{
  private static final String _S__7A_FRIENDLIST = "[S] 7a AllyInfo";
  private static L2PcInstance _cha;

  public AllyInfo(L2PcInstance cha)
  {
    _cha = cha;
  }

  protected final void writeImpl()
  {
    L2PcInstance activeChar = ((L2GameClient)getClient()).getActiveChar();
    if (activeChar == null) return;

    if (activeChar.getAllyId() == 0)
    {
      _cha.sendPacket(new SystemMessage(SystemMessageId.NO_CURRENT_ALLIANCES));
      return;
    }

    SystemMessage sm = new SystemMessage(SystemMessageId.ALLIANCE_INFO_HEAD);
    _cha.sendPacket(sm);

    sm = new SystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
    sm.addString(_cha.getClan().getAllyName());
    _cha.sendPacket(sm);
    int online = 0;
    int count = 0;
    int clancount = 0;
    for (L2Clan clan : ClanTable.getInstance().getClans()) {
      if (clan.getAllyId() == _cha.getAllyId()) {
        clancount++;
        online += clan.getOnlineMembers("").length;
        count += clan.getMembers().length;
      }
    }

    sm = new SystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
    sm.addString("" + online);
    sm.addString("" + count);
    _cha.sendPacket(sm);
    L2Clan leaderclan = ClanTable.getInstance().getClan(_cha.getAllyId());
    sm = new SystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
    sm.addString(leaderclan.getName());
    sm.addString(leaderclan.getLeaderName());
    _cha.sendPacket(sm);

    sm = new SystemMessage(SystemMessageId.ALLIANCE_CLAN_TOTAL_S1);
    sm.addString("" + clancount);
    _cha.sendPacket(sm);

    sm = new SystemMessage(SystemMessageId.CLAN_INFO_HEAD);
    _cha.sendPacket(sm);
    for (L2Clan clan : ClanTable.getInstance().getClans()) {
      if (clan.getAllyId() != _cha.getAllyId())
        continue;
      sm = new SystemMessage(SystemMessageId.CLAN_INFO_NAME);
      sm.addString(clan.getName());
      _cha.sendPacket(sm);

      sm = new SystemMessage(SystemMessageId.CLAN_INFO_LEADER);
      sm.addString(clan.getLeaderName());
      _cha.sendPacket(sm);

      sm = new SystemMessage(SystemMessageId.CLAN_INFO_LEVEL);
      sm.addNumber(clan.getLevel());
      _cha.sendPacket(sm);

      sm = new SystemMessage(SystemMessageId.CLAN_INFO_SEPARATOR);
      _cha.sendPacket(sm);
    }

    sm = new SystemMessage(SystemMessageId.CLAN_INFO_FOOT);
    _cha.sendPacket(sm);
  }

  public String getType()
  {
    return "[S] 7a AllyInfo";
  }
}