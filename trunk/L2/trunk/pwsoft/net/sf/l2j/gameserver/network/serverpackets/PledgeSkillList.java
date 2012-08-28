package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.L2Skill;

public class PledgeSkillList extends L2GameServerPacket
{
  private L2Clan _clan;

  public PledgeSkillList(L2Clan clan)
  {
    _clan = clan;
  }

  protected void writeImpl()
  {
    L2Skill[] skills = _clan.getAllSkills();

    writeC(254);
    writeH(57);
    writeD(skills.length);
    for (L2Skill sk : skills)
    {
      writeD(sk.getId());
      writeD(sk.getLevel());
    }
  }

  public String getType()
  {
    return "S.PledgeSkillList";
  }
}