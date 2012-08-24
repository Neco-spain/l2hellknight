/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package events.SavingSanta;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;

import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.ai.CtrlIntention;
import l2.hellknight.gameserver.datatables.EventDroplist;
import l2.hellknight.gameserver.datatables.ItemTable;
import l2.hellknight.gameserver.datatables.SkillTable;
import l2.hellknight.gameserver.model.L2Object;
import l2.hellknight.gameserver.model.L2World;
import l2.hellknight.gameserver.model.Location;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.holders.ItemHolder;
import l2.hellknight.gameserver.model.quest.Quest;
import l2.hellknight.gameserver.model.skills.L2Skill;
import l2.hellknight.gameserver.network.NpcStringId;
import l2.hellknight.gameserver.network.SystemMessageId;
import l2.hellknight.gameserver.network.serverpackets.ActionFailed;
import l2.hellknight.gameserver.network.serverpackets.MagicSkillUse;
import l2.hellknight.gameserver.network.serverpackets.NpcSay;
import l2.hellknight.gameserver.network.serverpackets.PlaySound;
import l2.hellknight.gameserver.network.serverpackets.SocialAction;
import l2.hellknight.gameserver.network.serverpackets.SystemMessage;
import l2.hellknight.gameserver.script.DateRange;
import l2.hellknight.gameserver.util.Broadcast;
import l2.hellknight.gameserver.util.Util;
import l2.hellknight.util.Rnd;

/**
 * Christmas Event: Saving Santa<br>
 * http://legacy.lineage2.com/archive/2008/12/saving_santa_ev.html<br>
 * TODO:<br>
 * 1) Heading for Santa's Helpers.<br>
 * 2) Unhardcode HTMLs.<br>
 * 3) Scrolls ?
 * @author Zoey76
 */
public class SavingSanta extends Quest
{
	private static final String qn = "SavingSanta";
	// Is Saving Santa event used?
	private static boolean SAVING_SANTA = true;
	// Use Santa's Helpers Auto Buff?
	private static boolean SANTAS_HELPER_AUTOBUFF = false;
	private static final ItemHolder[] _requiredItems =
	{
		new ItemHolder(5556, 4), new ItemHolder(5557, 4), new ItemHolder(5558, 10), new ItemHolder(5559, 1)
	};
	
	private static final int SantaTraineeId = 31863;
	private static final int SpecialChristmasTreeId = 13007;
	private static final int HolidaySantaId = 104;
	private static final int HolidaySledId = 105;
	private static final int ThomasDTurkeyId = 13183;
	private static final int ChristmasTreeId = 13006;
	private static final long MIN_TIME_BETWEEN_2_REWARDS = 43200000;
	private static final long MIN_TIME_BETWEEN_2_BLESSINGS = 14400000;
	private static final int BR_XMAS_PRESENT_NORMAL = 20101;
	private static final int BR_XMAS_PRESENT_JACKPOT = 20102;
	private static final int BR_XMAS_WPN_TICKET_NORMAL = 20107;
	private static final int BR_XMAS_WPN_TICKET_JACKPOT = 20108;
	private static final int BR_XMAS_REWARD_BUFF = 23017;
	private static final int BR_XMAS_GAWIBAWIBO_CAP = 20100;
	private static final int X_MAS_TREE1 = 5560;
	private static final int X_MAS_TREE2 = 5561;
	private static final int SantasHatId = 7836;
	private static final int[] RANDOM_A_PLUS_10_WEAPON =
	{
		81, 151, 164, 213, 236, 270, 289, 2500, 7895, 7902, 5706
	};
	
	private static final Location _thomasSpawn = new Location(117935, -126003, -2585, 54625);
	
	private static final int[] SantaMageBuffs =
	{
		7055, 7054, 7051
	};
	
	private static final int[] SantaFighterBuffs =
	{
		7043, 7057, 7051
	};
	
	private static final GregorianCalendar calendar = new GregorianCalendar();
	// 15th December 2011
	private static GregorianCalendar _startEventCalendar = new GregorianCalendar(2011, 11, 15);
	// 6th January 2012
	private static GregorianCalendar _endEventCalendar = new GregorianCalendar(2012, 0, 6);
	
