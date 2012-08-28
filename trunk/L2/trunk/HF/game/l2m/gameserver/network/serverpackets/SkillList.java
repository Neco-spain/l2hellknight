package l2m.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Skill;
import l2m.gameserver.data.tables.SkillTreeTable;

public class SkillList extends L2GameServerPacket
{
  private List<Skill> _skills;
  private boolean canEnchant;
  private Player activeChar;

  public SkillList(Player p)
  {
    _skills = new ArrayList(p.getAllSkills());
    canEnchant = (p.getTransformation() == 0);
    activeChar = p;
  }

  protected final void writeImpl()
  {
    writeC(95);
    writeD(_skills.size());

    for (Skill temp : _skills)
    {
      writeD((temp.isActive()) || (temp.isToggle()) ? 0 : 1);
      writeD(temp.getDisplayLevel());
      writeD(temp.getDisplayId());
      writeC(activeChar.isUnActiveSkill(temp.getId()) ? 1 : 0);
      writeC(canEnchant ? SkillTreeTable.isEnchantable(temp) : 0);
    }
  }
}