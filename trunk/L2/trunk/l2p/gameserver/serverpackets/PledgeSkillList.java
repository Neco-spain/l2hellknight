package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;

public class PledgeSkillList extends L2GameServerPacket
{
  private List<SkillInfo> _allSkills = Collections.emptyList();
  private List<UnitSkillInfo> _unitSkills = new ArrayList();

  public PledgeSkillList(Clan clan)
  {
    Collection skills = clan.getSkills();
    _allSkills = new ArrayList(skills.size());

    for (Skill sk : skills) {
      _allSkills.add(new SkillInfo(sk.getId(), sk.getLevel()));
    }
    for (Iterator i$ = clan.getAllSubUnits().iterator(); i$.hasNext(); ) { subUnit = (SubUnit)i$.next();

      for (Skill sk : subUnit.getSkills())
        _unitSkills.add(new UnitSkillInfo(subUnit.getType(), sk.getId(), sk.getLevel())); }
    SubUnit subUnit;
  }

  protected final void writeImpl()
  {
    writeEx(58);
    writeD(_allSkills.size());
    writeD(_unitSkills.size());

    for (SkillInfo info : _allSkills)
    {
      writeD(info._id);
      writeD(info._level);
    }

    for (UnitSkillInfo info : _unitSkills)
    {
      writeD(info._type);
      writeD(info._id);
      writeD(info._level);
    }
  }

  static class UnitSkillInfo extends PledgeSkillList.SkillInfo
  {
    private int _type;

    public UnitSkillInfo(int type, int id, int level)
    {
      super(level);
      _type = type;
    }
  }

  static class SkillInfo
  {
    public int _id;
    public int _level;

    public SkillInfo(int id, int level)
    {
      _id = id;
      _level = level;
    }
  }
}