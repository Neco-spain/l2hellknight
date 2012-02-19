package zone_scripts.Hellbound.Quarry;

import l2.brick.Config;
import l2.brick.gameserver.ThreadPoolManager;
import l2.brick.gameserver.ai.CtrlIntention;
import l2.brick.gameserver.instancemanager.HellboundManager;
import l2.brick.gameserver.model.L2Object;
import l2.brick.gameserver.model.L2Skill;
import l2.brick.gameserver.model.actor.L2Character;
import l2.brick.gameserver.model.actor.L2Npc;
import l2.brick.gameserver.model.actor.instance.L2MonsterInstance;
import l2.brick.gameserver.model.actor.instance.L2PcInstance;
import l2.brick.gameserver.model.quest.Quest;
import l2.brick.gameserver.model.zone.L2ZoneType;
import l2.brick.gameserver.network.serverpackets.CreatureSay;
import l2.brick.util.Rnd;

public class Quarry extends Quest
{
	private static final int SLAVE = 32299;
	private static final int TRUST = 10;
	private static final int ZONE = 40107;
	private static final int[] DROPLIST = { 1876, 1885, 9628 };
	private static final String MSG = "Thank you for saving me! Here is a small gift.";

	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("FollowMe"))
		{
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
			npc.setTarget(player);
			npc.setAutoAttackable(true);
			npc.setWalking();

			return null;
		}
		return event;
	}

	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setAutoAttackable(false);

		return super.onSpawn(npc);
	}

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (HellboundManager.getInstance().getLevel() != 5)
			return "32299.htm";
		else
		{
			if (player.getQuestState(getName()) == null)
				newQuestState(player);

			return "32299-01.htm";
		}
	}

	@Override
	public final String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if (!npc.isDead())
			npc.doDie(attacker);

		return null;
	}

	@Override
	public final String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (skill.isOffensive()
				&& !npc.isDead()
				&& targets.length > 0)
		{
			for (L2Object obj : targets)
			{
				if (obj == npc)
				{
					npc.doDie(caster);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		HellboundManager.getInstance().updateTrust(-TRUST, true);
		npc.setAutoAttackable(false);

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public final String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character instanceof L2Npc
				&& ((L2Npc)character).getNpcId() == SLAVE)
		{
			if (!character.isDead()
					&& !((L2Npc)character).isDecayed()
					&& character.getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW)
			{
				if (HellboundManager.getInstance().getLevel() == 5)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new Decay((L2Npc)character), 1000);
					try
					{
						character.broadcastPacket(new CreatureSay(character.getObjectId(), 0, character.getName(), MSG));
					}
					catch (Exception e)
					{
					}
				}
			}
		}
		return null; 
	}

	private final class Decay implements Runnable
	{
		private final L2Npc _npc;

		public Decay(L2Npc npc)
		{
			_npc = npc;
		}

		@Override
		public void run()
		{
			if (_npc != null && !_npc.isDead())
			{
				if (_npc.getTarget() instanceof L2PcInstance)
					((L2MonsterInstance)_npc).dropItem((L2PcInstance)(_npc.getTarget()), DROPLIST[Rnd.get(DROPLIST.length)], 1);

				_npc.setAutoAttackable(false);
				_npc.deleteMe();
				_npc.getSpawn().decreaseCount(_npc);
				HellboundManager.getInstance().updateTrust(TRUST, true);
			}
		}
	}

	public Quarry(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addSpawnId(SLAVE);
		addFirstTalkId(SLAVE);
		addStartNpc(SLAVE);
		addTalkId(SLAVE);
		addAttackId(SLAVE);
		addSkillSeeId(SLAVE);
		addKillId(SLAVE);
		addEnterZoneId(ZONE);
	}

	public static void main(String[] args)
	{
		new Quarry(-1, Quarry.class.getSimpleName(), "zone_scripts/Hellbound");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded Hellbound: Quarry");
	}
}