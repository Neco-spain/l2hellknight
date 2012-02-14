package l2rt.gameserver.ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.instancemanager.SiegeManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Clan;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill.SkillTargetType;
import l2rt.gameserver.model.entity.siege.Siege;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

import java.lang.ref.WeakReference;

public class L2StaticObjectAI extends L2CharacterAI
{
	private long _attacker;
	private WeakReference<Siege> _siege = null;

	public L2StaticObjectAI(L2Character actor)
	{
		super(actor);
	}

	private Siege getSiege()
	{
		Siege result = _siege == null ? null : _siege.get();
		if(result == null)
		{
			result = SiegeManager.getSiege(getActor(), true);
			_siege = result != null ? new WeakReference<Siege>(result) : null;
		}
		return result;
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2Character actor;
		if(attacker == null || (actor = getActor()) == null || !actor.isDoor())
			return;

		L2Player player = attacker.getPlayer();
		if(player == null)
			return;

		L2Clan clan = player.getClan();
		Siege siege = SiegeManager.getSiege(actor, true);

		if(siege == null)
			return;

		if(clan != null && siege == clan.getSiege() && clan.isDefender())
			return;

		for(L2NpcInstance npc : actor.getAroundNpc(900, 500))
		{
			if(!npc.isSiegeGuard())
				continue;
			npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.chance(20) ? 10000 : 2000);
		}
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{
		L2Character actor;
		L2Player player;
		if(attacker == null || (player = attacker.getPlayer()) == null || (actor = getActor()) == null)
			return;

		if(actor.isArtefact())
		{
			L2Clan clan = player.getClan();
			if(clan == null || !clan.isDefender() || getSiege() != clan.getSiege())
				ThreadPoolManager.getInstance().scheduleAi(new notifyGuard(player), 1000, false);
		}
	}

	class notifyGuard implements Runnable
	{
		public notifyGuard(L2Player attacker)
		{
			_attacker = attacker.getStoredId();
		}

		@Override
		public void run()
		{
			L2Character actor;
			L2Player attacker = L2ObjectsStorage.getAsPlayer(_attacker);
			if(attacker == null || (actor = getActor()) == null)
				return;

			for(L2NpcInstance npc : actor.getAroundNpc(1500, 200))
				if(npc.isSiegeGuard() && Rnd.chance(20))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _attacker, 5000);

			if(attacker.getCastingSkill() != null && attacker.getCastingSkill().getTargetType() == SkillTargetType.TARGET_HOLY)
				ThreadPoolManager.getInstance().scheduleAi(this, 10000, false);
		}
	}
}