package l2rt.gameserver.templates;

import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Skill.SkillType;
import l2rt.gameserver.model.items.L2ItemInstance.ItemClass;
import l2rt.gameserver.skills.funcs.FuncTemplate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2EtcItem.EtcItemType;
import l2rt.gameserver.templates.L2Weapon.WeaponType;

import java.util.regex.Pattern;

/**
 * This class contains all informations concerning the item (weapon, armor, etc).<BR>
 * Mother class of :
 * <LI>L2Armor</LI>
 * <LI>L2EtcItem</LI>
 * <LI>L2Weapon</LI>
 */
public abstract class L2Item
{
	/**
	 * Pc Cafe Bang Points item id. Используется на корейских серверах, но английский клиент в состоянии
	 * поддерживать даный функционал.
	 */
	public static final short ITEM_ID_PC_BANG_POINTS = -100;
	public static final short ITEM_ID_CLAN_REPUTATION_SCORE = -200;
	public static final short ITEM_ID_FAME = -300;
	public static final int ITEM_ID_ADENA = 57;

	public static final byte TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final byte TYPE1_SHIELD_ARMOR = 1;
	public static final byte TYPE1_OTHER = 2;
	public static final byte TYPE1_ITEM_QUESTITEM_ADENA = 4;

	public static final byte TYPE2_WEAPON = 0;
	public static final byte TYPE2_SHIELD_ARMOR = 1;
	public static final byte TYPE2_ACCESSORY = 2;
	public static final byte TYPE2_QUEST = 3;
	public static final byte TYPE2_MONEY = 4;
	public static final byte TYPE2_OTHER = 5;
	public static final byte TYPE2_PET_WOLF = 6;
	public static final byte TYPE2_PET_HATCHLING = 7;
	public static final byte TYPE2_PET_STRIDER = 8;
	public static final byte TYPE2_NODROP = 9;
	public static final byte TYPE2_PET_GWOLF = 10;
	public static final byte TYPE2_PENDANT = 11;
	public static final byte TYPE2_PET_BABY = 12;

	public static final int SLOT_NONE = 0x00000;
	public static final int SLOT_UNDERWEAR = 0x00001;
	public static final int SLOT_CLOAK = 0x0003; //TODO:????

	public static final int SLOT_R_EAR = 0x00002;
	public static final int SLOT_L_EAR = 0x00004;

	public static final int SLOT_NECK = 0x00008;

	public static final int SLOT_R_FINGER = 0x00010;
	public static final int SLOT_L_FINGER = 0x00020;

	public static final int SLOT_HEAD = 0x00040;
	public static final int SLOT_R_HAND = 0x00080;
	public static final int SLOT_L_HAND = 0x00100;
	public static final int SLOT_GLOVES = 0x00200;
	public static final int SLOT_CHEST = 0x00400;
	public static final int SLOT_LEGS = 0x00800;
	public static final int SLOT_FEET = 0x01000;
	public static final int SLOT_BACK = 0x02000;
	public static final int SLOT_LR_HAND = 0x04000;
	public static final int SLOT_FULL_ARMOR = 0x08000;
	public static final int SLOT_HAIR = 0x10000;
	public static final int SLOT_FORMAL_WEAR = 0x20000;
	public static final int SLOT_DHAIR = 0x40000;
	public static final int SLOT_HAIRALL = 0x80000;
	public static final int SLOT_R_BRACELET = 0x100000;
	public static final int SLOT_L_BRACELET = 0x200000;
	public static final int SLOT_DECO = 0x400000;
	public static final int SLOT_SIGIL = 0x000000; // TODO: fix
	public static final int SLOT_BELT = 0x10000000;
	public static final int SLOT_WOLF = -100;
	public static final int SLOT_HATCHLING = -101;
	public static final int SLOT_STRIDER = -102;
	public static final int SLOT_BABYPET = -103;
	public static final int SLOT_GWOLF = -104;
	public static final int SLOT_PENDANT = -105;

