package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class GMViewPledgeInfo extends L2GameServerPacket
{
  private static final String _S__A9_GMVIEWPLEDGEINFO = "[S] 90 GMViewPledgeInfo";
  private L2Clan _clan;
  private L2PcInstance _activeChar;

  public GMViewPledgeInfo(L2Clan clan, L2PcInstance activeChar)
  {
    _clan = clan;
    _activeChar = activeChar;
  }

  protected final void writeImpl()
  {
    writeC(144);
    writeS(_activeChar.getName());
    writeD(_clan.getClanId());
    writeD(0);
    writeS(_clan.getName());
    writeS(_clan.getLeaderName());
    writeD(_clan.getCrestId());
    writeD(_clan.getLevel());
    writeD(_clan.getHasCastle());
    writeD(_clan.getHasHideout());
    writeD(_clan.getRank());
    writeD(_clan.getReputationScore());
    writeD(0);
    writeD(0);

    writeD(_clan.getAllyId());
    writeS(_clan.getAllyName());
    writeD(_clan.getAllyCrestId());
    writeD(_clan.isAtWar());

    L2ClanMember[] members = _clan.getMembers();
    writeD(members.length);

    for (int i = 0; i < members.length; i++)
    {
      writeS(members[i].getName());
      writeD(members[i].getLevel());
      writeD(members[i].getClassId());
      writeD(0);
      writeD(1);
      writeD(members[i].isOnline() ? members[i].getObjectId() : 0);
      writeD(0);
    }
  }

  public String getType()
  {
    return "[S] 90 GMViewPledgeInfo";
  }
}