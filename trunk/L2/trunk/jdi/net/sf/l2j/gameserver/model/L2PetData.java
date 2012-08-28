package net.sf.l2j.gameserver.model;

public class L2PetData
{
  public static final String PET_TYPE = "typeID";
  public static final String PET_LEVEL = "level";
  public static final String PET_MAX_EXP = "expMax";
  public static final String PET_MAX_HP = "hpMax";
  public static final String PET_MAX_MP = "mpMax";
  public static final String PET_PATK = "patk";
  public static final String PET_PDEF = "pdef";
  public static final String PET_MATK = "matk";
  public static final String PET_MDEF = "mdef";
  public static final String PET_ACCURACY = "acc";
  public static final String PET_EVASION = "evasion";
  public static final String PET_CRITICAL = "crit";
  public static final String PET_SPEED = "speed";
  public static final String PET_ATK_SPEED = "atk_speed";
  public static final String PET_CAST_SPEED = "cast_speed";
  public static final String PET_MAX_FEED = "feedMax";
  public static final String PET_FEED_BATTLE = "feedbattle";
  public static final String PET_FEED_NORMAL = "feednormal";
  public static final String PET_MAX_LOAD = "loadMax";
  public static final String PET_REGEN_HP = "hpregen";
  public static final String PET_REGEN_MP = "mpregen";
  public static final String OWNER_EXP_TAKEN = "owner_exp_taken";
  private int _petId;
  private int _petLevel;
  private float _ownerExpTaken;
  private long _petMaxExp;
  private int _petMaxHP;
  private int _petMaxMP;
  private int _petPAtk;
  private int _petPDef;
  private int _petMAtk;
  private int _petMDef;
  private int _petAccuracy;
  private int _petEvasion;
  private int _petCritical;
  private int _petSpeed;
  private int _petAtkSpeed;
  private int _petCastSpeed;
  private int _petMaxFeed;
  private int _petFeedBattle;
  private int _petFeedNormal;
  private int _petMaxLoad;
  private int _petRegenHP;
  private int _petRegenMP;

  public void setStat(String stat, int value)
  {
    if (stat.equalsIgnoreCase("expMax"))
    {
      setPetMaxExp(value);
    }
    else if (stat.equalsIgnoreCase("hpMax"))
    {
      setPetMaxHP(value);
    }
    else if (stat.equalsIgnoreCase("mpMax"))
    {
      setPetMaxMP(value);
    }
    else if (stat.equalsIgnoreCase("patk"))
    {
      setPetPAtk(value);
    }
    else if (stat.equalsIgnoreCase("pdef"))
    {
      setPetPDef(value);
    }
    else if (stat.equalsIgnoreCase("matk"))
    {
      setPetMAtk(value);
    }
    else if (stat.equalsIgnoreCase("mdef"))
    {
      setPetMDef(value);
    }
    else if (stat.equalsIgnoreCase("acc"))
    {
      setPetAccuracy(value);
    }
    else if (stat.equalsIgnoreCase("evasion"))
    {
      setPetEvasion(value);
    }
    else if (stat.equalsIgnoreCase("crit"))
    {
      setPetCritical(value);
    }
    else if (stat.equalsIgnoreCase("speed"))
    {
      setPetSpeed(value);
    }
    else if (stat.equalsIgnoreCase("atk_speed"))
    {
      setPetAtkSpeed(value);
    }
    else if (stat.equalsIgnoreCase("cast_speed"))
    {
      setPetCastSpeed(value);
    }
    else if (stat.equalsIgnoreCase("feedMax"))
    {
      setPetMaxFeed(value);
    }
    else if (stat.equalsIgnoreCase("feednormal"))
    {
      setPetFeedNormal(value);
    }
    else if (stat.equalsIgnoreCase("feedbattle"))
    {
      setPetFeedBattle(value);
    }
    else if (stat.equalsIgnoreCase("loadMax"))
    {
      setPetMaxLoad(value);
    }
    else if (stat.equalsIgnoreCase("hpregen"))
    {
      setPetRegenHP(value);
    }
    else if (stat.equalsIgnoreCase("mpregen"))
    {
      setPetRegenMP(value);
    }
  }

  public void setStat(String stat, long value)
  {
    if (stat.equalsIgnoreCase("expMax"))
    {
      setPetMaxExp(value);
    }
  }

  public void setStat(String stat, float value)
  {
    if (stat.equalsIgnoreCase("owner_exp_taken"))
    {
      setOwnerExpTaken(value);
    }
  }

  public int getPetID()
  {
    return _petId;
  }

  public void setPetID(int pPetID)
  {
    _petId = pPetID;
  }

  public int getPetLevel()
  {
    return _petLevel;
  }

  public void setPetLevel(int pPetLevel)
  {
    _petLevel = pPetLevel;
  }

  public long getPetMaxExp()
  {
    return _petMaxExp;
  }

  public void setPetMaxExp(long pPetMaxExp)
  {
    _petMaxExp = pPetMaxExp;
  }

