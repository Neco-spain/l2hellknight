package l2rt.gameserver.model;

import l2rt.Config;
import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.cache.Msg;
import l2rt.gameserver.instancemanager.PartyRoomManager;
import l2rt.gameserver.model.L2ObjectTasks.SoulConsumeTask;
import l2rt.gameserver.model.base.Experience;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.items.L2ItemInstance;
import l2rt.gameserver.network.serverpackets.*;
import l2rt.gameserver.skills.Stats;
import l2rt.gameserver.tables.ReflectionTable;
import l2rt.gameserver.xml.ItemTemplates;
import l2rt.util.*;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;

public class L2Party
{
	private final Vector<Long> members_list = new Vector<Long>(7);
	public FastMap<Integer, Integer> icons = new FastMap<Integer, Integer>().setShared(true);
	private int _partyLvl = 0;
	private int _itemDistribution = 0;
	private int _itemOrder = 0;
	private long _dr;
	private long _reflection;
	private L2CommandChannel _commandChannel;

	public static final int ITEM_LOOTER = 0;
	public static final int ITEM_RANDOM = 1;
	public static final int ITEM_RANDOM_SPOIL = 2;
	public static final int ITEM_ORDER = 3;
	public static final int ITEM_ORDER_SPOIL = 4;

	public static final int MAX_SIZE = 7;

	public float _rateExp;
	public float _rateSp;
	public float _rateDrop;
	public float _rateAdena;
	public float _rateSpoil;
	private final UpdatePositionTask posTask = new UpdatePositionTask(this);
	private ScheduledFuture<?> posTaskThread;
    private int newLootDistr;
    private boolean votingChangeLoot;
    private GCSArray<L2Player> lootResponsers = new GCSArray<L2Player>();
    private static final int[] LOOT_SYSSTRINGS = {487, 488, 798, 799, 800};
    private ScheduledFuture<?> voteTask;

    /**
	 * constructor ensures party has always one member - leader
	 * @param leader создатель парти
	 * @param itemDistribution режим распределения лута
	 */
	public L2Party(L2Player leader, int itemDistribution)
	{
		_itemDistribution = itemDistribution;
		members_list.add(leader.getStoredId());
		_partyLvl = leader.getLevel();
		posTaskThread = ThreadPoolManager.getInstance().scheduleGeneral(posTask, 11000);

		// для надежности
		_rateExp = leader.getBonus().RATE_XP;
		_rateSp = leader.getBonus().RATE_SP;
		_rateAdena = leader.getBonus().RATE_DROP_ADENA;
		_rateDrop = leader.getBonus().RATE_DROP_ITEMS;
		_rateSpoil = leader.getBonus().RATE_DROP_SPOIL;
	}

	/**
	 * @return number of party members
	 */
	public int getMemberCount()
	{
		return members_list.size();
	}
	
	public void IconAdd(int key, int b)
	{
		icons.put(b, key);
	}
	
	public void Icon()
	{
		synchronized (members_list)
		{
			L2Player member;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					for (int a : icons.keySet()) 
						if (icons.get(a) != null)
							member.sendPacket(new ExTacticalSign(icons.get(a), a));
		}
	}
	
	public void IconDelPlayer(L2Player player)
	{
		for (int a : icons.keySet())
			if (icons.get(a) != null)
				player.sendPacket(new ExTacticalSign(icons.get(a), -1));	
	}

