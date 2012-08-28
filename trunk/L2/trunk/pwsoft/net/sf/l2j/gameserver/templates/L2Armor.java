package net.sf.l2j.gameserver.templates;

import java.util.List;
import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;

public final class L2Armor extends L2Item
{
  private final int _avoidModifier;
  private final int _pDef;
  private final int _mDef;
  private final int _mpBonus;
  private final int _hpBonus;
  private L2Skill _itemSkill = null;

  public L2Armor(L2ArmorType type, StatsSet set)
  {
    super(type, set);
    _avoidModifier = set.getInteger("avoid_modify");
    _pDef = set.getInteger("p_def");
    _mDef = set.getInteger("m_def");
    _mpBonus = set.getInteger("mp_bonus", 0);
    _hpBonus = set.getInteger("hp_bonus", 0);

    int sId = set.getInteger("item_skill_id");
    int sLv = set.getInteger("item_skill_lvl");
    if ((sId > 0) && (sLv > 0))
      _itemSkill = SkillTable.getInstance().getInfo(sId, sLv);
  }

  public L2ArmorType getItemType()
  {
    return (L2ArmorType)_type;
  }

  public final int getItemMask()
  {
    return getItemType().mask();
  }

  public final int getMDef()
  {
    return _mDef;
  }

  public final int getPDef()
  {
    return _pDef;
  }

  public final int getAvoidModifier()
  {
    return _avoidModifier;
  }

  public final int getMpBonus()
  {
    return _mpBonus;
  }

  public final int getHpBonus()
  {
    return _hpBonus;
  }

  public L2Skill getSkill()
  {
    return _itemSkill;
  }

  public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
  {
    List funcs = new FastList();
    if (_funcTemplates != null)
    {
      for (FuncTemplate t : _funcTemplates) {
        Env env = new Env();
        env.cha = player;
        env.item = instance;
        Func f = t.getFunc(env, instance);
        if (f != null)
          funcs.add(f);
      }
    }
    return (Func[])funcs.toArray(new Func[funcs.size()]);
  }

  public int maxOlyEnch()
  {
    return Config.OLY_MAX_ARMOT_ENCH;
  }
}