  public float getOwnerExpTaken()
  {
    return _ownerExpTaken;
  }

  public void setOwnerExpTaken(float pOwnerExpTaken)
  {
    _ownerExpTaken = pOwnerExpTaken;
  }

  public int getPetMaxHP()
  {
    return _petMaxHP;
  }

  public void setPetMaxHP(int pPetMaxHP)
  {
    _petMaxHP = pPetMaxHP;
  }

  public int getPetMaxMP()
  {
    return _petMaxMP;
  }

  public void setPetMaxMP(int pPetMaxMP)
  {
    _petMaxMP = pPetMaxMP;
  }

  public int getPetPAtk()
  {
    return _petPAtk;
  }

  public void setPetPAtk(int pPetPAtk)
  {
    _petPAtk = pPetPAtk;
  }

  public int getPetPDef()
  {
    return _petPDef;
  }

  public void setPetPDef(int pPetPDef)
  {
    _petPDef = pPetPDef;
  }

  public int getPetMAtk()
  {
    return _petMAtk;
  }

  public void setPetMAtk(int pPetMAtk)
  {
    _petMAtk = pPetMAtk;
  }

  public int getPetMDef()
  {
    return _petMDef;
  }

  public void setPetMDef(int pPetMDef)
  {
    _petMDef = pPetMDef;
  }

  public int getPetAccuracy()
  {
    return _petAccuracy;
  }

  public void setPetAccuracy(int pPetAccuracy)
  {
    _petAccuracy = pPetAccuracy;
  }

  public int getPetEvasion()
  {
    return _petEvasion;
  }

  public void setPetEvasion(int pPetEvasion)
  {
    _petEvasion = pPetEvasion;
  }

  public int getPetCritical()
  {
    return _petCritical;
  }

  public void setPetCritical(int pPetCritical)
  {
    _petCritical = pPetCritical;
  }

  public int getPetSpeed()
  {
    return _petSpeed;
  }

  public void setPetSpeed(int pPetSpeed)
  {
    _petSpeed = pPetSpeed;
  }

  public int getPetAtkSpeed()
  {
    return _petAtkSpeed;
  }

  public void setPetAtkSpeed(int pPetAtkSpeed)
  {
    _petAtkSpeed = pPetAtkSpeed;
  }

  public int getPetCastSpeed()
  {
    return _petCastSpeed;
  }

  public void setPetCastSpeed(int pPetCastSpeed)
  {
    _petCastSpeed = pPetCastSpeed;
  }

  public int getPetMaxFeed()
  {
    return _petMaxFeed;
  }

  public void setPetMaxFeed(int pPetMaxFeed)
  {
    _petMaxFeed = pPetMaxFeed;
  }

  public int getPetFeedNormal()
  {
    return _petFeedNormal;
  }

  public void setPetFeedNormal(int pPetFeedNormal)
  {
    _petFeedNormal = pPetFeedNormal;
  }

  public int getPetFeedBattle()
  {
    return _petFeedBattle;
  }

  public void setPetFeedBattle(int pPetFeedBattle)
  {
    _petFeedBattle = pPetFeedBattle;
  }

  public int getPetMaxLoad()
  {
    return _petMaxLoad;
  }

  public void setPetMaxLoad(int pPetMaxLoad)
  {
    _petMaxLoad = pPetMaxLoad;
  }

  public int getPetRegenHP()
  {
    return _petRegenHP;
  }

  public void setPetRegenHP(int pPetRegenHP)
  {
    _petRegenHP = pPetRegenHP;
  }

  public int getPetRegenMP()
  {
    return _petRegenMP;
  }

  public void setPetRegenMP(int pPetRegenMP)
  {
    _petRegenMP = pPetRegenMP;
  }

  public String toString()
  {
    return "PetID: " + getPetID() + " \t" + "PetLevel: " + getPetLevel() + " \t" + "expMax" + ": " + getPetMaxExp() + " \t" + "hpMax" + ": " + getPetMaxHP() + " \t" + "mpMax" + ": " + getPetMaxMP() + " \t" + "patk" + ": " + getPetPAtk() + " \t" + "pdef" + ": " + getPetPDef() + " \t" + "matk" + ": " + getPetMAtk() + " \t" + "mdef" + ": " + getPetMDef() + " \t" + "acc" + ": " + getPetAccuracy() + " \t" + "evasion" + ": " + getPetEvasion() + " \t" + "crit" + ": " + getPetCritical() + " \t" + "speed" + ": " + getPetSpeed() + " \t" + "atk_speed" + ": " + getPetAtkSpeed() + " \t" + "cast_speed" + ": " + getPetCastSpeed() + " \t" + "feedMax" + ": " + getPetMaxFeed() + " \t" + "feedbattle" + ": " + getPetFeedBattle() + " \t" + "feednormal" + ": " + getPetFeedNormal() + " \t" + "loadMax" + ": " + getPetMaxLoad() + " \t" + "hpregen" + ": " + getPetRegenHP() + " \t" + "mpregen" + ": " + getPetRegenMP();
  }
}