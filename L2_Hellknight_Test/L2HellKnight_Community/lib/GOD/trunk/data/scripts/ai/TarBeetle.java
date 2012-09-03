package ai;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Effect;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2World;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.Location;
import l2rt.util.Rnd;
import l2rt.util.GArray;

public class TarBeetle extends DefaultAI
{
	static final Location[] points =
	{
		new Location(179256, -117160, -3608),
		new Location(179752, -115000, -3608),
		new Location(177944, -119528, -4112),
		new Location(177144, -120808, -4112),
		new Location(181224, -120088, -3672),
		new Location(181960, -117864, -3328),
		new Location(186200, -118120, -3272),
		new Location(188840, -118696, -3288),
		new Location(185448, -120536, -3088),
		new Location(183672, -119048, -3088),
		new Location(188072, -120824, -3088),
		new Location(189592, -120392, -3048),
		new Location(189448, -117464, -3288),
		new Location(188456, -115816, -3288),
		new Location(186424, -114440, -3280),
		new Location(185112, -113272, -3280),
		new Location(187768, -112952, -3288),
		new Location(189176, -111672, -3288),
		new Location(189960, -108712, -3288),
		new Location(187816, -110536, -3288),
		new Location(185368, -109880, -3288),
		new Location(181848, -109368, -3664),
		new Location(181816, -112392, -3664),
		new Location(180136, -112632, -3664),
		new Location(183608, -111432, -3648),
		new Location(178632, -108568, -3664),
		new Location(176264, -109448, -3664),
		new Location(176072, -112952, -3488),
		new Location(175720, -112136, -5520),
		new Location(178504, -112712, -5816),
		new Location(180248, -116136, -6104),
		new Location(182552, -114824, -6104),
		new Location(184248, -116600, -6104),
		new Location(181336, -110536, -5832),
		new Location(182088, -106664, -6000),
		new Location(178808, -107736, -5832),
		new Location(178776, -110120, -5824),
	};
	
	private boolean canDebuff = false;
	private static final long TELEPORT_PERIOD = 3 * 60 * 1000;
	private long _lastTeleport = System.currentTimeMillis();
	
	public TarBeetle(L2Character actor)
	{
		super(actor);
	}
	
	protected boolean randomWalk()
	{
		return false;
	}
	private void CancelTarget(L2NpcInstance actor)
	{
		int id = 18804;
		if(id != actor.getDisplayId())
		{
			for(L2Player player : L2World.getAroundPlayers(actor))
				if(player.getTarget() == actor)
				{
					player.setTarget(null);
					player.abortAttack(true, false);
					player.abortCast(true);
				}
		}
	}
	@Override
	protected void onEvtAttacked(L2Character attacker, int damage)
	{
		L2NpcInstance actor = getActor();
		CancelTarget(actor);
		super.onEvtAttacked(attacker, damage);
	}
	
	@Override
	protected boolean thinkActive()
	{

		L2NpcInstance actor = getActor();
		CancelTarget(actor);
		
		if(Rnd.chance(3))
			canDebuff = true;
		if(canDebuff == true)
		{
			for(L2Player player : L2World.getAroundPlayers(actor, 300, 200))
				addEffect(actor, player);
				canDebuff = false;
		}
		
		if(actor == null || System.currentTimeMillis() - _lastTeleport < TELEPORT_PERIOD)
			return false;
		
		for(int i = 0; i < points.length; i++)
		{
			Location loc = points[Rnd.get(points.length)];
			if(actor.getLoc().equals(loc))
				continue;
			
			int x = loc.x + Rnd.get(1, 8);
			int y = loc.y + Rnd.get(1, 8);
			int z = GeoEngine.getHeight(x, y, loc.z, actor.getReflection().getGeoIndex());
			
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 4671, 1, 500, 0));
			ThreadPoolManager.getInstance().scheduleAi(new Teleport(new Location(x, y, z)), 500, false);
			_lastTeleport = System.currentTimeMillis();
			break;
		}
		return super.thinkActive();
	}
	
	private void addEffect(L2NpcInstance actor, L2Player player)
	{
		GArray<L2Effect> effect = player.getEffectList().getEffectsBySkillId(6142);
		if(effect != null)
		{
			int level = effect.get(0).getSkill().getLevel();
			if(level < 3)
			{
				effect.get(0).exit();
				L2Skill skill = SkillTable.getInstance().getInfo(6142, level + 1);
				skill.getEffects(actor, player, false, false);
				actor.broadcastPacket(new MagicSkillUse(actor, player, skill.getId(), level, skill.getHitTime(), 0));
			}
		}
		else
		{
			L2Skill skill = SkillTable.getInstance().getInfo(6142, 1);
			if(skill != null)
			{
				skill.getEffects(actor, player, false, false);
				actor.broadcastPacket(new MagicSkillUse(actor, player, skill.getId(), 1, skill.getHitTime(), 0));
			}
			else
				System.out.println("Skill " + skill.getId() + " is null, fix it.");
		}
	}
	
	public boolean isCrestEnable()
	{
		return false;
	}
}