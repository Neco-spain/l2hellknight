package net.sf.l2j.gameserver.templates;

import java.util.List;
import java.util.concurrent.TimeUnit;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;
import net.sf.l2j.gameserver.skills.funcs.Func;
import net.sf.l2j.gameserver.skills.funcs.FuncTemplate;

public abstract class L2Item
{
  public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
  public static final int TYPE1_SHIELD_ARMOR = 1;
  public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
  public static final int TYPE2_WEAPON = 0;
  public static final int TYPE2_SHIELD_ARMOR = 1;
  public static final int TYPE2_ACCESSORY = 2;
  public static final int TYPE2_QUEST = 3;
  public static final int TYPE2_MONEY = 4;
  public static final int TYPE2_OTHER = 5;
  public static final int TYPE2_PET_WOLF = 6;
  public static final int TYPE2_PET_HATCHLING = 7;
  public static final int TYPE2_PET_STRIDER = 8;
  public static final int TYPE2_PET_BABY = 9;
  public static final int SLOT_NONE = 0;
  public static final int SLOT_UNDERWEAR = 1;
  public static final int SLOT_R_EAR = 2;
  public static final int SLOT_L_EAR = 4;
  public static final int SLOT_NECK = 8;
  public static final int SLOT_R_FINGER = 16;
  public static final int SLOT_L_FINGER = 32;
  public static final int SLOT_HEAD = 64;
  public static final int SLOT_R_HAND = 128;
  public static final int SLOT_L_HAND = 256;
  public static final int SLOT_GLOVES = 512;
  public static final int SLOT_CHEST = 1024;
  public static final int SLOT_LEGS = 2048;
  public static final int SLOT_FEET = 4096;
  public static final int SLOT_BACK = 8192;
  public static final int SLOT_LR_HAND = 16384;
  public static final int SLOT_FULL_ARMOR = 32768;
  public static final int SLOT_HAIR = 65536;
  public static final int SLOT_WOLF = 131072;
  public static final int SLOT_HATCHLING = 1048576;
  public static final int SLOT_STRIDER = 2097152;
  public static final int SLOT_BABYPET = 4194304;
  public static final int SLOT_FACE = 262144;
  public static final int SLOT_DHAIR = 524288;
  public static final int MATERIAL_STEEL = 0;
  public static final int MATERIAL_FINE_STEEL = 1;
  public static final int MATERIAL_BLOOD_STEEL = 2;
  public static final int MATERIAL_BRONZE = 3;
  public static final int MATERIAL_SILVER = 4;
  public static final int MATERIAL_GOLD = 5;
  public static final int MATERIAL_MITHRIL = 6;
  public static final int MATERIAL_ORIHARUKON = 7;
  public static final int MATERIAL_PAPER = 8;
  public static final int MATERIAL_WOOD = 9;
  public static final int MATERIAL_CLOTH = 10;
  public static final int MATERIAL_LEATHER = 11;
  public static final int MATERIAL_BONE = 12;
  public static final int MATERIAL_HORN = 13;
  public static final int MATERIAL_DAMASCUS = 14;
  public static final int MATERIAL_ADAMANTAITE = 15;
  public static final int MATERIAL_CHRYSOLITE = 16;
  public static final int MATERIAL_CRYSTAL = 17;
  public static final int MATERIAL_LIQUID = 18;
  public static final int MATERIAL_SCALE_OF_DRAGON = 19;
  public static final int MATERIAL_DYESTUFF = 20;
  public static final int MATERIAL_COBWEB = 21;
  public static final int MATERIAL_SEED = 21;
  public static final int CRYSTAL_NONE = 0;
  public static final int CRYSTAL_D = 1;
  public static final int CRYSTAL_C = 2;
  public static final int CRYSTAL_B = 3;
  public static final int CRYSTAL_A = 4;
  public static final int CRYSTAL_S = 5;
  private static final int[] crystalItemId = { 0, 1458, 1459, 1460, 1461, 1462 };
  private static final int[] crystalEnchantBonusArmor = { 0, 11, 6, 11, 19, 25 };
  private static final int[] crystalEnchantBonusWeapon = { 0, 90, 45, 67, 144, 250 };
  private final int _itemId;
  private final String _name;
  private String _ex_name = "";
  private final int _type1;
  private final int _type2;
  private final int _weight;
  private final boolean _crystallizable;
  private final boolean _stackable;
  private final int _materialType;
  private final int _crystalType;
  private final int _duration;
  private final int _bodyPart;
  private final int _referencePrice;
  private final int _crystalCount;
  private final boolean _sellable;
  private final boolean _dropable;
  private final boolean _destroyable;
  private final boolean _tradeable;
  private final String _icon;
  private final boolean _oly;
  private boolean _boss = false;
  private long _expire = 0L;
  protected final Enum _type;
  protected FuncTemplate[] _funcTemplates;
  protected EffectTemplate[] _effectTemplates;
  protected L2Skill[] _skills;
  private static final Func[] _emptyFunctionSet = new Func[0];
  protected static final L2Effect[] _emptyEffectSet = new L2Effect[0];
  private int _maxEnchant;
  private boolean _hippy = false;

