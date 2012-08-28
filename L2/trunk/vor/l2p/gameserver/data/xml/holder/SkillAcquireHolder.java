package l2p.gameserver.data.xml.holder;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.Skill;
import l2p.gameserver.model.SkillLearn;
import l2p.gameserver.model.base.AcquireType;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.Race;
import l2p.gameserver.model.pledge.Clan;
import l2p.gameserver.model.pledge.SubUnit;

public final class SkillAcquireHolder extends AbstractHolder
{
  private static final SkillAcquireHolder _instance = new SkillAcquireHolder();

  private TIntObjectHashMap<List<SkillLearn>> _normalSkillTree = new TIntObjectHashMap();
  private TIntObjectHashMap<List<SkillLearn>> _transferSkillTree = new TIntObjectHashMap();

  private TIntObjectHashMap<List<SkillLearn>> _fishingSkillTree = new TIntObjectHashMap();
  private TIntObjectHashMap<List<SkillLearn>> _transformationSkillTree = new TIntObjectHashMap();

  private List<SkillLearn> _certificationSkillTree = new ArrayList();
  private List<SkillLearn> _collectionSkillTree = new ArrayList();
  private List<SkillLearn> _pledgeSkillTree = new ArrayList();
  private List<SkillLearn> _subUnitSkillTree = new ArrayList();

  public static SkillAcquireHolder getInstance()
  {
    return _instance;
  }

  public int getMinLevelForNewSkill(Player player, AcquireType type)
  {
    List skills;
    switch (1.$SwitchMap$l2p$gameserver$model$base$AcquireType[type.ordinal()])
    {
    case 1:
      skills = (List)_normalSkillTree.get(player.getActiveClassId());
      if (skills != null)
        break;
      info("skill tree for class " + player.getActiveClassId() + " is not defined !");
      return 0;
    case 2:
      skills = (List)_transformationSkillTree.get(player.getRace().ordinal());
      if (skills != null)
        break;
      info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
      return 0;
    case 3:
      skills = (List)_fishingSkillTree.get(player.getRace().ordinal());
      if (skills != null)
        break;
      info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
      return 0;
    default:
      return 0;
    }
    int minlevel = 0;
    for (SkillLearn temp : skills)
      if ((temp.getMinLevel() > player.getLevel()) && (
        (minlevel == 0) || (temp.getMinLevel() < minlevel)))
        minlevel = temp.getMinLevel();
    return minlevel;
  }

