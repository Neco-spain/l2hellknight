package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2m.gameserver.model.base.EnchantSkillLearn;
import l2m.gameserver.data.tables.SkillTreeTable;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
  private List<Integer> _routes;
  private int _id;
  private int _level;
  private int _canAdd;
  private int canDecrease;

  public ExEnchantSkillInfo(int id, int level)
  {
    _routes = new ArrayList();
    _id = id;
    _level = level;

    if (_level > 100)
    {
      canDecrease = 1;

      EnchantSkillLearn esd = SkillTreeTable.getSkillEnchant(_id, _level + 1);

      if (esd != null)
      {
        addEnchantSkillDetail(esd.getLevel());
        _canAdd = 1;
      }

      for (EnchantSkillLearn el : SkillTreeTable.getEnchantsForChange(_id, _level))
        addEnchantSkillDetail(el.getLevel());
    }
    else
    {
      for (EnchantSkillLearn esd : SkillTreeTable.getFirstEnchantsForSkill(_id))
      {
        addEnchantSkillDetail(esd.getLevel());
        _canAdd = 1;
      }
    }
  }

  public void addEnchantSkillDetail(int level) {
    _routes.add(Integer.valueOf(level));
  }

  protected void writeImpl()
  {
    writeEx(42);

    writeD(_id);
    writeD(_level);
    writeD(_canAdd);
    writeD(canDecrease);

    writeD(_routes.size());
    for (Integer route : _routes)
      writeD(route.intValue());
  }
}