package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;

public class L2BigheadZone extends L2ZoneType
{
  public L2BigheadZone(int id)
  {
    super(id);
  }

  protected void onEnter(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      L2PcInstance pci = (L2PcInstance)character;
      pci.enterDangerArea();
      SkillTable.getInstance().getInfo(4559, 1).getEffects(pci, pci);

      for (L2Effect e : pci.getAllEffects())
      {
        if (e == null)
          continue;
        if ((e.getSkill().getId() >= 4551) && (e.getSkill().getId() <= 4554))
          e.exit();
      }
    }
  }

  protected void onExit(L2Character character)
  {
    if ((character instanceof L2PcInstance))
    {
      ((L2PcInstance)character).exitDangerArea();
    }
  }

  protected void onDieInside(L2Character character)
  {
    onExit(character);
  }

  protected void onReviveInside(L2Character character)
  {
    onEnter(character);
  }
}