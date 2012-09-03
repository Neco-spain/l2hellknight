package l2rt.gameserver.model.base;

/**
 * This class defines all classes (ex : human fighter, darkFighter...) that a player can chose.<BR><BR>
 *
 * Data :<BR><BR>
 * <li>id : The Identifier of the class</li>
 * <li>isMage : True if the class is a mage class</li>
 * <li>race : The race of this class</li>
 * <li>parent : The parent ClassId for male or null if this class is the root</li>
 * <li>parent2 : The parent2 ClassId for female or null if parent2 like parent</li>
 * <li>level : The child level of this Class</li><BR><BR>
 */
@SuppressWarnings( { "nls", "unqualified-field-access", "boxing" })
public enum ClassId
{
	fighter(0, false, Race.human, null, null, 1),

	warrior(1, false, Race.human, fighter, null, 2),
	gladiator(2, false, Race.human, warrior, null, 3),
	warlord(3, false, Race.human, warrior, null, 3),
	knight(4, false, Race.human, fighter, null, 2),
	paladin(5, false, Race.human, knight, null, 3),
	darkAvenger(6, false, Race.human, knight, null, 3),
	rogue(7, false, Race.human, fighter, null, 2),
	treasureHunter(8, false, Race.human, rogue, null, 3),
	hawkeye(9, false, Race.human, rogue, null, 3),

	mage(10, true, Race.human, null, null, 1),
	wizard(11, true, Race.human, mage, null, 2),
	sorceror(12, true, Race.human, wizard, null, 3),
	necromancer(13, true, Race.human, wizard, null, 3),
	warlock(14, true, Race.human, wizard, null, 3),
	cleric(15, true, Race.human, mage, null, 2),
	bishop(16, true, Race.human, cleric, null, 3),
	prophet(17, true, Race.human, cleric, null, 3),

	elvenFighter(18, false, Race.elf, null, null, 1),
	elvenKnight(19, false, Race.elf, elvenFighter, null, 2),
	templeKnight(20, false, Race.elf, elvenKnight, null, 3),
	swordSinger(21, false, Race.elf, elvenKnight, null, 3),
	elvenScout(22, false, Race.elf, elvenFighter, null, 2),
	plainsWalker(23, false, Race.elf, elvenScout, null, 3),
	silverRanger(24, false, Race.elf, elvenScout, null, 3),

	elvenMage(25, true, Race.elf, null, null, 1),
	elvenWizard(26, true, Race.elf, elvenMage, null, 2),
	spellsinger(27, true, Race.elf, elvenWizard, null, 3),
	elementalSummoner(28, true, Race.elf, elvenWizard, null, 3),
	oracle(29, true, Race.elf, elvenMage, null, 2),
	elder(30, true, Race.elf, oracle, null, 3),

	darkFighter(31, false, Race.darkelf, null, null, 1),
	palusKnight(32, false, Race.darkelf, darkFighter, null, 2),
	shillienKnight(33, false, Race.darkelf, palusKnight, null, 3),
	bladedancer(34, false, Race.darkelf, palusKnight, null, 3),
	assassin(35, false, Race.darkelf, darkFighter, null, 2),
	abyssWalker(36, false, Race.darkelf, assassin, null, 3),
	phantomRanger(37, false, Race.darkelf, assassin, null, 3),

	darkMage(38, true, Race.darkelf, null, null, 1),
	darkWizard(39, true, Race.darkelf, darkMage, null, 2),
	spellhowler(40, true, Race.darkelf, darkWizard, null, 3),
	phantomSummoner(41, true, Race.darkelf, darkWizard, null, 3),
	shillienOracle(42, true, Race.darkelf, darkMage, null, 2),
	shillienElder(43, true, Race.darkelf, shillienOracle, null, 3),

	orcFighter(44, false, Race.orc, null, null, 1),
	orcRaider(45, false, Race.orc, orcFighter, null, 2),
	destroyer(46, false, Race.orc, orcRaider, null, 3),
	orcMonk(47, false, Race.orc, orcFighter, null, 2),
	tyrant(48, false, Race.orc, orcMonk, null, 3),

	orcMage(49, true, Race.orc, null, null, 1),
	orcShaman(50, true, Race.orc, orcMage, null, 2),
	overlord(51, true, Race.orc, orcShaman, null, 3),
	warcryer(52, true, Race.orc, orcShaman, null, 3),

