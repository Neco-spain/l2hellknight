package net.sf.l2j.gameserver.network.serverpackets;

import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Object;

public class MagicSkillLaunched extends L2GameServerPacket
{
  private int _charObjId;
  private int _skillId;
  private int _skillLevel;
  private FastList<L2Object> _targets;

  public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel, FastList<L2Object> targets)
  {
    _charObjId = cha.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _targets = targets;
  }

  public MagicSkillLaunched(L2Character cha, int skillId, int skillLevel) {
    _charObjId = cha.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _targets = new FastList();
    _targets.add(cha);
  }

  protected final void writeImpl()
  {
    writeC(118);
    writeD(_charObjId);
    writeD(_skillId);
    writeD(_skillLevel);
    writeD(_targets.size());
    FastList.Node n = _targets.head(); for (FastList.Node end = _targets.tail(); (n = n.getNext()) != end; ) {
      L2Object target = (L2Object)n.getValue();
      if (target == null)
      {
        continue;
      }
      writeD(target.getObjectId());
    }
  }

  public void gcb()
  {
  }
}