package net.sf.l2j.gameserver.datatables;

import java.util.HashMap;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.SkillsEngine;
import net.sf.l2j.gameserver.templates.L2WeaponType;

public class SkillTable
{
  private static SkillTable _instance;
  private HashMap<Integer, L2Skill> _skills;
  private boolean _initialized = true;

  private static final L2WeaponType[] weaponDbMasks = { L2WeaponType.ETC, L2WeaponType.BOW, L2WeaponType.POLE, L2WeaponType.DUALFIST, L2WeaponType.DUAL, L2WeaponType.BLUNT, L2WeaponType.SWORD, L2WeaponType.DAGGER, L2WeaponType.BIGSWORD, L2WeaponType.ROD, L2WeaponType.BIGBLUNT };

  public static SkillTable getInstance()
  {
    if (_instance == null)
      _instance = new SkillTable();
    return _instance;
  }

  private SkillTable()
  {
    _skills = new HashMap();
    SkillsEngine.getInstance().loadAllSkills(_skills);
  }

  public void reload()
  {
    _instance = new SkillTable();
  }

  public boolean isInitialized()
  {
    return _initialized;
  }

  public static int getSkillHashCode(L2Skill skill)
  {
    return getSkillHashCode(skill.getId(), skill.getLevel());
  }

  public static int getSkillHashCode(int skillId, int skillLevel)
  {
    return skillId * 256 + skillLevel;
  }

  public L2Skill getInfo(int skillId, int level)
  {
    return (L2Skill)_skills.get(Integer.valueOf(getSkillHashCode(skillId, level)));
  }

  public int getMaxLevel(int magicId, int level)
  {
    while (level < 100)
    {
      level++;
      L2Skill temp = (L2Skill)_skills.get(Integer.valueOf(getSkillHashCode(magicId, level)));

      if (temp == null) {
        return level - 1;
      }
    }
    return level;
  }

  public int calcWeaponsAllowed(int mask)
  {
    if (mask == 0) {
      return 0;
    }
    int weaponsAllowed = 0;

    for (int i = 0; i < weaponDbMasks.length; i++) {
      if ((mask & 1 << i) != 0)
        weaponsAllowed |= weaponDbMasks[i].mask();
    }
    return weaponsAllowed;
  }
}