	dwarvenFighter(53, false, Race.dwarf, null, null, 1),
	scavenger(54, false, Race.dwarf, dwarvenFighter, null, 2),
	bountyHunter(55, false, Race.dwarf, scavenger, null, 3),
	artisan(56, false, Race.dwarf, dwarvenFighter, null, 2),
	warsmith(57, false, Race.dwarf, artisan, null, 3),

	/*
	 * Dummy Entries (id's already in decimal format)
	 * btw FU NCSoft for the amount of work you put me
	 * through to do this!!
	 * <START>
	 */
	dummyEntry1(58, false, null, null, null, 0),
	dummyEntry2(59, false, null, null, null, 0),
	dummyEntry3(60, false, null, null, null, 0),
	dummyEntry4(61, false, null, null, null, 0),
	dummyEntry5(62, false, null, null, null, 0),
	dummyEntry6(63, false, null, null, null, 0),
	dummyEntry7(64, false, null, null, null, 0),
	dummyEntry8(65, false, null, null, null, 0),
	dummyEntry9(66, false, null, null, null, 0),
	dummyEntry10(67, false, null, null, null, 0),
	dummyEntry11(68, false, null, null, null, 0),
	dummyEntry12(69, false, null, null, null, 0),
	dummyEntry13(70, false, null, null, null, 0),
	dummyEntry14(71, false, null, null, null, 0),
	dummyEntry15(72, false, null, null, null, 0),
	dummyEntry16(73, false, null, null, null, 0),
	dummyEntry17(74, false, null, null, null, 0),
	dummyEntry18(75, false, null, null, null, 0),
	dummyEntry19(76, false, null, null, null, 0),
	dummyEntry20(77, false, null, null, null, 0),
	dummyEntry21(78, false, null, null, null, 0),
	dummyEntry22(79, false, null, null, null, 0),
	dummyEntry23(80, false, null, null, null, 0),
	dummyEntry24(81, false, null, null, null, 0),
	dummyEntry25(82, false, null, null, null, 0),
	dummyEntry26(83, false, null, null, null, 0),
	dummyEntry27(84, false, null, null, null, 0),
	dummyEntry28(85, false, null, null, null, 0),
	dummyEntry29(86, false, null, null, null, 0),
	dummyEntry30(87, false, null, null, null, 0),
	/*
	 * <END>
	 * Of Dummy entries
	 */

	duelist(88, false, Race.human, gladiator, null, 4),
	dreadnought(89, false, Race.human, warlord, null, 4),
	phoenixKnight(90, false, Race.human, paladin, null, 4),
	hellKnight(91, false, Race.human, darkAvenger, null, 4),
	sagittarius(92, false, Race.human, hawkeye, null, 4),
	adventurer(93, false, Race.human, treasureHunter, null, 4),
	archmage(94, true, Race.human, sorceror, null, 4),
	soultaker(95, true, Race.human, necromancer, null, 4),
	arcanaLord(96, true, Race.human, warlock, null, 4),
	cardinal(97, true, Race.human, bishop, null, 4),
	hierophant(98, true, Race.human, prophet, null, 4),

	evaTemplar(99, false, Race.elf, templeKnight, null, 4),
	swordMuse(100, false, Race.elf, swordSinger, null, 4),
	windRider(101, false, Race.elf, plainsWalker, null, 4),
	moonlightSentinel(102, false, Race.elf, silverRanger, null, 4),
	mysticMuse(103, true, Race.elf, spellsinger, null, 4),
	elementalMaster(104, true, Race.elf, elementalSummoner, null, 4),
	evaSaint(105, true, Race.elf, elder, null, 4),

	shillienTemplar(106, false, Race.darkelf, shillienKnight, null, 4),
	spectralDancer(107, false, Race.darkelf, bladedancer, null, 4),
	ghostHunter(108, false, Race.darkelf, abyssWalker, null, 4),
	ghostSentinel(109, false, Race.darkelf, phantomRanger, null, 4),
	stormScreamer(110, true, Race.darkelf, spellhowler, null, 4),
	spectralMaster(111, true, Race.darkelf, phantomSummoner, null, 4),
	shillienSaint(112, true, Race.darkelf, shillienElder, null, 4),

	titan(113, false, Race.orc, destroyer, null, 4),
	grandKhauatari(114, false, Race.orc, tyrant, null, 4),
	dominator(115, true, Race.orc, overlord, null, 4),
	doomcryer(116, true, Race.orc, warcryer, null, 4),

