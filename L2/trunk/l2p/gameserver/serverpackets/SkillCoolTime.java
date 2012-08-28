package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.skills.TimeStamp;

public class SkillCoolTime extends L2GameServerPacket
{
  private List<Skill> _list = Collections.emptyList();

  public SkillCoolTime(Player player)
  {
    Collection list = player.getSkillReuses();
    _list = new ArrayList(list.size());
    for (TimeStamp stamp : list)
    {
      if (!stamp.hasNotPassed())
        continue;
      Skill skill = player.getKnownSkill(stamp.getId());
      if (skill == null)
        continue;
      Skill sk = new Skill(null);
      sk.skillId = skill.getId();
      sk.level = skill.getLevel();
      sk.reuseBase = (int)Math.round(stamp.getReuseBasic() / 1000.0D);
      sk.reuseCurrent = (int)Math.round(stamp.getReuseCurrent() / 1000.0D);
      _list.add(sk);
    }
  }

  protected final void writeImpl()
  {
    writeC(199);
    writeD(_list.size());
    for (int i = 0; i < _list.size(); i++)
    {
      Skill sk = (Skill)_list.get(i);
      writeD(sk.skillId);
      writeD(sk.level);
      writeD(sk.reuseBase);
      writeD(sk.reuseCurrent);
    }
  }

  private static class Skill
  {
    public int skillId;
    public int level;
    public int reuseBase;
    public int reuseCurrent;
  }
}