	private static final Location[] _treesSpawn =
	{
		new Location(83254, 148340, -3405),
		new Location(83278, 147900, -3405),
		new Location(83241, 148898, -3405),
		new Location(83281, 149343, -3405),
		new Location(84304, 149133, -3402),
		new Location(84311, 148101, -3402),
		new Location(82948, 147658, -3469),
		new Location(80905, 147659, -3469),
		new Location(80908, 149556, -3469),
		new Location(82957, 149554, -3469),
		new Location(147849, -55119, -2734),
		new Location(147580, -55117, -2734),
		new Location(147581, -57244, -2781),
		new Location(147847, -57261, -2781),
		new Location(149085, -55826, -2781),
		new Location(146340, -55829, -2781),
		new Location(147826, -54095, -2735),
		new Location(147584, -54070, -2735),
		new Location(146235, 25921, -2013),
		new Location(147840, 25568, -2013),
		new Location(147055, 25568, -2013),
		new Location(148694, 25929, -2013),
		new Location(147733, 27366, -2205),
		new Location(147197, 27364, -2205),
		new Location(147266, 29065, -2269),
		new Location(147646, 29065, -2269),
		new Location(147456, 27664, -2204),
		new Location(148078, -55960, -2781),
		new Location(147348, -55939, -2781),
		new Location(117056, 75627, -2726),
		new Location(116473, 75352, -2712),
		new Location(115785, 76111, -2715),
		new Location(115939, 76544, -2719),
		new Location(116833, 77400, -2697),
		new Location(116666, 76210, -2730),
		new Location(-13130, 122533, -3117),
		new Location(-13165, 122425, -2989),
		new Location(-13126, 122806, -3117),
		new Location(15733, 142767, -2706),
		new Location(16208, 142710, -2706)
	};
	
	private static final Location[] _santasHelperSpawn =
	{
		new Location(147698, -56025, -2775),
		new Location(147443, 26942, -2205),
		new Location(82218, 148605, -3470),
		new Location(82754, 53573, -1496),
		new Location(15064, 143254, -2668),
		new Location(111067, 218933, -3543),
		new Location(-12965, 122914, -3117),
		new Location(87362, -143166, -1293),
		new Location(-81037, 150092, -3044),
		new Location(117412, 76642, -2695),
		new Location(43983, -47758, -797),
		new Location(-45907, 49387, -3060),
		new Location(12153, 16753, -4584),
		new Location(-84458, 244761, -3730),
		new Location(114750, -178692, -820),
		new Location(-45656, -113119, -240),
		new Location(-117195, 46837, 367)
	};
	
	private static boolean _christmasEvent = true;
	private static boolean _isSantaFree = true;
	private static boolean _isJackPot = false;
	private static boolean _isWaitingForPlayerSkill = false;
	private static List<L2Npc> _santaHelpers = new ArrayList<L2Npc>();
	private static List<L2Npc> _specialTrees = new ArrayList<L2Npc>();
	private final Map<String, Long> _rewardedPlayers = new FastMap<String, Long>();
	private final Map<String, Long> _blessedPlayers = new FastMap<String, Long>();
	
	private final NpcStringId[] _npcStringIds =
	{
		NpcStringId.ITS_HURTING_IM_IN_PAIN_WHAT_CAN_I_DO_FOR_THE_PAIN,
		NpcStringId.NO_WHEN_I_LOSE_THAT_ONE_ILL_BE_IN_MORE_PAIN,
		NpcStringId.HAHAHAH_I_CAPTURED_SANTA_CLAUS_THERE_WILL_BE_NO_GIFTS_THIS_YEAR,
		NpcStringId.NOW_WHY_DONT_YOU_TAKE_UP_THE_CHALLENGE,
		NpcStringId.COME_ON_ILL_TAKE_ALL_OF_YOU_ON,
		NpcStringId.HOW_ABOUT_IT_I_THINK_I_WON,
		NpcStringId.NOW_THOSE_OF_YOU_WHO_LOST_GO_AWAY,
		NpcStringId.WHAT_A_BUNCH_OF_LOSERS,
		NpcStringId.I_GUESS_YOU_CAME_TO_RESCUE_SANTA_BUT_YOU_PICKED_THE_WRONG_PERSON,
		NpcStringId.AH_OKAY,
		NpcStringId.UAGH_I_WASNT_GOING_TO_DO_THAT,
		NpcStringId.YOURE_CURSED_OH_WHAT,
		NpcStringId.STOP_IT_NO_MORE_I_DID_IT_BECAUSE_I_WAS_TOO_LONELY,
		NpcStringId.I_HAVE_TO_RELEASE_SANTA_HOW_INFURIATING,
		NpcStringId.I_HATE_HAPPY_MERRY_CHRISTMAS,
		NpcStringId.OH_IM_BORED,
		NpcStringId.SHALL_I_GO_TO_TAKE_A_LOOK_IF_SANTA_IS_STILL_THERE_HEHE,
		NpcStringId.OH_HO_HO_MERRY_CHRISTMAS,
		NpcStringId.SANTA_COULD_GIVE_NICE_PRESENTS_ONLY_IF_HES_RELEASED_FROM_THE_TURKEY,
		NpcStringId.OH_HO_HO_OH_HO_HO_THANK_YOU_LADIES_AND_GENTLEMEN_I_WILL_REPAY_YOU_FOR_SURE,
		NpcStringId.UMERRY_CHRISTMAS_YOURE_DOING_A_GOOD_JOB,
		NpcStringId.MERRY_CHRISTMAS_THANK_YOU_FOR_RESCUING_ME_FROM_THAT_WRETCHED_TURKEY,
		NpcStringId.S1_I_HAVE_PREPARED_A_GIFT_FOR_YOU,
		NpcStringId.I_HAVE_A_GIFT_FOR_S1,
		NpcStringId.TAKE_A_LOOK_AT_THE_INVENTORY_I_HOPE_YOU_LIKE_THE_GIFT_I_GAVE_YOU,
		NpcStringId.TAKE_A_LOOK_AT_THE_INVENTORY_PERHAPS_THERE_MIGHT_BE_A_BIG_PRESENT,
		NpcStringId.IM_TIRED_OF_DEALING_WITH_YOU_IM_LEAVING,
		NpcStringId.WHEN_ARE_YOU_GOING_TO_STOP_I_SLOWLY_STARTED_TO_BE_TIRED_OF_IT,
		NpcStringId.MESSAGE_FROM_SANTA_CLAUS_MANY_BLESSINGS_TO_S1_WHO_SAVED_ME
	};
	
