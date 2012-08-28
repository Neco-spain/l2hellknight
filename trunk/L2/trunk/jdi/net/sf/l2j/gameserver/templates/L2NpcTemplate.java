package net.sf.l2j.gameserver.templates;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.l2j.gameserver.model.L2DropCategory;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2MinionData;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.model.quest.Quest;
import net.sf.l2j.gameserver.model.quest.Quest.QuestEventType;
import net.sf.l2j.gameserver.skills.Stats;

public final class L2NpcTemplate extends L2CharTemplate
{
  protected static final Logger _log = Logger.getLogger(Quest.class.getName());
  public final int npcId;
  public final int idTemplate;
  public final String type;
  public final String name;
  public final boolean serverSideName;
  public final String title;
  public final boolean serverSideTitle;
  public final String sex;
  public final byte level;
  public final int rewardExp;
  public final int rewardSp;
  public final int aggroRange;
  public final int rhand;
  public final int lhand;
  public final int armor;
  public final String factionId;
  public final int factionRange;
  public final int absorbLevel;
  public final AbsorbCrystalType absorbType;
  public final short ss;
  public final short bss;
  public final short ssRate;
  public Race race;
  public final AIType AI;
  public boolean isQuestMonster;
  private final StatsSet _npcStatsSet;
  private final FastList<L2DropCategory> _categories = new FastList();

  private final List<L2MinionData> _minions = new FastList(0);
  private int _currentRHandId;
  private List<ClassId> _teachInfo;
  private Map<Integer, L2Skill> _skills;
  private Map<Stats, Double> _vulnerabilities;
  private Map<Quest.QuestEventType, Quest[]> _questEvents;

  public L2NpcTemplate(StatsSet set)
  {
    super(set);
    npcId = set.getInteger("npcId");
    idTemplate = set.getInteger("idTemplate");
    type = set.getString("type");
    name = set.getString("name");
    serverSideName = set.getBool("serverSideName");
    title = set.getString("title");
    if (title.equalsIgnoreCase("Quest Monster"))
      isQuestMonster = true;
    else
      isQuestMonster = false;
    serverSideTitle = set.getBool("serverSideTitle");
    sex = set.getString("sex");
    level = set.getByte("level");
    rewardExp = set.getInteger("rewardExp");
    rewardSp = set.getInteger("rewardSp");
    aggroRange = set.getInteger("aggroRange");
    rhand = set.getInteger("rhand");
    lhand = set.getInteger("lhand");
    armor = set.getInteger("armor");
    String f = set.getString("factionId", null);
    if (f == null)
      factionId = null;
    else
      factionId = f.intern();
    factionRange = set.getInteger("factionRange");
    absorbLevel = set.getInteger("absorb_level", 0);
    absorbType = AbsorbCrystalType.valueOf(set.getString("absorb_type"));
    ss = (short)set.getInteger("ss", 0);
    bss = (short)set.getInteger("bss", 0);
    ssRate = (short)set.getInteger("ssRate", 0);
    race = null;
    _npcStatsSet = set;
    _teachInfo = null;
    String ai = set.getString("AI", "fighter");
    if (ai.equalsIgnoreCase("archer")) AI = AIType.ARCHER;
    else if (ai.equalsIgnoreCase("balanced")) AI = AIType.BALANCED;
    else if (ai.equalsIgnoreCase("mage")) AI = AIType.MAGE; else
      AI = AIType.FIGHTER;
  }

  public void addTeachInfo(ClassId classId)
  {
    if (_teachInfo == null)
      _teachInfo = new FastList();
    _teachInfo.add(classId);
  }

  public ClassId[] getTeachInfo()
  {
    if (_teachInfo == null)
      return null;
    return (ClassId[])_teachInfo.toArray(new ClassId[_teachInfo.size()]);
  }

  public boolean canTeach(ClassId classId)
  {
    if (_teachInfo == null) {
      return false;
    }

    if (classId.getId() >= 88) {
      return _teachInfo.contains(classId.getParent());
    }
    return _teachInfo.contains(classId);
  }

  public void addDropData(L2DropData drop, int categoryType)
  {
    if (!drop.isQuestDrop())
    {
      synchronized (_categories)
      {
        boolean catExists = false;
        for (L2DropCategory cat : _categories)
        {
          if (cat.getCategoryType() == categoryType)
          {
            cat.addDropData(drop);
            catExists = true;
            break;
          }
        }
        if (!catExists)
        {
          L2DropCategory cat = new L2DropCategory(categoryType);
          cat.addDropData(drop);
          _categories.add(cat);
        }
      }
    }
  }

  public void addRaidData(L2MinionData minion)
  {
    _minions.add(minion);
  }

  public void addSkill(L2Skill skill)
  {
    if (_skills == null)
      _skills = new FastMap();
    _skills.put(Integer.valueOf(skill.getId()), skill);
  }

  public void addVulnerability(Stats id, double vuln) {
    if (_vulnerabilities == null)
      _vulnerabilities = new FastMap();
    _vulnerabilities.put(id, new Double(vuln));
  }

