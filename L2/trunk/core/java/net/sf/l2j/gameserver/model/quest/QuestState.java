package net.sf.l2j.gameserver.model.quest;

import java.util.Map;
import java.util.logging.Logger;

import javolution.util.FastMap;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.cache.HtmCache;
import net.sf.l2j.gameserver.instancemanager.QuestManager;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2DropData;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2MonsterInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2NpcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExShowQuestMark;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.QuestList;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml;
import net.sf.l2j.gameserver.network.serverpackets.TutorialEnableClientEvent;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowQuestionMark;
import net.sf.l2j.gameserver.skills.Stats;
import net.sf.l2j.util.Rnd;

public final class QuestState
{
	protected static final Logger _log = Logger.getLogger(Quest.class.getName());
	private final String _questName;
	private final L2PcInstance _player;
	private byte _state;
	private Map<String, String> _vars;
	private boolean _isExitQuestOnCleanUp = false;
	
	QuestState(Quest quest, L2PcInstance player, byte state)
	{
		_questName = quest.getName();
		_player = player;
		getPlayer().setQuestState(this);
		_state = state;
	}
	
	public String getQuestName()
	{
		return _questName;
	}
	
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	public byte getState()
	{
		return _state;
	}
	
	public boolean isCompleted()
	{
		return (getState() == State.COMPLETED);
	}
	
	public boolean isStarted()
	{
		return (getState() == State.STARTED);
	}
	
	public Object setState(byte state)
	{
		if (_state != state)
		{
			_state = state;
			
			Quest.updateQuestInDb(this);
			QuestList ql = new QuestList();
			
			getPlayer().sendPacket(ql);
		}
		return state;
	}
	
	String setInternal(String var, String val)
	{
		if (_vars == null)
			_vars = new FastMap<String, String>();
		
		if (val == null)
			val = "";
		
		_vars.put(var, val);
		return val;
	}
	
	public String set(String var, String val)
	{
		if (_vars == null)
			_vars = new FastMap<String, String>();
		
		if (val == null)
			val = "";
		
		String old = _vars.put(var, val);
		
		if (old != null)
			Quest.updateQuestVarInDb(this, var, val);
		else
			Quest.createQuestVarInDb(this, var, val);
		
		if (var == "cond")
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch (Exception e)
			{
				_log.finer(getPlayer().getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e);
			}
		}
		
