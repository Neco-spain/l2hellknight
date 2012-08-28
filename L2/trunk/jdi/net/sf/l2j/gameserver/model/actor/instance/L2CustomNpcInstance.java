package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.instancemanager.CustomNpcInstanceManager;
import net.sf.l2j.gameserver.instancemanager.CustomNpcInstanceManager.customInfo;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.base.Race;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.gameserver.templates.L2WeaponType;
import net.sf.l2j.util.Rnd;

public final class L2CustomNpcInstance
{
  private boolean _allowRandomWeapons = false;
  private boolean _allowRandomClass = false;
  private boolean _allowRandomArmorSets = false;
  private boolean _allowRandomAppearance = false;
  private int _randomMaxEnchant = 8;
  private String _name;
  private String _title;
  private int[] _int;
  private boolean[] _boolean;
  private L2NpcInstance _NpcInstance;
  private ClassId _classId;

  public L2CustomNpcInstance(L2NpcInstance myNpc)
  {
    _NpcInstance = myNpc;
    if (_NpcInstance == null) return;
    if (_NpcInstance.getSpawn() == null) return;
    initialize();
  }

  private final void initialize()
  {
    _int = new int[25];

    _boolean = new boolean[4];

    CustomNpcInstanceManager.customInfo ci = CustomNpcInstanceManager.getInstance().getCustomData(_NpcInstance.getSpawn().getId(), _NpcInstance.getNpcId());

    if (ci == null)
    {
      _NpcInstance.setCustomNpcInstance(null);
      _NpcInstance = null;
      return;
    }

    _NpcInstance.setCustomNpcInstance(this);

    setPcInstanceData(ci);

    if (_allowRandomClass) chooseRandomClass();
    if (_allowRandomAppearance) chooseRandomAppearance();
    if (_allowRandomWeapons) chooseRandomWeapon();
  }

  public final String getName()
  {
    return _name == null ? _NpcInstance.getName() : _name;
  }

  public final String getTitle()
  {
    return _NpcInstance.isChampion() ? "The Champion " + _title : _title == null ? _NpcInstance.getTitle() : _title;
  }

  public final int getKarma()
  {
    return _int[1] > 0 ? _int[1] : _NpcInstance.getAggroRange();
  }

  public final int getClanId()
  {
    return _int[2];
  }

  public final int getAllyId()
  {
    return _int[3];
  }

  public final int getClanCrestId()
  {
    return _int[4];
  }

  public final int getAllyCrestId()
  {
    return _int[5];
  }

  public final int getRace()
  {
    return _int[6];
  }

  public final int getClassId()
  {
    return _int[7];
  }

  public final int getEnchantWeapon()
  {
    return _int[8] > 127 ? 127 : (PAPERDOLL_RHAND() == 0) || (getCursedWeaponLevel() != 0) ? 0 : _int[8];
  }

  public final int getPledgeClass()
  {
    return _NpcInstance.isChampion() ? 8 : _int[9];
  }

  public final int getCursedWeaponLevel()
  {
    return (PAPERDOLL_RHAND() == 0) || (_int[8] > 0) ? 0 : _int[10];
  }

  public final int PAPERDOLL_RHAND()
  {
    return _int[11] != 0 ? _int[11] : _NpcInstance.getRightHandItem();
  }

  public final int PAPERDOLL_LHAND()
  {
    return _int[12] == 0 ? _NpcInstance.getLeftHandItem() : _int[12] > 0 ? _int[12] : 0;
  }

  public final int PAPERDOLL_GLOVES()
  {
    return _int[13];
  }

  public final int PAPERDOLL_CHEST()
  {
    return _int[14];
  }

  public final int PAPERDOLL_LEGS()
  {
    return _int[15];
  }

  public final int PAPERDOLL_FEET()
  {
    return _int[16];
  }

  public final int PAPERDOLL_HAIR()
  {
    return _int[17];
  }

  public final int PAPERDOLL_HAIR2()
  {
    return _int[18];
  }