  protected L2Item(Enum type, StatsSet set)
  {
    _type = type;
    _itemId = set.getInteger("item_id");
    _name = set.getString("name");
    _type1 = set.getInteger("type1");
    _type2 = set.getInteger("type2");
    _weight = set.getInteger("weight");
    _crystallizable = set.getBool("crystallizable");
    _stackable = set.getBool("stackable", false);
    _materialType = set.getInteger("material");
    _crystalType = set.getInteger("crystal_type", 0);
    _duration = set.getInteger("duration");
    _bodyPart = set.getInteger("bodypart");
    _referencePrice = set.getInteger("price");
    _crystalCount = set.getInteger("crystal_count", 0);
    _sellable = set.getBool("sellable", true);
    _dropable = set.getBool("dropable", true);
    _destroyable = set.getBool("destroyable", true);
    _tradeable = set.getBool("tradeable", true);
    _icon = set.getString("icon");
    _oly = set.getBool("oly", false);
    if ((!_name.startsWith("Shadow")) && (_duration > 0)) {
      _expire = TimeUnit.MINUTES.toMillis(_duration);

      _ex_name = (_name + "(" + _duration / 60 + " \u0447\u0430\u0441a)");
    }

    Integer maxEnchant = (Integer)Config.ENCHANT_LIMITS.get(Integer.valueOf(_itemId));
    if (maxEnchant != null)
      _maxEnchant = maxEnchant.intValue();
    else {
      switch (_type2) {
      case 0:
        _maxEnchant = Config.ENCHANT_MAX_WEAPON;
        break;
      case 2:
        _maxEnchant = Config.ENCHANT_MAX_JEWELRY;
        break;
      case 1:
        _maxEnchant = Config.ENCHANT_MAX_ARMOR;
      }

    }

    if (Config.HIPPY_ITEMS.contains(Integer.valueOf(_itemId))) {
      _hippy = true;
    }

    if (Config.BOSS_ITEMS.contains(Integer.valueOf(_itemId)))
      _boss = true;
  }

  public Enum getItemType()
  {
    return _type;
  }

  public final int getDuration()
  {
    return _duration;
  }

  public final long getExpire() {
    return _expire;
  }

  public final int getItemId()
  {
    return _itemId;
  }

  public abstract int getItemMask();

  public final int getMaterialType()
  {
    return _materialType;
  }

  public final int getType2()
  {
    return _type2;
  }

  public final int getWeight()
  {
    return _weight;
  }

  public final boolean isCrystallizable()
  {
    return _crystallizable;
  }

  public final int getCrystalType()
  {
    return _crystalType;
  }

  public final int getCrystalItemId()
  {
    return crystalItemId[_crystalType];
  }

  public final int getItemGrade()
  {
    return getCrystalType();
  }

  public final int getCrystalCount()
  {
    return _crystalCount;
  }

  public final int getCrystalCount(int enchantLevel)
  {
    if (enchantLevel > 3) {
      switch (_type2) {
      case 1:
      case 2:
        return _crystalCount + crystalEnchantBonusArmor[getCrystalType()] * (3 * enchantLevel - 6);
      case 0:
        return _crystalCount + crystalEnchantBonusWeapon[getCrystalType()] * (2 * enchantLevel - 3);
      }
      return _crystalCount;
    }
    if (enchantLevel > 0) {
      switch (_type2) {
      case 1:
      case 2:
        return _crystalCount + crystalEnchantBonusArmor[getCrystalType()] * enchantLevel;
      case 0:
        return _crystalCount + crystalEnchantBonusWeapon[getCrystalType()] * enchantLevel;
      }
      return _crystalCount;
    }

    return _crystalCount;
  }

  public final String getName()
  {
    return _ex_name.equals("") ? _name : _ex_name;
  }