	public static final int CRYSTAL_NONE = 0;
	public static final int CRYSTAL_D = 1458;
	public static final int CRYSTAL_C = 1459;
	public static final int CRYSTAL_B = 1460;
	public static final int CRYSTAL_A = 1461;
	public static final int CRYSTAL_S = 1462;
	public static final int CRYSTAL_R = 17371;

	public static enum Grade
	{
		NONE(CRYSTAL_NONE, 0),
		D(CRYSTAL_D, 1),
		C(CRYSTAL_C, 2),
		B(CRYSTAL_B, 3),
		A(CRYSTAL_A, 4),
		S(CRYSTAL_S, 5),
		S80(CRYSTAL_S, 5),
		S84(CRYSTAL_S, 5),
        R(CRYSTAL_R, 6),
        R95(CRYSTAL_R, 6),
        R99(CRYSTAL_R, 6);

		public final int cry;
		public final int externalOrdinal;

		private Grade(int crystal, int ext)
		{
			cry = crystal;
			externalOrdinal = ext;
		}
	}

    public static enum Bodypart {
        NONE(0x00),
        UNDERWEAR(0x01),
        CLOAK(0x03),
        REAR(0x02),
        LEAR(0x04),
        LREAR(0x06),
        NECK(0x08),
        RFINGER(0x10),
        LFINGER(0x20),
        LRFINGER(0x30),
        HEAD(0x40),
        RHAND(0x80),
        LHAND(0x0100),
        GLOVES(0x0200),
        CHEST(0x0400),
        LEGS(0x0800),
        FEET(0x1000),
        BACK(0x2000),
        LRHAND(0x4000),
        FULLARMOR(0x8000),
        HAIR(0x010000),
        FORMALWEAR(0x020000),
        FACE(0x040000),
        HAIRALL(0x080000),
        RBRACELET(0x100000),
        LBRACELET(0x200000),
        TALISMAN(0x400000),
        SIGIL(0x000000),
        BELT(0x10000000),
        WOLF(-100),
        HATCHLING(-101),
        STRIDER(-102),
        BABY(-103),
        GWOLF(-104),
        PENDANT(-105);

        private final int val;

        private Bodypart(int val) {
            this.val = val;
        }
        public int getVal() {
            return val;
        }
    }

	public static final byte ATTRIBUTE_NONE = -2;
	public static final byte ATTRIBUTE_FIRE = 0;
	public static final byte ATTRIBUTE_WATER = 1;
	public static final byte ATTRIBUTE_WIND = 2;
	public static final byte ATTRIBUTE_EARTH = 3;
	public static final byte ATTRIBUTE_HOLY = 4;
	public static final byte ATTRIBUTE_DARK = 5;

	protected final int _itemId;
	private final ItemClass _class;
	protected final String _name;
	protected final String _addname;
	protected final String _icon;
	private final short _type1; // needed for item list (inventory)
	private final short _type2; // different lists for armor, weapon, etc
	private final int _weight;
	private final boolean _crystallizable;
	private final boolean _stackable;
	protected final Grade _crystalType; // default to none-grade
	private final int _flags;
	private final int _durability;
	private final boolean _temporal;
	private final int _bodyPart;
	private final int _referencePrice;
	private final short _crystalCount;
	private final int _item_r;
	private final boolean _sellable;
	private final boolean _dropable;
	private final boolean _tradeable;
	private final boolean _destroyable;
	private boolean _rare = false;
	private boolean _pvp = false;
	private boolean _sa = false;
	private boolean _common = false;

	protected L2Skill[] _skills;
	protected L2Skill _enchant4Skill = null; // skill that activates when item is enchanted +4 (for duals)
	public final Enum type;

	protected FuncTemplate[] _funcTemplates;
	private static final Pattern _noskill;
	static
	{
		_noskill = Pattern.compile("(^0$)|(^-1$)");
	}