	fortuneSeeker(117, false, Race.dwarf, bountyHunter, null, 4),
	maestro(118, false, Race.dwarf, warsmith, null, 4),

	dummyEntry31(119, false, null, null, null, 0),
	dummyEntry32(120, false, null, null, null, 0),
	dummyEntry33(121, false, null, null, null, 0),
	dummyEntry34(122, false, null, null, null, 0),

	/** Kamael */
	maleSoldier(123, false, Race.kamael, null, null, 1),
	femaleSoldier(124, false, Race.kamael, null, null, 1),
	trooper(125, false, Race.kamael, maleSoldier, null, 2),
	warder(126, false, Race.kamael, femaleSoldier, null, 2),
	berserker(127, false, Race.kamael, trooper, null, 3),
	maleSoulbreaker(128, false, Race.kamael, trooper, null, 3),
	femaleSoulbreaker(129, false, Race.kamael, warder, null, 3),
	arbalester(130, false, Race.kamael, warder, null, 3),
	doombringer(131, false, Race.kamael, berserker, null, 4),
	maleSoulhound(132, false, Race.kamael, maleSoulbreaker, null, 4),
	femaleSoulhound(133, false, Race.kamael, femaleSoulbreaker, null, 4),
	trickster(134, false, Race.kamael, arbalester, null, 4),

	/**
	 * Kamael SubClass<br>
	 * Для инспектора родители нужны исключительно для
	 * получения скилов этих родителеей при взятии данного саб класса
	 */
	inspector(135, false, Race.kamael, trooper, warder, 3),
	/** 3я профа сабкласса inspector */
	judicator(136, false, Race.kamael, inspector, null, 4),
	
	dummyEntry35(137, false, null, null, null, 0),
	dummyEntry36(138, false, null, null, null, 0),
	
	/** Awaking классы. Нужно только для скилов.... */
	sigelKnight(139, false, null, null, null, 5),
	tyrrWarrior(140, false, null, null, null, 5),
	othellRogue(141, false, null, null, null, 5),
	yulArcher(142,	 false, null, null, null, 5),
	
	feohWizard(143, 	true, null, null, null, 5),
	issEnchanter(144, 	false, null, null, null, 5),
	wynnSummoner(145,	true, null, null, null, 5),
	aeoreHealer(146,	true, null, null, null, 5);
	
	/** The Identifier of the Class<?> */
	private final int _id;

	/** True if the class is a mage class */
	private final boolean _isMage;

	/** The Race object of the class */
	private final Race _race;

	/** The parent ClassId for male or null if this class is a root */
	private final ClassId _parent;

	/** The parent2 ClassId for female or null if parent2 class is parent */
	private final ClassId _parent2;

	private final int _level;

	/**
	 * Constructor<?> of ClassId.<BR><BR>
	 */
	private ClassId(int id, boolean isMage, Race race, ClassId parent, ClassId parent2, int level)
	{
		_id = id;
		_isMage = isMage;
		_race = race;
		_parent = parent;
		_parent2 = parent2;
		_level = level;
	}

	/**
	 * Return the Identifier of the Class.<BR><BR>
	 */
	public final int getId()
	{
		return _id;
	}

	/**
	 * Return True if the class is a mage class.<BR><BR>
	 */
	public final boolean isMage()
	{
		return _isMage;
	}

	/**
	 * Return the Race object of the class.<BR><BR>
	 */
	public final Race getRace()
	{
		return _race;
	}

	/**
	 * Return True if this Class<?> is a child of the selected ClassId.<BR><BR>
	 *
	 * @param cid The parent ClassId to check
	 */
	public final boolean childOf(ClassId cid)
	{
		if(_parent == null)
			return false;

		if(_parent == cid || _parent2 == cid)
			return true;

		return _parent.childOf(cid);

	}

	/**
	 * Return True if this Class<?> is equal to the selected ClassId or a child of the selected ClassId.<BR><BR>
	 *
	 * @param cid The parent ClassId to check
	 */
	public final boolean equalsOrChildOf(ClassId cid)
	{
		return this == cid || childOf(cid);
	}

	/**
	 * Return the child level of this Class<?> (0=root, 1=child leve 1...).<BR><BR>

	 */
	public final byte level()
	{
		if(_parent == null)
			return 0;

		return (byte) (1 + _parent.level());
	}

	public final ClassId getParent(byte sex)
	{
		return sex == 0 || _parent2 == null ? _parent : _parent2;
	}

	public final int getLevel()
	{
		return _level;
	}
}