		return val;
	}
	
	/**
	 * Internally handles the progression of the quest so that it is ready for sending
	 * appropriate packets to the client<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI>
	 * <LI>If not, check if quest progress steps have been skipped</LI>
	 * <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI>
	 * <LI>If no steps were skipped, flags do not need to be prepared...</LI>
	 * <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not
	 * considered, while skipped steps before the parameter, if any, maintain their info</LI>
	 * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
	 * @param old : int indicating the previously noted step
	 *
	 * For more info on the variable communicating the progress steps to the client, please see
	 * @link net.sf.l2j.loginserver.serverpacket.QuestList
	 */
	private void setCond(int cond, int old)
	{
		int completedStateFlags = 0; // initializing...
		
		// if there is no change since last setting, there is nothing to do here
		if (cond == old)
			return;
		
		// cond 0 and 1 do not need completedStateFlags.  Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped).  So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if (cond < 3 || cond > 31)
		{
			unset("__compltdStateFlags");
		}
		else
			completedStateFlags = getInt("__compltdStateFlags");
		
		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything.  If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...
			
			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > (old + 1))
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;
				
				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= ((1 << old) - 1);
				
				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else
		{
			// if this is a push back to a previous step, clear all completion flags ahead
			if (cond < old)
			{
				completedStateFlags &= ((1 << cond) - 1); // note, this also unsets the flag indicating that there exist skips
				
				//now, check if this resulted in no steps being skipped any more
				if (completedStateFlags == ((1 << cond) - 1))
					unset("__compltdStateFlags");
				else
				{
					// set the most significant bit back to 1 again, to correctly indicate that this skips states.
					// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
					// what the cond says)
					completedStateFlags |= 0x80000001;
					set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}
			}
			// if this moves forward, it changes nothing on previously skipped steps...so just mark this
			// state and we are done
			else
			{
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		
		// send a packet to the client to inform it of the quest progress (step change)
		QuestList ql = new QuestList();
		getPlayer().sendPacket(ql);
		
		int questId = getQuest().getQuestIntId();
		if (questId > 0 && questId < 999 && cond > 0)
			getPlayer().sendPacket(new ExShowQuestMark(questId));
	}
	
	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR><BR>
	 * <U><I>Concept : </I></U>
	 * Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(String var)
	{
		if (_vars == null)
			return null;
		
		String old = _vars.remove(var);
		
		if (old != null)
			Quest.deleteQuestVarInDb(this, var);
		
		return old;
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return Object
	 */
	public Object get(String var)
	{
		if (_vars == null)
			return null;
		
		return _vars.get(var);
	}
	
	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
	public int getInt(String var)
	{
		int varint = 0;
		
		try
		{
			varint = Integer.parseInt(_vars.get(var));
		}
		catch (Exception e)
		{
			_log.finer(getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint + e);
			//	    if (Config.AUTODELETE_INVALID_QUEST_DATA)
			//		exitQuest(true);
		}
		
		return varint;
	}
	
	/**
	 * Add player to get notification of characters death
	 * @param character : L2Character of the character to get notification of death
	 */
	public void addNotifyOfDeath(L2Character character)
	{
		if (character == null)
			return;
		
		character.addNotifyQuestOfDeath(this);
	}
	
	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId : ID of the item wanted to be count
	 * @return int
	 */
	public int getQuestItemsCount(int itemId)
	{
		int count = 0;
		
		if(getPlayer()!=null && getPlayer().getInventory()!=null && getPlayer().getInventory().getItems()!=null)
		{
			for(L2ItemInstance item : getPlayer().getInventory().getItems())
			if(item!=null && item.getItemId() == itemId)
			{
				count += item.getCount();
			}
		}
		
		return count;
	}
	
	/**
	 * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 * @param itemId : ID of the item to check enchantment
	 * @return int
	 */
	public int getEnchantLevel(int itemId)
	{
		L2ItemInstance enchanteditem = getPlayer().getInventory().getItemByItemId(itemId);
		
		if (enchanteditem == null)
			return 0;
		
		return enchanteditem.getEnchantLevel();
	}
	
	/**
	 * Give adena to the player
	 * @param count
	 * @param applyRates
	 */
	public void giveAdena(int count, boolean applyRates)
	{
		giveItems(57, count, applyRates ? 0 : 1);
	}

	/**
	 * Give item/reward to the player
	 * @param itemId
	 * @param count
	 */
	public void giveItems(int itemId, int count)
	{
		giveItems(itemId, count, 0);
	}
	
	public void giveItems(int itemId, int count, int enchantlevel)
	{
		if (count <= 0)
			return;
		
		// If item for reward is adena (ID=57), modify count with rate for quest reward if rates available
		if (itemId == 57 && !(enchantlevel > 0))
			count = (int) (count * Config.RATE_QUESTS_REWARD);
		
		// Add items to player's inventory
		L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());
		
		if (item == null)
			return;

		// set enchant level for item if that item is not adena
		if (enchantlevel > 0 && itemId != 57)
			item.setEnchantLevel(enchantlevel);
		
		if (itemId == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ADENA);
			smsg.addNumber(count);
			getPlayer().sendPacket(smsg);
		}
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item.getItemId());
				smsg.addNumber(count);
				getPlayer().sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item.getItemId());
				getPlayer().sendPacket(smsg);
			}
		}
		getPlayer().sendPacket(new ItemList(getPlayer(), false));
		
		StatusUpdate su = new StatusUpdate(getPlayer().getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, getPlayer().getCurrentLoad());
		getPlayer().sendPacket(su);
	}
	
	public boolean dropQuestItems(int itemId, int count, int neededCount, int dropChance, boolean sound)
	{
		return dropQuestItems(itemId, count, count, neededCount, dropChance, sound);
	}
	
	public boolean dropQuestItems(int itemId, int minCount, int maxCount, int neededCount, int dropChance, boolean sound)
	{
		dropChance *= Config.RATE_DROP_QUEST / ((getPlayer().getParty() != null) ? getPlayer().getParty().getMemberCount() : 1);
		int currentCount = getQuestItemsCount(itemId);
		
		if (neededCount > 0 && currentCount >= neededCount)
			return true;
		
		if (currentCount >= neededCount)
			return true;
		
		int itemCount = 0;
		int random = Rnd.get(L2DropData.MAX_CHANCE);
		
		while (random < dropChance)
		{
			// Get the item quantity dropped
			if (minCount < maxCount)
				itemCount += Rnd.get(minCount, maxCount);
			else if (minCount == maxCount)
				itemCount += minCount;
			else
				itemCount++;
			
			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}
		
		if (itemCount > 0)
		{
			// if over neededCount, just fill the gap
			if (neededCount > 0 && currentCount + itemCount > neededCount)
				itemCount = neededCount - currentCount;
			
			// Inventory slot check
			if (!getPlayer().getInventory().validateCapacityByItemId(itemId))
				return false;
			
			// Give the item to Player
			getPlayer().addItem("Quest", itemId, itemCount, getPlayer().getTarget(), true);
			
			if (sound)
				playSound((currentCount + itemCount < neededCount) ? "Itemsound.quest_itemget" : "Itemsound.quest_middle");
		}
		
		return (neededCount > 0 && currentCount + itemCount >= neededCount);
	}
	
	//TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	public void addRadar(int x, int y, int z)
	{
		getPlayer().getRadar().addMarker(x, y, z);
	}
	
	public void removeRadar(int x, int y, int z)
	{
		getPlayer().getRadar().removeMarker(x, y, z);
	}
	
	public void clearRadar()
	{
		getPlayer().getRadar().removeAllMarkers();
	}
	
	// END STUFF THAT WILL PROBABLY BE CHANGED
	
	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Destroy quantity of items wanted</LI>
	 * <LI>Send new inventory list to player</LI>
	 * @param itemId : Identifier of the item
	 * @param count : Quantity of items to destroy
	 */
	public void takeItems(int itemId, int count)
	{
		// Get object item from player's inventory list
		L2ItemInstance item = getPlayer().getInventory().getItemByItemId(itemId);
		
		if (item == null)
			return;
		
		// Tests on count value in order not to have negative value
		if (count < 0 || count > item.getCount())
			count = item.getCount();
		
		// Destroy the quantity of items wanted
		if (itemId == 57)
			getPlayer().reduceAdena("Quest", count, getPlayer(), true);
		else
			getPlayer().destroyItemByItemId("Quest", itemId, count, getPlayer(), true);
	}
	
	/**
	 * Send a packet in order to play sound at client terminal
	 * @param sound
	 */
	public void playSound(String sound)
	{
		getPlayer().sendPacket(new PlaySound(sound));
	}
	
	/**
	 * Add XP and SP as quest reward
	 * @param exp
	 * @param sp
	 */
	public void addExpAndSp(int exp, int sp)
	{
		getPlayer().addExpAndSp((int) getPlayer().calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUESTS_REWARD, null, null), (int) getPlayer().calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUESTS_REWARD, null, null));
	}
	
	/**
	 * Return random value
	 * @param max : max value for randomisation
	 * @return int
	 */
	public int getRandom(int max)
	{
		return Rnd.get(max);
	}
	
	/**
	 * Return number of ticks from GameTimeController
	 * @return int
	 */
	public int getItemEquipped(int loc)
	{
		return getPlayer().getInventory().getPaperdollItemId(loc);
	}
	
	/**
	 * Return the number of ticks from the GameTimeController
	 * @return int
	 */
	public int getGameTicks()
	{
		return GameTimeController.getGameTicks();
	}
	
	/**
	 * Return true if quest is to exited on clean up by QuestStateManager
	 * @return boolean
	 */
	public final boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}
	
	/**
	 * Return the QuestTimer object with the specified name
	 * @return QuestTimer<BR> Return null if name does not exist
	 */
	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}
	
	/**
	 * Start a timer for quest.<BR><BR>
	 * @param name<BR> The name of the timer. Will also be the value for event of onEvent
	 * @param time<BR> The milisecond value the timer will elapse
	 */
	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), false);
	}
	
	public void startQuestTimer(String name, long time, L2NpcInstance npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), false);
	}
	
	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), true);
	}
	
	public void startRepeatingQuestTimer(String name, long time, L2NpcInstance npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), true);
	}
	
	/**
	 * Return the QuestTimer object with the specified name
	 * @return QuestTimer<BR> Return null if name does not exist
	 */
	public final QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, getPlayer());
	}
	
	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, 0);
	}
	
	public L2NpcInstance addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, despawnDelay);
	}
	
	public L2NpcInstance addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}
	
	/**
	 * Add spawn for player instance
	 * Will despawn after the spawn length expires
	 * Uses player's coords and heading.
	 * Adds a little randomization in the x y coords
	 * Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha, true, 0);
	}
	
	public L2NpcInstance addSpawn(int npcId, L2Character cha, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance
	 * Will despawn after the spawn length expires
	 * Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance
	 * Inherits coords and heading from specified L2Character instance.
	 * It could be either the player, or any killed/attacked mob
	 * Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay);
	}
	
	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}
	
	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2NpcInstance addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn);
	}
	
	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(getPlayer(), fileName);
	}
	
	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 * @return QuestState
	 */
	public QuestState exitQuest(boolean repeatable)
	{
		if (isCompleted())
			return this;
		
		// Say quest is completed
		setState(State.COMPLETED);
		
		// Clean registered quest items
		//3.1.3
		/*int[] itemIdList = getQuest().getRegisteredItemIds();
		if (itemIdList != null)
		{
			for (int i = 0; i < itemIdList.length; i++)
			{
				takeItems(itemIdList[i], -1);
			}
		}
		*/
		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		if (repeatable)
		{
			getPlayer().delQuestState(getQuestName());
			Quest.deleteQuestInDb(this);
			
			_vars = null;
		}
		else
		{
			// Otherwise, delete variables for quest and update database (quest CANNOT be created again => not repeatable)
			if (_vars != null)
				for (String var : _vars.keySet())
					unset(var);
			
			Quest.updateQuestInDb(this);
		}
		
		return this;
	}
	
	public void showQuestionMark(int number)
	{
		getPlayer().sendPacket(new TutorialShowQuestionMark(number));
	}
	
	public void playTutorialVoice(String voice)
	{
		getPlayer().sendPacket(new PlaySound(2, voice, 0, 0, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ()));
	}
	
	public void showTutorialHTML(String html)
	{
		String text = HtmCache.getInstance().getHtm("data/scripts/quests/255_Tutorial/" + html);
		if (text == null)
		{
			_log.warning("missing html page data/scripts/quests/255_Tutorial/" + html);
			text = "<html><body>File data/scripts/quests/255_Tutorial/" + html + " not found or file is empty.</body></html>";
		}
		getPlayer().sendPacket(new TutorialShowHtml(text));
	}
	
	public void closeTutorialHtml()
	{
		getPlayer().sendPacket(new TutorialCloseHtml());
	}
	
	public void onTutorialClientEvent(int number)
	{
		getPlayer().sendPacket(new TutorialEnableClientEvent(number));
	}
	
	public void dropItem(L2MonsterInstance npc, L2PcInstance player, int itemId, int count)
	{
		npc.dropItem(player, itemId, count);
	}
}