	private static final int[][] _dropList =
	{
		{
			5556
		},
		{
			5557
		},
		{
			5558
		},
		{
			5559
		},
		{
			5562
		},
		{
			5563
		},
		{
			5564
		},
		{
			5565
		},
		{
			5566
		},
	};
	private static final int[] _chanceList =
	{
		40000, 40000, 80000, 10000, 20000, 20000, 20000, 20000, 20000
	};
	private static final int[] _minMax =
	{
		1, 1
	};
	private static final DateRange _dateRange = new DateRange(_startEventCalendar.getTime(), _endEventCalendar.getTime());
	
	public SavingSanta(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(SantaTraineeId);
		addFirstTalkId(SantaTraineeId);
		addTalkId(SantaTraineeId);
		addFirstTalkId(ThomasDTurkeyId);
		addFirstTalkId(HolidaySantaId);
		addFirstTalkId(HolidaySledId);
		addSkillSeeId(ThomasDTurkeyId);
		addSpellFinishedId(ThomasDTurkeyId);
		addSpawnId(SpecialChristmasTreeId);
		
		if (calendar.after(_startEventCalendar) && calendar.before(_endEventCalendar))
		{
			_log.info("[Christmas Event] ON");
			
			_christmasEvent = true;
			
			for (int i = 0; i < _dropList.length; i++)
			{
				EventDroplist.getInstance().addGlobalDrop(_dropList[i], _minMax, _chanceList[i], _dateRange);
			}
			for (Location ts : _treesSpawn)
			{
				addSpawn(ChristmasTreeId, ts.getX(), ts.getY(), ts.getZ(), 0, false, 0);
			}
			
			startQuestTimer("SpecialTreeHeal", 5000, null, null);
			
			for (Location sh : _santasHelperSpawn)
			{
				_santaHelpers.add(addSpawn(SantaTraineeId, sh.getX(), sh.getY(), sh.getZ(), 0, false, 0));
			}
			
			if (SANTAS_HELPER_AUTOBUFF)
			{
				startQuestTimer("SantaBlessings", 5000, null, null);
			}
			
			if (SAVING_SANTA)
			{
				startQuestTimer("ThomasQuest", 1000, null, null);
			}
		}
		else
		{
			_log.info("[Christmas Event] OFF");
			
			final GregorianCalendar endWeek = (GregorianCalendar) _endEventCalendar.clone();
			endWeek.add(Calendar.DAY_OF_MONTH, 7);
			if (calendar.after(_endEventCalendar) && calendar.before(endWeek))
			{
				for (Location sh : _santasHelperSpawn)
				{
					_santaHelpers.add(addSpawn(SantaTraineeId, sh.getX(), sh.getY(), sh.getZ(), 0, false, 0));
				}
			}
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		_specialTrees.add(npc);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (_isWaitingForPlayerSkill && (skill.getId() > 21013) && (skill.getId() < 21017))
		{
			caster.broadcastPacket(new MagicSkillUse(caster, caster, 23019, skill.getId() - 21013, 3000, 1));
			SkillTable.getInstance().getInfo(23019, skill.getId() - 21013).getEffects(caster, caster);
		}
		return "";
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		/**
		 * Turkey's Choice<br>
		 * Level 1: Scissors<br>
		 * Level 2: Rock<br>
		 * Level 3: Paper
		 */
		if (skill.getId() == 6100)// 
		{
			_isWaitingForPlayerSkill = false;
			for (L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(600))
			{
				/**
				 * Level 1: Scissors<br>
				 * Level 2: Rock<br>
				 * Level 3: Paper<br>
				 */
				if (pl.getFirstEffect(23019) == null)
				{
					continue;
				}
				
				int result = pl.getFirstEffect(23019).getSkill().getLevel() - skill.getLevel();
				
				if (result == 0)
				{
					// Oh. I'm bored.
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[15]));
				}
				else if ((result == -1) || (result == 2))
				{
					// Now!! Those of you who lost, go away!
					// What a bunch of losers.
					npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[6 + Rnd.get(2)]));
					pl.broadcastPacket(new MagicSkillUse(pl, pl, 23023, 1, 3000, 1));
					pl.stopSkillEffects(23022);
				}
				else if ((result == 1) || (result == -2))
				{
					int level = (pl.getFirstEffect(23022) != null ? (pl.getFirstEffect(23022).getSkill().getLevel() + 1) : 1);
					pl.broadcastPacket(new MagicSkillUse(pl, pl, 23022, level, 3000, 1));
					SkillTable.getInstance().getInfo(23022, level).getEffects(pl, pl);
					if ((level == 1) || (level == 2))
					{
						// Ah, okay...
						// Agh!! I wasn't going to do that!
						// You're cursed!! Oh.. What?
						// Have you done nothing but rock-paper-scissors??
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[10 + Rnd.get(4)]));
					}
					else if (level == 3)
					{
						SkillTable.getInstance().getInfo(23018, 1).getEffects(pl, pl);
						// Stop it, no more... I did it because I was too lonely...
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[13]));
					}
					else if (level == 4)
					{
						Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THOMAS_D_TURKEY_DEFETED));
						// I have to release Santa... How infuriating!!!
						// I hate happy Merry Christmas!!!
						npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[14 + Rnd.get(2)]));
						
						startQuestTimer("SantaSpawn", 120000, null, null);
						final L2Npc holidaySled = addSpawn(HolidaySledId, 117935, -126003, -2585, 54625, false, 12000);
						// Message from Santa Claus: Many blessings to $s1, who saved me~
						final NpcSay santaSaved = new NpcSay(holidaySled.getObjectId(), 10, holidaySled.getNpcId(), _npcStringIds[28]);
						santaSaved.addStringParameter(pl.getName());
						holidaySled.broadcastPacket(santaSaved);
						// Oh ho ho.... Merry Christmas!!
						holidaySled.broadcastPacket(new NpcSay(holidaySled.getObjectId(), 0, holidaySled.getNpcId(), _npcStringIds[17]));
						
						if (Rnd.get(100) > 90)
						{
							_isJackPot = true;
							pl.getInventory().addItem("SavingSantaPresent", BR_XMAS_PRESENT_JACKPOT, 1, pl, holidaySled);
						}
						else
						{
							pl.getInventory().addItem("SavingSantaPresent", BR_XMAS_PRESENT_NORMAL, 1, pl, holidaySled);
						}
						
						ThreadPoolManager.getInstance().scheduleGeneral(new SledAnimation(holidaySled), 7000);
						npc.decayMe();
						_isSantaFree = true;
						break;
					}
				}
			}
		}
		return "";
	}
	
	private static class SledAnimation implements Runnable
	{
		private final L2Npc _sled;
		
		public SledAnimation(L2Npc sled)
		{
			_sled = sled;
		}
		
		@Override
		public void run()
		{
			try
			{
				_sled.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				_sled.broadcastPacket(new SocialAction(_sled.getObjectId(), 1));
			}
			catch (Exception e)
			{
				//
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		if (event.equalsIgnoreCase("ThomasQuest"))
		{
			startQuestTimer("ThomasQuest", 14400000, null, null);
			L2Npc ThomasDTurkey = addSpawn(ThomasDTurkeyId, _thomasSpawn.getX(), _thomasSpawn.getY(), _thomasSpawn.getZ(), _thomasSpawn.getHeading(), false, 1800000);
			
			Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THOMAS_D_TURKEY_APPEARED));
			
			startQuestTimer("ThomasCast1", 15000, ThomasDTurkey, null);
			
			_isSantaFree = false;
		}
		else if (event.equalsIgnoreCase("SantaSpawn"))
		{
			if (_isSantaFree)
			{
				startQuestTimer("SantaSpawn", 120000, null, null);
				// for (L2PcInstance pl : L2World.getInstance().getAllPlayers().values())
				for (L2PcInstance pl : L2World.getInstance().getAllPlayersArray())
				{
					if ((pl != null) && pl.isOnline() && (pl.getLevel() >= 20) && pl.isInCombat() && !pl.isInsideZone(L2Character.ZONE_PEACE) && !pl.isFlyingMounted())
					{
						if (_rewardedPlayers.containsKey(pl.getAccountName()))
						{
							long elapsedTimeSinceLastRewarded = System.currentTimeMillis() - _rewardedPlayers.get(pl.getAccountName());
							if (elapsedTimeSinceLastRewarded < MIN_TIME_BETWEEN_2_REWARDS)
							{
								continue;
							}
						}
						else
						{
							String data = loadGlobalQuestVar(pl.getAccountName());
							if (!data.isEmpty() && ((System.currentTimeMillis() - Long.parseLong(data)) < MIN_TIME_BETWEEN_2_REWARDS))
							{
								_rewardedPlayers.put(pl.getAccountName(), Long.parseLong(data));
								continue;
							}
						}
						int locx = (int) (pl.getX() + (Math.pow(-1, Rnd.get(1, 2)) * 50));
						int locy = (int) (pl.getY() + (Math.pow(-1, Rnd.get(1, 2)) * 50));
						int heading = Util.calculateHeadingFrom(locx, locy, pl.getX(), pl.getY());
						L2Npc santa = addSpawn(HolidaySantaId, locx, locy, pl.getZ(), heading, false, 30000);
						_rewardedPlayers.put(pl.getAccountName(), System.currentTimeMillis());
						saveGlobalQuestVar(pl.getAccountName(), String.valueOf(System.currentTimeMillis()));
						startQuestTimer("SantaRewarding0", 500, santa, pl);
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("ThomasCast1"))
		{
			if (!npc.isDecayed())
			{
				_isWaitingForPlayerSkill = true;
				startQuestTimer("ThomasCast2", 4000, npc, null);
				npc.doCast(SkillTable.getInstance().getInfo(6116, 1));
				// It's hurting... I'm in pain... What can I do for the pain...
				// No... When I lose that one... I'll be in more pain...
				// Hahahah!!! I captured Santa Claus!! There will be no gifts this year!!!
				// Now! Why don't you take up the challenge?
				// Come on, I'll take all of you on!
				// How about it? I think I won?
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[Rnd.get(6)]));
			}
			else
			{
				if (!_isSantaFree)
				{
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THOMAS_D_TURKEY_DISAPPEARED));
					_isWaitingForPlayerSkill = false;
				}
			}
		}
		else if (event.equalsIgnoreCase("ThomasCast2"))
		{
			if (!npc.isDecayed())
			{
				startQuestTimer("ThomasCast1", 13000, npc, null);
				npc.doCast(SkillTable.getInstance().getInfo(6100, Rnd.get(1, 3)));
				// It's hurting... I'm in pain... What can I do for the pain...
				// No... When I lose that one... I'll be in more pain...
				// Hahahah!!! I captured Santa Claus!! There will be no gifts this year!!!
				// Now! Why don't you take up the challenge?
				// Come on, I'll take all of you on!
				// How about it? I think I won?
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[Rnd.get(6)]));
			}
			else
			{
				if (!_isSantaFree)
				{
					Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THOMAS_D_TURKEY_DISAPPEARED));
					_isWaitingForPlayerSkill = false;
				}
			}
		}
		else if (event.equalsIgnoreCase("SantaRewarding0"))
		{
			startQuestTimer("SantaRewarding1", 9500, npc, player);
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 3));
		}
		else if (event.equalsIgnoreCase("SantaRewarding1"))
		{
			startQuestTimer("SantaRewarding2", 5000, npc, player);
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 1));
			// Merry Christmas~ Thank you for rescuing me from that wretched Turkey.
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[21]));
		}
		else if (event.equalsIgnoreCase("SantaRewarding2"))
		{
			startQuestTimer("SantaRewarding3", 5000, npc, player);
			// I have a gift for $s1.
			final NpcSay IHaveAGift = new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[23]);
			IHaveAGift.addStringParameter(player.getName());
			npc.broadcastPacket(IHaveAGift);
		}
		else if (event.equalsIgnoreCase("SantaRewarding3"))
		{
			npc.broadcastPacket(new SocialAction(npc.getObjectId(), 2));
			if (_isJackPot)
			{
				// Take a look at the inventory. Perhaps there might be a big present~
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[25]));
				player.getInventory().addItem("SavingSantaPresent", BR_XMAS_PRESENT_JACKPOT, 1, player, npc);
				_isJackPot = false;
			}
			else
			{
				// Take a look at the inventory. I hope you like the gift I gave you.
				npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[24]));
				player.getInventory().addItem("SavingSantaPresent", BR_XMAS_PRESENT_NORMAL, 1, player, npc);
			}
		}
		else if (event.equalsIgnoreCase("SantaBlessings"))
		{
			if (_christmasEvent)
			{
				startQuestTimer("SantaBlessings", 15000, null, null);
				for (L2Npc santaHelper1 : _santaHelpers)
				{
					Collection<L2PcInstance> blessList = santaHelper1.getKnownList().getKnownPlayers().values();
					for (L2PcInstance plb : blessList)
					{
						if ((plb.getLevel() >= 20) && !plb.isFlyingMounted())
						{
							if (_blessedPlayers.containsKey(plb.getAccountName()))
							{
								long elapsedTimeSinceLastBlessed = System.currentTimeMillis() - _blessedPlayers.get(plb.getAccountName());
								if (elapsedTimeSinceLastBlessed < MIN_TIME_BETWEEN_2_BLESSINGS)
								{
									continue;
								}
							}
							else
							{
								String data = loadGlobalQuestVar(plb.getAccountName());
								if (!data.isEmpty() && ((System.currentTimeMillis() - Long.parseLong(data)) < MIN_TIME_BETWEEN_2_BLESSINGS))
								{
									_blessedPlayers.put(plb.getAccountName(), Long.parseLong(data));
									continue;
								}
							}
							for (L2Npc santaHelper : _santaHelpers)
							{
								Collection<L2PcInstance> playerList = santaHelper.getKnownList().getKnownPlayers().values();
								for (L2PcInstance playerx : playerList)
								{
									if (playerx.getClassId().isMage())
									{
										for (int buffId : SantaMageBuffs)
										{
											if (playerx.getFirstEffect(buffId) == null)
											{
												playerx.broadcastPacket(new MagicSkillUse(santaHelper, playerx, buffId, 1, 2000, 1));
												SkillTable.getInstance().getInfo(buffId, 1).getEffects(playerx, playerx);
												_blessedPlayers.put(playerx.getAccountName(), System.currentTimeMillis());
												saveGlobalQuestVar(playerx.getAccountName(), String.valueOf(System.currentTimeMillis()));
											}
										}
									}
									else
									{
										for (int buffId : SantaFighterBuffs)
										{
											if (playerx.getFirstEffect(buffId) == null)
											{
												playerx.broadcastPacket(new MagicSkillUse(santaHelper, playerx, buffId, 1, 2000, 1));
												SkillTable.getInstance().getInfo(buffId, 1).getEffects(playerx, playerx);
												_blessedPlayers.put(playerx.getAccountName(), System.currentTimeMillis());
												saveGlobalQuestVar(playerx.getAccountName(), String.valueOf(System.currentTimeMillis()));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else if (event.equalsIgnoreCase("SpecialTreeHeal"))
		{
			startQuestTimer("SpecialTreeHeal", 9000, null, null);
			for (L2Npc tree : _specialTrees)
			{
				Collection<L2PcInstance> playerList = tree.getKnownList().getKnownPlayers().values();
				for (L2PcInstance playerr : playerList)
				{
					int xxMin = tree.getX() - 60;
					int yyMin = tree.getY() - 60;
					int xxMax = tree.getX() + 60;
					int yyMax = tree.getY() + 60;
					int playerX = playerr.getX();
					int playerY = playerr.getY();
					
					if ((playerX > xxMin) && (playerX < xxMax) && (playerY > yyMin) && (playerY < yyMax))
					{
						SkillTable.getInstance().getInfo(2139, 1).getEffects(tree, playerr);
					}
				}
			}
		}
		else if (player != null)
		{
			// FIXME: Unhardcore html!
			if (event.equalsIgnoreCase("Tree"))
			{
				int itemsOk = 0;
				htmltext = "<html><title>Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Christmas Tree</font></td><td width=40><img src=\"Icon.etc_x_mas_tree_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				
				for (ItemHolder item : _requiredItems)
				{
					long pieceCount = player.getInventory().getInventoryItemCount(item.getId(), -1);
					if (pieceCount >= item.getCount())
					{
						itemsOk = itemsOk + 1;
						htmltext = htmltext + "<tr><td>" + ItemTable.getInstance().getTemplate(item.getId()).getName() + "</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
					}
					
					else
					{
						htmltext = htmltext + "<tr><td>" + ItemTable.getInstance().getTemplate(item.getId()).getName() + "</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
					}
				}
				
				if (itemsOk == 4)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext + "<tr><td><center><button value=\"Get the tree\" action=\"bypass -h Quest SavingSanta buyTree\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}
				else if (itemsOk < 4)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough items.</center></body></html>";
				}
				return htmltext;
			}
			else if (event.equalsIgnoreCase("buyTree"))
			{
				player.sendPacket(new PlaySound("ItemSound.quest_middle"));
				for (ItemHolder item : _requiredItems)
				{
					if (player.getInventory().getInventoryItemCount(item.getId(), -1) < item.getCount())
					{
						return "";
					}
				}
				
				for (ItemHolder item : _requiredItems)
				{
					player.getInventory().destroyItemByItemId(event, item.getId(), item.getCount(), player, npc);
				}
				player.getInventory().addItem(event, X_MAS_TREE1, 1, player, npc);
			}
			else if (event.equalsIgnoreCase("SpecialTree") && !SAVING_SANTA)
			{
				htmltext = "<html><title>Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Special Christmas Tree</font></td><td width=40><img src=\"Icon.etc_x_mas_tree_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				long pieceCount = player.getInventory().getInventoryItemCount(X_MAS_TREE1, -1);
				int itemsOk = 0;
				
				if (pieceCount >= 10)
				{
					itemsOk = 1;
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
				}
				else
				{
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
				}
				
				if (itemsOk == 1)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext + "<tr><td><center><button value=\"Get the tree\" action=\"bypass -h Quest SavingSanta buySpecialTree\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}
				else if (itemsOk == 0)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough items.</center></body></html>";
				}
				
				return htmltext;
			}
			else if (event.equalsIgnoreCase("buySpecialTree") && !SAVING_SANTA)
			{
				player.sendPacket(new PlaySound("ItemSound.quest_middle"));
				if (player.getInventory().getInventoryItemCount(X_MAS_TREE1, -1) < 10)
				{
					return "";
				}
				player.getInventory().destroyItemByItemId(event, X_MAS_TREE1, 10, player, npc);
				player.getInventory().addItem(event, X_MAS_TREE2, 1, player, npc);
			}
			else if (event.equalsIgnoreCase("SantaHat"))
			{
				htmltext = "<html><title>Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Santa's Hat</font></td><td width=40><img src=\"Icon.Accessory_santas_cap_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				long pieceCount = player.getInventory().getInventoryItemCount(X_MAS_TREE1, -1);
				int itemsOk = 0;
				
				if (pieceCount >= 10)
				{
					itemsOk = 1;
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
				}
				else
				{
					htmltext = htmltext + "<tr><td>Christmas Tree</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
				}
				
				if (itemsOk == 1)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext + "<tr><td><center><button value=\"Get the hat\" action=\"bypass -h Quest SavingSanta buyHat\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}
				else if (itemsOk == 0)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough items.</center></body></html>";
				}
				return htmltext;
			}
			else if (event.equalsIgnoreCase("buyHat"))
			{
				player.sendPacket(new PlaySound("ItemSound.quest_middle"));
				if (player.getInventory().getInventoryItemCount(X_MAS_TREE1, -1) < 10)
				{
					return "";
				}
				player.getInventory().destroyItemByItemId(event, X_MAS_TREE1, 10, player, npc);
				player.getInventory().addItem(event, SantasHatId, 1, player, npc);
			}
			// FIXME: Unhardcore html!
			else if (event.equalsIgnoreCase("SavingSantaHat") && SAVING_SANTA)
			{
				htmltext = "<html><title>Christmas Event</title><body><br><br><table width=260><tr><td></td><td width=40></td><td width=40></td></tr><tr><td><font color=LEVEL>Saving Santa's Hat</font></td><td width=40><img src=\"Icon.Accessory_santas_cap_i00\" width=32 height=32></td><td width=40></td></tr></table><br><br><table width=260>";
				long pieceCount = player.getInventory().getAdena();
				int itemsOk = 0;
				
				if (pieceCount >= 50000)
				{
					itemsOk = 1;
					htmltext = htmltext + "<tr><td>Adena</td><td width=40>" + pieceCount + "</td><td width=40><font color=0FF000>OK</font></td></tr>";
				}
				
				else
				{
					htmltext = htmltext + "<tr><td>Adena</td><td width=40>" + pieceCount + "</td><td width=40><font color=8ae2ffb>NO</font></td></tr>";
				}
				
				if (itemsOk == 1)
				{
					htmltext = htmltext + "<tr><td><br></td><td width=40></td><td width=40></td></tr></table><table width=260>";
					htmltext = htmltext + "<tr><td><center><button value=\"Get the hat\" action=\"bypass -h Quest SavingSanta buySavingHat\" width=110 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td></tr></table></body></html>";
				}
				
				else if (itemsOk == 0)
				{
					htmltext = htmltext + "</table><br><br>You do not have enough Adena.</center></body></html>";
				}
				
				return htmltext;
			}
			
			else if (event.equalsIgnoreCase("buySavingHat") && SAVING_SANTA)
			{
				player.sendPacket(new PlaySound("ItemSound.quest_middle"));
				if (player.getInventory().getAdena() < 50000)
				{
					return "";
				}
				player.getInventory().reduceAdena(event, 50000, player, npc);
				player.getInventory().addItem(event, BR_XMAS_GAWIBAWIBO_CAP, 1, player, npc);
			}
			else if (event.equalsIgnoreCase("HolidayFestival") && SAVING_SANTA)
			{
				if (_isSantaFree)
				{
					npc.broadcastPacket(new MagicSkillUse(npc, player, BR_XMAS_REWARD_BUFF, 1, 2000, 1));
					SkillTable.getInstance().getInfo(BR_XMAS_REWARD_BUFF, 1).getEffects(player, player);
				}
				else
				{
					return "savingsanta-nobuff.htm";
				}
			}
			else if (event.equalsIgnoreCase("getWeapon") && SAVING_SANTA)
			{
				if ((player.getInventory().getInventoryItemCount(BR_XMAS_WPN_TICKET_NORMAL, -1) > 0) && (player.getInventory().getInventoryItemCount(BR_XMAS_WPN_TICKET_JACKPOT, -1) > 0))
				{
					return "savingsanta-noweapon.htm";
				}
				return "savingsanta-weapon.htm";
			}
			else if (event.startsWith("weapon_") && SAVING_SANTA)
			{
				if (player.getInventory().getInventoryItemCount(BR_XMAS_WPN_TICKET_JACKPOT, -1) > 0)
				{
					player.getInventory().destroyItemByItemId(event, BR_XMAS_WPN_TICKET_JACKPOT, 1, player, npc);
					player.getInventory().addItem(event, RANDOM_A_PLUS_10_WEAPON[Rnd.get(RANDOM_A_PLUS_10_WEAPON.length)], 1, player, npc).setEnchantLevel(10);
					return "";
				}
				
				if ((player.getInventory().getInventoryItemCount(BR_XMAS_WPN_TICKET_NORMAL, -1) <= 0) || (player.getLevel() < 20))
				{
					return "";
				}
				
				int grade = player.getExpertiseLevel() - 1;
				if (grade < -1)
				{
					return "";
				}
				
				int itemId = Integer.parseInt(event.replace("weapon_", ""));
				if ((itemId < 1) || (itemId > 14))
				{
					return "";
				}
				
				if (grade > 4)
				{
					grade = 4;
				}
				
				itemId += (BR_XMAS_WPN_TICKET_JACKPOT + (grade * 14));
				player.getInventory().destroyItemByItemId(event, BR_XMAS_WPN_TICKET_NORMAL, 1, player, npc);
				player.getInventory().addItem(event, RANDOM_A_PLUS_10_WEAPON[Rnd.get(RANDOM_A_PLUS_10_WEAPON.length)], 1, player, npc).setEnchantLevel(Rnd.get(4, 16));
			}
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		final int npcId = npc.getNpcId();
		if ((npcId == ThomasDTurkeyId) || (npcId == HolidaySantaId) || (npcId == HolidaySledId))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			htmltext = null;
		}
		else if (npcId == SantaTraineeId)
		{
			if (SAVING_SANTA)
			{
				htmltext = "savingsanta.htm";
			}
			else
			{
				htmltext = "santa.htm";
			}
		}
		return htmltext;
	}
	
	@Override
	public boolean unload()
	{
		for (L2Npc santaHelper : _santaHelpers)
		{
			santaHelper.deleteMe();
		}
		for (L2Npc specialTree : _specialTrees)
		{
			specialTree.deleteMe();
		}
		return super.unload();
	}
	
	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		// FIXME: Increase Thomas D. Turkey aggro rage.
		if (npc.getNpcId() == ThomasDTurkeyId)
		{
			// I guess you came to rescue Santa. But you picked the wrong person.
			npc.broadcastPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), _npcStringIds[8]));
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new SavingSanta(-1, qn, "events");
	}
}
