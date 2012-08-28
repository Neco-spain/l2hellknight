package net.sf.l2j.gameserver.handler.itemhandlers;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.SkillTable;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PetInstance;
import net.sf.l2j.gameserver.model.actor.instance.L2PlayableInstance;
import net.sf.l2j.gameserver.model.entity.TvTEvent;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Potions implements IItemHandler
{
	protected static final Logger _log = Logger.getLogger(Potions.class.getName());
	private int _herbstask = 0;

	/** Task for Herbs */
	private class HerbTask implements Runnable
	{
		private L2PcInstance _activeChar;
		private int _magicId;
		private int _level;
		HerbTask(L2PcInstance activeChar, int magicId, int level)
		{
			_activeChar = activeChar;
			_magicId = magicId;
			_level = level;
		}
		public void run()
		{
			try
			{
				usePotion(_activeChar, _magicId, _level);
			}
			catch (Throwable t)
			{
				_log.log(Level.WARNING, "", t);
			}
		}
	}
	private static final int[] ITEM_IDS =
		{
			6652, 6553, 6554, 6555,
			8193, 8194, 8195, 8196, 8197, 8198, 8199, 8200, 8201, 8202,
			8600, 8601, 8602, 8603, 8604, 8605, 8606, 8607, 8608, 8609,
			8610, 8611, 8612, 8613, 8614,
			5283, 8786, 4679, 8787, 6319
		};

	public synchronized void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		boolean res = false;
		if (playable instanceof L2PcInstance)
			activeChar = (L2PcInstance) playable;
		else if (playable instanceof L2PetInstance)
			activeChar = ((L2PetInstance) playable).getOwner();
		else
			return;

		if ((TvTEvent.isStarted()) && (TvTEvent.isPlayerParticipant(activeChar.getObjectId())) && (!Config.TVT_EVENT_POTIONS_ALLOWED))
	    {
	      activeChar.sendPacket(new ActionFailed());
	      return;
	    }

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}

		if (activeChar.isAllSkillsDisabled())
		{
			ActionFailed af = new ActionFailed();
			activeChar.sendPacket(af);
			return;
		}

		int itemId = item.getItemId();

		switch(itemId)
		{
			case 6319: // Mimir's Elixir
			{
				res = usePotion(activeChar, 4339, 1);
				if(activeChar.getQuestState("235_MimirsElixir") != null) 
					activeChar.getQuestState("235_MimirsElixir").exitQuest(true);
				break;
			}
			case 4679: // Bless of Eva
				res = usePotion(activeChar, 2076, 1);
				break;
			case 5283: // Rice Cake
				res = usePotion(activeChar, 2136, 1);
				break;
			case 8786: // Primeval Potion
				res = usePotion(activeChar, 2305, 1);
				break;
			case 8787: // Sprigant's Fruit
				res = usePotion(activeChar, 2305, 1);
				break;
			// VALAKAS AMULETS
			case 6652: // Amulet Protection of Valakas
				res = usePotion(activeChar, 2231, 1);
				break;
			case 6653: // Amulet Flames of Valakas
				res = usePotion(activeChar, 2223, 1);
				break;
			case 6654: // Amulet Flames of Valakas
				res = usePotion(activeChar, 2233, 1);
				break;
			case 6655: // Amulet Slay Valakas
				res = usePotion(activeChar, 2232, 1);
				break;

			// HERBS
			case 8600: // Herb of Life
				res = usePotion(activeChar, 2278, 1);
				break;
			case 8601: // Greater Herb of Life
				res = usePotion(activeChar, 2278, 2);
				break;
			case 8602: // Superior Herb of Life
				res = usePotion(activeChar, 2278, 3);
				break;
			case 8603: // Herb of Mana
				res = usePotion(activeChar, 2279, 1);
				break;
			case 8604: // Greater Herb of Mane
				res = usePotion(activeChar, 2279, 2);
				break;
			case 8605: // Superior Herb of Mane
				res = usePotion(activeChar, 2279, 3);
				break;
			case 8606: // Herb of Strength
				res = usePotion(activeChar, 2280, 1);
				break;
			case 8607: // Herb of Magic
				res = usePotion(activeChar, 2281, 1);
				break;
			case 8608: // Herb of Atk. Spd.
				res = usePotion(activeChar, 2282, 1);
				break;
			case 8609: // Herb of Casting Spd.
				res = usePotion(activeChar, 2283, 1);
				break;
			case 8610: // Herb of Critical Attack
				res = usePotion(activeChar, 2284, 1);
				break;
			case 8611: // Herb of Speed
				res = usePotion(activeChar, 2285, 1);
				break;
			case 8612: // Herb of Warrior
				res = usePotion(activeChar, 2280, 1);// Herb of Strength
				res = usePotion(activeChar, 2282, 1);// Herb of Atk. Spd
				res = usePotion(activeChar, 2284, 1);// Herb of Critical Attack
				break;
			case 8613: // Herb of Mystic
				res = usePotion(activeChar, 2281, 1);// Herb of Magic
				res = usePotion(activeChar, 2283, 1);// Herb of Casting Spd.
				break;
			case 8614: // Herb of Warrior
				res = usePotion(activeChar, 2278, 3);// Superior Herb of Life
				res = usePotion(activeChar, 2279, 3);// Superior Herb of Mana
				break;

			// FISHERMAN POTIONS
			case 8193: // Fisherman's Potion - Green
				if (activeChar.getSkillLevel(1315) <= 3) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 1);
				break;
			case 8194: // Fisherman's Potion - Jade
				if (activeChar.getSkillLevel(1315) <= 6) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 2);
				break;
			case 8195: // Fisherman's Potion - Blue
				if (activeChar.getSkillLevel(1315) <= 9) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 3);
				break;
			case 8196: // Fisherman's Potion - Yellow
				if (activeChar.getSkillLevel(1315) <= 12) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 4);
				break;
			case 8197: // Fisherman's Potion - Orange
				if (activeChar.getSkillLevel(1315) <= 15) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 5);
				break;
			case 8198: // Fisherman's Potion - Purple
				if (activeChar.getSkillLevel(1315) <= 18) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 6);
				break;
			case 8199: // Fisherman's Potion - Red
				if (activeChar.getSkillLevel(1315) <= 21) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 7);
				break;
			case 8200: // Fisherman's Potion - White
				if (activeChar.getSkillLevel(1315) <= 24) {
					playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
					playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
					return;
				}
				res = usePotion(activeChar, 2274, 8);
				break;
			case 8201: // Fisherman's Potion - Black
				res = usePotion(activeChar, 2274, 9);
				break;
			case 8202: // Fishing Potion
				res = usePotion(activeChar, 2275, 1);
				break;
			default:
		}

		if (res)
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}

	public boolean usePotion(L2PcInstance activeChar, int magicId, int level)
	{
		if (activeChar.isCastingNow() && magicId>2277 && magicId<2285)
		{
			_herbstask += 100;
			ThreadPoolManager.getInstance().scheduleAi(new HerbTask(activeChar, magicId, level), _herbstask);
		}
		else
		{
			if (magicId>2277 && magicId<2285 && _herbstask>=100) _herbstask -= 100;
			L2Skill skill = SkillTable.getInstance().getInfo(magicId, level);
			if (skill != null)
			{
				activeChar.doCast(skill);
				if (!(activeChar.isSitting() && !skill.isPotion()))
					return true;
			}
		}
		return false;
	}

	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
