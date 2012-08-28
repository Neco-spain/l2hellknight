package l2m.gameserver.skills;

import l2m.gameserver.Config;
import l2m.gameserver.model.Creature;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;
import l2m.gameserver.model.base.BaseStats;
import l2m.gameserver.model.base.ClassId;
import l2m.gameserver.model.base.ClassType2;
import l2m.gameserver.model.base.Element;
import l2m.gameserver.model.base.Race;
import l2m.gameserver.model.entity.SevenSigns;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.skills.conditions.ConditionPlayerState;
import l2m.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import l2m.gameserver.skills.funcs.Func;
import l2m.gameserver.templates.CharTemplate;
import l2m.gameserver.templates.PlayerTemplate;
import l2m.gameserver.templates.item.WeaponTemplate;
import l2m.gameserver.templates.item.WeaponTemplate.WeaponType;

public class StatFunctions
{
  public static void addPredefinedFuncs(Creature cha)
  {
    if (cha.isPlayer())
    {
      cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_CP_RATE));
      cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_CP_RATE));
      cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_CP_RATE));
      cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_HP_RATE));
      cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_HP_RATE));
      cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_HP_RATE));
      cha.addStatFunc(FuncMultRegenResting.getFunc(Stats.REGENERATE_MP_RATE));
      cha.addStatFunc(FuncMultRegenStanding.getFunc(Stats.REGENERATE_MP_RATE));
      cha.addStatFunc(FuncMultRegenRunning.getFunc(Stats.REGENERATE_MP_RATE));

      cha.addStatFunc(FuncMaxCpAdd.func);
      cha.addStatFunc(FuncMaxHpAdd.func);
      cha.addStatFunc(FuncMaxMpAdd.func);

      cha.addStatFunc(FuncMaxCpMul.func);
      cha.addStatFunc(FuncMaxHpMul.func);
      cha.addStatFunc(FuncMaxMpMul.func);

      cha.addStatFunc(FuncAttackRange.func);

      cha.addStatFunc(FuncMoveSpeedMul.func);

      cha.addStatFunc(FuncHennaSTR.func);
      cha.addStatFunc(FuncHennaDEX.func);
      cha.addStatFunc(FuncHennaINT.func);
      cha.addStatFunc(FuncHennaMEN.func);
      cha.addStatFunc(FuncHennaCON.func);
      cha.addStatFunc(FuncHennaWIT.func);

      cha.addStatFunc(FuncInventory.func);
      cha.addStatFunc(FuncWarehouse.func);
      cha.addStatFunc(FuncTradeLimit.func);

      cha.addStatFunc(FuncSDefPlayers.func);

      cha.addStatFunc(FuncMaxHpLimit.func);
      cha.addStatFunc(FuncMaxMpLimit.func);
      cha.addStatFunc(FuncMaxCpLimit.func);
      cha.addStatFunc(FuncRunSpdLimit.func);
      cha.addStatFunc(FuncRunSpdLimit.func);
      cha.addStatFunc(FuncPDefLimit.func);
      cha.addStatFunc(FuncMDefLimit.func);
      cha.addStatFunc(FuncPAtkLimit.func);
      cha.addStatFunc(FuncMAtkLimit.func);
    }

    if ((cha.isPlayer()) || (cha.isPet()))
    {
      cha.addStatFunc(FuncPAtkMul.func);
      cha.addStatFunc(FuncMAtkMul.func);
      cha.addStatFunc(FuncPDefMul.func);
      cha.addStatFunc(FuncMDefMul.func);
    }

    if (cha.isSummon())
    {
      cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.FIRE));
      cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.WATER));
      cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.EARTH));
      cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.WIND));
      cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.HOLY));
      cha.addStatFunc(FuncAttributeAttackSet.getFunc(Element.UNHOLY));

      cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.FIRE));
      cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.WATER));
      cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.EARTH));
      cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.WIND));
      cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.HOLY));
      cha.addStatFunc(FuncAttributeDefenceSet.getFunc(Element.UNHOLY));
    }

    if (!cha.isPet())
    {
      cha.addStatFunc(FuncAccuracyAdd.func);
      cha.addStatFunc(FuncEvasionAdd.func);
    }

    if ((!cha.isPet()) && (!cha.isSummon()))
    {
      cha.addStatFunc(FuncPAtkSpeedMul.func);
      cha.addStatFunc(FuncMAtkSpeedMul.func);
      cha.addStatFunc(FuncSDefInit.func);
      cha.addStatFunc(FuncSDefAll.func);
    }

    cha.addStatFunc(FuncPAtkSpdLimit.func);
    cha.addStatFunc(FuncMAtkSpdLimit.func);
    cha.addStatFunc(FuncCAtkLimit.func);
    cha.addStatFunc(FuncEvasionLimit.func);
    cha.addStatFunc(FuncAccuracyLimit.func);
    cha.addStatFunc(FuncCritLimit.func);
    cha.addStatFunc(FuncMCritLimit.func);

    cha.addStatFunc(FuncMCriticalRateMul.func);
    cha.addStatFunc(FuncPCriticalRateMul.func);
    cha.addStatFunc(FuncPDamageResists.func);
    cha.addStatFunc(FuncMDamageResists.func);

    cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.FIRE));
    cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WATER));
    cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.EARTH));
    cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.WIND));
    cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.HOLY));
    cha.addStatFunc(FuncAttributeAttackInit.getFunc(Element.UNHOLY));

    cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.FIRE));
    cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WATER));
    cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.EARTH));
    cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.WIND));
    cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.HOLY));
    cha.addStatFunc(FuncAttributeDefenceInit.getFunc(Element.UNHOLY));
  }

  private static class FuncAttributeDefenceSet extends Func
  {
    static final Func[] func = new FuncAttributeDefenceSet[Element.VALUES.length];

    static Func getFunc(Element element)
    {
      return func[element.getId()];
    }

    private FuncAttributeDefenceSet(Stats stat)
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      if (env.character.getPlayer().getClassId().getType2() == ClassType2.Summoner)
        env.value = env.character.getPlayer().calcStat(stat, 0.0D);
    }

    static
    {
      for (int i = 0; i < Element.VALUES.length; i++)
        func[i] = new FuncAttributeDefenceSet(Element.VALUES[i].getDefence());
    }
  }

  private static class FuncAttributeAttackSet extends Func
  {
    static final Func[] func = new FuncAttributeAttackSet[Element.VALUES.length];

    static Func getFunc(Element element)
    {
      return func[element.getId()];
    }

    private FuncAttributeAttackSet(Stats stat)
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      if (env.character.getPlayer().getClassId().getType2() == ClassType2.Summoner)
        env.value = env.character.getPlayer().calcStat(stat, 0.0D);
    }

    static
    {
      for (int i = 0; i < Element.VALUES.length; i++)
        func[i] = new FuncAttributeAttackSet(Element.VALUES[i].getAttack());
    }
  }

  private static class FuncAttributeDefenceInit extends Func
  {
    static final Func[] func = new FuncAttributeDefenceInit[Element.VALUES.length];
    private Element element;

    static Func getFunc(Element element)
    {
      return func[element.getId()];
    }

    private FuncAttributeDefenceInit(Element element)
    {
      super(1, null);
      this.element = element;
    }

    public void calc(Env env)
    {
      env.value += env.character.getTemplate().baseAttributeDefence[element.getId()];
    }

    static
    {
      for (int i = 0; i < Element.VALUES.length; i++)
        func[i] = new FuncAttributeDefenceInit(Element.VALUES[i]);
    }
  }

  private static class FuncAttributeAttackInit extends Func
  {
    static final Func[] func = new FuncAttributeAttackInit[Element.VALUES.length];
    private Element element;

    static Func getFunc(Element element)
    {
      return func[element.getId()];
    }

    private FuncAttributeAttackInit(Element element)
    {
      super(1, null);
      this.element = element;
    }

    public void calc(Env env)
    {
      env.value += env.character.getTemplate().baseAttributeAttack[element.getId()];
    }

    static
    {
      for (int i = 0; i < Element.VALUES.length; i++)
        func[i] = new FuncAttributeAttackInit(Element.VALUES[i]);
    }
  }

  private static class FuncMCritLimit extends Func
  {
    static final Func func = new FuncMCritLimit();

    private FuncMCritLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_MCRIT, env.value);
    }
  }

  private static class FuncCritLimit extends Func
  {
    static final Func func = new FuncCritLimit();

    private FuncCritLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_CRIT, env.value);
    }
  }

  private static class FuncAccuracyLimit extends Func
  {
    static final Func func = new FuncAccuracyLimit();

    private FuncAccuracyLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_ACCURACY, env.value);
    }
  }

  private static class FuncEvasionLimit extends Func
  {
    static final Func func = new FuncEvasionLimit();

    private FuncEvasionLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_EVASION, env.value);
    }
  }

  private static class FuncCAtkLimit extends Func
  {
    static final Func func = new FuncCAtkLimit();

    private FuncCAtkLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_CRIT_DAM / 2.0D, env.value);
    }
  }

  private static class FuncMAtkSpdLimit extends Func
  {
    static final Func func = new FuncMAtkSpdLimit();

    private FuncMAtkSpdLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_MATK_SPD, env.value);
    }
  }

  private static class FuncPAtkSpdLimit extends Func
  {
    static final Func func = new FuncPAtkSpdLimit();

    private FuncPAtkSpdLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_PATK_SPD, env.value);
    }
  }

  private static class FuncMAtkLimit extends Func
  {
    static final Func func = new FuncMAtkLimit();

    private FuncMAtkLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_MATK, env.value);
    }
  }

  private static class FuncPAtkLimit extends Func
  {
    static final Func func = new FuncPAtkLimit();

    private FuncPAtkLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_PATK, env.value);
    }
  }

  private static class FuncMDefLimit extends Func
  {
    static final Func func = new FuncMDefLimit();

    private FuncMDefLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_MDEF, env.value);
    }
  }

  private static class FuncPDefLimit extends Func
  {
    static final Func func = new FuncPDefLimit();

    private FuncPDefLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_PDEF, env.value);
    }
  }

  private static class FuncRunSpdLimit extends Func
  {
    static final Func func = new FuncRunSpdLimit();

    private FuncRunSpdLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(Config.LIM_MOVE, env.value);
    }
  }

  private static class FuncMaxCpLimit extends Func
  {
    static final Func func = new FuncMaxCpLimit();

    private FuncMaxCpLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(100000.0D, env.value);
    }
  }

  private static class FuncMaxMpLimit extends Func
  {
    static final Func func = new FuncMaxMpLimit();

    private FuncMaxMpLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(40000.0D, env.value);
    }
  }

  private static class FuncMaxHpLimit extends Func
  {
    static final Func func = new FuncMaxHpLimit();

    private FuncMaxHpLimit()
    {
      super(256, null);
    }

    public void calc(Env env)
    {
      env.value = Math.min(40000.0D, env.value);
    }
  }

  private static class FuncSDefPlayers extends Func
  {
    static final FuncSDefPlayers func = new FuncSDefPlayers();

    private FuncSDefPlayers()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      if (env.value == 0.0D) {
        return;
      }
      Creature cha = env.character;
      ItemInstance shld = ((Player)cha).getInventory().getPaperdollItem(8);
      if ((shld == null) || (shld.getItemType() != WeaponTemplate.WeaponType.NONE))
        return;
      env.value *= BaseStats.DEX.calcBonus(env.character);
    }
  }

  private static class FuncSDefAll extends Func
  {
    static final FuncSDefAll func = new FuncSDefAll();

    private FuncSDefAll()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      if (env.value == 0.0D) {
        return;
      }
      Creature target = env.target;
      if (target != null)
      {
        WeaponTemplate weapon = target.getActiveWeaponItem();
        if (weapon != null)
          switch (StatFunctions.1.$SwitchMap$l2p$gameserver$templates$item$WeaponTemplate$WeaponType[weapon.getItemType().ordinal()])
          {
          case 1:
          case 2:
            env.value += 30.0D;
            break;
          case 3:
          case 4:
            env.value += 12.0D;
          }
      }
    }
  }

  private static class FuncSDefInit extends Func
  {
    static final Func func = new FuncSDefInit();

    private FuncSDefInit()
    {
      super(1, null);
    }

    public void calc(Env env)
    {
      Creature cha = env.character;
      env.value = cha.getTemplate().baseShldRate;
    }
  }

  private static class FuncTradeLimit extends Func
  {
    static final FuncTradeLimit func = new FuncTradeLimit();

    private FuncTradeLimit()
    {
      super(1, null);
    }

    public void calc(Env env)
    {
      Player _cha = (Player)env.character;
      if (_cha.getRace() == Race.dwarf)
        env.value = Config.MAX_PVTSTORE_SLOTS_DWARF;
      else
        env.value = Config.MAX_PVTSTORE_SLOTS_OTHER;
    }
  }

  private static class FuncWarehouse extends Func
  {
    static final FuncWarehouse func = new FuncWarehouse();

    private FuncWarehouse()
    {
      super(1, null);
    }

    public void calc(Env env)
    {
      Player player = (Player)env.character;
      if (player.getTemplate().race == Race.dwarf)
        env.value = Config.WAREHOUSE_SLOTS_DWARF;
      else
        env.value = Config.WAREHOUSE_SLOTS_NO_DWARF;
      env.value += player.getExpandWarehouse();
    }
  }

  private static class FuncInventory extends Func
  {
    static final FuncInventory func = new FuncInventory();

    private FuncInventory()
    {
      super(1, null);
    }

    public void calc(Env env)
    {
      Player player = (Player)env.character;
      if (player.isGM())
        env.value = Config.INVENTORY_MAXIMUM_GM;
      else if (player.getTemplate().race == Race.dwarf)
        env.value = Config.INVENTORY_MAXIMUM_DWARF;
      else
        env.value = Config.INVENTORY_MAXIMUM_NO_DWARF;
      env.value += player.getExpandInventory();
      env.value = Math.min(env.value, Config.SERVICES_EXPAND_INVENTORY_MAX);
    }
  }

  private static class FuncMDamageResists extends Func
  {
    static final FuncMDamageResists func = new FuncMDamageResists();

    private FuncMDamageResists()
    {
      super(48, null);
    }

    public void calc(Env env)
    {
      if ((env.target.isRaid()) && (Math.abs(env.character.getLevel() - env.target.getLevel()) > Config.RAID_MAX_LEVEL_DIFF))
      {
        env.value = 1.0D;
        return;
      }
      env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
    }
  }

  private static class FuncPDamageResists extends Func
  {
    static final FuncPDamageResists func = new FuncPDamageResists();

    private FuncPDamageResists()
    {
      super(48, null);
    }

    public void calc(Env env)
    {
      if ((env.target.isRaid()) && (env.character.getLevel() - env.target.getLevel() > Config.RAID_MAX_LEVEL_DIFF))
      {
        env.value = 1.0D;
        return;
      }

      WeaponTemplate weapon = env.character.getActiveWeaponItem();
      if (weapon == null)
        env.value *= 0.01D * env.target.calcStat(Stats.FIST_WPN_VULNERABILITY, env.character, env.skill);
      else if (weapon.getItemType().getDefence() != null) {
        env.value *= 0.01D * env.target.calcStat(weapon.getItemType().getDefence(), env.character, env.skill);
      }
      env.value = Formulas.calcDamageResists(env.skill, env.character, env.target, env.value);
    }
  }

  private static class FuncMaxMpMul extends Func
  {
    static final FuncMaxMpMul func = new FuncMaxMpMul();

    private FuncMaxMpMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.MEN.calcBonus(env.character);
    }
  }

  private static class FuncMaxMpAdd extends Func
  {
    static final FuncMaxMpAdd func = new FuncMaxMpAdd();

    private FuncMaxMpAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      PlayerTemplate t = (PlayerTemplate)env.character.getTemplate();
      int lvl = Math.max(0, env.character.getLevel() - t.classBaseLevel);
      double mpmod = t.lvlMpMod * lvl;
      double mpmax = (t.lvlMpAdd + mpmod) * lvl;
      double mpmin = t.lvlMpAdd * lvl + mpmod;
      env.value += (mpmax + mpmin) / 2.0D;
    }
  }

  private static class FuncMaxCpMul extends Func
  {
    static final FuncMaxCpMul func = new FuncMaxCpMul();

    private FuncMaxCpMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      double cpSSmod = 1.0D;
      int sealOwnedBy = SevenSigns.getInstance().getSealOwner(3);
      int playerCabal = SevenSigns.getInstance().getPlayerCabal((Player)env.character);

      if (sealOwnedBy != 0) {
        if (playerCabal == sealOwnedBy)
          cpSSmod = 1.1D;
        else
          cpSSmod = 0.9D;
      }
      env.value *= BaseStats.CON.calcBonus(env.character) * cpSSmod;
    }
  }

  private static class FuncMaxCpAdd extends Func
  {
    static final FuncMaxCpAdd func = new FuncMaxCpAdd();

    private FuncMaxCpAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      PlayerTemplate t = (PlayerTemplate)env.character.getTemplate();
      int lvl = Math.max(0, env.character.getLevel() - t.classBaseLevel);
      double cpmod = t.lvlCpMod * lvl;
      double cpmax = (t.lvlCpAdd + cpmod) * lvl;
      double cpmin = t.lvlCpAdd * lvl + cpmod;
      env.value += (cpmax + cpmin) / 2.0D;
    }
  }

  private static class FuncMaxHpMul extends Func
  {
    static final FuncMaxHpMul func = new FuncMaxHpMul();

    private FuncMaxHpMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.CON.calcBonus(env.character);
    }
  }

  private static class FuncMaxHpAdd extends Func
  {
    static final FuncMaxHpAdd func = new FuncMaxHpAdd();

    private FuncMaxHpAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      PlayerTemplate t = (PlayerTemplate)env.character.getTemplate();
      int lvl = Math.max(0, env.character.getLevel() - t.classBaseLevel);
      double hpmod = t.lvlHpMod * lvl;
      double hpmax = (t.lvlHpAdd + hpmod) * lvl;
      double hpmin = t.lvlHpAdd * lvl + hpmod;
      env.value += (hpmax + hpmin) / 2.0D;
    }
  }

  private static class FuncHennaWIT extends Func
  {
    static final FuncHennaWIT func = new FuncHennaWIT();

    private FuncHennaWIT()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      Player pc = (Player)env.character;
      if (pc != null)
        env.value = Math.max(1.0D, env.value + pc.getHennaStatWIT());
    }
  }

  private static class FuncHennaCON extends Func
  {
    static final FuncHennaCON func = new FuncHennaCON();

    private FuncHennaCON()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      Player pc = (Player)env.character;
      if (pc != null)
        env.value = Math.max(1.0D, env.value + pc.getHennaStatCON());
    }
  }

  private static class FuncHennaMEN extends Func
  {
    static final FuncHennaMEN func = new FuncHennaMEN();

    private FuncHennaMEN()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      Player pc = (Player)env.character;
      if (pc != null)
        env.value = Math.max(1.0D, env.value + pc.getHennaStatMEN());
    }
  }

  private static class FuncHennaINT extends Func
  {
    static final FuncHennaINT func = new FuncHennaINT();

    private FuncHennaINT()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      Player pc = (Player)env.character;
      if (pc != null)
        env.value = Math.max(1.0D, env.value + pc.getHennaStatINT());
    }
  }

  private static class FuncHennaDEX extends Func
  {
    static final FuncHennaDEX func = new FuncHennaDEX();

    private FuncHennaDEX()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      Player pc = (Player)env.character;
      if (pc != null)
        env.value = Math.max(1.0D, env.value + pc.getHennaStatDEX());
    }
  }

  private static class FuncHennaSTR extends Func
  {
    static final FuncHennaSTR func = new FuncHennaSTR();

    private FuncHennaSTR()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      Player pc = (Player)env.character;
      if (pc != null)
        env.value = Math.max(1.0D, env.value + pc.getHennaStatSTR());
    }
  }

  private static class FuncMAtkSpeedMul extends Func
  {
    static final FuncMAtkSpeedMul func = new FuncMAtkSpeedMul();

    private FuncMAtkSpeedMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.WIT.calcBonus(env.character);
    }
  }

  private static class FuncPAtkSpeedMul extends Func
  {
    static final FuncPAtkSpeedMul func = new FuncPAtkSpeedMul();

    private FuncPAtkSpeedMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.DEX.calcBonus(env.character);
    }
  }

  private static class FuncMoveSpeedMul extends Func
  {
    static final FuncMoveSpeedMul func = new FuncMoveSpeedMul();

    private FuncMoveSpeedMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.DEX.calcBonus(env.character);
    }
  }

  private static class FuncPCriticalRateMul extends Func
  {
    static final FuncPCriticalRateMul func = new FuncPCriticalRateMul();

    private FuncPCriticalRateMul()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      if (!(env.character instanceof Summon))
        env.value *= BaseStats.DEX.calcBonus(env.character);
      env.value *= 0.01D * env.character.calcStat(Stats.CRITICAL_RATE, env.target, env.skill);
    }
  }

  private static class FuncMCriticalRateMul extends Func
  {
    static final FuncMCriticalRateMul func = new FuncMCriticalRateMul();

    private FuncMCriticalRateMul()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.WIT.calcBonus(env.character);
    }
  }

  private static class FuncEvasionAdd extends Func
  {
    static final FuncEvasionAdd func = new FuncEvasionAdd();

    private FuncEvasionAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      env.value += Math.sqrt(env.character.getDEX()) * 6.0D + env.character.getLevel();

      if (env.character.getLevel() > 77)
        env.value += env.character.getLevel() - 77;
      if (env.character.getLevel() > 69)
        env.value += env.character.getLevel() - 69;
    }
  }

  private static class FuncAccuracyAdd extends Func
  {
    static final FuncAccuracyAdd func = new FuncAccuracyAdd();

    private FuncAccuracyAdd()
    {
      super(16, null);
    }

    public void calc(Env env)
    {
      if (env.character.isPet()) {
        return;
      }

      env.value += Math.sqrt(env.character.getDEX()) * 6.0D + env.character.getLevel();

      if (env.character.isSummon()) {
        env.value += (env.character.getLevel() < 60 ? 4.0D : 5.0D);
      }
      if (env.character.getLevel() > 77)
        env.value += env.character.getLevel() - 77;
      if (env.character.getLevel() > 69)
        env.value += env.character.getLevel() - 69;
    }
  }

  private static class FuncAttackRange extends Func
  {
    static final FuncAttackRange func = new FuncAttackRange();

    private FuncAttackRange()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      WeaponTemplate weapon = env.character.getActiveWeaponItem();
      if (weapon != null)
        env.value += weapon.getAttackRange();
    }
  }

  private static class FuncMDefMul extends Func
  {
    static final FuncMDefMul func = new FuncMDefMul();

    private FuncMDefMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.MEN.calcBonus(env.character) * env.character.getLevelMod();
    }
  }

  private static class FuncPDefMul extends Func
  {
    static final FuncPDefMul func = new FuncPDefMul();

    private FuncPDefMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= env.character.getLevelMod();
    }
  }

  private static class FuncMAtkMul extends Func
  {
    static final FuncMAtkMul func = new FuncMAtkMul();

    private FuncMAtkMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      double ib = BaseStats.INT.calcBonus(env.character);
      double lvlb = env.character.getLevelMod();
      env.value *= lvlb * lvlb * ib * ib;
    }
  }

  private static class FuncPAtkMul extends Func
  {
    static final FuncPAtkMul func = new FuncPAtkMul();

    private FuncPAtkMul()
    {
      super(32, null);
    }

    public void calc(Env env)
    {
      env.value *= BaseStats.STR.calcBonus(env.character) * env.character.getLevelMod();
    }
  }

  private static class FuncMultRegenRunning extends Func
  {
    static final FuncMultRegenRunning[] func = new FuncMultRegenRunning[Stats.NUM_STATS];

    static Func getFunc(Stats stat)
    {
      int pos = stat.ordinal();
      if (func[pos] == null)
        func[pos] = new FuncMultRegenRunning(stat);
      return func[pos];
    }

    private FuncMultRegenRunning(Stats stat)
    {
      super(48, null);
      setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RUNNING, true));
    }

    public void calc(Env env)
    {
      env.value *= 0.7D;
    }
  }

  private static class FuncMultRegenStanding extends Func
  {
    static final FuncMultRegenStanding[] func = new FuncMultRegenStanding[Stats.NUM_STATS];

    static Func getFunc(Stats stat)
    {
      int pos = stat.ordinal();
      if (func[pos] == null)
        func[pos] = new FuncMultRegenStanding(stat);
      return func[pos];
    }

    private FuncMultRegenStanding(Stats stat)
    {
      super(48, null);
      setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.STANDING, true));
    }

    public void calc(Env env)
    {
      env.value *= 1.1D;
    }
  }

  private static class FuncMultRegenResting extends Func
  {
    static final FuncMultRegenResting[] func = new FuncMultRegenResting[Stats.NUM_STATS];

    static Func getFunc(Stats stat)
    {
      int pos = stat.ordinal();
      if (func[pos] == null)
        func[pos] = new FuncMultRegenResting(stat);
      return func[pos];
    }

    private FuncMultRegenResting(Stats stat)
    {
      super(48, null);
      setCondition(new ConditionPlayerState(ConditionPlayerState.CheckPlayerState.RESTING, true));
    }

    public void calc(Env env)
    {
      if ((env.character.isPlayer()) && (env.character.getLevel() <= 40) && (((Player)env.character).getClassId().getLevel() < 3) && (stat == Stats.REGENERATE_HP_RATE))
        env.value *= 6.0D;
      else
        env.value *= 1.5D;
    }
  }
}