	/**
	 * Constructor<?> of the L2Item that fill class variables.<BR><BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>type</LI>
	 * <LI>_itemId</LI>
	 * <LI>_name</LI>
	 * <LI>_type1 & _type2</LI>
	 * <LI>_weight</LI>
	 * <LI>_crystallizable</LI>
	 * <LI>_stackable</LI>
	 * <LI>_materialType & _crystalType & _crystlaCount</LI>
	 * <LI>_durability</LI>
	 * <LI>_bodypart</LI>
	 * <LI>_referencePrice</LI>
	 * <LI>_sellable</LI>
	 * @param type : Enum designating the type of the item
	 * @param set : StatsSet corresponding to a set of couples (key,value) for description of the item
	 */
	protected L2Item(final Enum<?> type, final StatsSet set)
	{
		this.type = type;
		_itemId = set.getInteger("item_id");
		_class = set.getEnum("class", ItemClass.class, ItemClass.OTHER);
		_name = set.getString("name");
		_addname = set.getString("additional_name", "");
		_icon = set.getString("icon", "");
		_type1 = set.getShort("type1"); // needed for item list (inventory)
		_type2 = set.getShort("type2"); // different lists for armor, weapon, etc
		_weight = set.getInteger("weight", 0);
		_stackable = set.getBool("stackable", false);
		_durability = set.getInteger("durability", -1);
		_temporal = set.getBool("temporal", false);
		_bodyPart = set.getEnum("bodypart", L2Item.Bodypart.class, Bodypart.NONE).getVal();
		_referencePrice = set.getInteger("price", 0);
        _crystalType = set.getEnum("crystal_type", Grade.class, Grade.NONE); // default to none-grade
        _crystalCount = set.getShort("crystal_count", (short) 0);
        _item_r = set.getInteger("item_r", 0);
        _crystallizable = _crystalCount > 0 && _crystalType != Grade.NONE;
		_sellable = _referencePrice > 0;
		_dropable = set.getBool("dropable", true);
		_destroyable = set.getBool("destroyable", true);
		_tradeable = set.getBool("tradeable", true);
		_flags = set.getInteger("flags", 0);
        _rare = set.getBool("isRare", false);
        _pvp = set.getBool("isPvP", false);
        _sa = set.getBool("isSa", false);
        _common = set.getBool("isCommon", false);
		String[] skills = set.getString("skill_id", "0").split(";");
		String[] skilllevels = set.getString("skill_level", "1").split(";");
		try
		{
			for(int i = 0; i < skills.length; i++)
				if(!(_noskill.matcher(skills[i]).matches() || _noskill.matcher(skilllevels[i]).matches()))
				{
					L2Skill skill = SkillTable.getInstance().getInfo(Integer.parseInt(skills[i]), Integer.parseInt(skilllevels[i]));
					if(skill != null)
					{
						if(skill.getSkillType() == SkillType.NOTDONE)
							System.out.println("WARNING: item " + _itemId + " action attached skill not done: " + skill);
						attachSkill(skill);
					}
					else
						System.out.println("WARNING: item " + _itemId + " attached skill not exist: " + skills[i] + " " + skilllevels[i]);
				}

			if(getItemType() == WeaponType.POLE)
				attachSkill(SkillTable.getInstance().getInfo(3599, 1));
		}
		catch(Exception e)
		{
			System.out.println("Skill: " + set.getString("skill_id", "0"));
			System.out.println("Level: " + set.getString("skill_level", "1"));
			e.printStackTrace();
		}
	}

	/**
	 * Returns the itemType.
	 * @return Enum
	 */
	public Enum getItemType()
	{
		return type;
	}

	public String getIcon()
	{
		return _icon;
	}

	/**
	 * Returns the durability of th item
	 * @return int
	 */
	public final int getDurability()
	{
		return _durability;
	}

	public final boolean isTemporal()
	{
		return _temporal;
	}

	/**
	 * Returns the ID of the item
	 * @return int
	 */
	public final int getItemId()
	{
		return _itemId;
	}

	public abstract long getItemMask();

	/**
	 * Returns the type 2 of the item
	 * @return int
	 */
	public final int getType2()
	{
		return _type2;
	}

