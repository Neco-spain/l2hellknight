package l2rt.gameserver.model.items;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.Stat;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.extensions.scripts.Events;
import l2rt.gameserver.ai.CtrlIntention;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.instancemanager.MercTicketManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.base.L2Augmentation;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.InventoryUpdate;
import l2rt.gameserver.network.serverpackets.ItemList;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.skills.funcs.Func;
import l2rt.gameserver.skills.funcs.FuncTemplate;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.taskmanager.ItemsAutoDestroy;
import l2rt.gameserver.templates.L2Armor;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Item.Grade;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.Location;
import l2rt.util.Log;
import l2rt.util.Rnd;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class L2ItemInstance extends L2Object
{
	private static final Logger _log = Logger.getLogger(L2ItemInstance.class.getName());

    /** Enumeration of locations for item */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		FREIGHT,
		LEASE, // это вещи отправленные по почте, но еще не полученные адресатом
		MONSTER,
		DUMMY
	}

	/** Item types to select */
	public static enum ItemClass
	{
		/** List all deposited items */
		ALL,
		/** Weapons, Armor, Jevels, Arrows, Baits*/
		EQUIPMENT,
		/** Soul/Spiritshot, Potions, Scrolls */
		CONSUMABLE,
		/** Common craft matherials */
		MATHERIALS,
		/** Special (item specific) craft matherials */
		PIECES,
		/** Crafting recipies */
		RECIPIES,
		/** Skill learn books */
		SPELLBOOKS,
		/** Dyes, lifestones */
		MISC,
		/** All other */
		OTHER
	}

	private long ownerStoreId, itemDropOwnerStoreId;

	/** Время жизни призрачных вещей **/
	ScheduledFuture<?> _itemLifeTimeTask;
	private int _lifeTimeRemaining;

	/** Quantity of the item */
	private long _count;

	/** ID of the item */
	private int _itemId;

	/** Object L2Item associated to the item */
	private L2Item _itemTemplate;
	
	/** Object L2Item associated to the item */
	private L2Weapon _WeaponTemplate;

	/** Location of the item */
	private ItemLocation _loc;

	/** Slot where item is stored */
	private int _loc_data;

	/** Level of enchantment of the item */
	private int _enchantLevel;

	/** Price of the item for selling */
	private long _price_sell;

	private long _count_sell;

	/** Wear Item */
	private boolean _wear;

	private L2Augmentation _augmentation = null;

	/** Custom item types (used loto, race tickets) */
	private int _type1;
	private int _type2;

	/** Item drop time for autodestroy task */
	private long _dropTime;

	/** Item drop time */
	private long _dropTimeOwner;

	public static final byte CHARGED_NONE = 0;
	public static final byte CHARGED_SOULSHOT = 1;
	public static final byte CHARGED_SPIRITSHOT = 1;
	public static final byte CHARGED_BLESSED_SPIRITSHOT = 2;

	private byte _chargedSoulshot = CHARGED_NONE;
	private byte _chargedSpiritshot = CHARGED_NONE;

	private boolean _chargedFishtshot = false;

	public static final byte UNCHANGED = 0;
	public static final byte ADDED = 1;
	public static final byte REMOVED = 3;
	public static final byte MODIFIED = 2;
	private byte _lastChange = 2; //1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	/** Element (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None) */
	private byte attackAttributeElement = L2Item.ATTRIBUTE_NONE;
	private int attackAttributeValue = 0;
    private int[] defenseAttributes = new int[]{0,0,0,0,0,0};
    private List<FuncTemplate> _enchantAttributeFuncTemplate = new ArrayList<FuncTemplate>(6);

    private byte _enchantAttributeElement = L2Item.ATTRIBUTE_NONE;
	private int _enchantAttributeValue = 0;
    
    /**
	 * Спецфлаги для конкретного инстанса
	 */
	private int _customFlags = 0;

	public static final int FLAG_NO_DROP = 1 << 0;
	public static final int FLAG_NO_TRADE = 1 << 1;
	public static final int FLAG_NO_TRANSFER = 1 << 2;
	public static final int FLAG_NO_CRYSTALLIZE = 1 << 3;
	public static final int FLAG_NO_ENCHANT = 1 << 4;
	public static final int FLAG_NO_DESTROY = 1 << 5;
	public static final int FLAG_NO_UNEQUIP = 1 << 6;
	public static final int FLAG_ALWAYS_DROP_ON_DIE = 1 << 7;
	public static final int FLAG_EQUIP_ON_PICKUP = 1 << 8;
	public static final int FLAG_NO_RIDER_PICKUP = 1 << 9;
	public static final int FLAG_PET_EQUIPPED = 1 << 10;

	private Future<?> _lazyUpdateInDb;

	/** Task of delayed update item info in database */
	private class LazyUpdateInDb implements Runnable
	{
		private final long itemStoreId;

		public LazyUpdateInDb(L2ItemInstance item)
		{
			itemStoreId = item.getStoredId();
		}

		public void run()
		{
			L2ItemInstance _item = L2ObjectsStorage.getAsItem(itemStoreId);
			if(_item == null)
				return;
			try
			{
				_item.updateInDb();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				_item.stopLazyUpdateTask(false);
			}
		}
	}

	private int _bodypart;

	/**
	 * Constructor<?> of the L2ItemInstance from the objectId and the itemId.
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		this(objectId, ItemTemplates.getInstance().getTemplate(itemId), true);
	}

	/**
	 * Constructor<?> of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 * @param objectId : int designating the ID of the object in the world
	 * @param item : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item, boolean putInStorage)
	{
		super(objectId, putInStorage && objectId > 0);
		if(item == null)
		{
			_log.warning("Not found template for item id: " + _itemId);
			throw new IllegalArgumentException();
		}

		_itemId = item.getItemId();
		_itemTemplate = item;
		_count = 1;
		_loc = ItemLocation.VOID;
		_customFlags = item.getFlags();

		_dropTime = 0;
		_dropTimeOwner = 0;
		setItemDropOwner(null, 0);

		_lifeTimeRemaining = _itemTemplate.isTemporal() ? (int) (System.currentTimeMillis() / 1000) + _itemTemplate.getDurability() * 60 : _itemTemplate.getDurability();

		_bodypart = _itemTemplate.getBodyPart();
        for(int i=0; i<6; i++)
            _enchantAttributeFuncTemplate.add( null);

	}

	public int getBodyPart()
	{
		return _bodypart;
	}

	public void setBodyPart(int bodypart)
	{
		_bodypart = bodypart;
	}

	public void setOwnerId(int ownerId)
	{
		if(getOwnerId() != ownerId)
			_storedInDb = false;
		L2Player owner = L2ObjectsStorage.getPlayer(ownerId);
		ownerStoreId = owner != null ? owner.getStoredId() : ownerId + 0L;
		startTemporalTask(owner);
	}

	/**
	 * Returns the ownerID of the item
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return L2ObjectsStorage.getStoredObjectId(ownerStoreId);
	}

	/**
	 * Sets the location of the item
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	/**
	 * Sets the location of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param loc : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, int loc_data)
	{
		if(loc == _loc && loc_data == _loc_data)
			return;

		if(getItemId() == 57 && loc != _loc)
			if(loc == ItemLocation.VOID)
				Stat.addAdena(-_count);
			else if(_loc == ItemLocation.VOID)
				Stat.addAdena(_count);

		_loc = loc;
		_loc_data = loc_data;
		_storedInDb = false;
	}

	public ItemLocation getLocation()
	{
		return _loc;
	}

	public long getLongLimitedCount()  
	{  
		return Math.min(_count, Long.MAX_VALUE);  
	}  
	
	/**
	 * Возвращает количество предметов
	 * @return long
	 */
	public long getCount()
	{
		return _count;
	}

	/**
	 * Sets the quantity of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param count : long
	 */
	public void setCount(long count)
	{
		if(getItemId() == 57 && _loc != ItemLocation.VOID)
			Stat.addAdena(count - _count);

		if(count < 0)
			count = 0;
		if(!isStackable() && count > 1)
		{
			_count = 1;
			Log.IllegalPlayerAction(getPlayer(), "tried to stack unstackable item " + getItemId(), 0);
			return;
		}
		if(_count == count)
			return;
		_count = count;
		_storedInDb = false;
	}

	/**
	 * Returns if item is equipable
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return _itemTemplate.isEquipable();
	}

	/**
	 * Returns if item is equipped
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL;
	}

	/**
	 * Returns the slot where the item is stored
	 * @return int
	 */
	public int getEquipSlot()
	{
		return _loc_data;
	}

	/**
	 * Returns the characteristics of the item
	 * @return L2Item
	 */
	public L2Item getItem()
	{
		return _itemTemplate;
	}
	
	public L2Weapon getWeapon()
	{
		return _WeaponTemplate;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}

	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	public long getDropTime()
	{
		return _dropTime;
	}

	public long getDropTimeOwner()
	{
		return _dropTimeOwner;
	}

	public void setItemDropOwner(L2Player owner, long time)
	{
		itemDropOwnerStoreId = owner != null ? owner.getStoredId() : 0;
		_dropTimeOwner = owner != null ? System.currentTimeMillis() + time : 0;
	}

	public L2Player getItemDropOwner()
	{
		return L2ObjectsStorage.getAsPlayer(itemDropOwnerStoreId);
	}

	public boolean isWear()
	{
		return _wear;
	}

	public void setWear(boolean newwear)
	{
		_wear = newwear;
	}

	/**
	 * Returns the type of item
	 * @return Enum
	 */
	public Enum getItemType()
	{
		return _itemTemplate.getItemType();
	}

	/**
	 * Returns the ID of the item
	 * @return int
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * Returns the reference price of the item
	 * @return int
	 */
	public int getReferencePrice()
	{
		return _itemTemplate.getReferencePrice();
	}

	/**
	 * Returns the price of the item for selling
	 * @return int
	 */
	public long getPriceToSell()
	{
		return _price_sell;
	}

	/**
	 * Sets the price of the item for selling
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 * @param price : int designating the price
	 */
	public void setPriceToSell(long price)
	{
		_price_sell = price;
	}

	public void setCountToSell(long count)
	{
		_count_sell = count;
	}

	public long getCountToSell() //TODO: long
	{
		return _count_sell;
	}

	/**
	 * Returns the last change of the item
	 * @return int
	 */
	public int getLastChange()
	{
		return _lastChange;
	}

	/**
	 * Sets the last change of the item
	 * @param lastChange : int
	 */
	public void setLastChange(byte lastChange)
	{
		_lastChange = lastChange;
	}

	/**
	 * Returns if item is stackable
	 * @return boolean
	 */
	public boolean isStackable()
	{
		return _itemTemplate.isStackable();
	}

	@Override
	public void onAction(L2Player player, boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;

		if(player.isCursedWeaponEquipped() && CursedWeaponsManager.getInstance().isCursed(_itemId))
			return;

		int _castleId = MercTicketManager.getInstance().getTicketCastleId(_itemId);
		if(_castleId > 0)
		{
			L2Clan clan = player.getClan();
			// mercenary tickets can only be picked up by the castle owner.
			if(clan != null && clan.getHasCastle() == _castleId && (player.getClanPrivileges() & L2Clan.CP_CS_MERCENARIES) == L2Clan.CP_CS_MERCENARIES || player.isGM())
				if(player.isInParty())
					player.sendMessage(new CustomMessage("l2rt.gameserver.model.items.L2ItemInstance.NoMercInParty", player));
				else
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this);
			else
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_THE_AUTHORITY_TO_CANCEL_MERCENARY_POSITIONING);

			player.setTarget(this);
			player.sendActionFailed();
		}
		else
			player.getAI().setIntention(CtrlIntention.AI_INTENTION_PICK_UP, this, null);
	}

	/**
	 * Returns the level of enchantment of the item
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	/**
	 * Sets the level of enchantment of the item
	 * @param enchantLevel level of enchant
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if(_enchantLevel == enchantLevel)
			return;
		_enchantLevel = enchantLevel;
		_storedInDb = false;
	}

	/**
	 * Returns false cause item can't be attacked
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	public boolean isAugmented()
	{
		return _augmentation == null ? false : true;
	}

	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}

	public int getAugmentationId()
	{
		return _augmentation == null ? 0 : _augmentation.getAugmentationId();
	}

	public boolean setAugmentation(L2Augmentation augmentation)
	{
		if(_augmentation != null)
			return false;
		_augmentation = augmentation;
		updateItemAttributes();
		setCustomFlags(_customFlags & ~FLAG_PET_EQUIPPED, true);
		return true;
	}

	public synchronized void removeAugmentation()
	{
		_augmentation = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(hasAttribute())
				statement = con.prepareStatement("UPDATE item_attributes SET augAttributes = -1, augSkillId = -1, augSkillLevel = -1 WHERE itemId = ? LIMIT 1");
			else
				statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ? LIMIT 1");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.info("Could not remove augmentation for item: " + getObjectId() + " from DB:");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

    public boolean hasAttribute() {
        for (int i = 0; i < defenseAttributes.length; i++)
            if (defenseAttributes[i] > 0)
                return true;
        return attackAttributeElement != L2Item.ATTRIBUTE_NONE;
    }

    /**
	 * Удаляет и аугментации, и элементальные аттрибуты.
	 */
	public synchronized void removeAttributes()
	{
		_augmentation = null;

        for(FuncTemplate func : _enchantAttributeFuncTemplate) {
            if(func != null) {
                detachFunction(func);
                func._value = 0; // На всякий случай
                }
        }

		_enchantAttributeFuncTemplate = new ArrayList<FuncTemplate>(6);
        for(int i=0; i <6; i++)
            _enchantAttributeFuncTemplate.add(null);
		attackAttributeElement = L2Item.ATTRIBUTE_NONE;
		attackAttributeValue = 0;
        defenseAttributes = new int[]{0,0,0,0,0,0};

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId = ?");
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.info("Could not remove attributes for item: " + getObjectId() + " from DB:");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public synchronized void restoreAttributes()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT augAttributes,augSkillId,augSkillLevel,elemType,elemValue,elem0,elem1,elem2,elem3,elem4,elem5 FROM item_attributes WHERE itemId=? LIMIT 1");
			statement.setInt(1, getObjectId());
			ResultSet rs = statement.executeQuery();
			rs = statement.executeQuery();
			if(rs.next())
			{
				int aug_attributes = rs.getInt(1);
				int aug_skillId = rs.getInt(2);
				int aug_skillLevel = rs.getInt(3);
				byte elem_type = rs.getByte(4);
				int elem_value = rs.getInt(5);
                int[] deffAttr = new int[]{0,0,0,0,0,0};
                for(int i=0; i<6; i++)
                    deffAttr[i] = rs.getInt(6+i);
                setAttributeElement(elem_type, elem_value, deffAttr, false);
				if(aug_attributes != -1 && aug_skillId != -1 && aug_skillLevel != -1)
				{
					_augmentation = new L2Augmentation(aug_attributes, aug_skillId, aug_skillLevel);
					setCustomFlags(_customFlags & ~FLAG_PET_EQUIPPED, false);
				}
			}
		}
		catch(Exception e)
		{
			_log.info("Could not restore augmentation and elemental data for item " + getObjectId() + " from DB: " + e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		// Очищаем некорректные аугментации из базы
		if(_augmentation != null && !_augmentation.isLoaded())
		{
			System.out.println("Remove incorrect augmentation from item objId: " + getObjectId() + ", id: " + getItemId());
			removeAugmentation();
		}
	}

	public synchronized void updateItemAttributes()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO item_attributes VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getObjectId());
			if(_augmentation == null)
			{
				statement.setInt(2, -1);
				statement.setInt(3, -1);
				statement.setInt(4, -1);
			}
			else
			{
				statement.setInt(2, _augmentation.getAugmentationId());
				if(_augmentation.getSkill() == null)
				{
					statement.setInt(3, 0);
					statement.setInt(4, 0);
				}
				else
				{
					statement.setInt(3, _augmentation.getSkill().getId());
					statement.setInt(4, _augmentation.getSkill().getLevel());
				}
			}
			statement.setByte(5, attackAttributeElement);
			statement.setInt(6, attackAttributeValue);
            for(int i=0; i < defenseAttributes.length; i++)
                statement.setInt(7+i, defenseAttributes[i]);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.info("Could not remove elemental enchant for item: " + getObjectId() + " from DB:");
			e.printStackTrace();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Returns the type of charge with SoulShot of the item.
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public byte getChargedSoulshot()
	{
		return _chargedSoulshot;
	}

	/**
	 * Returns the type of charge with SpiritShot of the item
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public byte getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	/**
	 * Sets the type of charge with SoulShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(byte type)
	{
		_chargedSoulshot = type;
	}

	/**
	 * Sets the type of charge with SpiritShot of the item
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(byte type)
	{
		_chargedSpiritshot = type;
	}

	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}

	protected FuncTemplate[] _funcTemplates;

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

	public void detachFunction(FuncTemplate f)
	{
		if(_funcTemplates == null || f == null)
			return;
		for(int i = 0; i < _funcTemplates.length; i++)
			if(f.equals(_funcTemplates[i]))
			{
				int len = _funcTemplates.length - 1;
				_funcTemplates[i] = _funcTemplates[len];
				FuncTemplate[] tmp = new FuncTemplate[len];
				System.arraycopy(_funcTemplates, 0, tmp, 0, len);
				_funcTemplates = tmp;
				break;
			}
	}

	/**
	 * This function basically returns a set of functions from
	 * L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched
	 * for a particular player.
	 * @return Func[]
	 */
	public Func[] getStatFuncs()
	{
		GArray<Func> funcs = new GArray<Func>();
		if(_itemTemplate.getAttachedFuncs() != null)
			for(FuncTemplate t : _itemTemplate.getAttachedFuncs())
			{
				Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}
		if(_funcTemplates != null)
			for(FuncTemplate t : _funcTemplates)
			{
				Func f = t.getFunc(this);
				if(f != null)
					funcs.add(f);
			}
		if(funcs.size() == 0)
			return new Func[0];
		return funcs.toArray(new Func[funcs.size()]);
	}

	/**
	 * Updates database.<BR><BR>
	 * <U><I>Concept : </I></U><BR>
	 *
	 * <B>IF</B> the item exists in database :
	 * <UL>
	 *		<LI><B>IF</B> the item has no owner, or has no location, or has a null quantity : remove item from database</LI>
	 *		<LI><B>ELSE</B> : update item in database</LI>
	 * </UL>
	 *
	 * <B> Otherwise</B> :
	 * <UL>
	 *		<LI><B>IF</B> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item in database</LI>
	 * </UL>
	 */
	public void updateDatabase()
	{
		updateDatabase(false, true);
	}

	public synchronized void updateDatabase(boolean commit, boolean AllowRemoveAttributes)
	{
		if(_existsInDb)
		{
			if(_loc == ItemLocation.VOID || _count == 0 || getOwnerId() == 0)
				removeFromDb(AllowRemoveAttributes);
			else if(Config.LAZY_ITEM_UPDATE && (isStackable() || Config.LAZY_ITEM_UPDATE_ALL))
			{
				if(commit)
				{
					// cancel lazy update task if need
					if(stopLazyUpdateTask(true))
					{
						insertIntoDb(); // на всякий случай...
						return;
					}
					updateInDb();
					Stat.increaseUpdateItemCount();
					return;
				}
				Future<?> lazyUpdateInDb = _lazyUpdateInDb;
				if(lazyUpdateInDb == null || lazyUpdateInDb.isDone())
				{
					_lazyUpdateInDb = ThreadPoolManager.getInstance().scheduleGeneral(new LazyUpdateInDb(this), isStackable() ? Config.LAZY_ITEM_UPDATE_TIME : Config.LAZY_ITEM_UPDATE_ALL_TIME);
					Stat.increaseLazyUpdateItem();
				}
			}
			else
			{
				updateInDb();
				Stat.increaseUpdateItemCount();
			}
		}
		else
		{
			if(_count == 0 || _loc == ItemLocation.VOID || getOwnerId() == 0)
				return;
			insertIntoDb();
		}
	}

	public boolean stopLazyUpdateTask(boolean interrupt)
	{
		boolean ret = false;
		if(_lazyUpdateInDb != null)
		{
			ret = _lazyUpdateInDb.cancel(interrupt);
			_lazyUpdateInDb = null;
		}
		return ret;
	}

	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 * @param objectId : int designating the objectID of the item
	 * @return L2ItemInstance
	 */
	public synchronized static L2ItemInstance restoreFromDb(long objectId, boolean putInStorage)
	{
		L2ItemInstance inst = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet item_rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM items WHERE object_id=? LIMIT 1");
			statement.setLong(1, objectId);
			item_rset = statement.executeQuery();
			if(item_rset.next())
				inst = restoreFromDb(item_rset, con, putInStorage);
			else
				_log.severe("Item object_id=" + objectId + " not found");
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore item " + objectId + " from DB: " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, item_rset);
		}
		return inst;
	}

	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 * @param item_rset : ResultSet
	 * @return L2ItemInstance
	 */
	public synchronized static L2ItemInstance restoreFromDb(ResultSet item_rset, ThreadConnection con, boolean putInStorage)
	{
		if(item_rset == null)
			return null;
		int objectId = 0;
		try
		{
			objectId = item_rset.getInt("object_id");

			L2Item item = ItemTemplates.getInstance().getTemplate(item_rset.getInt("item_id"));
			if(item == null)
			{
				_log.severe("Item item_id=" + item_rset.getInt("item_id") + " not known, object_id=" + objectId);
				return null;
			}

			if(item.isTemporal() && item_rset.getInt("shadow_life_time") <= System.currentTimeMillis()/1000 && item_rset.getInt("shadow_life_time") > 1262304000)
			{
				removeFromDb(objectId);
				return null;
			}
			L2ItemInstance inst = new L2ItemInstance(objectId, item, putInStorage);
			inst._existsInDb = true;
			inst._storedInDb = true;
			inst._lifeTimeRemaining = item_rset.getInt("shadow_life_time");
			inst.setOwnerId(item_rset.getInt("owner_id"));
			inst._count = item_rset.getLong("count");
			inst._enchantLevel = item_rset.getInt("enchant_level");
			inst._type1 = item_rset.getInt("custom_type1");
			inst._type2 = item_rset.getInt("custom_type2");
			inst._loc = ItemLocation.valueOf(item_rset.getString("loc"));
			inst._loc_data = item_rset.getInt("loc_data");
			inst._customFlags = item_rset.getInt("flags");

			// load augmentation and elemental enchant
			if(inst.isEquipable())
				inst.restoreAttributes();

			return inst;
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore(2) item " + objectId + " from DB: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Update the database with values of the item
	 * Не вызывать нестандартными способами
	 */
	public synchronized void updateInDb()
	{
		if(isWear())
			return;

		if(_storedInDb)
			return;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,shadow_life_time=?,item_id=?,flags=? WHERE object_id = ? LIMIT 1");
			statement.setInt(1, getOwnerId());
			statement.setLong(2, _count);
			statement.setString(3, _loc.name());
			statement.setInt(4, _loc_data);
			statement.setInt(5, getEnchantLevel());
			statement.setInt(6, _lifeTimeRemaining);
			statement.setInt(7, getItemId());
			statement.setInt(8, _customFlags);
			statement.setInt(9, getObjectId());
			statement.executeUpdate();

			_existsInDb = true;
			_storedInDb = true;
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not update item " + getObjectId() + " itemID " + _itemId + " count " + getCount() + " owner " + getOwnerId() + " in DB:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Insert the item in database
	 */
	private synchronized void insertIntoDb()
	{
		if(isWear())
			return;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,shadow_life_time,class,flags) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, getOwnerId());
			statement.setInt(2, _itemId);
			statement.setLong(3, _count);
			statement.setString(4, _loc.name());
			statement.setInt(5, _loc_data);
			statement.setInt(6, getEnchantLevel());
			statement.setInt(7, getObjectId());
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, _lifeTimeRemaining);
			statement.setString(11, getItemClass().name());
			statement.setInt(12, _customFlags);
			statement.executeUpdate();

			_existsInDb = true;
			_storedInDb = true;

			Stat.increaseInsertItemCount();
		}
		catch(Exception e)
		{
			_log.warning("Could not insert item " + getObjectId() + "; itemID=" + _itemId + "; count=" + getCount() + "; owner=" + getOwnerId() + "; exception: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Delete item from database
	 */
	public synchronized void removeFromDb(boolean AllowRemoveAttributes)
	{
		if(isWear() || !_existsInDb)
			return;

		// cancel lazy update task if need
		stopLazyUpdateTask(true);

		if(AllowRemoveAttributes)
			removeAttributes();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM items WHERE object_id = ? LIMIT 1");
			statement.setInt(1, _objectId);
			statement.executeUpdate();

			_existsInDb = false;
			_storedInDb = false;

			Stat.increaseDeleteItemCount();
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete item " + _objectId + " in DB:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
	public static synchronized void removeFromDb(int _objectIds)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM items WHERE object_id = ? LIMIT 1");
			statement.setInt(1, _objectIds);
			statement.executeUpdate();
			Stat.increaseDeleteItemCount();
		}
		catch(Exception e)
		{
			_log.log(Level.SEVERE, "Could not delete item " + _objectIds + " in DB:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * При фейле эти свитки не ломают вещь, но сбрасывают заточку
	 */
	public boolean isBlessedEnchantScroll()
	{
		switch(_itemId)
		{
			case 6569: // Wpn A
			case 6570: // Arm A
			case 6571: // Wpn B
			case 6572: // Arm B
			case 6573: // Wpn C
			case 6574: // Arm C
			case 6575: // Wpn D
			case 6576: // Arm D
			case 6577: // Wpn S
			case 6578: // Arm S
			case 19447: // Wpn R
			case 19448: // Arm R
				return true;
		}
		return false;
	}

	/**
	 * При фейле эти свитки не имеют побочных эффектов
	 */
	public boolean isAncientEnchantScroll()
	{
		switch(_itemId)
		{
			case 22014: // item Mall Wpn B
			case 22015: // item Mall Wpn A
			case 20519: // item Mall Wpn S
			case 22016: // item Mall Arm B
			case 22017: // item Mall Arm A
			case 20520: // item Mall Arm S
				return true;
		}
		return false;
	}

	/**
	 * Эти свитки имеют 10% бонус шанса заточки
	 */
	public boolean isItemMallEnchantScroll()
	{
		switch(_itemId)
		{
			case 22006: // item Mall Wpn D
			case 22007: // item Mall Wpn C
			case 22008: // item Mall Wpn B
			case 22009: // item Mall Wpn A
			case 20517: // item Mall Wpn S
			case 22010: // item Mall Arm D
			case 22011: // item Mall Arm C
			case 22012: // item Mall Arm B
			case 22013: // item Mall Arm A
			case 20518: // item Mall Arm S
			case 33478: // item Mall Wpn R
			case 33479: // item Mall Arm R
				return true;
			default:
				return isAncientEnchantScroll();
		}
	}

	/**
	 * Эти свитки имеют 100% шанс
	 */
	public boolean isDivineEnchantScroll()
	{
		switch(_itemId)
		{
			case 22018: // item Mall Wpn B
			case 22019: // item Mall Wpn A
			case 20521: // item Mall Wpn S
			case 22020: // item Mall Arm B
			case 22021: // item Mall Arm A
			case 20522: // item Mall Arm S
				return true;
		}
		return false;
	}

	/**
	 * Они не используются официальным серером, но могут быть задействованы альтами
	 */
	public boolean isCrystallEnchantScroll()
	{
		switch(_itemId)
		{
			case 731:
			case 732:
			case 949:
			case 950:
			case 953:
			case 954:
			case 957:
			case 958:
			case 961:
			case 962:
			case 33519:
			case 33520:
				return true;
		}
		return false;
	}

	/**
	 * Проверка соответствия свитка и катализатора грейду вещи.
	 * @return id кристалла для соответствующих и 0 для несоответствующих.
	 */
	public int getEnchantCrystalId(L2ItemInstance scroll, L2ItemInstance catalyst)
	{
		boolean scrollValid = false, catalystValid = false;

		for(int scrollId : getEnchantScrollId())
			if(scroll.getItemId() == scrollId)
			{
				scrollValid = true;
				break;
			}

		if(catalyst == null)
			catalystValid = true;
		else
			for(int catalystId : getEnchantCatalystId())
				if(catalystId == catalyst.getItemId())
				{
					catalystValid = true;
					break;
				}

		if(scrollValid && catalystValid)
			switch(_itemTemplate.getCrystalType().cry)
			{
				case L2Item.CRYSTAL_A:
					return 1461;
				case L2Item.CRYSTAL_B:
					return 1460;
				case L2Item.CRYSTAL_C:
					return 1459;
				case L2Item.CRYSTAL_D:
					return 1458;
				case L2Item.CRYSTAL_S:
					return 1462;
				case L2Item.CRYSTAL_R:
					return 17371;
			}

		return 0;
	}

	/**
	 * Возвращает список свитков, которые подходят для вещи.
	 */
	public int[] getEnchantScrollId()
	{
		if(_itemTemplate.getType2() == L2Item.TYPE2_WEAPON)
			switch(_itemTemplate.getCrystalType().cry)
			{
				case L2Item.CRYSTAL_A:
					return new int[] { 729, 6569, 731, 22009, 22015, 22019 };
				case L2Item.CRYSTAL_B:
					return new int[] { 947, 6571, 949, 22008, 22014, 22018 };
				case L2Item.CRYSTAL_C:
					return new int[] { 951, 6573, 953, 22007 };
				case L2Item.CRYSTAL_D:
					return new int[] { 955, 6575, 957, 22006 };
				case L2Item.CRYSTAL_S:
					return new int[] { 959, 6577, 961, 20519 };
                case L2Item.CRYSTAL_R:
					return new int[] { 19447, 17526, 33478, 33519};
			}
		else if(_itemTemplate.getType2() == L2Item.TYPE2_SHIELD_ARMOR || _itemTemplate.getType2() == L2Item.TYPE2_ACCESSORY)
			switch(_itemTemplate.getCrystalType().cry)
			{
				case L2Item.CRYSTAL_A:
					return new int[] { 730, 6570, 732, 22013, 22017, 22021 };
				case L2Item.CRYSTAL_B:
					return new int[] { 948, 6572, 950, 22012, 22016, 22020 };
				case L2Item.CRYSTAL_C:
					return new int[] { 952, 6574, 954, 22011 };
				case L2Item.CRYSTAL_D:
					return new int[] { 956, 6576, 958, 22010 };
				case L2Item.CRYSTAL_S:
					return new int[] { 960, 6578, 962, 20520 };
                case L2Item.CRYSTAL_R:
					return new int[] { 19448, 17527, 33479 };
			}
		return new int[0];
	}

	private static final int[][] catalyst = {
			// enchant catalyst list
			{ 12362, 14078, 14702 }, // 0 - W D
			{ 12363, 14079, 14703 }, // 1 - W C
			{ 12364, 14080, 14704 }, // 2 - W B
			{ 12365, 14081, 14705 }, // 3 - W A
			{ 12366, 14082, 14706 }, // 4 - W S
			{ 12367, 14083, 14707 }, // 5 - A D
			{ 12368, 14084, 14708 }, // 6 - A C
			{ 12369, 14085, 14709 }, // 7 - A B
			{ 12370, 14086, 14710 }, // 8 - A A
			{ 12371, 14087, 14711 }, // 9 - A S
			{ 30381}, //10 - W R
			{ 30382}, //11 - A R
	};

	public int[] getEnchantCatalystId()
	{
		if(_itemTemplate.getType2() == L2Item.TYPE2_WEAPON)
			switch(_itemTemplate.getCrystalType().cry)
			{
				case L2Item.CRYSTAL_A:
					return catalyst[3];
				case L2Item.CRYSTAL_B:
					return catalyst[2];
				case L2Item.CRYSTAL_C:
					return catalyst[1];
				case L2Item.CRYSTAL_D:
					return catalyst[0];
				case L2Item.CRYSTAL_S:
					return catalyst[4];	
				case L2Item.CRYSTAL_R:
					return catalyst[10];
			}
		else if(_itemTemplate.getType2() == L2Item.TYPE2_SHIELD_ARMOR || _itemTemplate.getType2() == L2Item.TYPE2_ACCESSORY)
			switch(_itemTemplate.getCrystalType().cry)
			{
				case L2Item.CRYSTAL_A:
					return catalyst[8];
				case L2Item.CRYSTAL_B:
					return catalyst[7];
				case L2Item.CRYSTAL_C:
					return catalyst[6];
				case L2Item.CRYSTAL_D:
					return catalyst[5];
				case L2Item.CRYSTAL_S:
					return catalyst[9];
				case L2Item.CRYSTAL_R:
					return catalyst[11];
			}
		return new int[] { 0, 0, 0 };
	}

	public int getCatalystPower()
	{
		/*
		14702	Agathion Auxiliary Stone: Enchant Weapon (D-Grade)	The Agathion Auxilary Stone raises the ability to enchant a D-Grade weapon by 20%
		14703	Agathion Auxiliary Stone: Enchant Weapon (C-Grade)	The Agathion Auxilary Stone raises the ability to enchant a C-Grade weapon by 18%
		14704	Agathion Auxiliary Stone: Enchant Weapon (B-Grade)	The Agathion Auxilary Stone raises the ability to enchant a B-Grade weapon by 15%
		14705	Agathion Auxiliary Stone: Enchant Weapon (A-Grade)	The Agathion Auxilary Stone raises the ability to enchant a A-Grade weapon by 12%
		14706	Agathion Auxiliary Stone: Enchant Weapon (S-Grade)	The Agathion Auxilary Stone raises the ability to enchant a S-Grade weapon by 10%
		14707	Agathion Auxiliary Stone: Enchant Armor (D-Grade)		The Agathion Auxilary Stone raises the ability to enchant a D-Grade armor by 35%
		14708	Agathion Auxiliary Stone: Enchant Armor (C-Grade)		The Agathion Auxilary Stone raises the ability to enchant a C-Grade armor by 27%
		14709	Agathion Auxiliary Stone: Enchant Armor (B-Grade)		The Agathion Auxilary Stone raises the ability to enchant a B-Grade armor by 23%
		14710	Agathion Auxiliary Stone: Enchant Armor (A-Grade)		The Agathion Auxilary Stone raises the ability to enchant a A-Grade armor by 18%
		14711	Agathion Auxiliary Stone: Enchant Armor (S-Grade)		The Agathion Auxilary Stone raises the ability to enchant a S-Grade armor by 15%
		 */
		for(int i = 0; i < catalyst.length; i++)
			for(int id : catalyst[i])
				if(id == _itemId)
					switch(i)
					{
						case 0:
							return 20;
						case 1:
							return 18;
						case 2:
							return 15;
						case 3:
							return 12;
						case 4:
							return 10;
						case 5:
							return 35;
						case 6:
							return 27;
						case 7:
							return 23;
						case 8:
							return 18;
						case 9:
							return 15;
					}

		return 0;
		/*
		switch(_itemId)
		{
			case 14702:
			case 14078:
			case 12362:
				return 20;
			case 14703:
			case 14079:
			case 12363:
				return 18;
			case 14704:
			case 14080:
			case 12364:
				return 15;
			case 14705:
			case 14081:
			case 12365:
				return 12;
			case 14706:
			case 14082:
			case 12366:
				return 10;
			case 14707:
			case 14083:
			case 12367:
				return 35;
			case 14708:
			case 14084:
			case 12368:
				return 27;
			case 14709:
			case 14085:
			case 12369:
				return 23;
			case 14710:
			case 14086:
			case 12370:
				return 18;
			case 14711:
			case 14087:
			case 12371:
				return 15;
			default:
				return 0;
		}
		*/
	}

	/**
	 * Return true if item is hero-item
	 * @return boolean
	 */
	public boolean isHeroWeapon()
	{
		return _itemTemplate.isHeroWeapon();
	}

	/**
	 * Return true if item is ClanApella-item
	 * @return boolean
	 */
	public boolean isClanApellaItem()
	{
		int myid = _itemId;
		return myid >= 7860 && myid <= 7879 || myid >= 9830 && myid <= 9839;
	}

	public boolean isLifeStone()
	{
		int myid = _itemId;
		return myid >= 8723 && myid <= 8762 || myid >= 9573 && myid <= 9576 || myid >= 10483 && myid <= 10486 || myid >= 14166 && myid <= 14169;
	}

	public boolean isAccessoryLifeStone()
	{
		int myid = _itemId;
		return myid >= 12754 && myid <= 12763 || myid >= 12840 && myid <= 12851 || myid == 12821 || myid == 12822 || myid == 14008;
	}

	/**
	 * Return true if item can be destroyed
	 */
	public boolean canBeDestroyed(L2Player player)
	{
		if((_customFlags & FLAG_NO_DESTROY) == FLAG_NO_DESTROY)
			return false;

		if(isHeroWeapon())
			return false;

		if(PetDataTable.isPetControlItem(this) && player.isMounted())
			return false;

		if(player.getPet() != null && getObjectId() == player.getPet().getControlItemObjId())
			return false;

		if(isCursed())
			return false;

		if(isEquipped())
			return false;

		if(isWear())
			return false;

		return isDestroyable();
	}

	/**
	 * Return true if item can be dropped
	 */
	public boolean canBeDropped(L2Player player, boolean pk)
	{
		if((_customFlags & FLAG_NO_DROP) == FLAG_NO_DROP)
			return false;

		if(isHeroWeapon())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(isAugmented() && (!pk || !Config.DROP_ITEMS_AUGMENTED) && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(_itemTemplate.getType2() == L2Item.TYPE2_QUEST)
			return false;

		if(PetDataTable.isPetControlItem(this) && player.isMounted())
			return false;

		if(player.getPet() != null && getObjectId() == player.getPet().getControlItemObjId())
			return false;

		if(isCursed() || getItem().isCombatFlag() || getItem().isTerritoryFlag())
			return false;

		if(!pk && isEquipped())
			return false;

		if(isWear())
			return false;

		if(getItem().getType2() == L2Item.TYPE2_QUEST)
			return false;

		if(player.getEnchantScroll() == this)
			return false;

		return _itemTemplate.isDropable();
	}

	public boolean canBeTraded(L2Player owner)
	{
		if((_customFlags & FLAG_NO_TRADE) == FLAG_NO_TRADE)
			return false;

		if(isHeroWeapon())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		if(PetDataTable.isPetControlItem(this) && owner.isMounted())
			return false;

		if(owner.getPet() != null && getObjectId() == owner.getPet().getControlItemObjId())
			return false;

		if(isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(isCursed())
			return false;

		if(isEquipped())
			return false;

		if(isWear())
			return false;

		if(getItem().getType2() == L2Item.TYPE2_QUEST)
			return false;

		if(owner.getEnchantScroll() == this)
			return false;

		return _itemTemplate.isTradeable();
	}

	/**
	 * Можно ли положить на клановый склад или передать фрейтом
	 */
	public boolean canBeStored(L2Player player, boolean privatewh)
	{
		if((_customFlags & FLAG_NO_TRANSFER) == FLAG_NO_TRANSFER)
			return false;

		if(isHeroWeapon())
			return false;

		if(!privatewh && (isShadowItem() || isTemporalItem()))
			return false;

		if(PetDataTable.isPetControlItem(this) && player.isMounted())
			return false;

		if(player.getPet() != null && getObjectId() == player.getPet().getControlItemObjId())
			return false;

		if(!privatewh && isAugmented() && !Config.ALT_ALLOW_DROP_AUGMENTED)
			return false;

		if(isCursed())
			return false;

		if(isEquipped())
			return false;

		if(isWear())
			return false;

		if(getItem().getType2() == L2Item.TYPE2_QUEST)
			return false;

		if(player.getEnchantScroll() == this)
			return false;

		return privatewh || _itemTemplate.isTradeable();
	}

	public boolean canBeCrystallized(L2Player player, boolean msg)
	{
		if((_customFlags & FLAG_NO_CRYSTALLIZE) == FLAG_NO_CRYSTALLIZE)
			return false;

		if(isHeroWeapon())
			return false;

		if(isShadowItem())
			return false;

		if(isTemporalItem())
			return false;

		//can player crystallize?
		int level = player.getSkillLevel(L2Skill.SKILL_CRYSTALLIZE);
		if(level < 1 || _itemTemplate.getCrystalType().cry - L2Item.CRYSTAL_D + 1 > level)
		{
			if(msg)
			{
				player.sendPacket(Msg.CANNOT_CRYSTALLIZE_CRYSTALLIZATION_SKILL_LEVEL_TOO_LOW);
				player.sendActionFailed();
			}
			return false;
		}

		if(PetDataTable.isPetControlItem(this) && player.isMounted())
			return false;

		if(player.getPet() != null && getObjectId() == player.getPet().getControlItemObjId())
			return false;

		if(isCursed())
			return false;

		if(isEquipped())
			return false;

		if(isWear())
			return false;

		if(getItem().getType2() == L2Item.TYPE2_QUEST)
			return false;

		return _itemTemplate.isCrystallizable();
	}

	public boolean canBeEnchanted()
	{
		if((_customFlags & FLAG_NO_ENCHANT) == FLAG_NO_ENCHANT)
			return false;

		if(isWear())
			return false;

		return _itemTemplate.canBeEnchanted();
	}

	public boolean canBeAugmented(L2Player player, boolean isAccessoryLifeStone)
	{
		if(!canBeEnchanted())
			return false;

		if(isAugmented())
			return false;

		if(isRaidAccessory())
			return false;

		if(getItem().getItemGrade().ordinal() < Grade.C.ordinal())
			return false;

		if(getItemId() >= 14801 && getItemId() <= 14809 || getItemId() >= 15282 && getItemId() <= 15299)
			return false; // бижутерия с ТВ

		int itemType = getItem().getType2();

		if((isAccessoryLifeStone ? itemType != L2Item.TYPE2_ACCESSORY : itemType != L2Item.TYPE2_WEAPON) && !Config.ALT_ALLOW_AUGMENT_ALL)
			return false;

		if(player.getPrivateStoreType() != L2Player.STORE_PRIVATE_NONE || player.isDead() || player.isParalyzed() || player.isFishing() || player.isSitting())
			return false;

		return true;
	}

	public boolean isRaidAccessory()
	{
		return _itemTemplate.isRaidAccessory();
	}

	/**
	 * Returns the item in String format
	 */
	@Override
	public String toString()
	{
		return _itemTemplate.toString();
	}

	public boolean isNightLure()
	{
		return _itemId >= 8505 && _itemId <= 8513 || _itemId == 8485;
	}

	/**
	 * Используется только для Shadow вещей
	 */
	public void shadowNotify(boolean equipped)
	{
		if(!isShadowItem()) // Вещь не теневая? До свидания.
			return;

		if(!equipped) // При снятии прерывать таск
		{
			if(_itemLifeTimeTask != null)
				_itemLifeTimeTask.cancel(false);
			_itemLifeTimeTask = null;
			return;
		}

		if(_itemLifeTimeTask != null && !_itemLifeTimeTask.isDone()) // Если таск уже тикает, то повторно дергать не надо
			return;

		L2Player owner = getOwner();
		if(owner == null)
			return;

		setLifeTimeRemaining(owner, getLifeTimeRemaining() - 1);

		if(!checkDestruction(owner)) // Если у вещи ещё есть мана - запустить таск уменьшения
			_itemLifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new LifeTimeTask(), 60000);
	}

	public void startTemporalTask(L2Player owner)
	{
		if(!isTemporalItem() || owner == null) // Вещь не временная? До свидания.
			return;

		if(_itemLifeTimeTask != null && !_itemLifeTimeTask.isDone()) // Если таск уже тикает, то повторно дергать не надо
			return;

		if(!checkDestruction(owner)) // Если у вещи ещё есть мана - запустить таск уменьшения
			_itemLifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new LifeTimeTask(), 60000);
	}

	public boolean isShadowItem()
	{
		return _itemTemplate.isShadowItem();
	}

	public boolean isTemporalItem()
	{
		return _itemTemplate.isTemporal();
	}

	public boolean isCommonItem()
	{
		return _itemTemplate.isCommonItem();
	}

	public boolean isAltSeed()
	{
		return _itemTemplate.isAltSeed();
	}

	public boolean isCursed()
	{
		return _itemTemplate.isCursed();
	}

	private L2Player getOwner()
	{
		return L2ObjectsStorage.getAsPlayer(ownerStoreId);
	}

	/**
	 * true означает завершить таск, false продолжить
	 */
	private boolean checkDestruction(L2Player owner)
	{
		if(!isShadowItem() && !isTemporalItem())
			return true;

		int left = getLifeTimeRemaining();
		if(isTemporalItem())
			left /= 60;
		if(left == 10 || left == 5 || left == 1 || left <= 0)
		{
			if(isShadowItem())
			{
				SystemMessage sm;
				if(left == 10)
					sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_10);
				else if(left == 5)
					sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_5);
				else if(left == 1)
					sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_1_IT_WILL_DISAPPEAR_SOON);
				else
					sm = new SystemMessage(SystemMessage.S1S_REMAINING_MANA_IS_NOW_0_AND_THE_ITEM_HAS_DISAPPEARED);
				sm.addItemName(getItemId());
				owner.sendPacket(sm);
			}

			if(left <= 0)
			{
				owner.getInventory().unEquipItem(this);
				owner.getInventory().destroyItem(this, getCount(), true);
				if(isTemporalItem())
					owner.sendPacket(new SystemMessage(SystemMessage.THE_LIMITED_TIME_ITEM_HAS_BEEN_DELETED).addItemName(_itemTemplate.getItemId()));
				owner.sendPacket(new ItemList(owner, false)); // перестраховка
				owner.broadcastUserInfo(true);
				return true;
			}
		}

		return false;
	}

	public int getLifeTimeRemaining()
	{
		if(isTemporalItem())
			return _lifeTimeRemaining - (int) (System.currentTimeMillis() / 1000);
		return _lifeTimeRemaining;
	}

	private void setLifeTimeRemaining(L2Player owner, int lt)
	{
		assert !isTemporalItem();

		_lifeTimeRemaining = lt;
		_storedInDb = false;

		owner.sendPacket(new InventoryUpdate().addModifiedItem(this));
	}

	public class LifeTimeTask implements Runnable
	{
		public void run()
		{
			L2Player owner = getOwner();
			if(owner == null || !owner.isOnline())
				return;

			if(isShadowItem())
			{
				if(!isEquipped())
					return;

				setLifeTimeRemaining(owner, getLifeTimeRemaining() - 1);
			}

			if(checkDestruction(owner))
				return;

			_itemLifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(this, 60000); // У шэдовов 1 цикл = 60 сек.
		}
	}

	public void dropToTheGround(L2Player lastAttacker, L2NpcInstance dropper)
	{
		if(dropper == null)
		{
			Location dropPos = Rnd.coordsRandomize(lastAttacker, 70);
			for(int i = 0; i < 20 && !GeoEngine.canMoveWithCollision(lastAttacker.getX(), lastAttacker.getY(), lastAttacker.getZ(), dropPos.x, dropPos.y, dropPos.z, getReflection().getGeoIndex()); i++)
				dropPos = Rnd.coordsRandomize(lastAttacker, 70);
			dropMe(lastAttacker, dropPos);
			if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
				ItemsAutoDestroy.getInstance().addItem(this);
			return;
		}

		// 20 попыток уронить дроп в точке смерти моба
		Location dropPos = Rnd.coordsRandomize(dropper, 70);
		if(lastAttacker != null)
		{
			for(int i = 0; i < 20 && !GeoEngine.canMoveWithCollision(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, getReflection().getGeoIndex()); i++)
				dropPos = Rnd.coordsRandomize(dropper, 70);

			// Если в точке смерти моба дропу негде упасть, то падает под ноги чару
			if(!GeoEngine.canMoveWithCollision(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, getReflection().getGeoIndex()))
				dropPos = lastAttacker.getLoc();
		}

		// Init the dropped L2ItemInstance and add it in the world as a visible object at the position where mob was last
		dropMe(dropper, dropPos);

		// Add drop to auto destroy item task
		if(isHerb())
			ItemsAutoDestroy.getInstance().addHerb(this);
		else if(Config.AUTODESTROY_ITEM_AFTER > 0 && !isCursed())
			ItemsAutoDestroy.getInstance().addItem(this);

		// activate non owner penalty
		if(lastAttacker != null) // lastAttacker в данном случае top damager
			setItemDropOwner(lastAttacker, Config.NONOWNER_ITEM_PICKUP_DELAY + (dropper.isRaid() ? 285000 : 0));
	}

	/**
	 * Бросает вещь на землю туда, где ее можно поднять
	 */
	public void dropToTheGround(L2Character dropper, Location dropPos)
	{
		if(GeoEngine.canMoveToCoord(dropper.getX(), dropper.getY(), dropper.getZ(), dropPos.x, dropPos.y, dropPos.z, getReflection().getGeoIndex()))
			dropMe(dropper, dropPos);
		else
			dropMe(dropper, dropper.getLoc());

		// Add drop to auto destroy item task
		if(Config.AUTODESTROY_PLAYER_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance().addItem(this);
	}

	public boolean isDestroyable()
	{
		return _itemTemplate.isDestroyable();
	}

	public ItemClass getItemClass()
	{
		return _itemTemplate.getItemClass();
	}

	public void setItemId(int id)
	{
		_itemId = id;
		_itemTemplate = ItemTemplates.getInstance().getTemplate(id);
		_storedInDb = false;
	}

	/**
	 * Возвращает защиту от элемента: огонь.
	 * @return значение защиты
	 */
	public int getDefenceFire()
	{
		return _itemTemplate instanceof L2Armor ? defenseAttributes[0] : 0;
	}

	/**
	 * Возвращает защиту от элемента: вода.
	 * @return значение защиты
	 */
	public int getDefenceWater()
	{
		return _itemTemplate instanceof L2Armor ? defenseAttributes[1] : 0;
	}

	/**
	 * Возвращает защиту от элемента: воздух.
	 * @return значение защиты
	 */
	public int getDefenceWind()
	{
		return _itemTemplate instanceof L2Armor ? defenseAttributes[2] : 0;
	}

	/**
	 * Возвращает защиту от элемента: земля.
	 * @return значение защиты
	 */
	public int getDefenceEarth()
	{
		return _itemTemplate instanceof L2Armor ? defenseAttributes[3] : 0;
	}

	/**
	 * Возвращает защиту от элемента: свет.
	 * @return значение защиты
	 */
	public int getDefenceHoly()
	{
		return _itemTemplate instanceof L2Armor ? defenseAttributes[4] : 0;
	}

	/**
	 * Возвращает защиту от элемента: тьма.
	 * @return значение защиты
	 */
	public int getDefenceUnholy()
	{
		return _itemTemplate instanceof L2Armor ? defenseAttributes[5] : 0;
	}

	/**
	 * Возвращает элемент атрибуции предмета.<br>
	 * Element (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None)
	 * @return id элемента
	 */
	public byte getAttackAttributeElement()
	{
		if(_enchantAttributeFuncTemplate.get(0) == null)
			return L2Item.ATTRIBUTE_NONE;
		return getEnchantAttributeByStat(_enchantAttributeFuncTemplate.get(0)._stat);
	}

    public int[] getAttackElementAndValue() 
	{
        if(_enchantAttributeFuncTemplate.get(0) == null && _itemTemplate instanceof L2Weapon)
            return new int[]{L2Item.ATTRIBUTE_NONE, 0};
        return new int[]{attackAttributeElement, attackAttributeValue};
    }

	public int[] getAttackElement() 
	{
		if(_enchantAttributeFuncTemplate == null || !(_itemTemplate instanceof L2Weapon))
		{
			return new int[]{L2Item.ATTRIBUTE_NONE, 0};
		}
		return new int[]{_enchantAttributeElement, _enchantAttributeValue};
    }

    public int getAttackElementValue() {
        return attackAttributeValue;
    }

    public byte[] getArmorAttributeLevel() {
        byte[] levels = new byte[]{0,0,0,0,0,0};
        for(int i=0; i < getDeffAttr().length; i++) {
            levels[i] = getArmorAttributeLevel(getDeffAttr()[i]);
        }
        return levels;
    }

    public int[] getDeffAttr() 
	{
        return defenseAttributes;
    }

    public int getElementDefAttr(byte element)
	{
        return isArmor() ? defenseAttributes[element] : 0;
    }

    /**
	 * Возвращает значение элемента атрибуции предмета
	 * @return сила элемента
	 */

	public List<FuncTemplate> getAttributeFuncTemplate()
	{
		return _enchantAttributeFuncTemplate;
	}

	/**
	 * Устанавливает элемент атрибуции предмета.<br>
	 * Element (0 - Fire, 1 - Water, 2 - Wind, 3 - Earth, 4 - Holy, 5 - Dark, -2 - None)
	 * @param element элемент
	 */
	public void setAttributeElement(byte element, int value, int[] deffAttr, boolean updateDb)
	{
		Stats stat = getStatByEnchantAttribute(element);

        if (isWeapon()) {
            if (stat == null || value == 0) {
                if (_enchantAttributeFuncTemplate.get(0) != null) {
                    detachFunction(_enchantAttributeFuncTemplate.get(0));
                }
                _enchantAttributeFuncTemplate.set(0, null);
                attackAttributeElement = L2Item.ATTRIBUTE_NONE;
                attackAttributeValue = 0;
                if (updateDb)
                    updateItemAttributes();
                return;
            } else if (_enchantAttributeFuncTemplate.get(0) == null) {
                _enchantAttributeFuncTemplate.set(0, new FuncTemplate(null, "Add", stat, 0x40, value));
                attachFunction(_enchantAttributeFuncTemplate.get(0));
            } else {
                _enchantAttributeFuncTemplate.get(0)._stat = stat;
                _enchantAttributeFuncTemplate.get(0)._value = value;
            }
        } else if (isArmor()) {
            for (int i = 0; i < 6; i++) {
                stat = getStatByEnchantAttribute((byte) i);
                if (deffAttr[i] == 0 && _enchantAttributeFuncTemplate.get(i) != null) { //удалили элемент, но функция осталась
                    detachFunction(_enchantAttributeFuncTemplate.get(i));
                    _enchantAttributeFuncTemplate.set(i, null);
                } else if (deffAttr[i] > 0 && _enchantAttributeFuncTemplate.get(i) == null) { //добавили новый элемент, добавляем функцию
                    _enchantAttributeFuncTemplate.set(i, new FuncTemplate(null, "Sub", stat, 0x40, deffAttr[i]));
                    attachFunction(_enchantAttributeFuncTemplate.get(i));
                } else if (deffAttr[i] > 0 && _enchantAttributeFuncTemplate.get(i) != null) { //функция элемента уже существует, просто добавляем стат
                    _enchantAttributeFuncTemplate.get(i)._stat = stat;
                    _enchantAttributeFuncTemplate.get(i)._value = deffAttr[i];
                }
            }
        }

        attackAttributeElement = element;
        attackAttributeValue = value;
        defenseAttributes = deffAttr;

        if (updateDb)
            updateItemAttributes();
    }

    public boolean isArmor() {
        return getItem().isArmor();
    }

    public byte getEnchantAttributeByStat(Stats stat)
	{
		switch(stat)
		{
			case ATTACK_ELEMENT_FIRE:
			case FIRE_RECEPTIVE:
				return L2Item.ATTRIBUTE_FIRE;
			case ATTACK_ELEMENT_WATER:
			case WATER_RECEPTIVE:
				return L2Item.ATTRIBUTE_WATER;
			case ATTACK_ELEMENT_EARTH:
			case EARTH_RECEPTIVE:
				return L2Item.ATTRIBUTE_EARTH;
			case ATTACK_ELEMENT_WIND:
			case WIND_RECEPTIVE:
				return L2Item.ATTRIBUTE_WIND;
			case ATTACK_ELEMENT_UNHOLY:
			case UNHOLY_RECEPTIVE:
				return L2Item.ATTRIBUTE_DARK;
			case ATTACK_ELEMENT_SACRED:
			case SACRED_RECEPTIVE:
				return L2Item.ATTRIBUTE_HOLY;
			default:
				return L2Item.ATTRIBUTE_NONE;
		}
	}

	public Stats getStatByEnchantAttribute(byte attribute)
	{
		if(getItem() instanceof L2Weapon)
			switch(attribute)
			{
				case L2Item.ATTRIBUTE_FIRE:
					return Stats.ATTACK_ELEMENT_FIRE;
				case L2Item.ATTRIBUTE_WATER:
					return Stats.ATTACK_ELEMENT_WATER;
				case L2Item.ATTRIBUTE_EARTH:
					return Stats.ATTACK_ELEMENT_EARTH;
				case L2Item.ATTRIBUTE_WIND:
					return Stats.ATTACK_ELEMENT_WIND;
				case L2Item.ATTRIBUTE_DARK:
					return Stats.ATTACK_ELEMENT_UNHOLY;
				case L2Item.ATTRIBUTE_HOLY:
					return Stats.ATTACK_ELEMENT_SACRED;
			}
		else
			switch(attribute)
			{
				case L2Item.ATTRIBUTE_FIRE:
					return Stats.FIRE_RECEPTIVE;
				case L2Item.ATTRIBUTE_WATER:
					return Stats.WATER_RECEPTIVE;
				case L2Item.ATTRIBUTE_EARTH:
					return Stats.EARTH_RECEPTIVE;
				case L2Item.ATTRIBUTE_WIND:
					return Stats.WIND_RECEPTIVE;
				case L2Item.ATTRIBUTE_DARK:
					return Stats.UNHOLY_RECEPTIVE;
				case L2Item.ATTRIBUTE_HOLY:
					return Stats.SACRED_RECEPTIVE;
			}
		return null;
	}

	/**
	 * Возвращает тип элемента для камня атрибуции
	 * @return значение элемента
	 */
	public byte getEnchantAttributeStoneElement(boolean inverse)
	{
		switch(_itemId)
		{
			case 9546:
			case 9552:
			case 10521:
				return inverse ? L2Item.ATTRIBUTE_WATER : L2Item.ATTRIBUTE_FIRE;
			case 9547:
			case 9553:
			case 10522:
				return inverse ? L2Item.ATTRIBUTE_FIRE : L2Item.ATTRIBUTE_WATER;
			case 9548:
			case 9554:
			case 10523:
				return inverse ? L2Item.ATTRIBUTE_WIND : L2Item.ATTRIBUTE_EARTH;
			case 9549:
			case 9555:
			case 10524:
				return inverse ? L2Item.ATTRIBUTE_EARTH : L2Item.ATTRIBUTE_WIND;
			case 9550:
			case 9556:
			case 10525:
				return inverse ? L2Item.ATTRIBUTE_HOLY : L2Item.ATTRIBUTE_DARK;
			case 9551:
			case 9557:
			case 10526:
				return inverse ? L2Item.ATTRIBUTE_DARK : L2Item.ATTRIBUTE_HOLY;
			//TODO атрибуты ГоДа
			default:
				return L2Item.ATTRIBUTE_NONE;
		}
	}

    public byte getAttributeElementLevel() {
		switch(_itemId) {
			/*Stone*/
			case 9546:
			case 9547:
			case 9548:
			case 9549:
			case 9550:
			case 9551:
			case 10521:
			case 10522:
			case 10523:
			case 10524:
			case 10525:
			case 10526:
				return 3;
			/*Crystals*/
			case 9552:
			case 9557:
			case 9555:
			case 9554:
			case 9553:
			case 9556:
				return 6;
			/*Jewels*/
			case 9558:
			case 9563:
			case 9561:
			case 9560:
			case 9562:
			case 9559:
				return 9;
			/*Energy*/
			case 9567:
			case 9566:
			case 9568:
			case 9565:
			case 9564:
			case 9569:
				return 12;
        }
        return -1;
    }

    public byte getWeaponElementLevel() {
        int val = getAttackElementValue();
        if(!isWeapon() || val == 0)
            return 0;
        if(val > 0 && val < 25)
            return 1;
        else if(val >= 25 && val < 75)
            return 2;
        else if(val >= 75 && val < 150)
            return 3;
        else if(val >= 150 && val < 175)
            return 4;
        else if(val >= 175 && val < 225)
            return 5;
        else if(val >= 225 && val < 300)
            return 6;
        else if(val >= 300 && val < 325)
            return 7;
        else if(val >= 325 && val < 375)
            return 8;
        else if(val >= 375 && val < 450)
            return 9;
        else if(val >= 450 && val < 475)
            return 10;
        else if(val >= 475 && val < 525)
            return 11;
        else if(val >= 525 && val < 600)
            return 12;
        else if(val >= 600)
            return 13;
        return 0;
    }

    private byte getArmorAttributeLevel(int val) {
        if(!isArmor() || val == 0)
            return 0;
        if(val > 0 && val < 12)
            return 1;
        else if(val >= 12 && val < 30)
            return 2;
        else if(val >= 30 && val < 60)
            return 3;
        else if(val >= 60 && val < 72)
            return 4;
        else if(val >= 72 && val < 90)
            return 5;
        else if(val >= 90 && val < 120)
            return 6;
        else if(val >= 120 && val < 132)
            return 7;
        else if(val >= 132 && val < 150)
            return 8;
        else if(val >= 150 && val < 180)
            return 9;
        else if(val >= 180 && val < 192)
            return 10;
        else if(val >= 192 && val < 210)
            return 11;
        else if(val >= 210 && val < 240)
            return 12;
        else if(val >= 240)
            return 13;
        return 0;
    }
	public boolean isAttributeCrystal()
	{
		return _itemId == 9552 || _itemId == 9553 || _itemId == 9554 || _itemId == 9555 || _itemId == 9556 || _itemId == 9557;
	}

	/**
	 * Проверяет, является ли данный инстанс предмета хербом
	 * @return true если предмет является хербом
	 */
	public boolean isHerb()
	{
		return getItem().isHerb();
	}

	public Grade getCrystalType()
	{
		return _itemTemplate.getCrystalType();
	}

	public void setCustomFlags(int i, boolean updateDb)
	{
		if(_customFlags != i)
		{
			_customFlags = i;
			if(updateDb)
				updateDatabase();
			else
				_storedInDb = false;
		}
	}

	public int getCustomFlags()
	{
		return _customFlags;
	}

	@Override
	public String getName()
	{
		return getItem().getName();
	}


    public boolean isWeapon() {
        return getItem().isWeapon();
    }
}