  public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type)
  {
    return getAvailableSkills(player, type, null);
  }

  public Collection<SkillLearn> getAvailableSkills(Player player, AcquireType type, SubUnit subUnit)
  {
    Collection skills;
    switch (1.$SwitchMap$l2p$gameserver$model$base$AcquireType[type.ordinal()])
    {
    case 1:
      skills = (Collection)_normalSkillTree.get(player.getActiveClassId());
      if (skills == null)
      {
        info("skill tree for class " + player.getActiveClassId() + " is not defined !");
        return Collections.emptyList();
      }
      return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
    case 4:
      skills = _collectionSkillTree;
      if (skills == null)
      {
        info("skill tree for class " + player.getActiveClassId() + " is not defined !");
        return Collections.emptyList();
      }
      return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
    case 2:
      skills = (Collection)_transformationSkillTree.get(player.getRace().ordinal());
      if (skills == null)
      {
        info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
        return Collections.emptyList();
      }
      return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
    case 5:
    case 6:
    case 7:
      skills = (Collection)_transferSkillTree.get(type.transferClassId());
      if (skills == null)
      {
        info("skill tree for class " + type.transferClassId() + " is not defined !");
        return Collections.emptyList();
      }
      if (player == null) {
        return skills;
      }

      Map skillLearnMap = new TreeMap();
      for (SkillLearn temp : skills) {
        if (temp.getMinLevel() <= player.getLevel())
        {
          int knownLevel = player.getSkillLevel(Integer.valueOf(temp.getId()));
          if (knownLevel == -1)
            skillLearnMap.put(Integer.valueOf(temp.getId()), temp);
        }
      }
      return skillLearnMap.values();
    case 3:
      skills = (Collection)_fishingSkillTree.get(player.getRace().ordinal());
      if (skills == null)
      {
        info("skill tree for race " + player.getRace().ordinal() + " is not defined !");
        return Collections.emptyList();
      }
      return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
    case 8:
      skills = _pledgeSkillTree;
      Collection skls = player.getClan().getSkills();

      return getAvaliableList(skills, (Skill[])skls.toArray(new Skill[skls.size()]), player.getClan().getLevel());
    case 9:
      skills = _subUnitSkillTree;
      Collection st = subUnit.getSkills();

      return getAvaliableList(skills, (Skill[])st.toArray(new Skill[st.size()]), player.getClan().getLevel());
    case 10:
      skills = _certificationSkillTree;
      if (player == null) {
        return skills;
      }
      return getAvaliableList(skills, player.getAllSkillsArray(), player.getLevel());
    }
    return Collections.emptyList();
  }

  private Collection<SkillLearn> getAvaliableList(Collection<SkillLearn> skillLearns, Skill[] skills, int level)
  {
    Map skillLearnMap = new TreeMap();
    for (SkillLearn temp : skillLearns) {
      if (temp.getMinLevel() <= level)
      {
        boolean knownSkill = false;
        for (int j = 0; (j < skills.length) && (!knownSkill); j++) {
          if (skills[j].getId() != temp.getId())
            continue;
          knownSkill = true;
          if (skills[j].getLevel() == temp.getLevel() - 1)
            skillLearnMap.put(Integer.valueOf(temp.getId()), temp);
        }
        if ((!knownSkill) && (temp.getLevel() == 1))
          skillLearnMap.put(Integer.valueOf(temp.getId()), temp);
      }
    }
    return skillLearnMap.values();
  }

  public SkillLearn getSkillLearn(Player player, int id, int level, AcquireType type)
  {
    List skills;
    switch (1.$SwitchMap$l2p$gameserver$model$base$AcquireType[type.ordinal()])
    {
    case 1:
      skills = (List)_normalSkillTree.get(player.getActiveClassId());
      break;
    case 4:
      skills = _collectionSkillTree;
      break;
    case 2:
      skills = (List)_transformationSkillTree.get(player.getRace().ordinal());
      break;
    case 5:
    case 6:
    case 7:
      skills = (List)_transferSkillTree.get(player.getActiveClassId());
      break;
    case 3:
      skills = (List)_fishingSkillTree.get(player.getRace().ordinal());
      break;
    case 8:
      skills = _pledgeSkillTree;
      break;
    case 9:
      skills = _subUnitSkillTree;
      break;
    case 10:
      skills = _certificationSkillTree;
      break;
    default:
      return null;
    }

    if (skills == null) {
      return null;
    }
    for (SkillLearn temp : skills) {
      if ((temp.getLevel() == level) && (temp.getId() == id))
        return temp;
    }
    return null;
  }

  public boolean isSkillPossible(Player player, Skill skill, AcquireType type)
  {
    Clan clan = null;
    List skills;
    switch (1.$SwitchMap$l2p$gameserver$model$base$AcquireType[type.ordinal()])
    {
    case 1:
      skills = (List)_normalSkillTree.get(player.getActiveClassId());
      break;
    case 4:
      skills = _collectionSkillTree;
      break;
    case 2:
      skills = (List)_transformationSkillTree.get(player.getRace().ordinal());
      break;
    case 3:
      skills = (List)_fishingSkillTree.get(player.getRace().ordinal());
      break;
    case 5:
    case 6:
    case 7:
      int transferId = type.transferClassId();
      if (player.getActiveClassId() != transferId) {
        return false;
      }
      skills = (List)_transferSkillTree.get(transferId);
      break;
    case 8:
      clan = player.getClan();
      if (clan == null)
        return false;
      skills = _pledgeSkillTree;
      break;
    case 9:
      clan = player.getClan();
      if (clan == null) {
        return false;
      }
      skills = _subUnitSkillTree;
      break;
    case 10:
      skills = _certificationSkillTree;
      break;
    default:
      return false;
    }

    return isSkillPossible(skills, skill);
  }

  private boolean isSkillPossible(Collection<SkillLearn> skills, Skill skill)
  {
    for (SkillLearn learn : skills)
      if ((learn.getId() == skill.getId()) && (learn.getLevel() <= skill.getLevel()))
        return true;
    return false;
  }

  public boolean isSkillPossible(Player player, Skill skill)
  {
    for (AcquireType aq : AcquireType.VALUES) {
      if (isSkillPossible(player, skill, aq))
        return true;
    }
    return false;
  }

  public List<SkillLearn> getSkillLearnListByItemId(Player player, int itemId)
  {
    List learns = (List)_normalSkillTree.get(player.getActiveClassId());
    if (learns == null) {
      return Collections.emptyList();
    }
    List l = new ArrayList(1);
    for (SkillLearn $i : learns) {
      if ($i.getItemId() == itemId)
        l.add($i);
    }
    return l;
  }

  public List<SkillLearn> getAllNormalSkillTreeWithForgottenScrolls()
  {
    List a = new ArrayList();
    for (TIntObjectIterator i = _normalSkillTree.iterator(); i.hasNext(); )
    {
      i.advance();
      for (SkillLearn learn : (List)i.value()) {
        if ((learn.getItemId() > 0) && (learn.isClicked()))
          a.add(learn);
      }
    }
    return a;
  }

  public void addAllNormalSkillLearns(TIntObjectHashMap<List<SkillLearn>> map)
  {
    for (ClassId classId : ClassId.VALUES)
    {
      if (classId.name().startsWith("dummyEntry")) {
        continue;
      }
      int classID = classId.getId();

      List temp = (List)map.get(classID);
      if (temp == null)
      {
        info("Not found NORMAL skill learn for class " + classID);
      }
      else
      {
        _normalSkillTree.put(classId.getId(), temp);

        ClassId secondparent = classId.getParent(1);
        if (secondparent == classId.getParent(0)) {
          secondparent = null;
        }
        classId = classId.getParent(0);

        while (classId != null)
        {
          List parentList = (List)_normalSkillTree.get(classId.getId());
          temp.addAll(parentList);

          classId = classId.getParent(0);
          if ((classId == null) && (secondparent != null))
          {
            classId = secondparent;
            secondparent = secondparent.getParent(1);
          }
        }
      }
    }
  }

  public void addAllFishingLearns(int race, List<SkillLearn> s) {
    _fishingSkillTree.put(race, s);
  }

  public void addAllTransferLearns(int classId, List<SkillLearn> s)
  {
    _transferSkillTree.put(classId, s);
  }

  public void addAllTransformationLearns(int race, List<SkillLearn> s)
  {
    _transformationSkillTree.put(race, s);
  }

  public void addAllCertificationLearns(List<SkillLearn> s)
  {
    _certificationSkillTree.addAll(s);
  }

  public void addAllCollectionLearns(List<SkillLearn> s)
  {
    _collectionSkillTree.addAll(s);
  }

  public void addAllSubUnitLearns(List<SkillLearn> s)
  {
    _subUnitSkillTree.addAll(s);
  }

  public void addAllPledgeLearns(List<SkillLearn> s)
  {
    _pledgeSkillTree.addAll(s);
  }

  public void log()
  {
    info("load " + sizeTroveMap(_normalSkillTree) + " normal learns for " + _normalSkillTree.size() + " classes.");
    info("load " + sizeTroveMap(_transferSkillTree) + " transfer learns for " + _transferSkillTree.size() + " classes.");

    info("load " + sizeTroveMap(_transformationSkillTree) + " transformation learns for " + _transformationSkillTree.size() + " races.");
    info("load " + sizeTroveMap(_fishingSkillTree) + " fishing learns for " + _fishingSkillTree.size() + " races.");

    info("load " + _certificationSkillTree.size() + " certification learns.");
    info("load " + _collectionSkillTree.size() + " collection learns.");
    info("load " + _pledgeSkillTree.size() + " pledge learns.");
    info("load " + _subUnitSkillTree.size() + " sub unit learns.");
  }

  @Deprecated
  public int size()
  {
    return 0;
  }

  public void clear()
  {
    _normalSkillTree.clear();
    _fishingSkillTree.clear();
    _transferSkillTree.clear();
    _certificationSkillTree.clear();
    _collectionSkillTree.clear();
    _pledgeSkillTree.clear();
    _subUnitSkillTree.clear();
  }

  private int sizeTroveMap(TIntObjectHashMap<List<SkillLearn>> a)
  {
    int i = 0;
    for (TIntObjectIterator iterator = a.iterator(); iterator.hasNext(); )
    {
      iterator.advance();
      i += ((List)iterator.value()).size();
    }

    return i;
  }
}