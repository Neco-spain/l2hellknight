package net.sf.l2j.gameserver.model.base;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public enum PlayerClass
{
  HumanFighter(PlayerRace.Human, ClassType.Fighter, ClassLevel.First), Warrior(PlayerRace.Human, ClassType.Fighter, ClassLevel.Second), Gladiator(PlayerRace.Human, ClassType.Fighter, ClassLevel.Third), 
  Warlord(PlayerRace.Human, ClassType.Fighter, ClassLevel.Third), HumanKnight(PlayerRace.Human, ClassType.Fighter, ClassLevel.Second), Paladin(PlayerRace.Human, ClassType.Fighter, ClassLevel.Third), 
  DarkAvenger(PlayerRace.Human, ClassType.Fighter, ClassLevel.Third), Rogue(PlayerRace.Human, ClassType.Fighter, ClassLevel.Second), TreasureHunter(PlayerRace.Human, ClassType.Fighter, ClassLevel.Third), 
  Hawkeye(PlayerRace.Human, ClassType.Fighter, ClassLevel.Third), HumanMystic(PlayerRace.Human, ClassType.Mystic, ClassLevel.First), HumanWizard(PlayerRace.Human, ClassType.Mystic, ClassLevel.Second), 
  Sorceror(PlayerRace.Human, ClassType.Mystic, ClassLevel.Third), Necromancer(PlayerRace.Human, ClassType.Mystic, ClassLevel.Third), Warlock(PlayerRace.Human, ClassType.Mystic, ClassLevel.Third), 
  Cleric(PlayerRace.Human, ClassType.Priest, ClassLevel.Second), Bishop(PlayerRace.Human, ClassType.Priest, ClassLevel.Third), Prophet(PlayerRace.Human, ClassType.Priest, ClassLevel.Third), 

  ElvenFighter(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.First), ElvenKnight(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Second), TempleKnight(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Third), 
  Swordsinger(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Third), ElvenScout(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Second), 
  Plainswalker(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Third), SilverRanger(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Third), 
  ElvenMystic(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.First), ElvenWizard(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.Second), Spellsinger(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.Third), 
  ElementalSummoner(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.Third), ElvenOracle(PlayerRace.LightElf, ClassType.Priest, ClassLevel.Second), 
  ElvenElder(PlayerRace.LightElf, ClassType.Priest, ClassLevel.Third), 
  DarkElvenFighter(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.First), PalusKnight(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Second), ShillienKnight(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Third), 
  Bladedancer(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Third), Assassin(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Second), 
  AbyssWalker(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Third), PhantomRanger(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Third), DarkElvenMystic(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.First), 
  DarkElvenWizard(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.Second), Spellhowler(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.Third), 
  PhantomSummoner(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.Third), ShillienOracle(PlayerRace.DarkElf, ClassType.Priest, ClassLevel.Second), 
  ShillienElder(PlayerRace.DarkElf, ClassType.Priest, ClassLevel.Third), 
  OrcFighter(PlayerRace.Orc, ClassType.Fighter, ClassLevel.First), OrcRaider(PlayerRace.Orc, ClassType.Fighter, ClassLevel.Second), Destroyer(PlayerRace.Orc, ClassType.Fighter, ClassLevel.Third), OrcMonk(PlayerRace.Orc, ClassType.Fighter, ClassLevel.Second), 
  Tyrant(PlayerRace.Orc, ClassType.Fighter, ClassLevel.Third), OrcMystic(PlayerRace.Orc, ClassType.Mystic, ClassLevel.First), OrcShaman(PlayerRace.Orc, ClassType.Mystic, ClassLevel.Second), 
  Overlord(PlayerRace.Orc, ClassType.Mystic, ClassLevel.Third), Warcryer(PlayerRace.Orc, ClassType.Mystic, ClassLevel.Third), 
  DwarvenFighter(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.First), DwarvenScavenger(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.Second), BountyHunter(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.Third), 
  DwarvenArtisan(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.Second), Warsmith(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.Third), 
  dummyEntry1(null, null, null), dummyEntry2(null, null, null), dummyEntry3(null, null, null), dummyEntry4(null, null, null), 
  dummyEntry5(null, null, null), dummyEntry6(null, null, null), dummyEntry7(null, null, null), 
  dummyEntry8(null, null, null), dummyEntry9(null, null, null), dummyEntry10(null, null, null), 
  dummyEntry11(null, null, null), dummyEntry12(null, null, null), dummyEntry13(null, null, null), 
  dummyEntry14(null, null, null), dummyEntry15(null, null, null), dummyEntry16(null, null, null), 
  dummyEntry17(null, null, null), dummyEntry18(null, null, null), dummyEntry19(null, null, null), 
  dummyEntry20(null, null, null), dummyEntry21(null, null, null), dummyEntry22(null, null, null), 
  dummyEntry23(null, null, null), dummyEntry24(null, null, null), dummyEntry25(null, null, null), 
  dummyEntry26(null, null, null), dummyEntry27(null, null, null), dummyEntry28(null, null, null), 
  dummyEntry29(null, null, null), dummyEntry30(null, null, null), 

  duelist(PlayerRace.Human, ClassType.Fighter, ClassLevel.Fourth), dreadnought(PlayerRace.Human, ClassType.Fighter, ClassLevel.Fourth), phoenixKnight(PlayerRace.Human, ClassType.Fighter, ClassLevel.Fourth), 
  hellKnight(PlayerRace.Human, ClassType.Fighter, ClassLevel.Fourth), sagittarius(PlayerRace.Human, ClassType.Fighter, ClassLevel.Fourth), adventurer(PlayerRace.Human, ClassType.Fighter, ClassLevel.Fourth), 
  archmage(PlayerRace.Human, ClassType.Mystic, ClassLevel.Fourth), soultaker(PlayerRace.Human, ClassType.Mystic, ClassLevel.Fourth), arcanaLord(PlayerRace.Human, ClassType.Mystic, ClassLevel.Fourth), 
  cardinal(PlayerRace.Human, ClassType.Mystic, ClassLevel.Fourth), hierophant(PlayerRace.Human, ClassType.Mystic, ClassLevel.Fourth), 
  evaTemplar(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Fourth), swordMuse(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Fourth), windRider(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Fourth), 
  moonlightSentinel(PlayerRace.LightElf, ClassType.Fighter, ClassLevel.Fourth), mysticMuse(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.Fourth), 
  elementalMaster(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.Fourth), evaSaint(PlayerRace.LightElf, ClassType.Mystic, ClassLevel.Fourth), 
  shillienTemplar(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Fourth), spectralDancer(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Fourth), ghostHunter(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Fourth), 
  ghostSentinel(PlayerRace.DarkElf, ClassType.Fighter, ClassLevel.Fourth), stormScreamer(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.Fourth), 
  spectralMaster(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.Fourth), shillienSaint(PlayerRace.DarkElf, ClassType.Mystic, ClassLevel.Fourth), 

  titan(PlayerRace.Orc, ClassType.Fighter, ClassLevel.Fourth), grandKhauatari(PlayerRace.Orc, ClassType.Fighter, ClassLevel.Fourth), dominator(PlayerRace.Orc, ClassType.Mystic, ClassLevel.Fourth), doomcryer(PlayerRace.Orc, ClassType.Mystic, ClassLevel.Fourth), 

  fortuneSeeker(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.Fourth), maestro(PlayerRace.Dwarf, ClassType.Fighter, ClassLevel.Fourth);

  private PlayerRace _race;
  private ClassLevel _level;
  private ClassType _type;
  private static final Set<PlayerClass> mainSubclassSet;
  private static final Set<PlayerClass> neverSubclassed;
  private static final Set<PlayerClass> subclasseSet1;
  private static final Set<PlayerClass> subclasseSet2;
  private static final Set<PlayerClass> subclasseSet3;
  private static final Set<PlayerClass> subclasseSet4;
  private static final Set<PlayerClass> subclasseSet5;
  private static final EnumMap<PlayerClass, Set<PlayerClass>> subclassSetMap;

  private PlayerClass(PlayerRace pRace, ClassType pType, ClassLevel pLevel)
  {
    _race = pRace;
    _level = pLevel;
    _type = pType;
  }

  public final Set<PlayerClass> getAvailableSubclasses(L2PcInstance player) {
    Set subclasses = null;

    if (_level == ClassLevel.Third) {
      subclasses = EnumSet.copyOf(mainSubclassSet);

      subclasses.removeAll(neverSubclassed);
      subclasses.remove(this);

      switch (1.$SwitchMap$net$sf$l2j$gameserver$model$base$Race[player.getRace().ordinal()]) {
      case 1:
        subclasses.removeAll(getSet(PlayerRace.DarkElf, ClassLevel.Third));
        break;
      case 2:
        subclasses.removeAll(getSet(PlayerRace.LightElf, ClassLevel.Third));
      }

      Set unavailableClasses = (Set)subclassSetMap.get(this);

      if (unavailableClasses != null) {
        subclasses.removeAll(unavailableClasses);
      }
    }

    return subclasses;
  }

  public final Set<PlayerClass> getAllSubclasses() {
    Set subclasses = null;
    if (_level == ClassLevel.Third) {
      subclasses = EnumSet.copyOf(mainSubclassSet);
      subclasses.remove(this);
      if (Config.ALT_ANY_SUBCLASS_OVERCRAF) {
        subclasses.addAll(neverSubclassed);
      }
    }
    return subclasses;
  }

  public static final EnumSet<PlayerClass> getSet(PlayerRace race, ClassLevel level) {
    EnumSet allOf = EnumSet.noneOf(PlayerClass.class);

    for (PlayerClass playerClass : EnumSet.allOf(PlayerClass.class)) {
      if (((race == null) || (playerClass.isOfRace(race))) && (
        (level == null) || (playerClass.isOfLevel(level)))) {
        allOf.add(playerClass);
      }

    }

    return allOf;
  }

  public final boolean isOfRace(PlayerRace pRace) {
    return _race == pRace;
  }

  public final boolean isOfType(ClassType pType) {
    return _type == pType;
  }

  public final boolean isOfLevel(ClassLevel pLevel) {
    return _level == pLevel;
  }

  public final ClassLevel getLevel() {
    return _level;
  }

  static
  {
    neverSubclassed = EnumSet.of(Overlord, Warsmith);
    subclasseSet1 = EnumSet.of(DarkAvenger, Paladin, TempleKnight, ShillienKnight);

    subclasseSet2 = EnumSet.of(TreasureHunter, AbyssWalker, Plainswalker);

    subclasseSet3 = EnumSet.of(Hawkeye, SilverRanger, PhantomRanger);

    subclasseSet4 = EnumSet.of(Warlock, ElementalSummoner, PhantomSummoner);

    subclasseSet5 = EnumSet.of(Sorceror, Spellsinger, Spellhowler);
    subclassSetMap = new EnumMap(PlayerClass.class);

    Set subclasses = getSet(null, ClassLevel.Third);
    subclasses.removeAll(neverSubclassed);

    mainSubclassSet = subclasses;

    subclassSetMap.put(DarkAvenger, subclasseSet1);
    subclassSetMap.put(Paladin, subclasseSet1);
    subclassSetMap.put(TempleKnight, subclasseSet1);
    subclassSetMap.put(ShillienKnight, subclasseSet1);

    subclassSetMap.put(TreasureHunter, subclasseSet2);
    subclassSetMap.put(AbyssWalker, subclasseSet2);
    subclassSetMap.put(Plainswalker, subclasseSet2);

    subclassSetMap.put(Hawkeye, subclasseSet3);
    subclassSetMap.put(SilverRanger, subclasseSet3);
    subclassSetMap.put(PhantomRanger, subclasseSet3);

    subclassSetMap.put(Warlock, subclasseSet4);
    subclassSetMap.put(ElementalSummoner, subclasseSet4);
    subclassSetMap.put(PhantomSummoner, subclasseSet4);

    subclassSetMap.put(Sorceror, subclasseSet5);
    subclassSetMap.put(Spellsinger, subclasseSet5);
    subclassSetMap.put(Spellhowler, subclasseSet5);
  }
}