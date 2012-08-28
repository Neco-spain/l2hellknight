package l2p.gameserver.model.actor.instances.player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.SubClass;
import l2p.gameserver.model.base.ClassId;
import l2p.gameserver.model.base.ClassLevel;
import l2p.gameserver.model.base.ClassType;
import l2p.gameserver.model.base.Race;

public final class SubClassInfo
{
  private static final Set<ClassId> MAIN_SUBCLASS_SET;
  private static final Set<ClassId> KAMAEL_SUBCLASS_SET;
  private static final Set<ClassId> BANNED_SUBCLASSES = EnumSet.of(ClassId.OVERLORD, ClassId.WARSMITH);
  private static final Set<ClassId> SUBCLASS_SET_1 = EnumSet.of(ClassId.DARK_AVENGER, ClassId.PALADIN, ClassId.TEMPLE_KNIGHT, ClassId.SHILLEN_KNIGHT);
  private static final Set<ClassId> SUBCLASS_SET_2 = EnumSet.of(ClassId.TREASURE_HUNTER, ClassId.ABYSS_WALKER, ClassId.PLAIN_WALKER);
  private static final Set<ClassId> SUBCLASS_SET_3 = EnumSet.of(ClassId.HAWKEYE, ClassId.SILVER_RANGER, ClassId.PHANTOM_RANGER);
  private static final Set<ClassId> SUBCLASS_SET_4 = EnumSet.of(ClassId.WARLOCK, ClassId.ELEMENTAL_SUMMONER, ClassId.PHANTOM_SUMMONER);
  private static final Set<ClassId> SUBCLASS_SET_5 = EnumSet.of(ClassId.SORCERER, ClassId.SPELLSINGER, ClassId.SPELLHOWLER);
  private static final Set<ClassId> SUBCLASS_SET_6 = EnumSet.of(ClassId.INSPECTOR);

  private static final EnumMap<ClassId, Set<ClassId>> SUBCLASS_SET_MAP = new EnumMap(ClassId.class);

  public static Set<ClassId> getAvailableSubClasses(Player player, Race npcRace, ClassType npcTeachType, boolean isNew)
  {
    Set availSubs = null;
    Race race = player.getRace();
    if (race == Race.KAMAEL)
    {
      availSubs = EnumSet.copyOf(KAMAEL_SUBCLASS_SET);
    }
    else
    {
      ClassId classId = player.getClassId();
      if ((classId.isOfLevel(ClassLevel.SECOND)) || (classId.isOfLevel(ClassLevel.THIRD)))
      {
        availSubs = EnumSet.copyOf(MAIN_SUBCLASS_SET);

        availSubs.removeAll(BANNED_SUBCLASSES);
        availSubs.remove(classId);

        switch (1.$SwitchMap$l2s$gameserver$model$base$Race[race.ordinal()])
        {
        case 1:
          availSubs.removeAll(getSet(Race.DARKELF, ClassLevel.SECOND));
          break;
        case 2:
          availSubs.removeAll(getSet(Race.ELF, ClassLevel.SECOND));
        }

        Set unavailableClasses = (Set)SUBCLASS_SET_MAP.get(classId);

        if (unavailableClasses != null) {
          availSubs.removeAll(unavailableClasses);
        }
      }
    }
    int charClassId = player.getBaseClassId();

    ClassId currClass = ClassId.VALUES[charClassId];

    if (availSubs == null) {
      return Collections.emptySet();
    }

    availSubs.remove(currClass);

    for (ClassId availSub : availSubs)
    {
      for (SubClass subClass : player.getSubClasses().values())
      {
        if (availSub.getId() == subClass.getClassId())
        {
          availSubs.remove(availSub);
          continue;
        }

        ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
        if ((parent != null) && (parent.getId() == subClass.getClassId()))
        {
          availSubs.remove(availSub);
          continue;
        }

        ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
        if ((subParent != null) && (subParent.getId() == availSub.getId())) {
          availSubs.remove(availSub);
        }
      }
      if ((npcRace != null) && (npcTeachType != null))
      {
        if ((!availSub.isOfRace(Race.HUMAN)) && (!availSub.isOfRace(Race.ELF)))
        {
          if (!availSub.isOfRace(npcRace))
            availSubs.remove(availSub);
        }
        else if (!availSub.isOfType(npcTeachType)) {
          availSubs.remove(availSub);
        }
      }

      if (availSub.isOfRace(Race.KAMAEL))
      {
        if (((currClass == ClassId.M_SOUL_HOUND) || (currClass == ClassId.F_SOUL_HOUND) || (currClass == ClassId.F_SOUL_BREAKER) || (currClass == ClassId.M_SOUL_BREAKER)) && ((availSub == ClassId.F_SOUL_BREAKER) || (availSub == ClassId.M_SOUL_BREAKER))) {
          availSubs.remove(availSub);
        }

        if (((currClass == ClassId.BERSERKER) || (currClass == ClassId.DOOMBRINGER) || (currClass == ClassId.ARBALESTER) || (currClass == ClassId.TRICKSTER)) && (
          ((player.getSex() == 1) && (availSub == ClassId.M_SOUL_BREAKER)) || ((player.getSex() == 0) && (availSub == ClassId.F_SOUL_BREAKER)))) {
          availSubs.remove(availSub);
        }

        if (availSub == ClassId.INSPECTOR) if (player.getSubClasses().size() < (isNew ? 3 : 4))
            availSubs.remove(availSub);
      }
    }
    return availSubs;
  }

