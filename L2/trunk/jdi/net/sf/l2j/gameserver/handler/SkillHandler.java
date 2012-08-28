package net.sf.l2j.gameserver.handler;

import java.util.Map;
import java.util.TreeMap;
import net.sf.l2j.gameserver.model.L2Skill.SkillType;

public class SkillHandler
{
  private static SkillHandler _instance;
  private Map<L2Skill.SkillType, ISkillHandler> _datatable;

  public static SkillHandler getInstance()
  {
    if (_instance == null)
    {
      _instance = new SkillHandler();
    }
    return _instance;
  }

  private SkillHandler()
  {
    _datatable = new TreeMap();
  }

  public void registerSkillHandler(ISkillHandler handler)
  {
    L2Skill.SkillType[] types = handler.getSkillIds();
    for (L2Skill.SkillType t : types)
    {
      _datatable.put(t, handler);
    }
  }

  public ISkillHandler getSkillHandler(L2Skill.SkillType skillType)
  {
    return (ISkillHandler)_datatable.get(skillType);
  }

  public int size()
  {
    return _datatable.size();
  }
}