	public final int getType2ForPackets()
	{
		int type2 = _type2;
		switch(_type2)
		{
			case TYPE2_PET_WOLF:
			case TYPE2_PET_HATCHLING:
			case TYPE2_PET_STRIDER:
			case TYPE2_PET_GWOLF:
			case TYPE2_PET_BABY:
				if(_bodyPart == L2Item.SLOT_CHEST)
					type2 = TYPE2_SHIELD_ARMOR;
				else
					type2 = TYPE2_WEAPON;
				break;
			case TYPE2_PENDANT:
				type2 = TYPE2_ACCESSORY;
				break;
		}
		return type2;
	}

	/**
	 * Returns the weight of the item
	 * @return int
	 */
	public final int getWeight()
	{
		return _weight;
	}

	/**
	 * Returns if the item is crystallizable
	 * @return boolean
	 */
	public final boolean isCrystallizable()
	{
		return _crystallizable && !isStackable() && getCrystalType() != Grade.NONE && getCrystalCount() > 0;
	}

	/**
	 * Return the type of crystal if item is crystallizable
	 * @return int
	 */
	public final Grade getCrystalType()
	{
		return _crystalType;
	}

	/**
	 * Returns the grade of the item.<BR><BR>
	 * <U><I>Concept :</I></U><BR>
	 * In fact, this fucntion returns the type of crystal of the item.
	 * @return int
	 */
	public final Grade getItemGrade()
	{
		return getCrystalType();
	}

	/**
	 * Returns the quantity of crystals for crystallization
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return _crystalCount;
	}

	/**
	 * Returns the name of the item
	 * @return String
	 */
	public final String getName()
	{
		return _name;
	}

	/**
	 * Returns the additional name of the item
	 * @return String
	 */
	public final String getAdditionalName()
	{
		return _addname;
	}

	/**
	 * Return the part of the body used with the item.
	 * @return int
	 */
	public final int getBodyPart()
	{
		return _bodyPart;
	}

	/**
	 * Returns the type 1 of the item
	 * @return int
	 */
	public final int getType1()
	{
		return _type1;
	}

	/**
	 * Returns if the item is stackable
	 * @return boolean
	 */
	public final boolean isStackable()
	{
		return _stackable;
	}

	/**
	 * Returns the price of reference of the item
	 * @return int
	 */
	public final int getReferencePrice()
	{
		return _referencePrice;
	}

	/**
	 * Returns if the item can be sold
	 * @return boolean
	 */
	public final boolean isSellable()
	{
		return _sellable;
	}

	/**
	 * Returns if item is for hatchling
	 * @return boolean
	 */
	public boolean isForHatchling()
	{
		return _type2 == TYPE2_PET_HATCHLING;
	}

	/**
	 * Returns if item is for strider
	 * @return boolean
	 */
	public boolean isForStrider()
	{
		return _type2 == TYPE2_PET_STRIDER;
	}

	/**
	 * Returns if item is for wolf
	 * @return boolean
	 */
	public boolean isForWolf()
	{
		return _type2 == TYPE2_PET_WOLF;
	}

	public boolean isForPetBaby()
	{
		return _type2 == TYPE2_PET_BABY;
	}

	/**
	 * Returns if item is for great wolf
	 * @return boolean
	 */
	public boolean isForGWolf()
	{
		return _type2 == TYPE2_PET_GWOLF;
	}

	/**
	 *  РњР°РіРёС‡РµСЃРєР°СЏ Р±СЂРѕРЅСЏ РґР»СЏ РїРµС‚РѕРІ 
	 */
	public boolean isPendant()
	{
		return _type2 == TYPE2_PENDANT;
	}

	public boolean isTradeable()
	{
		return _tradeable;
	}

	public boolean isDestroyable()
	{
		return _destroyable;
	}

	public boolean isDropable()
	{
		return _dropable;
	}

