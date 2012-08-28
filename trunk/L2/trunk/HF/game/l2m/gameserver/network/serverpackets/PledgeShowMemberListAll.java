package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.pledge.Alliance;
import l2m.gameserver.model.pledge.Clan;
import l2m.gameserver.model.pledge.SubUnit;
import l2m.gameserver.model.pledge.UnitMember;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
  private int _clanObjectId;
  private int _clanCrestId;
  private int _level;
  private int _rank;
  private int _reputation;
  private int _allianceObjectId;
  private int _allianceCrestId;
  private int _hasCastle;
  private int _hasClanHall;
  private int _hasFortress;
  private int _atClanWar;
  private String _unitName;
  private String _leaderName;
  private String _allianceName;
  private int _pledgeType;
  private int _territorySide;
  private List<PledgePacketMember> _members;

  public PledgeShowMemberListAll(Clan clan, SubUnit sub)
  {
    _pledgeType = sub.getType();
    _clanObjectId = clan.getClanId();
    _unitName = sub.getName();
    _leaderName = sub.getLeaderName();
    _clanCrestId = clan.getCrestId();
    _level = clan.getLevel();
    _hasCastle = clan.getCastle();
    _hasClanHall = clan.getHasHideout();
    _hasFortress = clan.getHasFortress();
    _rank = clan.getRank();
    _reputation = clan.getReputationScore();
    _atClanWar = clan.isAtWarOrUnderAttack();
    _territorySide = clan.getWarDominion();

    Alliance ally = clan.getAlliance();

    if (ally != null)
    {
      _allianceObjectId = ally.getAllyId();
      _allianceName = ally.getAllyName();
      _allianceCrestId = ally.getAllyCrestId();
    }

    _members = new ArrayList(sub.size());

    for (UnitMember m : sub.getUnitMembers())
      _members.add(new PledgePacketMember(m));
  }

  protected final void writeImpl()
  {
    writeC(90);

    writeD(_pledgeType == 0 ? 0 : 1);
    writeD(_clanObjectId);
    writeD(_pledgeType);
    writeS(_unitName);
    writeS(_leaderName);
    writeD(_clanCrestId);
    writeD(_level);
    writeD(_hasCastle);
    writeD(_hasClanHall);
    writeD(_hasFortress);
    writeD(_rank);
    writeD(_reputation);
    writeD(0);
    writeD(0);
    writeD(_allianceObjectId);
    writeS(_allianceName);
    writeD(_allianceCrestId);
    writeD(_atClanWar);
    writeD(_territorySide);

    writeD(_members.size());
    for (PledgePacketMember m : _members)
    {
      writeS(m._name);
      writeD(m._level);
      writeD(m._classId);
      writeD(m._sex);
      writeD(m._race);
      writeD(m._online);
      writeD(m._hasSponsor ? 1 : 0); } 
  }
  private class PledgePacketMember { private String _name;
    private int _level;
    private int _classId;
    private int _sex;
    private int _race;
    private int _online;
    private boolean _hasSponsor;

    public PledgePacketMember(UnitMember m) { _name = m.getName();
      _level = m.getLevel();
      _classId = m.getClassId();
      _sex = m.getSex();
      _race = 0;
      _online = (m.isOnline() ? m.getObjectId() : 0);
      _hasSponsor = (m.getSponsor() != 0);
    }
  }
}