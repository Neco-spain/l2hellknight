package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class GMViewSkillInfo extends L2GameServerPacket
{
  private L2PcInstance _activeChar;
  private L2Skill[] _skills;

  public GMViewSkillInfo(L2PcInstance cha)
  {
    _activeChar = cha;
    _skills = _activeChar.getAllSkills();
    if (_skills.length == 0)
      _skills = new L2Skill[0];
  }

  protected final void writeImpl()
  {
    writeC(145);
    writeS(_activeChar.getName());
    writeD(_skills.length);

    for (int i = 0; i < _skills.length; i++)
    {
      L2Skill skill = _skills[i];
      writeD(skill.isPassive() ? 1 : 0);
      writeD(skill.getLevel());
      writeD(skill.getId());
      writeC(0);
    }
  }
}