	public boolean isForPet()
	{
		return _type2 == TYPE2_PENDANT || _type2 == TYPE2_PET_HATCHLING || _type2 == TYPE2_PET_WOLF || _type2 == TYPE2_PET_STRIDER || _type2 == TYPE2_PET_GWOLF || _type2 == TYPE2_PET_BABY;
	}

	/**
	 * Add the FuncTemplate f to the list of functions used with the item
	 * @param f : FuncTemplate to add
	 */
	public void attachFunction(FuncTemplate f)
	{
		if(_funcTemplates == null)
			_funcTemplates = new FuncTemplate[] { f };
		else
		{
			int len = _funcTemplates.length;
			FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}

	public FuncTemplate[] getAttachedFuncs()
	{
		return _funcTemplates;
	}

	/**
	 * Add the L2Skill skill to the list of skills generated by the item
	 * @param skill : L2Skill
	 */
	public void attachSkill(L2Skill skill)
	{
		if(_skills == null)
			_skills = new L2Skill[] { skill };
		else
		{
			int len = _skills.length;
			L2Skill[] tmp = new L2Skill[len + 1];
			System.arraycopy(_skills, 0, tmp, 0, len);
			tmp[len] = skill;
			_skills = tmp;
		}
	}

	public L2Skill[] getAttachedSkills()
	{
		return _skills;
	}

	public L2Skill getFirstSkill()
	{
		if(_skills != null && _skills.length > 0)
			return _skills[0];
		return null;
	}

	/**
	 * @return skill that player get when has equipped weapon +4  or more  (for duals SA)
	 */
	public L2Skill getEnchant4Skill()
	{
		return _enchant4Skill;
	}
	
	public L2Skill[] getSkill()
	{
		return _skills;
	}

	/**
	 * Returns the name of the item
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _name;
	}

	/**
	 * Определяет призрачный предмет или нет
	 * @return true, если предмет призрачный
	 */
	public boolean isShadowItem()
	{
		return _durability > 0 && !isTemporal();
	}

	public boolean isCommonItem()
	{
		return _common;
	}

	public boolean isRare()
	{
		return _rare;
	}

	public boolean isPvP()
	{
		return _pvp;
	}

	public boolean isSa()
	{
		return _sa;
	}

	public void setRare(boolean isRare)
	{
		_rare = isRare;
	}

	public void setPvP(boolean isPvP)
	{
		_pvp = isPvP;
	}

	public void setSa(boolean isSa)
	{
		_sa = isSa;
	}

	public boolean isAltSeed()
	{
		return _name.contains("Alternative");
	}

	public ItemClass getItemClass()
	{
		return _class;
	}

	/**
	* Является ли вещь аденой или камнем печати
	*/
	public boolean isAdena()
	{
		return _itemId == 57 || _itemId == 6360 || _itemId == 6361 || _itemId == 6362;
	}

	public boolean isEquipment()
	{
		return _type1 != TYPE1_ITEM_QUESTITEM_ADENA;
	}

	public boolean isKeyMatherial()
	{
		return _class == ItemClass.PIECES;
	}

	public boolean isSpellBook()
	{
		return _class == ItemClass.SPELLBOOKS;
	}

	public boolean isRaidAccessory()
	{
		return _itemId == 6661 || _itemId == 6659 || _itemId == 6656 || _itemId == 6660 || _itemId == 6662 || _itemId == 6658 || _itemId == 8191 || _itemId == 6657;
	}

	public boolean isSpecialKey()
	{
		if(_itemId == 1661) // thief key
			return false;
		if(_itemId >= 6665 && _itemId <= 6672) // deluxe chest key
			return false;
		return getName().contains("Key");
	}
	
	public boolean isUnknown()
	{
		return (_itemId >= 18103 && _itemId <= 18272);
	}
	
	public boolean isConstrained()
	{
		return (_itemId >= 17831 && _itemId <= 17952) || (_itemId >= 17846 && _itemId <= 17966) || (_itemId >= 19586 && _itemId <= 19649) || (_itemId >= 19794 && _itemId <= 19857) || (_itemId >= 30434 && _itemId <= 31573) || (_itemId >= 33591 && _itemId <= 33607);
	}
	
	public boolean isConstrainedSa()
	{
		return (_itemId >= 18620 && _itemId <= 18661) || (_itemId >= 18746 && _itemId <= 18787) || (_itemId >= 18872 && _itemId <= 18913) || (_itemId >= 18998 && _itemId <= 19039) || (_itemId >= 30440 && _itemId <= 31578);
	}
	
	public boolean isArtefact()
	{
		return (_itemId >= 32817 && _itemId <= 33974);
	}
	
	public boolean isBlessed()
	{
		return (_itemId >= 17967 && _itemId <= 18088);
	}
	
	public boolean isS()
	{
		return (_itemId >= 6364 && _itemId <= 6372) || (_itemId >= 6579 && _itemId <= 6610);
	}
	
	public boolean isDynasty()
	{
		return (_itemId >= 9442 && _itemId <= 9450) || (_itemId >= 9372 && _itemId <= 9387) || (_itemId >= 9854 && _itemId <= 9880);
	}
	
	public boolean isVesper()
	{
		return (_itemId >= 13457 && _itemId <= 13470) || (_itemId >= 14118 && _itemId <= 14159);
	}
	
	public boolean isFreya()
	{
		return (_itemId >= 15544 && _itemId <= 15571) || (_itemId >= 15829 && _itemId <= 15912);
	}
	
	public boolean isRequiem()
	{
		return (_itemId >= 17290 && _itemId <= 17303) || (_itemId >= 18578 && _itemId <= 18703);
	}
	
	public boolean isApocalipsis()
	{
		return (_itemId >= 17324 && _itemId <= 17337) || (_itemId >= 18704 && _itemId <= 18829);
	}
	
	public boolean isFantazm()
	{
		return (_itemId >= 17382 && _itemId <= 17395) || (_itemId >= 18830 && _itemId <= 18955);
	}

	public boolean isHelios()
	{
		return (_itemId >= 17416 && _itemId <= 17429) || (_itemId >= 18956 && _itemId <= 19081);
	}
	
	public boolean isGod()
	{
		return (_itemId >= 19082 && _itemId <= 19165);
	}

	public boolean isAllGodWeapon()
	{
		return (_itemId >= 17967 && _itemId <= 18088) || (_itemId >= 18662 && _itemId <= 18703) || (_itemId >= 18788 && _itemId <= 18829) || (_itemId >= 18914 && _itemId <= 18955) || (_itemId >= 19040 && _itemId <= 19165) || (_itemId >= 19980 && _itemId <= 19999) || (_itemId >= 30000 && _itemId <= 30021) || (_itemId >= 30064 && _itemId <= 30105) || (_itemId >= 30148 && _itemId <= 30189) || (_itemId >= 30232 && _itemId <= 30273);
	}
	
	public boolean isDualSa()
	{
		return (_itemId >= 19938 && _itemId <= 19979) || (_itemId >= 30022 && _itemId <= 30063) || (_itemId >= 30106 && _itemId <= 30147) || (_itemId >= 30190 && _itemId <= 30231);
	}
	
	public boolean isArmorS()
	{
		return (_itemId >= 6373 && _itemId <= 6386);
	}
	
	public boolean isArmorDynasty()
	{
		return (_itemId >= 9416 && _itemId <= 9440) || _itemId == 11513 || _itemId == 11514 || _itemId == 11515 || _itemId == 11524 || _itemId == 11525 || _itemId == 11526 || _itemId == 11533 || _itemId == 11539 || _itemId == 11557;
	}
	
	public boolean isArmorVesper()
	{
		return (_itemId >= 13137 && _itemId <= 13139) || (_itemId >= 13432 && _itemId <= 13446);
	}
	
	public boolean isArmorVerpes()
	{
		return (_itemId >= 15589 && _itemId <= 15605);
	}
	
	public boolean isArmorElegia()
	{
		return (_itemId >= 15573 && _itemId <= 15588);
	}
	
	public boolean isArmorInvul()
	{
		return (_itemId >= 19522 && _itemId <= 19537);
	}
	
	public boolean isArmorAd()
	{
		return (_itemId >= 19538 && _itemId <= 19553);
	}
	
	public boolean isArmorKadeyra()
	{
		return (_itemId >= 19554 && _itemId <= 19569);
	}
	
	public boolean isArmorAidios()
	{
		return (_itemId >= 19570 && _itemId <= 19585);
	}
	
	public boolean isFullArmor()
	{
		return _bodyPart == SLOT_FULL_ARMOR;
	}
	
	public boolean isChestLegs()
	{
		return _bodyPart == SLOT_CHEST || _bodyPart == SLOT_LEGS;
	}
	
	public boolean isArrow()
	{
		return type == EtcItemType.ARROW;
	}

	public boolean isBelt()
	{
		return _bodyPart == SLOT_BELT;
	}

	public boolean isBracelet()
	{
		return _bodyPart == SLOT_R_BRACELET || _bodyPart == SLOT_L_BRACELET;
	}

	public boolean isUnderwear()
	{
		return _bodyPart == SLOT_UNDERWEAR;
	}

	public boolean isCloak()
	{
		return _bodyPart == SLOT_BACK;
	}

	public boolean isTalisman()
	{
		return _bodyPart == SLOT_DECO;
	}

	public boolean isHerb()
	{
		return _itemId >= 8600 && _itemId <= 8614 || _itemId >= 10655 && _itemId <= 10657 || _itemId == 13028 || _itemId == 9849;
	}

	public boolean isMageSA()
	{
		return _addname.equalsIgnoreCase("Acumen") //
				|| _addname.equalsIgnoreCase("Empower") //
				|| _addname.equalsIgnoreCase("Magic Silence") //
				|| _addname.equalsIgnoreCase("Magic Mental Shield") //
				|| _addname.equalsIgnoreCase("Mana Up") //
				|| _addname.equalsIgnoreCase("Updown") //
				|| _addname.equalsIgnoreCase("Magic Hold") //
				|| _addname.equalsIgnoreCase("MP Regeneration");
	}

	public boolean isHeroWeapon()
	{
		return _itemId >= 6611 && _itemId <= 6621 || _itemId >= 9388 && _itemId <= 9390;
	}

	public boolean isCursed()
	{
		return CursedWeaponsManager.getInstance().isCursed(_itemId);
	}

	public boolean isCombatFlag()
	{
		return _itemId == 9819;
	}

	public boolean isTerritoryFlag()
	{
		return _itemId == 13560 || _itemId == 13561 || _itemId == 13562 || _itemId == 13563 || _itemId == 13564 || _itemId == 13565 || _itemId == 13566 || _itemId == 13567 || _itemId == 13568;
	}

	public boolean isRod()
	{
		return getItemType() == WeaponType.ROD;
	}

	public boolean isWeapon()
	{
		return getType2() == L2Item.TYPE2_WEAPON;
	}

	public boolean isArmor()
	{
		return getType2() == L2Item.TYPE2_SHIELD_ARMOR;
	}

	public boolean isAccessory()
	{
		return getType2() == L2Item.TYPE2_ACCESSORY;
	}

	public boolean isQuest()
	{
		return getType2() == L2Item.TYPE2_QUEST;
	}

	public boolean canBeEnchanted()
	{
		if(isHeroWeapon())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporal())
			return false;

		if(isCommonItem())
			return false;

		if(isRod())
			return false;

		if(isBracelet())
			return false;

		if(isCloak())
			return false;

		if(isCursed())
			return false;

		if(isQuest())
			return false;

		return isCrystallizable();
	}

	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return getItemType() == EtcItemType.BAIT || getItemType() == EtcItemType.ARROW || getItemType() == EtcItemType.BOLT || !(getBodyPart() == 0 || this instanceof L2EtcItem);
	}

	public int getFlags()
	{
		return _flags;
	}
	
	public int getItem_r()
	{
			return _item_r;
	}
}