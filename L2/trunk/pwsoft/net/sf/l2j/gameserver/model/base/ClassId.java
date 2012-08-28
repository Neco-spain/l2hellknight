package net.sf.l2j.gameserver.model.base;

public enum ClassId
{
  fighter(0, false, Race.human, null), 

  warrior(1, false, Race.human, fighter), gladiator(2, false, Race.human, warrior), warlord(3, false, Race.human, warrior), 
  knight(4, false, Race.human, fighter), paladin(5, false, Race.human, knight), 
  darkAvenger(6, false, Race.human, knight), rogue(7, false, Race.human, fighter), 
  treasureHunter(8, false, Race.human, rogue), hawkeye(9, false, Race.human, rogue), 

  mage(10, true, Race.human, null), wizard(11, true, Race.human, mage), sorceror(12, true, Race.human, wizard), 
  necromancer(13, true, Race.human, wizard), warlock(14, true, Race.human, wizard), 
  cleric(15, true, Race.human, mage), bishop(16, true, Race.human, cleric), 
  prophet(17, true, Race.human, cleric), 

  elvenFighter(18, false, Race.elf, null), elvenKnight(19, false, Race.elf, elvenFighter), templeKnight(20, false, Race.elf, elvenKnight), 
  swordSinger(21, false, Race.elf, elvenKnight), elvenScout(22, false, Race.elf, elvenFighter), 
  plainsWalker(23, false, Race.elf, elvenScout), silverRanger(24, false, Race.elf, elvenScout), 

  elvenMage(25, true, Race.elf, null), elvenWizard(26, true, Race.elf, elvenMage), spellsinger(27, true, Race.elf, elvenWizard), 
  elementalSummoner(28, true, Race.elf, elvenWizard), oracle(29, true, Race.elf, elvenMage), 
  elder(30, true, Race.elf, oracle), 

  darkFighter(31, false, Race.darkelf, null), palusKnight(32, false, Race.darkelf, darkFighter), shillienKnight(33, false, Race.darkelf, palusKnight), 
  bladedancer(34, false, Race.darkelf, palusKnight), assassin(35, false, Race.darkelf, darkFighter), 
  abyssWalker(36, false, Race.darkelf, assassin), phantomRanger(37, false, Race.darkelf, assassin), 

  darkMage(38, true, Race.darkelf, null), darkWizard(39, true, Race.darkelf, darkMage), spellhowler(40, true, Race.darkelf, darkWizard), 
  phantomSummoner(41, true, Race.darkelf, darkWizard), shillienOracle(42, true, Race.darkelf, darkMage), 
  shillenElder(43, true, Race.darkelf, shillienOracle), 

  orcFighter(44, false, Race.orc, null), orcRaider(45, false, Race.orc, orcFighter), destroyer(46, false, Race.orc, orcRaider), 
  orcMonk(47, false, Race.orc, orcFighter), tyrant(48, false, Race.orc, orcMonk), 

  orcMage(49, true, Race.orc, null), orcShaman(50, true, Race.orc, orcMage), overlord(51, true, Race.orc, orcShaman), 
  warcryer(52, true, Race.orc, orcShaman), 

  dwarvenFighter(53, false, Race.dwarf, null), scavenger(54, false, Race.dwarf, dwarvenFighter), bountyHunter(55, false, Race.dwarf, scavenger), 
  artisan(56, false, Race.dwarf, dwarvenFighter), warsmith(57, false, Race.dwarf, artisan), 

  dummyEntry1(58, false, null, null), dummyEntry2(59, false, null, null), dummyEntry3(60, false, null, null), 
  dummyEntry4(61, false, null, null), dummyEntry5(62, false, null, null), dummyEntry6(63, false, null, null), 
  dummyEntry7(64, false, null, null), dummyEntry8(65, false, null, null), 
  dummyEntry9(66, false, null, null), dummyEntry10(67, false, null, null), dummyEntry11(68, false, null, null), 
  dummyEntry12(69, false, null, null), dummyEntry13(70, false, null, null), 
  dummyEntry14(71, false, null, null), dummyEntry15(72, false, null, null), dummyEntry16(73, false, null, null), 
  dummyEntry17(74, false, null, null), dummyEntry18(75, false, null, null), 
  dummyEntry19(76, false, null, null), dummyEntry20(77, false, null, null), dummyEntry21(78, false, null, null), 
  dummyEntry22(79, false, null, null), dummyEntry23(80, false, null, null), 
  dummyEntry24(81, false, null, null), dummyEntry25(82, false, null, null), dummyEntry26(83, false, null, null), 
  dummyEntry27(84, false, null, null), dummyEntry28(85, false, null, null), 
  dummyEntry29(86, false, null, null), dummyEntry30(87, false, null, null), 

  duelist(88, false, Race.human, gladiator), dreadnought(89, false, Race.human, warlord), phoenixKnight(90, false, Race.human, paladin), 
  hellKnight(91, false, Race.human, darkAvenger), sagittarius(92, false, Race.human, hawkeye), 
  adventurer(93, false, Race.human, treasureHunter), archmage(94, true, Race.human, sorceror), 
  soultaker(95, true, Race.human, necromancer), arcanaLord(96, true, Race.human, warlock), 
  cardinal(97, true, Race.human, bishop), hierophant(98, true, Race.human, prophet), 

  evaTemplar(99, false, Race.elf, templeKnight), swordMuse(100, false, Race.elf, swordSinger), windRider(101, false, Race.elf, plainsWalker), 
  moonlightSentinel(102, false, Race.elf, silverRanger), mysticMuse(103, true, Race.elf, spellsinger), 
  elementalMaster(104, true, Race.elf, elementalSummoner), evaSaint(105, true, Race.elf, elder), 

  shillienTemplar(106, false, Race.darkelf, shillienKnight), spectralDancer(107, false, Race.darkelf, bladedancer), 
  ghostHunter(108, false, Race.darkelf, abyssWalker), ghostSentinel(109, false, Race.darkelf, phantomRanger), 
  stormScreamer(110, true, Race.darkelf, spellhowler), 
  spectralMaster(111, true, Race.darkelf, phantomSummoner), shillienSaint(112, true, Race.darkelf, shillenElder), 

  titan(113, false, Race.orc, destroyer), grandKhauatari(114, false, Race.orc, tyrant), dominator(115, true, Race.orc, overlord), 
  doomcryer(116, true, Race.orc, warcryer), 

  fortuneSeeker(117, false, Race.dwarf, bountyHunter), maestro(118, false, Race.dwarf, warsmith);

  private final int _id;
  private final boolean _isMage;
  private final Race _race;
  private final ClassId _parent;

  private ClassId(int pId, boolean pIsMage, Race pRace, ClassId pParent)
  {
    _id = pId;
    _isMage = pIsMage;
    _race = pRace;
    _parent = pParent;
  }

  public final int getId()
  {
    return _id;
  }

  public final boolean isMage()
  {
    return _isMage;
  }

  public final Race getRace()
  {
    return _race;
  }

  public final boolean childOf(ClassId cid)
  {
    if (_parent == null) return false;

    if (_parent == cid) return true;

    return _parent.childOf(cid);
  }

  public final boolean equalsOrChildOf(ClassId cid)
  {
    return (this == cid) || (childOf(cid));
  }

  public final int level()
  {
    if (_parent == null) return 0;

    return 1 + _parent.level();
  }

  public final ClassId getParent()
  {
    return _parent;
  }
}