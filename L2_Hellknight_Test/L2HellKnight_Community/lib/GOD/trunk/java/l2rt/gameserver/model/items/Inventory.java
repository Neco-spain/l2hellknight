package l2rt.gameserver.model.items;

import l2rt.database.DatabaseUtils;
import l2rt.database.FiltredPreparedStatement;
import l2rt.database.L2DatabaseFactory;
import l2rt.database.ThreadConnection;
import l2rt.extensions.multilang.CustomMessage;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.CastleManager;
import l2rt.gameserver.instancemanager.CursedWeaponsManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.base.ClassType;
import l2rt.gameserver.model.base.PlayerClass;
import l2rt.gameserver.model.base.Race;
import l2rt.gameserver.model.entity.vehicle.L2AirShip;
import l2rt.gameserver.model.items.L2ItemInstance.ItemLocation;
import l2rt.gameserver.model.items.listeners.*;
import l2rt.gameserver.network.serverpackets.InventoryUpdate;
import l2rt.gameserver.network.serverpackets.PetInventoryUpdate;
import l2rt.gameserver.network.serverpackets.SystemMessage;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.PetDataTable;
import l2rt.gameserver.templates.L2Armor;
import l2rt.gameserver.templates.L2Armor.ArmorType;
import l2rt.gameserver.templates.L2EtcItem;
import l2rt.gameserver.templates.L2EtcItem.EtcItemType;
import l2rt.gameserver.templates.L2Item.Grade;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.GArray;
import l2rt.util.GCSArray;
import l2rt.util.Log;
import l2rt.util.Util;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Inventory
{
	protected static final Logger _log = Logger.getLogger(Inventory.class.getName());

	public static final byte PAPERDOLL_UNDER = 0;
	public static final byte PAPERDOLL_REAR = 1;
	public static final byte PAPERDOLL_LEAR = 2;
	public static final byte PAPERDOLL_NECK = 3;
	public static final byte PAPERDOLL_RFINGER = 4;
	public static final byte PAPERDOLL_LFINGER = 5;
	public static final byte PAPERDOLL_HEAD = 6;
	public static final byte PAPERDOLL_RHAND = 7;
	public static final byte PAPERDOLL_LHAND = 8;
	public static final byte PAPERDOLL_GLOVES = 9;
	public static final byte PAPERDOLL_CHEST = 10;
	public static final byte PAPERDOLL_LEGS = 11;
	public static final byte PAPERDOLL_FEET = 12;
	public static final byte PAPERDOLL_BACK = 13;
	public static final byte PAPERDOLL_LRHAND = 14;
	public static final byte PAPERDOLL_HAIR = 15;
	public static final byte PAPERDOLL_DHAIR = 16;
	public static final byte PAPERDOLL_RBRACELET = 17;
	public static final byte PAPERDOLL_LBRACELET = 18;
	public static final byte PAPERDOLL_DECO1 = 19;
	public static final byte PAPERDOLL_DECO2 = 20;
	public static final byte PAPERDOLL_DECO3 = 21;
	public static final byte PAPERDOLL_DECO4 = 22;
	public static final byte PAPERDOLL_DECO5 = 23;
	public static final byte PAPERDOLL_DECO6 = 24;
	public static final byte PAPERDOLL_BELT = 25;

	public static final byte PAPERDOLL_MAX = 26;

	private final L2ItemInstance[] _paperdoll;

	private final CopyOnWriteArrayList<PaperdollListener> _paperdollListeners;

	private GCSArray<L2ItemInstance> _listenedItems;

	// protected to be accessed from child classes only
	// Отдельно синхронизировать этот список не надо, ибо ConcurrentLinkedQueue уже синхронизирован
	protected final ConcurrentLinkedQueue<L2ItemInstance> _items;
	protected final ConcurrentLinkedQueue<L2ItemInstance> _refundItems;

	private int _totalWeight;

	private boolean _refreshingListeners;

	// used to quickly check for using of items of special type
	private long _wearedMask;

	// Castle circlets, WARNING: position == castle.id !
	public static final Integer[] _castleCirclets = { 0, // no castle - no circlet.. :)
			6838, // Circlet of Gludio
			6835, // Circlet of Dion
			6839, // Circlet of Giran
			6837, // Circlet of Oren
			6840, // Circlet of Aden
			6834, // Circlet of Innadril
			6836, // Circlet of Goddard
			8182, // Circlet of Rune
			8183, // Circlet of Schuttgart
	};

	public static final int FORMAL_WEAR_ID = 6408;

	protected Inventory()
	{
		_paperdoll = new L2ItemInstance[PAPERDOLL_MAX];
		_items = new ConcurrentLinkedQueue<L2ItemInstance>();
		_refundItems = new ConcurrentLinkedQueue<L2ItemInstance>();
		_paperdollListeners = new CopyOnWriteArrayList<PaperdollListener>();
		addPaperdollListener(new BraceletListener(this));
		addPaperdollListener(new BowListener(this));
		addPaperdollListener(new ArmorSetListener(this));
		addPaperdollListener(new StatsListener(this));
		addPaperdollListener(new ItemSkillsListener(this));
		addPaperdollListener(new ItemAugmentationListener(this));
	}

	public abstract L2Character getOwner();

	protected abstract ItemLocation getBaseLocation();

	protected abstract ItemLocation getEquipLocation();

	public int getOwnerId()
	{
		L2Character owner = getOwner();
		return owner == null ? 0 : owner.getObjectId();
	}

	public ChangeRecorder newRecorder()
	{
		return new ChangeRecorder(this);
	}

	public int getSize()
	{
		return getItemsList().size();
	}

	public L2ItemInstance[] getItems()
	{
		return getItemsList().toArray(new L2ItemInstance[getItemsList().size()]);
	}

	public ConcurrentLinkedQueue<L2ItemInstance> getItemsList()
	{
		return _items;
	}

	public ConcurrentLinkedQueue<L2ItemInstance> getRefundItemsList()
	{
		return _refundItems;
	}

	public L2ItemInstance addItem(int id, long count)
	{
		L2ItemInstance newItem = ItemTemplates.getInstance().createItem(id);
		newItem.setCount(count);
		return addItem(newItem);
	}

	public L2ItemInstance addItem(L2ItemInstance newItem)
	{
		return addItem(newItem, true, true);
	}

	private L2ItemInstance addItem(L2ItemInstance newItem, boolean dbUpdate, boolean log)
	{
		L2Character owner = getOwner();
		if(owner == null || newItem == null)
			return null;

		if(newItem.isHerb() && !owner.getPlayer().isGM())
		{
			Util.handleIllegalPlayerAction(owner.getPlayer(), "tried to pickup herb into inventory", "Inventory[179]", 1);
			return null;
		}

		if(newItem.getCount() < 0)
		{
			_log.warning("AddItem: count < 0 owner:" + owner.getName());
			Thread.dumpStack();
			return null;
		}

		L2ItemInstance result = newItem;
		boolean stackableFound = false;

		if(log)
			Log.add("Inventory|" + owner.getName() + "|Get item|" + result.getItemId() + "|" + result.getCount() + "|" + result.getObjectId(), "items");

		// If stackable, search item in inventory in order to add to current quantity
		if(newItem.isStackable())
		{
			int itemId = newItem.getItemId();
			L2ItemInstance old = getItemByItemId(itemId);
			if(old != null)
			{
				// add new item quantity to existing stack
				old.setCount(old.getCount() + newItem.getCount());

				// reset new item to null
				if(log)
					Log.add("Inventory|" + owner.getName() + "|join item from-to|" + result.getItemId() + "|" + newItem.getObjectId() + "|" + old.getObjectId(), "items");

				newItem.setCount(0);
				newItem.setOwnerId(0);
				newItem.setLocation(ItemLocation.VOID);
				newItem.removeFromDb(true);
				newItem.deleteMe();

				stackableFound = true;

				sendModifyItem(old);

				// update old item in inventory and destroy new item
				old.updateDatabase();

				result = old;
			}
		}

		// If item hasn't be found in inventory
		if(!stackableFound)
		{
			// Add item in inventory
			if(getItemByObjectId(newItem.getObjectId()) == null)
			{
				getItemsList().add(newItem);
				if(!newItem.isEquipable() && newItem.getItem() instanceof L2EtcItem && !newItem.isStackable() && (newItem.getStatFuncs() != null || newItem.getItem().getAttachedSkills() != null))
				{
					if(_listenedItems == null)
						_listenedItems = new GCSArray<L2ItemInstance>();
					_listenedItems.add(newItem);
					for(PaperdollListener listener : _paperdollListeners)
						listener.notifyEquipped(-1, newItem);
				}
			}
			else if(log)
				Log.add("Inventory|" + owner.getName() + "|add double link to item in inventory list!|" + newItem.getItemId() + "|" + newItem.getObjectId(), "items");

			if(newItem.getOwnerId() != owner.getPlayer().getObjectId() || dbUpdate)
			{
				newItem.setOwnerId(owner.getPlayer().getObjectId());
				newItem.setLocation(getBaseLocation(), findSlot(0));
				sendNewItem(newItem);
			}

			// If database wanted to be updated, update item
			if(dbUpdate)
				newItem.updateDatabase();
		}

		if(dbUpdate && result.isCursed() && owner.isPlayer())
			CursedWeaponsManager.getInstance().checkPlayer((L2Player) owner, result);

		refreshWeight();
		return result;
	}

	public void restoreCursedWeapon()
	{
		L2Character owner = getOwner();
		if(owner == null || !owner.isPlayer())
			return;

		for(L2ItemInstance i : getItemsList())
			if(i.isCursed())
			{
				CursedWeaponsManager.getInstance().checkPlayer((L2Player) owner, i);
				_log.info("Restored CursedWeapon [" + i + "] for: " + owner);
				break;
			}
	}

	/**
	 * Находит и возвращает пустой слот в инвентаре. Вызывается с параметром 0, рекурсивно вызывает себя увеличивая номер слота пока не найдет свободный.
	 */
	public int findSlot(int slot)
	{
		for(L2ItemInstance i : _items)
		{
			if(i.isEquipped() || i.getItem().getType2() == L2Item.TYPE2_QUEST) // игнорируем надетое и квестовые вещи
				continue;
			if(i.getEquipSlot() == slot) // слот занят?
				return findSlot(++slot); // пробуем следующий
		}
		return slot; // слот не занят, возвращаем
	}

	public L2ItemInstance getPaperdollItem(int slot)
	{
		return _paperdoll[slot];
	}

	public L2ItemInstance[] getPaperdollItems()
	{
		return _paperdoll;
	}

	public int getPaperdollItemId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if(item != null)
			return item.getItemId();
		else if(slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if(item != null)
				return item.getItemId();
		}
		else if(slot == PAPERDOLL_RHAND && getOwner().isPlayer())
		{
			L2Player player = getOwner().getPlayer();
			if(player.getVehicle() == null || !player.getVehicle().isAirShip())
				return 0;
			L2AirShip airship = (L2AirShip) player.getVehicle();
			if(airship.getDriver() == player)
				return 13556; // Затычка на отображение штурвала - Airship Helm
		}
		return 0;
	}

	public int getPaperdollObjectId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if(item != null)
			return item.getObjectId();
		else if(slot == PAPERDOLL_HAIR)
		{
			item = _paperdoll[PAPERDOLL_DHAIR];
			if(item != null)
				return item.getObjectId();
		}
		return 0;
	}

	public synchronized void addPaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.add(listener);
	}

	public synchronized void removePaperdollListener(PaperdollListener listener)
	{
		_paperdollListeners.remove(listener);
	}

	public L2ItemInstance setPaperdollItem(int slot, L2ItemInstance item)
	{
		L2ItemInstance old = _paperdoll[slot];
		if(old != item)
		{
			if(old != null)
			{
				_paperdoll[slot] = null;
				old.setLocation(getBaseLocation(), findSlot(0));
				sendModifyItem(old);
				long mask = 0;
				for(int i = 0; i < PAPERDOLL_MAX; i++)
				{
					L2ItemInstance pi = _paperdoll[i];
					if(pi != null)
						mask |= pi.getItem().getItemMask();
				}
				_wearedMask = mask;
				old.updateDatabase();
				for(PaperdollListener listener : _paperdollListeners)
					listener.notifyUnequipped(slot, old);
				old.shadowNotify(false);
			}
			if(item != null)
			{
				_paperdoll[slot] = item;
				item.setLocation(getEquipLocation(), slot);
				sendModifyItem(item);
				_wearedMask |= item.getItem().getItemMask();
				item.updateDatabase();
				for(PaperdollListener listener : _paperdollListeners)
					listener.notifyEquipped(slot, item);
				item.shadowNotify(true);
			}
		}
		return old;
	}

	public long getWearedMask()
	{
		return _wearedMask;
	}

	public void unEquipItem(L2ItemInstance item)
	{
		if(item.isEquipped())
			unEquipItemInBodySlot(item.getBodyPart(), item);
	}

	/**
	 * Снимает предмет, и все зависимые от него, и возвращает отличия.
	 */
	public L2ItemInstance[] unEquipItemInBodySlotAndRecord(int slot, L2ItemInstance item)
	{
		ChangeRecorder recorder = newRecorder();
		try
		{
			unEquipItemInBodySlot(slot, item);
		}
		finally
		{
			removePaperdollListener(recorder);
		}
		return recorder.getChangedItems();
	}

	/**
	 * Снимет вещь, записывает это в базу, шлет все нужные пакеты и перепроверяет пенальти
	 * @param slot L2Item.SLOT_
	 * @param item
	 */
	public void unEquipItemInBodySlotAndNotify(int slot, L2ItemInstance item)
	{
		L2Player cha = getOwner().getPlayer();
		if(cha == null)
			return;

		L2ItemInstance[] unequipped = unEquipItemInBodySlotAndRecord(slot, item);
		if(unequipped == null || unequipped.length == 0)
			return;

		L2ItemInstance weapon = cha.getActiveWeaponInstance();

		for(L2ItemInstance uneq : unequipped)
		{
			if(uneq == null || uneq.isWear())
				continue;

			cha.sendDisarmMessage(uneq);

			if(weapon != null && uneq == weapon)
			{
				uneq.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				uneq.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
				cha.abortAttack(true, true);
				cha.abortCast(true);
			}
		}

        if(item != null)
            cha.validateItemExpertisePenalties(false, item.getItem() instanceof L2Armor, item.getItem() instanceof L2Weapon);
		cha.broadcastUserInfo(true);
	}

	public L2ItemInstance unEquipItemInSlot(int pdollSlot)
	{
		return setPaperdollItem(pdollSlot, null);
	}

	/**
	 * Unequips item in slot (i.e. equips with default value)
	 *
	 * @param slot : int designating the slot
	 */
	public void unEquipItemInBodySlot(int slot, L2ItemInstance item)
	{
		byte pdollSlot = -1;
		switch(slot)
		{
			case L2Item.SLOT_NECK:
				pdollSlot = PAPERDOLL_NECK;
				break;
			case L2Item.SLOT_L_EAR:
				pdollSlot = PAPERDOLL_LEAR;
				break;
			case L2Item.SLOT_R_EAR:
				pdollSlot = PAPERDOLL_REAR;
				break;
			case L2Item.SLOT_L_FINGER:
				pdollSlot = PAPERDOLL_LFINGER;
				break;
			case L2Item.SLOT_R_FINGER:
				pdollSlot = PAPERDOLL_RFINGER;
				break;
			case L2Item.SLOT_HAIR:
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_DHAIR:
				pdollSlot = PAPERDOLL_DHAIR;
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_DHAIR, null); // This should be the same as in DHAIR
				pdollSlot = PAPERDOLL_HAIR;
				break;
			case L2Item.SLOT_HEAD:
				pdollSlot = PAPERDOLL_HEAD;
				break;
			case L2Item.SLOT_R_HAND:
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_HAND:
				pdollSlot = PAPERDOLL_LHAND;
				break;
			case L2Item.SLOT_GLOVES:
				pdollSlot = PAPERDOLL_GLOVES;
				break;
			case L2Item.SLOT_LEGS:
				pdollSlot = PAPERDOLL_LEGS;
				break;
			case L2Item.SLOT_CHEST:
			case L2Item.SLOT_FULL_ARMOR:
			case L2Item.SLOT_FORMAL_WEAR:
				pdollSlot = PAPERDOLL_CHEST;
				break;
			case L2Item.SLOT_BACK:
				pdollSlot = PAPERDOLL_BACK;
				break;
			case L2Item.SLOT_FEET:
				pdollSlot = PAPERDOLL_FEET;
				break;
			case L2Item.SLOT_UNDERWEAR:
				pdollSlot = PAPERDOLL_UNDER;
				break;
			case L2Item.SLOT_BELT:
				pdollSlot = PAPERDOLL_BELT;
				break;
			case L2Item.SLOT_LR_HAND:
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null); // this should be the same as in LRHAND
				pdollSlot = PAPERDOLL_RHAND;
				break;
			case L2Item.SLOT_L_BRACELET:
				pdollSlot = PAPERDOLL_LBRACELET;
				break;
			case L2Item.SLOT_R_BRACELET:
				pdollSlot = PAPERDOLL_RBRACELET;
				break;
			case L2Item.SLOT_DECO:
				if(item == null)
					return;
				else if(getPaperdollObjectId(PAPERDOLL_DECO1) == item.getObjectId())
					pdollSlot = PAPERDOLL_DECO1;
				else if(getPaperdollObjectId(PAPERDOLL_DECO2) == item.getObjectId())
					pdollSlot = PAPERDOLL_DECO2;
				else if(getPaperdollObjectId(PAPERDOLL_DECO3) == item.getObjectId())
					pdollSlot = PAPERDOLL_DECO3;
				else if(getPaperdollObjectId(PAPERDOLL_DECO4) == item.getObjectId())
					pdollSlot = PAPERDOLL_DECO4;
				else if(getPaperdollObjectId(PAPERDOLL_DECO5) == item.getObjectId())
					pdollSlot = PAPERDOLL_DECO5;
				else if(getPaperdollObjectId(PAPERDOLL_DECO6) == item.getObjectId())
					pdollSlot = PAPERDOLL_DECO6;
				break;
			default:
				String name = getOwner() == null ? "null" : getOwner().getPlayer().getName();
				_log.warning("Requested invalid body slot: " + slot + ", Item: " + item + ", owner: '" + name + "'");
				Thread.dumpStack();
		}
		if(pdollSlot >= 0)
			setPaperdollItem(pdollSlot, null);
	}

	/**
	 * Одевает предмет
	 * @param item
	 * @param checks false при восстановлении инвентаря
	 */
	public synchronized void equipItem(L2ItemInstance item, boolean checks)
	{
		int targetSlot = item.getItem().getBodyPart();

		if(checks)
		{
			L2Character owner = getOwner();
			if(owner.isPlayer() && owner.getName() != null)
			{
				SystemMessage msg = checkConditions(item);
				if(msg != null)
				{
					owner.sendPacket(msg);
					return;
				}
			}
		}

		double mp = 0; // при смене робы ману не сбрасываем

		switch(targetSlot)
		{
			case L2Item.SLOT_LR_HAND:
			{
				setPaperdollItem(PAPERDOLL_LHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, null);
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}

			case L2Item.SLOT_L_HAND:
			{
				L2ItemInstance slot = getPaperdollItem(PAPERDOLL_RHAND);

				L2Item oldItem = slot == null ? null : slot.getItem();
				L2Item newItem = item.getItem();

				if(oldItem != null && newItem.getItemType() == EtcItemType.ARROW && oldItem.getItemType() == WeaponType.BOW && oldItem.getCrystalType() != newItem.getCrystalType())
					return;
				if(oldItem != null && newItem.getItemType() == EtcItemType.BOLT && oldItem.getItemType() == WeaponType.CROSSBOW && oldItem.getCrystalType() != newItem.getCrystalType())
					return;

				if(newItem.getItemType() != EtcItemType.ARROW && newItem.getItemType() != EtcItemType.BOLT && newItem.getItemType() != EtcItemType.BAIT)
				{
					if(oldItem != null && oldItem.getBodyPart() == L2Item.SLOT_LR_HAND)
					{
						setPaperdollItem(PAPERDOLL_RHAND, null);
						setPaperdollItem(PAPERDOLL_LHAND, null);
					}
					else
						setPaperdollItem(PAPERDOLL_LHAND, null);
					setPaperdollItem(PAPERDOLL_LHAND, item);
				}
				else if(oldItem != null && (newItem.getItemType() == EtcItemType.ARROW && oldItem.getItemType() == WeaponType.BOW || newItem.getItemType() == EtcItemType.BOLT && oldItem.getItemType() == WeaponType.CROSSBOW || newItem.getItemType() == EtcItemType.BAIT && oldItem.getItemType() == WeaponType.ROD))
				{
					setPaperdollItem(PAPERDOLL_LHAND, item);
					if(newItem.getItemType() == EtcItemType.BAIT && getOwner().isPlayer())
					{
						L2Player owner = (L2Player) getOwner();
						owner.setVar("LastLure", String.valueOf(item.getObjectId()));
					}
				}
				break;
			}

			case L2Item.SLOT_R_HAND:
			{
				setPaperdollItem(PAPERDOLL_RHAND, item);
				break;
			}
			case L2Item.SLOT_L_EAR:
			case L2Item.SLOT_R_EAR:
			case L2Item.SLOT_L_EAR | L2Item.SLOT_R_EAR:
			{
				if(_paperdoll[PAPERDOLL_LEAR] == null)
				{
					item.setBodyPart(L2Item.SLOT_L_EAR);
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				else if(_paperdoll[PAPERDOLL_REAR] == null)
				{
					item.setBodyPart(L2Item.SLOT_R_EAR);
					setPaperdollItem(PAPERDOLL_REAR, item);
				}
				else
				{
					item.setBodyPart(L2Item.SLOT_L_EAR);
					setPaperdollItem(PAPERDOLL_LEAR, null);
					setPaperdollItem(PAPERDOLL_LEAR, item);
				}
				break;
			}
			case L2Item.SLOT_L_FINGER:
			case L2Item.SLOT_R_FINGER:
			case L2Item.SLOT_L_FINGER | L2Item.SLOT_R_FINGER:
			{
				if(_paperdoll[PAPERDOLL_LFINGER] == null)
				{
					item.setBodyPart(L2Item.SLOT_L_FINGER);
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				else if(_paperdoll[PAPERDOLL_RFINGER] == null)
				{
					item.setBodyPart(L2Item.SLOT_R_FINGER);
					setPaperdollItem(PAPERDOLL_RFINGER, item);
				}
				else
				{
					item.setBodyPart(L2Item.SLOT_L_FINGER);
					setPaperdollItem(PAPERDOLL_LFINGER, null);
					setPaperdollItem(PAPERDOLL_LFINGER, item);
				}
				break;
			}
			case L2Item.SLOT_NECK:
				setPaperdollItem(PAPERDOLL_NECK, item);
				break;
			case L2Item.SLOT_FULL_ARMOR:
				if(getOwner() != null)
					mp = getOwner().getCurrentMp();
				setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				if(mp > getOwner().getCurrentMp())
					getOwner().setCurrentMp(mp);
				break;
			case L2Item.SLOT_CHEST:
				if(getOwner() != null)
					mp = getOwner().getCurrentMp();
				setPaperdollItem(PAPERDOLL_CHEST, item);
				if(mp > getOwner().getCurrentMp())
					getOwner().setCurrentMp(mp);
				break;
			case L2Item.SLOT_LEGS:
			{
				// handle full armor
				L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
				if(chest != null && chest.getBodyPart() == L2Item.SLOT_FULL_ARMOR)
					setPaperdollItem(PAPERDOLL_CHEST, null);

				if(getPaperdollItemId(PAPERDOLL_CHEST) == FORMAL_WEAR_ID)
					setPaperdollItem(PAPERDOLL_CHEST, null);

				if(getOwner() != null)
					mp = getOwner().getCurrentMp();
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_LEGS, item);
				if(mp > getOwner().getCurrentMp())
					getOwner().setCurrentMp(mp);
				break;
			}
			case L2Item.SLOT_FEET:
				if(getPaperdollItemId(PAPERDOLL_CHEST) == FORMAL_WEAR_ID)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_FEET, item);
				break;
			case L2Item.SLOT_GLOVES:
				if(getPaperdollItemId(PAPERDOLL_CHEST) == FORMAL_WEAR_ID)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_GLOVES, item);
				break;
			case L2Item.SLOT_HEAD:
				if(getPaperdollItemId(PAPERDOLL_CHEST) == FORMAL_WEAR_ID)
					setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_HEAD, item);
				break;
			case L2Item.SLOT_HAIR:
				L2ItemInstance slot = getPaperdollItem(PAPERDOLL_DHAIR);
				if(slot != null && slot.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_DHAIR, null);
				}
				setPaperdollItem(PAPERDOLL_HAIR, item);
				break;
			case L2Item.SLOT_DHAIR:
				L2ItemInstance slot2 = getPaperdollItem(PAPERDOLL_DHAIR);
				if(slot2 != null && slot2.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
				{
					setPaperdollItem(PAPERDOLL_HAIR, null);
					setPaperdollItem(PAPERDOLL_DHAIR, null);
				}
				setPaperdollItem(PAPERDOLL_DHAIR, item);
				break;
			case L2Item.SLOT_HAIRALL:
				setPaperdollItem(PAPERDOLL_HAIR, null);
				setPaperdollItem(PAPERDOLL_DHAIR, null);
				setPaperdollItem(PAPERDOLL_DHAIR, item);
				break;
			case L2Item.SLOT_R_BRACELET:
				setPaperdollItem(PAPERDOLL_RBRACELET, null);
				setPaperdollItem(PAPERDOLL_RBRACELET, item);
				break;
			case L2Item.SLOT_L_BRACELET:
				setPaperdollItem(PAPERDOLL_LBRACELET, null);
				setPaperdollItem(PAPERDOLL_LBRACELET, item);
				break;
			case L2Item.SLOT_UNDERWEAR:
				setPaperdollItem(PAPERDOLL_UNDER, item);
				break;
			case L2Item.SLOT_BACK:
				setPaperdollItem(PAPERDOLL_BACK, item);
				break;
			case L2Item.SLOT_BELT:
				setPaperdollItem(PAPERDOLL_BELT, item);
				break;
			case L2Item.SLOT_DECO:
				if(_paperdoll[PAPERDOLL_DECO1] == null)
				{
					setPaperdollItem(PAPERDOLL_DECO1, item);
					break;
				}
				if(_paperdoll[PAPERDOLL_DECO2] == null)
				{
					setPaperdollItem(PAPERDOLL_DECO2, item);
					break;
				}
				if(_paperdoll[PAPERDOLL_DECO3] == null)
				{
					setPaperdollItem(PAPERDOLL_DECO3, item);
					break;
				}
				if(_paperdoll[PAPERDOLL_DECO4] == null)
				{
					setPaperdollItem(PAPERDOLL_DECO4, item);
					break;
				}
				if(_paperdoll[PAPERDOLL_DECO5] == null)
				{
					setPaperdollItem(PAPERDOLL_DECO5, item);
					break;
				}
				if(_paperdoll[PAPERDOLL_DECO6] == null)
					setPaperdollItem(PAPERDOLL_DECO6, item);
				else
					setPaperdollItem(PAPERDOLL_DECO1, item);
				break;

			case L2Item.SLOT_FORMAL_WEAR:
				// При одевании свадебного платья руки не трогаем
				setPaperdollItem(PAPERDOLL_LEGS, null);
				setPaperdollItem(PAPERDOLL_CHEST, null);
				setPaperdollItem(PAPERDOLL_HEAD, null);
				setPaperdollItem(PAPERDOLL_FEET, null);
				setPaperdollItem(PAPERDOLL_GLOVES, null);
				setPaperdollItem(PAPERDOLL_CHEST, item);
				break;
			default:
				_log.warning("unknown body slot:" + targetSlot + " for item id: " + item.getItemId());
		}

		if(getOwner().isPlayer())
			((L2Player) getOwner()).AutoShot();
	}

	public L2ItemInstance getItemByItemId(int itemId)
	{
		for(L2ItemInstance temp : getItemsList())
			if(temp.getItemId() == itemId)
				return temp;
		return null;
	}

	public L2ItemInstance[] getAllItemsById(int itemId)
	{
		GArray<L2ItemInstance> ar = new GArray<L2ItemInstance>();
		for(L2ItemInstance i : getItemsList())
			if(i.getItemId() == itemId)
				ar.add(i);
		return ar.toArray(new L2ItemInstance[ar.size()]);
	}

	public int getPaperdollAugmentationId(int slot)
	{
		L2ItemInstance item = _paperdoll[slot];
		if(item != null && item.getAugmentation() != null)
			return item.getAugmentation().getAugmentationId();
		return 0;
	}

	public L2ItemInstance getItemByObjectId(Integer objectId)
	{
		for(L2ItemInstance temp : getItemsList())
			if(temp.getObjectId() == objectId)
				return temp;
		return null;
	}

	public L2ItemInstance destroyItem(int objectId, long count, boolean toLog)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		return destroyItem(item, count, toLog);
	}

	/**
	 * Destroy item from inventory and updates database
	 */
	public L2ItemInstance destroyItem(L2ItemInstance item, long count, boolean toLog)
	{
		if(getOwner() == null || item == null)
			return null;

		if(count < 0)
		{
			_log.warning("DestroyItem: count < 0 owner:" + getOwner().getName());
			Thread.dumpStack();
			return null;
		}

		if(toLog)
		{
			Log.LogItem(getOwner(), null, Log.DeleteItem, item, count);
			Log.add("Inventory|" + getOwner().getName() + "|Destroys item|" + item.getItemId() + "|" + count + "|" + item.getObjectId(), "items");
		}

		if(item.getCount() <= count)
		{
			if(item.getCount() < count && toLog)
				Log.add("!Inventory|" + getOwner().getName() + "|Destroys item|" + item.getItemId() + "|" + count + " but item count " + item.getCount() + "|" + item.getObjectId(), "items");
			removeItemFromInventory(item, true, true);
			// При удалении ошейника, удалить пета
			if(PetDataTable.isPetControlItem(item))
				PetDataTable.deletePet(item, getOwner());
		}
		else
		{
			item.setCount(item.getCount() - count);
			sendModifyItem(item);
			item.updateDatabase();
		}

		refreshWeight();

		return item;
	}

	private void sendModifyItem(L2ItemInstance item)
	{
		if(getOwner().isPet())
			getOwner().getPlayer().sendPacket(new PetInventoryUpdate().addModifiedItem(item));
		else
			getOwner().sendPacket(new InventoryUpdate().addModifiedItem(item));
	}

	private void sendRemoveItem(L2ItemInstance item)
	{
		if(getOwner().isPet())
			getOwner().getPlayer().sendPacket(new PetInventoryUpdate().addRemovedItem(item));
		else
			getOwner().sendPacket(new InventoryUpdate().addRemovedItem(item));
	}

	private void sendNewItem(L2ItemInstance item)
	{
		if(getOwner().isPet())
			getOwner().getPlayer().sendPacket(new PetInventoryUpdate().addNewItem(item));
		else
			getOwner().sendPacket(new InventoryUpdate().addNewItem(item));
	}

	// we need this one cuz warehouses send itemId only
	/**
	 * Destroy item from inventory by using its <B>itemID</B> and updates
	 * database
	 *
	 * @param itemId : int pointing out the itemID of the item
	 * @param count : long designating the quantity of item to destroy
	 * @return L2ItemInstance designating the item up-to-date
	 */
	public L2ItemInstance destroyItemByItemId(int itemId, long count, boolean toLog)
	{
		L2ItemInstance item = getItemByItemId(itemId);
		Log.LogItem(getOwner(), Log.Sys_DeleteItem, item, count);
		return destroyItem(item, count, toLog);
	}

	/**
	 * Destroy item from inventory and from database.
	 *
	 * @param item : L2ItemInstance designating the item to remove from inventory
	 * @param clearCount : boolean : if true, set the item quantity to 0
	 */
	private void removeItemFromInventory(L2ItemInstance item, boolean clearCount, boolean AllowRemoveAttributes)
	{
		if(getOwner() == null)
			return;

		if(getOwner().isPlayer())
		{
			L2Player player = (L2Player) getOwner();
			player.removeItemFromShortCut(item.getObjectId());
			if(item.isEquipped())
				unEquipItem(item);
		}

		getItemsList().remove(item);
		item.shadowNotify(false);
		if(!item.isEquipable() && item.getItem() instanceof L2EtcItem && !item.isStackable() && (item.getStatFuncs() != null || item.getItem().getAttachedSkills() != null))
		{
			if(_listenedItems != null)
			{
				_listenedItems.remove(item);
				if(_listenedItems.isEmpty())
					_listenedItems = null;
			}
			for(PaperdollListener listener : _paperdollListeners)
				listener.notifyUnequipped(-1, item);
		}
		if(clearCount)
			item.setCount(0);

		item.setOwnerId(0);
		item.setLocation(ItemLocation.VOID);
		sendRemoveItem(item);
		item.updateDatabase(true, AllowRemoveAttributes);
		item.deleteMe();
	}

	public L2ItemInstance dropItem(int objectId, long count, boolean allowRemoveAttributes)
	{
		L2ItemInstance item = getItemByObjectId(objectId);
		if(item == null)
		{
			_log.warning("DropItem: item objectId: " + objectId + " does not exist in inventory");
			Thread.dumpStack();
			return null;
		}
		return dropItem(item, count, allowRemoveAttributes);
	}

	/**
	 * Drop item from inventory by using <B>object L2ItemInstance</B><BR>
	 * <BR>
	 * <U><I>Concept :</I></U><BR>
	 * item equipped are unequipped
	 * <LI>If quantity of items in inventory after drop is negative or null,
	 * change location of item</LI>
	 * <LI>Otherwise, change quantity in inventory and create new object with
	 * quantity dropped</LI>
	 *
	 * @param oldItem : L2ItemInstance designating the item to drop
	 * @param count : int designating the quantity of item to drop
	 * @return L2ItemInstance designating the item dropped
	 */
	public L2ItemInstance dropItem(L2ItemInstance oldItem, long count, boolean AllowRemoveAttributes)
	{
		if(getOwner() == null)
			return null;

		if(getOwner().isPlayer() && ((L2Player) getOwner()).getPlayerAccess() != null && ((L2Player) getOwner()).getPlayerAccess().BlockInventory)
			return null;

		if(count < 0)
		{
			_log.warning("DropItem: count < 0 owner:" + getOwner().getName());
			return null;
		}

		if(oldItem == null)
		{
			_log.warning("DropItem: item id does not exist in inventory");
			return null;
		}

		Log.LogItem(getOwner(), null, Log.Drop, oldItem, count);

		if(oldItem.getCount() <= count || oldItem.getCount() <= 1)
		{
			Log.add("Inventory|" + getOwner().getName() + "|Drop item|" + oldItem.getItemId() + "|" + count + "|" + oldItem.getObjectId(), "items");
			removeItemFromInventory(oldItem, false, AllowRemoveAttributes);
			refreshWeight();

			// check drop pet controls items
			if(PetDataTable.isPetControlItem(oldItem))
				PetDataTable.unSummonPet(oldItem, getOwner());
			return oldItem;
		}
		oldItem.setCount(oldItem.getCount() - count);
		sendModifyItem(oldItem);
		L2ItemInstance newItem = ItemTemplates.getInstance().createItem(oldItem.getItemId());
		newItem.setCount(count);
		oldItem.updateDatabase();
		refreshWeight();
		Log.add("Inventory|" + getOwner().getName() + "|Split item from-to|" + oldItem.getItemId() + "|" + oldItem.getObjectId() + "|" + newItem.getObjectId(), "items");
		Log.add("Inventory|" + getOwner().getName() + "|Drop item|" + newItem.getItemId() + "|" + count + "|" + newItem.getObjectId(), "items");
		return newItem;
	}

	/**
	 * Refresh the weight of equipment loaded
	 */
	private void refreshWeight()
	{
		int weight = 0;

		for(L2ItemInstance element : getItemsList())
			weight += element.getItem().getWeight() * element.getCount();

		_totalWeight = weight;
		// notify char for overload checking
		if(getOwner().isPlayer())
			((L2Player) getOwner()).refreshOverloaded();
		// Отключено, иначе во время автоматического кормления, мешает писать в чат.
		// При передаче вещей, шлется в другом месте
		//else if(getOwner().isPet)
		//	((L2PetInstance) getOwner()).sendPetInfo();
	}

	public int getTotalWeight()
	{
		return _totalWeight;
	}

	private static final int[][] arrows = {
	//
			{ 17 }, // NG
			{ 1341, 22067 }, // D
			{ 1342, 22068 }, // C
			{ 1343, 22069 }, // B
			{ 1344, 22070 }, // A
			{ 1345, 22071 }, // S
			{ 18550}, // R
	};

	public L2ItemInstance findArrowForBow(L2Item bow)
	{
		int[] arrowsId = arrows[bow.getCrystalType().externalOrdinal];
		L2ItemInstance ret = null;
		for(int id : arrowsId)
			if((ret = getItemByItemId(id)) != null)
				return ret;
		return null;
	}

	private static final int[][] bolts = {
	//
			{ 9632 }, // NG
			{ 9633, 22144 }, // D
			{ 9634, 22145 }, // C
			{ 9635, 22146 }, // B
			{ 9636, 22147 }, // A
			{ 9637, 22148 }, // S
			{ 19443 }, // R
	};

	public L2ItemInstance findArrowForCrossbow(L2Item xbow)
	{
		int[] boltsId = bolts[xbow.getCrystalType().externalOrdinal];
		L2ItemInstance ret = null;
		for(int id : boltsId)
			if((ret = getItemByItemId(id)) != null)
				return ret;
		return null;
	}

	public L2ItemInstance findEquippedLure()
	{
		L2ItemInstance res = null;
		int last_lure = 0;
		if(getOwner() != null && getOwner().isPlayer())
			try
			{
				L2Player owner = (L2Player) getOwner();
				String LastLure = owner.getVar("LastLure");
				if(LastLure != null && !LastLure.isEmpty())
					last_lure = Integer.valueOf(LastLure);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		for(L2ItemInstance temp : getItemsList())
			if(temp.getItemType() == EtcItemType.BAIT)
				if(temp.getLocation() == ItemLocation.PAPERDOLL && temp.getEquipSlot() == PAPERDOLL_LHAND)
					return temp;
				else if(last_lure > 0 && res == null && temp.getObjectId() == last_lure)
					res = temp;
		return res;
	}

	/**
	 * Delete item object from world
	 */
	public synchronized void deleteMe()
	{
		for(L2ItemInstance inst : getItemsList())
		{
			inst.updateInDb();
			inst.deleteMe();
		}
		getItemsList().clear();
	}

	public void updateDatabase(boolean commit)
	{
		updateDatabase(getItemsList(), commit);
	}

	private void updateDatabase(ConcurrentLinkedQueue<L2ItemInstance> items, boolean commit)
	{
		if(getOwner() != null)
			for(L2ItemInstance inst : items)
				inst.updateDatabase(commit, true);
	}

	/**
	 * Функция для валидации вещей в инвентаре. Вызывается при загрузке персонажа.
	 */
	public void validateItems()
	{
		for(L2ItemInstance item : getItemsList())
		{
			if(!getOwner().isPlayer())
				continue;
			L2Player player = getOwner().getPlayer();
			// Clan Apella armor
			if(item.isClanApellaItem() && player.getPledgeClass() < L2Player.RANK_WISEMAN)
				unEquipItem(item);
			else if(item.getItem().isCloak() && item.getName().contains("Knight") && player.getPledgeClass() < L2Player.RANK_KNIGHT)
				unEquipItem(item);
			// Clan Oath Armor
			else if(item.getItemId() >= 7850 && item.getItemId() <= 7859 && player.getLvlJoinedAcademy() == 0)
				unEquipItem(item);
			// Hero Weapons
			else if(item.isHeroWeapon() && !getOwner().isHero())
			{
				unEquipItem(item);
				destroyItem(item, 1, false);
			}
			// Wings of Destiny Circlet
			else if(item.getItemId() == 6842 && !getOwner().isHero())
				unEquipItem(item);
		}
	}

	public void restore()
	{
		final int OWNER = getOwner().getObjectId();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY object_id DESC");
			statement.setInt(1, OWNER);
			statement.setString(2, getBaseLocation().name());
			statement.setString(3, getEquipLocation().name());
			rset = statement.executeQuery();

			L2ItemInstance item, newItem;
			while(rset.next())
			{
				if((item = L2ItemInstance.restoreFromDb(rset, con, true)) == null)
					continue;
				newItem = addItem(item, false, false);
				if(newItem == null)
					continue;
				if(item.isEquipped())
					equipItem(item, false);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARNING, "could not restore inventory for player " + getOwner().getName() + ":", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Refresh all listeners
	 * дергать осторожно, если какой-то предмет дает хп/мп то текущее значение будет сброшено
	 */
	public void refreshListeners()
	{
		setRefreshingListeners(true);
		for(int i = 0; i < _paperdoll.length; i++)
		{
			L2ItemInstance item = getPaperdollItem(i);
			if(item == null)
				continue;
			for(PaperdollListener listener : _paperdollListeners)
			{
				listener.notifyUnequipped(i, item);
				listener.notifyEquipped(i, item);
			}
		}
		if(_listenedItems != null)
			for(L2ItemInstance item : _listenedItems)
				for(PaperdollListener listener : _paperdollListeners)
				{
					listener.notifyUnequipped(-1, item);
					listener.notifyEquipped(-1, item);
				}
		setRefreshingListeners(false);
	}

	public boolean isRefreshingListeners()
	{
		return _refreshingListeners;
	}

	public void setRefreshingListeners(boolean refreshingListeners)
	{
		_refreshingListeners = refreshingListeners;
	}

	/**
	 * Вызывается из RequestSaveInventoryOrder
	 */
	public void sort(int[][] order)
	{
		L2ItemInstance _item;
		ItemLocation _itemloc;
		for(int[] element : order)
		{
			_item = getItemByObjectId(element[0]);
			if(_item == null)
				continue;
			_itemloc = _item.getLocation();
			if(_itemloc != ItemLocation.INVENTORY)
				continue;
			_item.setLocation(_itemloc, element[1]);
		}
	}

	public long getCountOf(int itemId)
	{
		long result = 0;
		for(L2ItemInstance item : getItemsList())
			if(item != null && item.getItemId() == itemId)
				result += item.getCount();
		return result;
	}

	/**
	 * Снимает все вещи, которые нельзя носить.
	 * Применяется при смене саба, захвате замка, выходе из клана.
	 */
	public void checkAllConditions()
	{
		for(L2ItemInstance item : _paperdoll)
			if(item != null && checkConditions(item) != null)
			{
				unEquipItem(item);
				getOwner().getPlayer().sendDisarmMessage(item);
			}
	}

	/**
	 * Проверяет возможность носить эту вещь.
	 */
	private SystemMessage checkConditions(L2ItemInstance item)
	{
		L2Player owner = getOwner().getPlayer();
		int itemId = item.getItemId();
		int targetSlot = item.getItem().getBodyPart();
		L2Clan ownersClan = owner.getClan();

		// Hero items
		if((item.isHeroWeapon() || item.getItemId() == 6842) && !owner.isHero())
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// камаэли и хеви/робы/щиты/сигилы
		if(owner.getRace() == Race.kamael && item.getItem().getItemGrade().ordinal() < Grade.R.ordinal() && (item.getItemType() == ArmorType.HEAVY || item.getItemType() == ArmorType.MAGIC || item.getItemType() == ArmorType.SIGIL || item.getItemType() == WeaponType.NONE && !item.getItem().isCombatFlag()))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// не камаэли и рапиры/арбалеты/древние мечи
		if(owner.getRace() != Race.kamael && item.getItem().getItemGrade().ordinal() < Grade.R.ordinal() && (item.getItemType() == WeaponType.CROSSBOW || item.getItemType() == WeaponType.RAPIER || item.getItemType() == WeaponType.ANCIENTSWORD))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(itemId >= 7850 && itemId <= 7859 && owner.getLvlJoinedAcademy() == 0) // Clan Oath Armor
			return Msg.THIS_ITEM_CAN_ONLY_BE_WORN_BY_A_MEMBER_OF_THE_CLAN_ACADEMY;

		if(item.isClanApellaItem() && owner.getPledgeClass() < L2Player.RANK_WISEMAN)
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(item.getItemType() == WeaponType.DUALDAGGER && !PlayerClass.values()[owner.getActiveClassId()].isOfType(ClassType.DaggerMaster))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(item.getItem() instanceof L2Armor && ((L2Armor) item.getItem()).getClassType() != null && !PlayerClass.values()[owner.getActiveClassId()].isOfType(((L2Armor) item.getItem()).getClassType()))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Замковые короны, доступные для всех членов клана
		if(Arrays.asList(_castleCirclets).contains(itemId) && (ownersClan == null || itemId != _castleCirclets[ownersClan.getHasCastle()]))
			return new SystemMessage(new CustomMessage("l2rt.gameserver.model.Inventory.CircletWorn", owner).addString(CastleManager.getInstance().getCastleByIndex(Arrays.asList(_castleCirclets).indexOf(itemId)).getName()));

		// Корона лидера клана, владеющего замком
		if(itemId == 6841 && (ownersClan == null || !owner.isClanLeader() || ownersClan.getHasCastle() == 0))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Нельзя одевать оружие, если уже одето проклятое оружие. Проверка двумя способами, для надежности. 
		if(targetSlot == L2Item.SLOT_LR_HAND || targetSlot == L2Item.SLOT_L_HAND || targetSlot == L2Item.SLOT_R_HAND)
		{
			if(itemId != getPaperdollItemId(PAPERDOLL_RHAND) && CursedWeaponsManager.getInstance().isCursed(getPaperdollItemId(PAPERDOLL_RHAND)))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			if(owner.isCursedWeaponEquipped() && itemId != owner.getCursedWeaponEquippedId())
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}

		// Плащи
		if(item.getItem().isCloak())
		{
			// Can be worn by Knights or higher ranks who own castle
			if(item.getName().contains("Knight") && (owner.getPledgeClass() < L2Player.RANK_KNIGHT || owner.getCastle() == null))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Плащи для камаэлей
			if(item.getName().contains("Kamael") && owner.getRace() != Race.kamael)
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Плащи можно носить только с S80 или S84 сетом
			boolean cloack_usable = false;
			for(int skill : L2Skill.SKILLS_S80_AND_S84_SETS)
				if(owner.getSkillLevel(skill) > 0)
					cloack_usable = true;
			if(!cloack_usable)
				return Msg.THE_CLOAK_CANNOT_BE_EQUIPPED_BECAUSE_A_NECESSARY_ITEM_IS_NOT_EQUIPPED;
		}

		if(targetSlot == L2Item.SLOT_R_BRACELET)
		{
			// Agathion Seal Bracelet - %Castle%
			if(itemId >= 9607 && itemId <= 9615 && (ownersClan == null || itemId - 9606 != ownersClan.getHasCastle()))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			// Agathion Seal Bracelet - Fortress
			if(itemId == 10018 && (ownersClan == null || ownersClan.getHasFortress() == 0))
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
			//TODO: Agathion Seal Bracelet - Rudolph
			//if((itemId == 10606 || itemId == 10607) && !Chrismas:))
			//{}
			//TODO: 9605/9606 - для нереализованых КХ
		}

		// Для квеста _128_PailakaSongofIceandFire
		// SpritesSword = 13034; EnhancedSpritesSword = 13035; SwordofIceandFire = 13036;
		if((itemId == 13034 || itemId == 13035 || itemId == 13036) && !owner.getReflection().getName().equalsIgnoreCase("Pailaka Song of Ice and Fire"))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Для квеста _129_PailakaDevilsLegacy
		// SWORD = 13042; ENCHSWORD = 13043; LASTSWORD = 13044;
		if((itemId == 13042 || itemId == 13043 || itemId == 13044) && !owner.getReflection().getName().equalsIgnoreCase("Pailaka Devils Legacy"))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		// Для квеста _144_PailakaInjuredDragon
		// SPEAR = 13052; ENCHSPEAR = 13053; LASTSPEAR = 13054;
		if((itemId == 13052 || itemId == 13053 || itemId == 13054) && !owner.getReflection().getName().equalsIgnoreCase("Pailaka Injured Dragon"))
			return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

		if(targetSlot == L2Item.SLOT_DECO)
		{
			// Нельзя одевать талисманы без правого браслета
			if(_paperdoll[PAPERDOLL_RBRACELET] == null)
				return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;

			// Проверяем на количество слотов и одинаковые талисманы
			int talismans_count = 0;
			int max = (int) owner.calcStat(Stats.TALISMANS_LIMIT, 0, null, null);
			for(int slot = PAPERDOLL_DECO1; slot <= PAPERDOLL_DECO6; slot++)
				if(_paperdoll[slot] != null && (_paperdoll[slot].getItemId() == itemId || ++talismans_count >= max))
					return Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM;
		}
		return null;
	}

	private static class ItemOrderComparator implements Comparator<L2ItemInstance>
	{
		@Override
		public int compare(L2ItemInstance o1, L2ItemInstance o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o1.getEquipSlot() - o2.getEquipSlot();
		}
	}

	public static ItemOrderComparator OrderComparator = new ItemOrderComparator();
}