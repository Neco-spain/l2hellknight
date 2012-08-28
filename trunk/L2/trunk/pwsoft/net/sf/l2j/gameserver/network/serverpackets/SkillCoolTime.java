package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Collection;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance.TimeStamp;

public class SkillCoolTime extends L2GameServerPacket
{
  public Collection<L2PcInstance.TimeStamp> _reuseTimeStamps;

  public SkillCoolTime(L2PcInstance cha)
  {
    _reuseTimeStamps = cha.getReuseTimeStamps();
  }

  protected void writeImpl()
  {
    writeC(193);
    writeD(_reuseTimeStamps.size());
    for (L2PcInstance.TimeStamp ts : _reuseTimeStamps)
    {
      writeD(ts.getSkill());
      writeD(0);
      writeD((int)ts.getReuse() / 1000);
      writeD((int)ts.getRemaining() / 1000);
    }
  }
}