  public static EnumSet<ClassId> getSet(Race race, ClassLevel level)
  {
    EnumSet allOf = EnumSet.noneOf(ClassId.class);

    for (ClassId classId : EnumSet.allOf(ClassId.class)) {
      if (((race == null) || (classId.isOfRace(race))) && (
        (level == null) || (classId.isOfLevel(level))))
        allOf.add(classId);
    }
    return allOf;
  }

  public static boolean areClassesComportable(ClassId c1, ClassId c2)
  {
    if (c1.isOfRace(Race.KAMAEL) != c2.isOfRace(Race.KAMAEL))
      return false;
    if (((c1.isOfRace(Race.ELF)) && (c2.isOfRace(Race.DARKELF))) || ((c1.isOfRace(Race.DARKELF)) && (c2.isOfRace(Race.ELF))))
      return false;
    if ((c1 == ClassId.OVERLORD) || (c1 == ClassId.WARSMITH) || (c2 == ClassId.OVERLORD) || (c2 == ClassId.WARSMITH)) {
      return false;
    }
    return SUBCLASS_SET_MAP.get(c1) != SUBCLASS_SET_MAP.get(c2);
  }

  static
  {
    KAMAEL_SUBCLASS_SET = getSet(Race.KAMAEL, ClassLevel.SECOND);

    MAIN_SUBCLASS_SET = getSet(null, ClassLevel.SECOND);
    MAIN_SUBCLASS_SET.removeAll(BANNED_SUBCLASSES);
    MAIN_SUBCLASS_SET.removeAll(KAMAEL_SUBCLASS_SET);

    SUBCLASS_SET_MAP.put(ClassId.DARK_AVENGER, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.HELL_KNIGHT, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.PALADIN, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.PHOENIX_KNIGHT, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.TEMPLE_KNIGHT, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.EVAS_TEMPLAR, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.SHILLEN_KNIGHT, SUBCLASS_SET_1);
    SUBCLASS_SET_MAP.put(ClassId.SHILLIEN_TEMPLAR, SUBCLASS_SET_1);

    SUBCLASS_SET_MAP.put(ClassId.TREASURE_HUNTER, SUBCLASS_SET_2);
    SUBCLASS_SET_MAP.put(ClassId.ADVENTURER, SUBCLASS_SET_2);
    SUBCLASS_SET_MAP.put(ClassId.ABYSS_WALKER, SUBCLASS_SET_2);
    SUBCLASS_SET_MAP.put(ClassId.GHOST_HUNTER, SUBCLASS_SET_2);
    SUBCLASS_SET_MAP.put(ClassId.PLAIN_WALKER, SUBCLASS_SET_2);
    SUBCLASS_SET_MAP.put(ClassId.WIND_RIDER, SUBCLASS_SET_2);

    SUBCLASS_SET_MAP.put(ClassId.HAWKEYE, SUBCLASS_SET_3);
    SUBCLASS_SET_MAP.put(ClassId.SAGITTARIUS, SUBCLASS_SET_3);
    SUBCLASS_SET_MAP.put(ClassId.SILVER_RANGER, SUBCLASS_SET_3);
    SUBCLASS_SET_MAP.put(ClassId.MOONLIGHT_SENTINEL, SUBCLASS_SET_3);
    SUBCLASS_SET_MAP.put(ClassId.PHANTOM_RANGER, SUBCLASS_SET_3);
    SUBCLASS_SET_MAP.put(ClassId.GHOST_SENTINEL, SUBCLASS_SET_3);

    SUBCLASS_SET_MAP.put(ClassId.WARLOCK, SUBCLASS_SET_4);
    SUBCLASS_SET_MAP.put(ClassId.ARCANA_LORD, SUBCLASS_SET_4);
    SUBCLASS_SET_MAP.put(ClassId.ELEMENTAL_SUMMONER, SUBCLASS_SET_4);
    SUBCLASS_SET_MAP.put(ClassId.ELEMENTAL_MASTER, SUBCLASS_SET_4);
    SUBCLASS_SET_MAP.put(ClassId.PHANTOM_SUMMONER, SUBCLASS_SET_4);
    SUBCLASS_SET_MAP.put(ClassId.SPECTRAL_MASTER, SUBCLASS_SET_4);

    SUBCLASS_SET_MAP.put(ClassId.SORCERER, SUBCLASS_SET_5);
    SUBCLASS_SET_MAP.put(ClassId.ARCHMAGE, SUBCLASS_SET_5);
    SUBCLASS_SET_MAP.put(ClassId.SPELLSINGER, SUBCLASS_SET_5);
    SUBCLASS_SET_MAP.put(ClassId.MYSTIC_MUSE, SUBCLASS_SET_5);
    SUBCLASS_SET_MAP.put(ClassId.SPELLHOWLER, SUBCLASS_SET_5);
    SUBCLASS_SET_MAP.put(ClassId.STORM_SCREAMER, SUBCLASS_SET_5);

    SUBCLASS_SET_MAP.put(ClassId.DOOMBRINGER, SUBCLASS_SET_6);
    SUBCLASS_SET_MAP.put(ClassId.M_SOUL_HOUND, SUBCLASS_SET_6);
    SUBCLASS_SET_MAP.put(ClassId.F_SOUL_HOUND, SUBCLASS_SET_6);
    SUBCLASS_SET_MAP.put(ClassId.TRICKSTER, SUBCLASS_SET_6);

    SUBCLASS_SET_MAP.put(ClassId.DUELIST, EnumSet.of(ClassId.GLADIATOR));
    SUBCLASS_SET_MAP.put(ClassId.DREADNOUGHT, EnumSet.of(ClassId.WARLORD));
    SUBCLASS_SET_MAP.put(ClassId.SOULTAKER, EnumSet.of(ClassId.NECROMANCER));
    SUBCLASS_SET_MAP.put(ClassId.CARDINAL, EnumSet.of(ClassId.BISHOP));
    SUBCLASS_SET_MAP.put(ClassId.HIEROPHANT, EnumSet.of(ClassId.PROPHET));
    SUBCLASS_SET_MAP.put(ClassId.SWORD_MUSE, EnumSet.of(ClassId.SWORDSINGER));
    SUBCLASS_SET_MAP.put(ClassId.EVAS_SAINT, EnumSet.of(ClassId.ELDER));
    SUBCLASS_SET_MAP.put(ClassId.SPECTRAL_DANCER, EnumSet.of(ClassId.BLADEDANCER));
    SUBCLASS_SET_MAP.put(ClassId.TITAN, EnumSet.of(ClassId.DESTROYER));
    SUBCLASS_SET_MAP.put(ClassId.GRAND_KHAVATARI, EnumSet.of(ClassId.TYRANT));
    SUBCLASS_SET_MAP.put(ClassId.DOMINATOR, EnumSet.of(ClassId.OVERLORD));
    SUBCLASS_SET_MAP.put(ClassId.DOOMCRYER, EnumSet.of(ClassId.WARCRYER));
  }
}