package l2m.gameserver.templates.item;

import l2m.gameserver.skills.Stats;
import l2m.gameserver.skills.funcs.FuncTemplate;
import l2m.gameserver.templates.StatsSet;

public final class WeaponTemplate extends ItemTemplate
{
  private final int _soulShotCount;
  private final int _spiritShotCount;
  private final int _kamaelConvert;
  private final int _rndDam;
  private final int _atkReuse;
  private final int _mpConsume;
  private int _critical;

  public WeaponTemplate(StatsSet set)
  {
    super(set);
    type = ((ItemType)set.getEnum("type", WeaponType.class));
    _soulShotCount = set.getInteger("soulshots", 0);
    _spiritShotCount = set.getInteger("spiritshots", 0);
    _kamaelConvert = set.getInteger("kamael_convert", 0);

    _rndDam = set.getInteger("rnd_dam", 0);
    _atkReuse = set.getInteger("atk_reuse", type == WeaponType.CROSSBOW ? 820 : type == WeaponType.BOW ? 1500 : 0);
    _mpConsume = set.getInteger("mp_consume", 0);

    if (getItemType() == WeaponType.NONE)
    {
      _type1 = 1;
      _type2 = 1;
    }
    else
    {
      _type1 = 0;
      _type2 = 0;
    }

    if (getItemType() == WeaponType.PET)
    {
      _type1 = 0;

      if (_bodyPart == -100)
        _type2 = 6;
      else if (_bodyPart == -104)
        _type2 = 10;
      else if (_bodyPart == -101)
        _type2 = 7;
      else {
        _type2 = 8;
      }
      _bodyPart = 128;
    }
  }

  public WeaponType getItemType()
  {
    return (WeaponType)type;
  }

  public long getItemMask()
  {
    return getItemType().mask();
  }

  public int getSoulShotCount()
  {
    return _soulShotCount;
  }

  public int getSpiritShotCount()
  {
    return _spiritShotCount;
  }

  public int getCritical()
  {
    return _critical;
  }

  public int getRandomDamage()
  {
    return _rndDam;
  }

  public int getAttackReuseDelay()
  {
    return _atkReuse;
  }

  public int getMpConsume()
  {
    return _mpConsume;
  }

  public int getAttackRange()
  {
    switch (1.$SwitchMap$l2p$gameserver$templates$item$WeaponTemplate$WeaponType[getItemType().ordinal()])
    {
    case 1:
      return 460;
    case 2:
      return 360;
    case 3:
      return 40;
    }
    return 0;
  }

  public void attachFunc(FuncTemplate f)
  {
    if ((f._stat == Stats.CRITICAL_BASE) && (f._order == 8))
    {
      _critical = (int)Math.round(f._value / 10.0D);
    }
    super.attachFunc(f);
  }

  public int getKamaelConvert()
  {
    return _kamaelConvert;
  }

  public static enum WeaponType
    implements ItemType
  {
    NONE(1, "Shield", null), 
    SWORD(2, "Sword", Stats.SWORD_WPN_VULNERABILITY), 
    BLUNT(3, "Blunt", Stats.BLUNT_WPN_VULNERABILITY), 
    DAGGER(4, "Dagger", Stats.DAGGER_WPN_VULNERABILITY), 
    BOW(5, "Bow", Stats.BOW_WPN_VULNERABILITY), 
    POLE(6, "Pole", Stats.POLE_WPN_VULNERABILITY), 
    ETC(7, "Etc", null), 
    FIST(8, "Fist", Stats.FIST_WPN_VULNERABILITY), 
    DUAL(9, "Dual Sword", Stats.DUAL_WPN_VULNERABILITY), 
    DUALFIST(10, "Dual Fist", Stats.FIST_WPN_VULNERABILITY), 
    BIGSWORD(11, "Big Sword", Stats.SWORD_WPN_VULNERABILITY), 
    PET(12, "Pet", Stats.FIST_WPN_VULNERABILITY), 
    ROD(13, "Rod", null), 
    BIGBLUNT(14, "Big Blunt", Stats.BLUNT_WPN_VULNERABILITY), 
    CROSSBOW(15, "Crossbow", Stats.CROSSBOW_WPN_VULNERABILITY), 
    RAPIER(16, "Rapier", Stats.DAGGER_WPN_VULNERABILITY), 
    ANCIENTSWORD(17, "Ancient Sword", Stats.SWORD_WPN_VULNERABILITY), 
    DUALDAGGER(18, "Dual Dagger", Stats.DAGGER_WPN_VULNERABILITY);

    public static final WeaponType[] VALUES;
    private final long _mask;
    private final String _name;
    private final Stats _defence;

    private WeaponType(int id, String name, Stats defence) { _mask = (1L << id);
      _name = name;
      _defence = defence;
    }

    public long mask()
    {
      return _mask;
    }

    public Stats getDefence()
    {
      return _defence;
    }

    public String toString()
    {
      return _name;
    }

    static
    {
      VALUES = values();
    }
  }
}