  public final int getHairStyle()
  {
    return _int[19];
  }

  public final int getHairColor()
  {
    return _int[20];
  }

  public final int getFace()
  {
    return _int[21];
  }

  public final int nameColor()
  {
    return _int[22] == 0 ? 16777215 : _int[22];
  }

  public final int titleColor()
  {
    return _int[23] == 0 ? 16777079 : _int[23];
  }

  public final boolean getPvpFlag()
  {
    return _boolean[0];
  }

  public final boolean isNoble()
  {
    return _boolean[1];
  }

  public final boolean isHero()
  {
    return _NpcInstance.isChampion() ? 1 : _boolean[2];
  }

  public final boolean isFemaleSex()
  {
    return _boolean[3];
  }

  private final void chooseRandomWeapon()
  {
    L2WeaponType wpnType = null;
    if (_NpcInstance.getTemplate().baseAtkRange > 100)
    {
      wpnType = L2WeaponType.BOW;
    }
    else
    {
      do
      {
        wpnType = L2WeaponType.values()[Rnd.get(L2WeaponType.values().length)];
      }while ((wpnType == null) || 
        (wpnType == L2WeaponType.BOW));
    }
  }

  private final void chooseRandomClass()
  {
    while (true)
    {
      _classId = ClassId.values()[Rnd.get(ClassId.values().length)];
      if ((_classId != null) && 
        (_classId.getRace() != null) && (_classId.getParent() != null))
        break;
    }
    _int[6] = _classId.getRace().ordinal();
    _int[7] = _classId.getId();
  }

  private final void chooseRandomAppearance()
  {
    _boolean[1] = (Rnd.get(100) < 15 ? 1 : false);
    _boolean[3] = (Rnd.get(100) < 50 ? 1 : false);
    int tmp55_54 = 0; _int[23] = tmp55_54; _int[22] = tmp55_54;
    if (Rnd.get(100) < 5) _int[22] = 255;
    else if (Rnd.get(100) < 5) _int[22] = 65280;
    if (Rnd.get(100) < 5) _int[23] = 255;
    else if (Rnd.get(100) < 5) _int[23] = 65280;
    _int[1] = (Rnd.get(100) > 10 ? 50 : Rnd.get(100) > 95 ? 0 : 1000);
    _int[19] = (Rnd.get(100) < 34 ? 1 : Rnd.get(100) < 34 ? 0 : 2);
    _int[20] = (Rnd.get(100) < 34 ? 1 : Rnd.get(100) < 34 ? 0 : 2);
    _int[21] = (Rnd.get(100) < 34 ? 1 : Rnd.get(100) < 34 ? 0 : 2);

    int pledgeLevel = Rnd.get(100);

    if (pledgeLevel > 30) _int[9] = 1;
    if (pledgeLevel > 50) _int[9] = 2;
    if (pledgeLevel > 60) _int[9] = 3;
    if (pledgeLevel > 80) _int[9] = 4;
    if (pledgeLevel > 90) _int[9] = 5;
    if (pledgeLevel > 95) _int[9] = 6;
    if (pledgeLevel > 98) _int[9] = 7;
  }

  public void setPcInstanceData(CustomNpcInstanceManager.customInfo ci)
  {
    if (ci == null) return;

    for (int i = 0; i < 25; i++) _int[i] = ci.integerData[i];
    for (int i = 0; i < 4; i++) _boolean[i] = ci.booleanData[i];

    _name = ci.stringData[0];
    _title = ci.stringData[1];
    if ((_name != null) && (_name.equals(""))) _name = null;
    if ((_title != null) && (_title.equals(""))) _title = null;

    ClassId[] ids = ClassId.values();
    if (ids != null)
    {
      for (int i = 0; i < ids.length; i++) {
        if (ids[i] == null)
          continue;
        if (ids[i].getId() != _int[7])
          continue;
        _classId = ids[i];
        _int[6] = ids[i].getRace().ordinal();
        break;
      }
    }
  }
}