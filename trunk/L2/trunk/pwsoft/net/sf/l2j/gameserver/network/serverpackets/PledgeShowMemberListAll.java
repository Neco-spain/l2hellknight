package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Clan.SubPledge;
import net.sf.l2j.gameserver.model.L2ClanMember;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
  private L2Clan _clan;
  private L2PcInstance _activeChar;
  private L2ClanMember[] _members;
  private int _pledgeType;

  public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
  {
    _clan = clan;
    _activeChar = activeChar;
    _members = _clan.getMembers();
  }

  protected final void writeImpl()
  {
    _pledgeType = 0;
    writePledge(0);

    L2Clan.SubPledge[] subPledge = _clan.getAllSubPledges();
    for (int i = 0; i < subPledge.length; i++)
    {
      _activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge[i]));
    }

    for (L2ClanMember m : _members)
    {
      if (m.getPledgeType() != 0) {
        _activeChar.sendPacket(new PledgeShowMemberListAdd(m));
      }
    }

    _activeChar.sendPacket(new UserInfo(_activeChar));
  }

  void writePledge(int mainOrSubpledge)
  {
    writeC(83);

    writeD(mainOrSubpledge);
    writeD(_clan.getClanId());
    writeD(_pledgeType);
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
    writeD(_clan.getSubPledgeMembersCount(_pledgeType));

    for (L2ClanMember m : _members)
    {
      if (m.getPledgeType() == _pledgeType) {
        writeS(m.getName());
        writeD(m.getLevel());
        writeD(m.getClassId());
        writeD(0);
        writeD(m.getObjectId());
        writeD(m.isOnline() ? 1 : 0);
        writeD(0);
      }
    }
  }

  public String getType()
  {
    return "S.PledgeShowMemberListAll";
  }
}