  public final int getBodyPart()
  {
    return _bodyPart;
  }

  public final int getType1()
  {
    return _type1;
  }

  public final boolean isStackable()
  {
    return _stackable;
  }

  public boolean isConsumable()
  {
    return false;
  }

  public final int getReferencePrice()
  {
    return isConsumable() ? (int)(_referencePrice * Config.RATE_CONSUMABLE_COST) : _referencePrice;
  }

  public final boolean isSellable()
  {
    return _sellable;
  }

  public final boolean isDropable()
  {
    return _dropable;
  }

  public final boolean isDestroyable()
  {
    return _destroyable;
  }

  public final boolean isTradeable()
  {
    return _tradeable;
  }

  public boolean isForHatchling()
  {
    return _type2 == 7;
  }

  public boolean isForStrider()
  {
    return _type2 == 8;
  }

  public boolean isForWolf()
  {
    return _type2 == 6;
  }

  public boolean isForBabyPet()
  {
    return _type2 == 9;
  }

  public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
  {
    if (_funcTemplates == null) {
      return _emptyFunctionSet;
    }
    List funcs = new FastList();
    for (FuncTemplate t : _funcTemplates) {
      Env env = new Env();
      env.cha = player;
      env.target = player;
      env.item = instance;
      Func f = t.getFunc(env, this);
      if (f != null) {
        funcs.add(f);
      }
    }
    if (funcs.isEmpty()) {
      return _emptyFunctionSet;
    }
    return (Func[])funcs.toArray(new Func[funcs.size()]);
  }

  public L2Effect[] getEffects(L2ItemInstance instance, L2Character player)
  {
    if (_effectTemplates == null) {
      return _emptyEffectSet;
    }
    List effects = new FastList();
    for (EffectTemplate et : _effectTemplates) {
      Env env = new Env();
      env.cha = player;
      env.target = player;
      env.item = instance;
      L2Effect e = et.getEffect(env);
      if (e != null) {
        effects.add(e);
      }
    }
    if (effects.isEmpty()) {
      return _emptyEffectSet;
    }
    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public L2Effect[] getSkillEffects(L2Character caster, L2Character target)
  {
    if (_skills == null) {
      return _emptyEffectSet;
    }
    List effects = new FastList();

    for (L2Skill skill : _skills) {
      if (!skill.checkCondition(caster, target, true)) {
        continue;
      }
      if (target.getFirstEffect(skill.getId()) != null) {
        target.removeEffect(target.getFirstEffect(skill.getId()));
      }
      for (L2Effect e : skill.getEffects(caster, target)) {
        effects.add(e);
      }
    }
    if (effects.isEmpty()) {
      return _emptyEffectSet;
    }
    return (L2Effect[])effects.toArray(new L2Effect[effects.size()]);
  }

  public void attach(FuncTemplate f)
  {
    if (_funcTemplates == null) {
      _funcTemplates = new FuncTemplate[] { f };
    } else {
      int len = _funcTemplates.length;
      FuncTemplate[] tmp = new FuncTemplate[len + 1];

      System.arraycopy(_funcTemplates, 0, tmp, 0, len);
      tmp[len] = f;
      _funcTemplates = tmp;
    }
  }

  public void attach(EffectTemplate effect)
  {
    if (_effectTemplates == null) {
      _effectTemplates = new EffectTemplate[] { effect };
    } else {
      int len = _effectTemplates.length;
      EffectTemplate[] tmp = new EffectTemplate[len + 1];

      System.arraycopy(_effectTemplates, 0, tmp, 0, len);
      tmp[len] = effect;
      _effectTemplates = tmp;
    }
  }

  public void attach(L2Skill skill)
  {
    if (_skills == null) {
      _skills = new L2Skill[] { skill };
    } else {
      int len = _skills.length;
      L2Skill[] tmp = new L2Skill[len + 1];

      System.arraycopy(_skills, 0, tmp, 0, len);
      tmp[len] = skill;
      _skills = tmp;
    }
  }

  public String toString()
  {
    return _name;
  }

  public String getIcon()
  {
    return _icon;
  }

  public boolean notForOly() {
    return _oly;
  }

  public int maxOlyEnch() {
    return 65535;
  }

  public int getMaxEnchant() {
    return _maxEnchant;
  }

  public boolean isHippy() {
    return _hippy;
  }

  public boolean isNotForBossZone() {
    return _boss;
  }
}