	public int getMemberCountInRange(L2Player player, int range)
	{
		int ret = 0;
		L2Player member;

		synchronized (members_list)
		{
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null && (member == player || member.getReflectionId() == player.getReflectionId() && member.isInRange(player, range)))
					ret++;
		}
		return ret;
	}

	/**
	 * @return all party members
	 */
	public GArray<L2Player> getPartyMembers()
	{
		synchronized (members_list)
		{
			GArray<L2Player> result = new GArray<L2Player>(members_list.size());
			L2Player member;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					result.add(member);
			return result;
		}
	}

	public GArray<Integer> getPartyMembersObjIds()
	{
		synchronized (members_list)
		{
			GArray<Integer> result = new GArray<Integer>(members_list.size());
			for(Long storedId : members_list)
				result.add(L2ObjectsStorage.getStoredObjectId(storedId));
			return result;
		}
	}

	public GArray<L2Playable> getPartyMembersWithPets()
	{
		synchronized (members_list)
		{
			GArray<L2Playable> result = new GArray<L2Playable>(members_list.size());
			L2Player member;
			L2Summon member_pet;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				{
					result.add(member);
					if((member_pet = member.getPet()) != null)
						result.add(member_pet);
				}
			return result;
		}
	}

	public L2Player getRandomMember()
	{
		GArray<L2Player> members = getPartyMembers();
		return members.get(Rnd.get(members.size()));
	}

	/**
	 * @return random member from party
	 */
	private L2Player getRandomMemberInRange(L2Player player, L2ItemInstance item, int range)
	{
		GArray<L2Player> ret = new GArray<L2Player>();

		synchronized (members_list)
		{
			L2Player member;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					if(member.getReflectionId() == player.getReflectionId() && member.isInRange(player, range) && !member.isDead() && member.getInventory().validateCapacity(item) && member.getInventory().validateWeight(item))
						ret.add(member);
		}

		return ret.isEmpty() ? null : ret.get(Rnd.get(ret.size()));
	}

	/**
	 * @return next item looter
	 */
	private L2Player getNextLooterInRange(L2Player player, L2ItemInstance item, int range)
	{
		synchronized (members_list)
		{
			int antiloop = members_list.size();
			while(--antiloop > 0)
			{
				int looter = _itemOrder;
				_itemOrder++;
				if(_itemOrder > members_list.size() - 1)
					_itemOrder = 0;

				L2Player ret = looter < members_list.size() ? L2ObjectsStorage.getAsPlayer(members_list.get(looter)) : player;

				if(ret != null && ret.getReflectionId() == player.getReflectionId() && ret.isInRange(player, range) && !ret.isDead())
					return ret;
			}
			return player;
		}
	}

	/**
	 * true if player is party leader
	 */
	public boolean isLeader(L2Player player)
	{
		L2Player leader = getPartyLeader();
		return leader != null && player.equals(leader);
	}

	/**
	 * Returns the Object ID for the party leader to be used as a unique identifier of this party
	 */
	public int getPartyLeaderOID()
	{
		synchronized (members_list)
		{
			if(members_list.size() == 0)
				return 0;
			return L2ObjectsStorage.getStoredObjectId(members_list.get(0));
		}
	}

	/**
	 * Возвращает лидера партии
	 * @return L2Player Лидер партии
	 */
	public L2Player getPartyLeader()
	{
		if(members_list.size() == 0)
			return null;
		return L2ObjectsStorage.getAsPlayer(members_list.get(0));
	}

	/**
	 * Broadcasts packet to every party member
	 * @param msg packet to broadcast
	 */
	public void broadcastToPartyMembers(L2GameServerPacket... msg)
	{
		synchronized (members_list)
		{
			L2Player member;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					member.sendPacket(msg);
		}
	}

	/**
	 * Рассылает текстовое сообщение всем членам группы
	 * @param msg сообщение
	 */
	public void broadcastMessageToPartyMembers(String msg)
	{
		broadcastToPartyMembers(new SystemMessage(msg));
	}

	/**
	 * Рассылает пакет всем членам группы исключая указанного персонажа<BR><BR>
	 */
	public void broadcastToPartyMembers(L2Player exclude, L2GameServerPacket msg)
	{
		synchronized (members_list)
		{
			L2Player member;
			for(Long storedId : members_list)
				if(exclude.getStoredId() != storedId && (member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					member.sendPacket(msg);
		}
	}

	public void broadcastToPartyMembersInRange(L2Player player, L2GameServerPacket msg, int range)
	{
		synchronized (members_list)
		{
			L2Player member;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null && member.getReflectionId() == player.getReflectionId() && player.isInRange(member, range))
					member.sendPacket(msg);
		}
	}

	public boolean containsMember(L2Character cha)
	{
		L2Player player = cha.getPlayer();
		if(player == null)
			return false;

		synchronized (members_list)
		{
			int player_id = player.getObjectId();
			for(Long storedId : members_list)
				if(L2ObjectsStorage.getStoredObjectId(storedId) == player_id)
					return true;
		}

		return false;
	}

	/**
	 * adds new member to party
	 * @param player L2Player to add
	 */
	public void addPartyMember(L2Player player)
	{
		L2Player leader = getPartyLeader();
		if(leader == null)
			return;

		L2Player member;
		L2Summon player_pet, member_pet;
		Collection<L2GameServerPacket> pmember, pmember_proto = new GArray<L2GameServerPacket>(), pplayer = new GArray<L2GameServerPacket>();

		synchronized (members_list)
		{
			members_list.add(player.getStoredId());

			//sends new member party window for all members
			//we do all actions before adding member to a list, this speeds things up a little
			pplayer.add(new PartySmallWindowAll(this, player));
			pplayer.add(new SystemMessage(SystemMessage.YOU_HAVE_JOINED_S1S_PARTY).addString(leader.getName()));

			pmember_proto.add(new SystemMessage(SystemMessage.S1_HAS_JOINED_THE_PARTY).addString(player.getName()));
			pmember_proto.add(new PartySmallWindowAdd(player));
			pmember_proto.add(new PartySpelled(player, true));
			if((player_pet = player.getPet()) != null)
			{
				pmember_proto.add(new ExPartyPetWindowAdd(player_pet));
				pmember_proto.add(new PartySpelled(player_pet, true));
			}

			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null && member != player)
				{
					pmember = new GArray<L2GameServerPacket>();
					pmember.addAll(pmember_proto);
					pmember.addAll(RelationChanged.update(member, player, member));
					member.sendPackets(pmember);
					pmember = null;

					pplayer.add(new PartySpelled(member, true));
					if((member_pet = member.getPet()) != null)
						pplayer.add(new PartySpelled(member_pet, true));
					pplayer.addAll(RelationChanged.update(player, member, player)); //FIXME
				}

			// Если партия уже в СС, то вновь прибывшем посылаем пакет открытия окна СС
			if(isInCommandChannel())
				pplayer.add(Msg.ExMPCCOpen);
		}

		player.sendPackets(pplayer);
		pplayer = null;
		pmember_proto = null;

		recalculatePartyData();

		if(player.getPartyRoom() > 0)
		{
			PartyRoom room = PartyRoomManager.getInstance().getRooms().get(player.getPartyRoom());
			if(room != null)
				room.updateInfo();
		}
		Icon();
	}

	/**
	 * Удаляет все связи
	 */
	public void dissolveParty()
	{
		synchronized (members_list)
		{
			for(L2Player p : getPartyMembers()) {
				p.setParty(null);
				IconDelPlayer(p);
			}
			members_list.clear();
		}
		_commandChannel = null;
		posTaskThread.cancel(false);
		//IconDel();
	}

	/**
	 * removes player from party
	 * @param player L2Player to remove
	 */
	private void removePartyMember(L2Player player)
	{
		synchronized (members_list)
		{
			members_list.remove(player.getStoredId());
			posTask.remove(player);
		}

		recalculatePartyData();

		Collection<L2GameServerPacket> pplayer = new GArray<L2GameServerPacket>();

		// Отсылаемы вышедшему пакет закрытия СС
		if(isInCommandChannel())
			pplayer.add(Msg.ExMPCCClose);

		pplayer.add(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY);
		pplayer.add(Msg.PartySmallWindowDeleteAll);
		player.setParty(null);
		
		player.sendPacket(new ExTacticalSign(0, 0)); //wtf?

		L2Summon player_pet;
		Collection<L2GameServerPacket> pmember_proto = new GArray<L2GameServerPacket>();
		if((player_pet = player.getPet()) != null)
			pmember_proto.add(new ExPartyPetWindowDelete(player_pet));
		pmember_proto.add(new PartySmallWindowDelete(player));
		pmember_proto.add(new SystemMessage(SystemMessage.S1_HAS_LEFT_THE_PARTY).addString(player.getName()));

		synchronized (members_list)
		{
			L2Player member;
			Collection<L2GameServerPacket> pmember;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				{
					pmember = new GArray<L2GameServerPacket>();
					pmember.addAll(pmember_proto);
					pmember.addAll(RelationChanged.update(member, player, member));
					member.sendPackets(pmember);
					pmember = null;
					pplayer.addAll(RelationChanged.update(player, member, player));
				}
		}

		player.sendPackets(pplayer);
		pplayer = null;

		Reflection reflection = getReflection();

		/*if(reflection != null && player.getReflection().getId() == reflection.getId() && reflection.getReturnLoc() != null)
			player.teleToLocation(reflection.getReturnLoc(), 0);*/ //Не должно кикать из рефлекта при добровольном выходе из пати.

		if(player.getDuel() != null)
			player.getDuel().onRemoveFromParty(player);

		L2Player leader = getPartyLeader();

		if(members_list.size() == 1 || leader == null)
		{
			if(leader != null && leader.getDuel() != null)
				leader.getDuel().onRemoveFromParty(leader);

			// Если в партии остался 1 человек, то удаляем ее из СС
			if(isInCommandChannel())
				_commandChannel.removeParty(this);
			else if(reflection != null)
			{
				//lastMember.teleToLocation(getReflection().getReturnLoc(), 0);
				//getReflection().stopCollapseTimer();
				//getReflection().collapse();

				if(reflection.getParty() == this) // TODO: убрать затычку
					reflection.startCollapseTimer(60000);
				if(leader != null && leader.getReflection().getId() == reflection.getId())
					leader.broadcastPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(1));

				setReflection(null);
			}

			if(leader != null)
				leader.setParty(null);

			dissolveParty();
		}
		else if(isInCommandChannel() && _commandChannel.getChannelLeader() == player)
			_commandChannel.setChannelLeader(leader);

		if(player.getPartyRoom() > 0)
		{
			PartyRoom room = PartyRoomManager.getInstance().getRooms().get(player.getPartyRoom());
			if(room != null)
				room.updateInfo();
		}
		
		IconDelPlayer(player);
	}

	/**
	 * Change party leader (used for string arguments)
	 * @param name имя нового лидера парти
	 */
	public void changePartyLeader(String name)
	{
		L2Player new_leader = getPlayerByName(name);

		synchronized (members_list)
		{
			L2Player current_leader = getPartyLeader();

			if(new_leader == null || current_leader == null)
				return;

			if(current_leader.equals(new_leader))
			{
				current_leader.sendPacket(Msg.YOU_CANNOT_TRANSFER_RIGHTS_TO_YOURSELF);
				return;
			}

			if(!members_list.contains(new_leader.getStoredId()))
			{
				current_leader.sendPacket(Msg.YOU_CAN_TRANSFER_RIGHTS_ONLY_TO_ANOTHER_PARTY_MEMBER);
				return;
			}

			// Меняем местами нового и текущего лидера
			int idx = members_list.indexOf(new_leader.getStoredId());
			members_list.set(0, new_leader.getStoredId());
			members_list.set(idx, current_leader.getStoredId());

			updateLeaderInfo();

			if(isInCommandChannel() && _commandChannel.getChannelLeader() == current_leader)
				_commandChannel.setChannelLeader(new_leader);
		}
	}

	public void updateLeaderInfo()
	{
		synchronized (members_list)
		{
			L2Player member = getPartyLeader();
			if(member == null)
				return;
			SystemMessage msg = new SystemMessage(SystemMessage.S1_HAS_BECOME_A_PARTY_LEADER).addString(member.getName());
			for(Long storedId : members_list)
				// индивидуальные пакеты - удаления и инициализация пати
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
					member.sendPacket(Msg.PartySmallWindowDeleteAll, // Удаляем все окошки
					new PartySmallWindowAll(this, member), // Показываем окошки
					msg); // Сообщаем о смене лидера
			for(Long storedId : members_list)
				// броадкасты состояний
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				{
					broadcastToPartyMembers(member, new PartySpelled(member, true)); // Показываем иконки
					if(member.getPet() != null)
						broadcastToPartyMembers(new ExPartyPetWindowAdd(member.getPet())); // Показываем окошки петов
					// broadcastToPartyMembers(member, new PartyMemberPosition(member)); // Обновляем позицию на карте
				}
			posTask.lastpositions.clear();
		}
	}

	/**
	 * finds a player in the party by name
	 * @param name имя для поиска
	 * @return найденый L2Player или null если не найдено
	 */
	public L2Player getPlayerByName(String name)
	{
		synchronized (members_list)
		{
			L2Player member;
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null && name.equalsIgnoreCase(member.getName()))
					return member;
		}
		return null;
	}

	/**
	 * Oust player from party
	 * @param player L2Player которого выгоняют
	 */
	public void oustPartyMember(L2Player player)
	{
		synchronized (members_list)
		{
			if(player == null || !members_list.contains(player.getStoredId()))
				return;
		}

		boolean leader = isLeader(player);

		removePartyMember(player);

		if(leader && members_list.size() > 1)
			updateLeaderInfo();
	}

	/**
	 * Oust player from party Overloaded method that takes player's name as
	 * parameter
	 *
	 * @param name имя игрока для изгнания
	 */
	public void oustPartyMember(String name)
	{
		oustPartyMember(getPlayerByName(name));
	}

	/**
	 * distribute item(s) to party members
	 * @param player
	 * @param item
	 */
	public void distributeItem(L2Player player, L2ItemInstance item)
	{
		distributeItem(player, item, null);
	}

	public void distributeItem(L2Player player, L2ItemInstance item, L2NpcInstance fromNpc)
	{
		L2Player target = player;

		switch(_itemDistribution)
		{
			case ITEM_RANDOM:
			case ITEM_RANDOM_SPOIL:
				target = getRandomMemberInRange(player, item, Config.ALT_PARTY_DISTRIBUTION_RANGE);
				break;
			case ITEM_ORDER:
			case ITEM_ORDER_SPOIL:
				target = getNextLooterInRange(player, item, Config.ALT_PARTY_DISTRIBUTION_RANGE);
				break;
			case ITEM_LOOTER:
			default:
				target = player;
				break;
		}

		if(target == null)
		{
			item.dropToTheGround(player, fromNpc);
			return;
		}

		if(!target.getInventory().validateWeight(item))
		{
			target.sendPacket(Msg.ActionFail, Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			item.dropToTheGround(target, fromNpc);
			return;
		}

		if(!target.getInventory().validateCapacity(item))
		{
			target.sendPacket(Msg.ActionFail, Msg.YOUR_INVENTORY_IS_FULL);
			item.dropToTheGround(player, fromNpc);
			return;
		}

		if(!item.pickupMe(target))
			return;

		target.sendPacket(SystemMessage.obtainItems(item));
		broadcastToPartyMembers(target, SystemMessage.obtainItemsBy(item, target.getName()));

		L2ItemInstance item2 = target.getInventory().addItem(item);
		Log.LogItem(target, fromNpc, Log.GetItemInPaty, item2);

		target.sendChanges();
	}

	/**
	 * distribute adena to party members
	 * @param adena инстанс адены для распределения
	 */
	public void distributeAdena(L2ItemInstance adena, L2Player player)
	{
		distributeAdena(adena, null, player);
	}

	public void distributeAdena(L2ItemInstance adena, L2NpcInstance fromNpc, L2Player player)
	{
		if(player == null)
			return;

		GArray<L2Player> membersInRange = new GArray<L2Player>();

		synchronized (members_list)
		{
			if(adena.getCount() < members_list.size())
				membersInRange.add(player);
			else
			{
				L2Player member;
				for(Long storedId : members_list)
					if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
						if(member.equals(player) || player.isInRange(member, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !member.isDead())
							membersInRange.add(member);
			}
		}

		if(membersInRange.isEmpty())
			membersInRange.add(player);

		long totalAdena = adena.getCount();
		long amount = totalAdena / membersInRange.size();
		long ost = totalAdena % membersInRange.size();

		for(L2Player member : membersInRange)
		{
			L2ItemInstance newAdena = ItemTemplates.getInstance().createItem(57);
			newAdena.setCount(member.equals(player) ? amount + ost : amount);
			member.sendPacket(SystemMessage.obtainItems(newAdena));

			L2ItemInstance item2 = member.getInventory().addItem(newAdena);
			if(fromNpc == null)
				Log.LogItem(member, Log.GetItemInPaty, item2);
			else
				Log.LogItem(member, fromNpc, Log.GetItemInPaty, item2);
		}
	}

	public void distributeXpAndSp(double xpReward, double spReward, GArray<L2Player> rewardedMembers, L2Character lastAttacker, L2MonsterInstance monster)
	{
		recalculatePartyData();

		GArray<L2Player> mtr = new GArray<L2Player>();
		int minPartyLevel = lastAttacker.getLevel();
		int maxPartyLevel = lastAttacker.getLevel();
		double partyLvlSum = 0;

		// считаем минимальный/максимальный уровень
		for(L2Player member : rewardedMembers)
		{
			if(!lastAttacker.isInRange(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			minPartyLevel = Math.min(minPartyLevel, member.getLevel());
			maxPartyLevel = Math.max(maxPartyLevel, member.getLevel());
		}

		// составляем список игроков, удовлетворяющих требованиям
		for(L2Player member : rewardedMembers)
		{
			if(!lastAttacker.isInRange(member, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				continue;
			if(member.getLevel() < maxPartyLevel - 20)
				continue;
			partyLvlSum += member.getLevel();
			mtr.add(member);
		}

		if(mtr.size() == 0)
			return;

		// бонус за пати
		double bonus = Config.ALT_PARTY_BONUS[mtr.size() - 1];

		// количество эксп и сп для раздачи на всех
		double XP = xpReward * bonus;
		double SP = spReward * bonus;

		for(L2Player member : mtr)
		{
			double lvlPenalty = Experience.penaltyModifier(monster.calculateLevelDiffForDrop(member.getLevel()), 9);

			// отдаем его часть с учетом пенальти
			double memberXp = XP * lvlPenalty * member.getLevel() / partyLvlSum;
			double memberSp = SP * lvlPenalty * member.getLevel() / partyLvlSum;

			// больше чем соло не дадут
			memberXp = Math.min(memberXp, xpReward);
			memberSp = Math.min(memberSp, spReward);

			// Начисление душ камаэлянам
			double neededExp = member.calcStat(Stats.SOULS_CONSUME_EXP, 0, monster, null);
			if(neededExp > 0 && memberXp > neededExp)
			{
				monster.broadcastPacket(new SpawnEmitter(monster, member));
				ThreadPoolManager.getInstance().scheduleGeneral(new SoulConsumeTask(member), 1000);
			}

			double[] xpsp = member.applyVitality(monster, memberXp, memberSp, memberXp / xpReward);

			member.addExpAndSp((long) xpsp[0], (long) xpsp[1], false, true);
		}

		recalculatePartyData();
	}

	public void recalculatePartyData()
	{
		_partyLvl = 0;
		float rateExp = 0;
		float rateSp = 0;
		float rateDrop = 0;
		float rateAdena = 0;
		float rateSpoil = 0;
		float minRateExp = Float.MAX_VALUE;
		float minRateSp = Float.MAX_VALUE;
		float minRateDrop = Float.MAX_VALUE;
		float minRateAdena = Float.MAX_VALUE;
		float minRateSpoil = Float.MAX_VALUE;
		byte count = 0;
		L2Player member;
		synchronized (members_list)
		{
			for(Long storedId : members_list)
				if((member = L2ObjectsStorage.getAsPlayer(storedId)) != null)
				{
					int level = member.getLevel();
					_partyLvl = Math.max(_partyLvl, level);
					count++;

					rateExp += member.getBonus().RATE_XP;
					rateSp += member.getBonus().RATE_SP;
					rateDrop += member.getBonus().RATE_DROP_ITEMS;
					rateAdena += member.getBonus().RATE_DROP_ADENA;
					rateSpoil += member.getBonus().RATE_DROP_SPOIL;

					minRateExp = Math.min(minRateExp, member.getBonus().RATE_XP);
					minRateSp = Math.min(minRateSp, member.getBonus().RATE_SP);
					minRateDrop = Math.min(minRateDrop, member.getBonus().RATE_DROP_ITEMS);
					minRateAdena = Math.min(minRateAdena, member.getBonus().RATE_DROP_ADENA);
					minRateSpoil = Math.min(minRateSpoil, member.getBonus().RATE_DROP_SPOIL);
				}
		}
		_rateExp = Config.RATE_PARTY_MIN ? minRateExp : rateExp / count;
		_rateSp = Config.RATE_PARTY_MIN ? minRateSp : rateSp / count;
		_rateDrop = Config.RATE_PARTY_MIN ? minRateDrop : rateDrop / count;
		_rateAdena = Config.RATE_PARTY_MIN ? minRateAdena : rateAdena / count;
		_rateSpoil = Config.RATE_PARTY_MIN ? minRateSpoil : rateSpoil / count;
	}

	public int getLevel()
	{
		return _partyLvl;
	}

	public int getLootDistribution()
	{
		return _itemDistribution;
	}

	public boolean isDistributeSpoilLoot()
	{
		boolean rv = false;

		if(_itemDistribution == ITEM_RANDOM_SPOIL || _itemDistribution == ITEM_ORDER_SPOIL)
			rv = true;

		return rv;
	}

	public boolean isInReflection()
	{
		if(_reflection > 0)
			return true;
		if(_commandChannel != null)
			return _commandChannel.isInReflection();
		return false;
	}

	public void setReflection(Reflection reflection)
	{
		_reflection = reflection == null ? 0 : reflection.getId();
	}

	public Reflection getReflection()
	{
		if(_reflection > 0)
			return ReflectionTable.getInstance().get(_reflection);
		if(_commandChannel != null)
			return _commandChannel.getReflection();
		return null;
	}

	public boolean isInCommandChannel()
	{
		return _commandChannel != null;
	}

	public L2CommandChannel getCommandChannel()
	{
		return _commandChannel;
	}

	public void setCommandChannel(L2CommandChannel channel)
	{
		_commandChannel = channel;
	}

	/**
	 * Телепорт всей пати в одну точку (x,y,z)
	 */
	public void Teleport(int x, int y, int z)
	{
		TeleportParty(getPartyMembers(), new Location(x, y, z));
	}

	/**
	 * Телепорт всей пати в одну точку dest
	 */
	public void Teleport(Location dest)
	{
		TeleportParty(getPartyMembers(), dest);
	}

	/**
	 * Телепорт всей пати на территорию, игроки расставляются рандомно по территории
	 */
	public void Teleport(L2Territory territory)
	{
		RandomTeleportParty(getPartyMembers(), territory);
	}

	/**
	 * Телепорт всей пати на территорию, лидер попадает в точку dest, а все остальные относительно лидера
	 */
	public void Teleport(L2Territory territory, Location dest)
	{
		TeleportParty(getPartyMembers(), territory, dest);
	}

	public static void TeleportParty(GArray<L2Player> members, Location dest)
	{
		for(L2Player _member : members)
		{
			if(_member == null)
				continue;
			_member.teleToLocation(dest);
		}
	}

	public static void TeleportParty(GArray<L2Player> members, L2Territory territory, Location dest)
	{
		if(!territory.isInside(dest.x, dest.y))
		{
			Log.add("TeleportParty: dest is out of territory", "errors");
			Thread.dumpStack();
			return;
		}
		int base_x = members.get(0).getX();
		int base_y = members.get(0).getY();

		for(L2Player _member : members)
		{
			if(_member == null)
				continue;
			int diff_x = _member.getX() - base_x;
			int diff_y = _member.getY() - base_y;
			Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
			while(!territory.isInside(loc.x, loc.y))
			{
				diff_x = loc.x - dest.x;
				diff_y = loc.y - dest.y;
				if(diff_x != 0)
					loc.x -= diff_x / Math.abs(diff_x);
				if(diff_y != 0)
					loc.y -= diff_y / Math.abs(diff_y);
			}
			_member.teleToLocation(loc);
		}
	}

	public static void RandomTeleportParty(GArray<L2Player> members, L2Territory territory)
	{
		for(L2Player _member : members)
		{
			int[] _loc = territory.getRandomPoint();
			if(_member == null || _loc == null)
				continue;
			_member.teleToLocation(_loc[0], _loc[1], _loc[2]);
		}
	}


    public void requestLootModification(int mode) {
        newLootDistr = mode;
        votingChangeLoot = true;
        //формируем список из тех, от кого будем ждать ответ, и заодно рассылаем всем запрос.
        for (L2Player player : getPartyMembers()) {
            if (player != getPartyLeader()) {
                lootResponsers.add(player);
                player.sendPacket(new ExAskModifyPartyLooting(getPartyLeader().getName(), newLootDistr));
            }
        }
      //  getPartyLeader().sendPacket(new SystemMessage(3135).addSystemString(LOOT_SYSSTRINGS[newLootDistr]));
        if (voteTask != null)
            voteTask.cancel(true);
        voteTask = ThreadPoolManager.getInstance().scheduleGeneral(new VoteLootChangeTask(), 10000);

    }
    
    private void setItemDistribution(boolean forse) {
        if (_itemDistribution == newLootDistr)
            return;
        if (forse) {//кто то дал отрицательный ответ
            if (voteTask != null)
                voteTask.cancel(true);
            voteTask = null;
            newLootDistr = _itemDistribution;
            broadcastToPartyMembers(new ExSetPartyLooting(_itemDistribution, 0));
            broadcastToPartyMembers(new SystemMessage(3137));
            return;
        } else if (lootResponsers.size() > 0) {
            newLootDistr = _itemDistribution;
            broadcastToPartyMembers(new ExSetPartyLooting(_itemDistribution, 0));
            broadcastToPartyMembers(new SystemMessage(3137));
            return;
        }
        lootResponsers = new GCSArray<L2Player>();
        _itemDistribution = newLootDistr;
        getPartyLeader().sendPacket(new ExSetPartyLooting(_itemDistribution, 1));
   //     broadcastToPartyMembers(new SystemMessage(3138).addString(LOOT_SYSSTRINGS[_itemDistribution]));
    }

    public void answerLootModification(L2Player member, int answer) {
        if (lootResponsers.contains(member) && answer == 1) {
            lootResponsers.remove(member);
            if (lootResponsers.size() == 0)
                setItemDistribution(false);
        } else
            setItemDistribution(true);
    }

    class VoteLootChangeTask implements Runnable {
        @Override
        public void run() {
            L2Party.this.setItemDistribution(false);
        }
    }

    private class UpdatePositionTask implements Runnable
	{
		private final WeakReference<L2Party> party_ref;
		private final HashMap<Integer, int[]> lastpositions = new HashMap<Integer, int[]>();

		public UpdatePositionTask(L2Party party)
		{
			party_ref = new WeakReference<L2Party>(party);
		}

		public void remove(L2Player player)
		{
			synchronized (lastpositions)
			{
				lastpositions.remove(new Integer(player.getObjectId()));
			}
		}

		public void run()
		{
			L2Party party = party_ref.get();
			if(party == null || party.getMemberCount() < 2)
			{
				synchronized (lastpositions)
				{
					lastpositions.clear();
				}
				party_ref.clear();
				dissolveParty();
				return;
			}
			try
			{
				GArray<L2Player> full_updated = new GArray<L2Player>();
				GArray<L2Player> members = party.getPartyMembers();
				PartyMemberPosition just_updated = new PartyMemberPosition();
				int[] lastpos;
				for(L2Player member : members)
				{
					if(member == null)
						continue;
					synchronized (lastpositions)
					{
						lastpos = lastpositions.get(new Integer(member.getObjectId()));
						if(lastpos == null)
						{
							just_updated.add(member);
							full_updated.add(member);
							lastpositions.put(member.getObjectId(), new int[] { member.getX(), member.getY(), member.getZ() });
						}
						else if(member.getDistance(lastpos[0], lastpos[1], lastpos[2]) > 256) //TODO подкорректировать
						{
							just_updated.add(member);
							lastpos[0] = member.getX();
							lastpos[1] = member.getY();
							lastpos[2] = member.getZ();
						}
					}
				}

				// посылаем изменения позиций старым членам пати
				if(just_updated.size() > 0)
					for(L2Player member : members)
						if(!full_updated.contains(member))
							member.sendPacket(just_updated);

				// посылаем полный список позиций новым членам пати
				if(full_updated.size() > 0)
				{
					just_updated = new PartyMemberPosition().add(members);
					for(L2Player member : full_updated)
						member.sendPacket(just_updated);
					full_updated.clear();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				posTaskThread = ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
			}
		}
	}
}