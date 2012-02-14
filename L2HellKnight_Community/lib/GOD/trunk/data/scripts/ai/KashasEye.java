package ai;

import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Object;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2MonsterInstance;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.L2Zone;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.gameserver.network.serverpackets.DeleteObject;
import l2rt.gameserver.network.serverpackets.L2GameServerPacket;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.NpcInfo;
import l2rt.gameserver.network.serverpackets.StatusUpdate;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Rnd;

/**
 * AI KashasEye в Den of Evil:
 * @re-work by Drizzy: Реализовано отменение бафа\дебафа при выходе из локации.
 * @date: 15.08.10
 */
 
public class KashasEye extends DefaultAI
{
	private static final int BuffsGreen[] = { 6150, 6152, 6154 };
	private static final int BuffsBlue[] = { 6151, 6153, 6155 };
	private static final int DebuffRed = 6149;
	private static final L2Zone _zone = ZoneManager.getInstance().getZoneById(ZoneType.other, 854222, true);
	private ZoneListener _zoneListener = new ZoneListener();

	public KashasEye(L2Character actor)
	{
		super(actor);
	}
	
	public class ZoneListener extends L2ZoneEnterLeaveListener
	{
		public ZoneListener()
		{
		}
		
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			if(object != null && object.isPlayer())
			{
				removebuff((L2Player) object);
			}
		}
	}
	
	public void removebuff(L2Player p)
	{
		if(p.getEffectList().getEffectsBySkillId(6149) != null)
			p.getEffectList().stopEffect(6149);
		if(p.getEffectList().getEffectsBySkillId(6150) != null)
			p.getEffectList().stopEffect(6150);
		if(p.getEffectList().getEffectsBySkillId(6151) != null)
			p.getEffectList().stopEffect(6151);
		if(p.getEffectList().getEffectsBySkillId(6152) != null)
			p.getEffectList().stopEffect(6152);
		if(p.getEffectList().getEffectsBySkillId(6153) != null)
			p.getEffectList().stopEffect(6153);
		if(p.getEffectList().getEffectsBySkillId(6154) != null)
			p.getEffectList().stopEffect(6154);
		if(p.getEffectList().getEffectsBySkillId(6155) != null)
			p.getEffectList().stopEffect(6155);
	}

	@Override
	public void startAITask()
	{
		if(_aiTask == null)
		{
			_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
			L2MonsterInstance actor = (L2MonsterInstance) getActor();
			if(actor != null)
				changeAura(actor);
		}
		super.startAITask();
	}

	@Override
	protected boolean thinkActive()
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;

		if(Rnd.chance(5))
			for(L2Player player : L2World.getAroundPlayers(actor, 300, 200))
				switch(actor.getDisplayId())
				{
					case 18812: // red
						addEffect(actor, player, DebuffRed);
						break;
					case 18813: // green
						addEffect(actor, player, BuffsGreen[Rnd.get(BuffsGreen.length)]);
						break;
					case 18814: // blue
						addEffect(actor, player, BuffsBlue[Rnd.get(BuffsBlue.length)]);
						break;
				}

		return super.thinkActive();
	}

	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		if(actor != null && attacker != null && Rnd.chance(10))
			changeAura(actor);
	}

	@Override
	protected void onEvtAggression(L2Character attacker, int aggro)
	{}
	
	public static L2Zone getZone()
	{
		return _zone;
	}

	private void changeAura(L2NpcInstance actor)
	{
		int id = 18812 + Rnd.get(3);
		if(id != actor.getDisplayId())
		{
			actor.setDisplayId(id);
			DeleteObject d = new DeleteObject(actor);
			L2GameServerPacket su = actor.makeStatusUpdate(StatusUpdate.CUR_HP, StatusUpdate.MAX_HP);
			for(L2Player player : L2World.getAroundPlayers(actor))
			{
				player.sendPacket(d, new NpcInfo(actor, player));
				if(player.getTarget() == actor)
				{
					player.setTarget(null);
					player.setTarget(actor);
					player.sendPacket(su);
				}
			}
		}
	}

	private void addEffect(L2NpcInstance actor, L2Player player, int id)
	{
		GArray<L2Effect> effect = player.getEffectList().getEffectsBySkillId(id);
		if(effect != null)
		{
			if(id == DebuffRed)
				return;
			int level = effect.get(0).getSkill().getLevel();
			if(level < 4)
			{
				effect.get(0).exit();
				L2Skill skill = SkillTable.getInstance().getInfo(id, level + 1);
				skill.getEffects(actor, player, false, false);
				actor.broadcastPacket(new MagicSkillUse(actor, player, skill.getId(), level, skill.getHitTime(), 0));
			}
		}
		else
		{
			L2Skill skill = SkillTable.getInstance().getInfo(id, 1);
			if(skill != null)
			{
				skill.getEffects(actor, player, false, false);
				actor.broadcastPacket(new MagicSkillUse(actor, player, skill.getId(), 1, skill.getHitTime(), 0));
			}
			else
				System.out.println("Skill " + id + " is null, fix it.");
		}
	}
}