  public double getVulnerability(Stats id) {
    if ((_vulnerabilities == null) || (_vulnerabilities.get(id) == null))
      return 1.0D;
    return ((Double)_vulnerabilities.get(id)).doubleValue();
  }

  public double removeVulnerability(Stats id) {
    return ((Double)_vulnerabilities.remove(id)).doubleValue();
  }

  public FastList<L2DropCategory> getDropData()
  {
    return _categories;
  }

  public List<L2DropData> getAllDropData()
  {
    List lst = new FastList();
    for (L2DropCategory tmp : _categories)
    {
      lst.addAll(tmp.getAllDrops());
    }
    return lst;
  }

  public synchronized void clearAllDropData()
  {
    while (_categories.size() > 0)
    {
      ((L2DropCategory)_categories.getFirst()).clearAllDrops();
      _categories.removeFirst();
    }
    _categories.clear();
  }

  public List<L2MinionData> getMinionData()
  {
    return _minions;
  }

  public Map<Integer, L2Skill> getSkills()
  {
    return _skills;
  }

  public void addQuestEvent(Quest.QuestEventType EventType, Quest q)
  {
    if (_questEvents == null) {
      _questEvents = new FastMap();
    }
    if (_questEvents.get(EventType) == null) {
      _questEvents.put(EventType, new Quest[] { q });
    }
    else
    {
      Quest[] _quests = (Quest[])_questEvents.get(EventType);
      int len = _quests.length;

      if (!EventType.isMultipleRegistrationAllowed())
      {
        if (_quests[0].getName().equals(q.getName()))
          _quests[0] = q;
        else {
          _log.warning("Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + EventType + "\" for NPC \"" + name + "\" and quest \"" + q.getName() + "\".");
        }
      }
      else
      {
        Quest[] tmp = new Quest[len + 1];

        for (int i = 0; i < len; i++) {
          if (_quests[i].getName().equals(q.getName()))
          {
            _quests[i] = q;
            return;
          }
          tmp[i] = _quests[i];
        }
        tmp[len] = q;
        _questEvents.put(EventType, tmp);
      }
    }
  }

  public Quest[] getEventQuests(Quest.QuestEventType EventType) {
    if (_questEvents == null)
      return null;
    return (Quest[])_questEvents.get(EventType);
  }

  public StatsSet getStatsSet()
  {
    return _npcStatsSet;
  }

  public void setRace(int raceId)
  {
    switch (raceId)
    {
    case 1:
      race = Race.UNDEAD;
      break;
    case 2:
      race = Race.MAGICCREATURE;
      break;
    case 3:
      race = Race.BEAST;
      break;
    case 4:
      race = Race.ANIMAL;
      break;
    case 5:
      race = Race.PLANT;
      break;
    case 6:
      race = Race.HUMANOID;
      break;
    case 7:
      race = Race.SPIRIT;
      break;
    case 8:
      race = Race.ANGEL;
      break;
    case 9:
      race = Race.DEMON;
      break;
    case 10:
      race = Race.DRAGON;
      break;
    case 11:
      race = Race.GIANT;
      break;
    case 12:
      race = Race.BUG;
      break;
    case 13:
      race = Race.FAIRIE;
      break;
    case 14:
      race = Race.HUMAN;
      break;
    case 15:
      race = Race.ELVE;
      break;
    case 16:
      race = Race.DARKELVE;
      break;
    case 17:
      race = Race.ORC;
      break;
    case 18:
      race = Race.DWARVE;
      break;
    case 19:
      race = Race.OTHER;
      break;
    case 20:
      race = Race.NONLIVING;
      break;
    case 21:
      race = Race.SIEGEWEAPON;
      break;
    case 22:
      race = Race.DEFENDINGARMY;
      break;
    case 23:
      race = Race.MERCENARIE;
      break;
    default:
      race = Race.UNKNOWN;
    }
  }

  public Race getRace()
  {
    if (race == null) {
      race = Race.UNKNOWN;
    }
    return race;
  }

  public void setRhand(int newWeaponId)
  {
    _currentRHandId = newWeaponId;
  }

  public static enum Race
  {
    UNDEAD, 
    MAGICCREATURE, 
    BEAST, 
    ANIMAL, 
    PLANT, 
    HUMANOID, 
    SPIRIT, 
    ANGEL, 
    DEMON, 
    DRAGON, 
    GIANT, 
    BUG, 
    FAIRIE, 
    HUMAN, 
    ELVE, 
    DARKELVE, 
    ORC, 
    DWARVE, 
    OTHER, 
    NONLIVING, 
    SIEGEWEAPON, 
    DEFENDINGARMY, 
    MERCENARIE, 
    UNKNOWN;
  }

  public static enum AIType
  {
    FIGHTER, 
    ARCHER, 
    BALANCED, 
    MAGE;
  }

  public static enum AbsorbCrystalType
  {
    LAST_HIT, 
    FULL_PARTY, 
    PARTY_ONE_RANDOM;
  }
}