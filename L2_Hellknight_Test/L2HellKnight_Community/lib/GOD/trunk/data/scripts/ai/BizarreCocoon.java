package ai;

import l2rt.gameserver.ai.CtrlEvent;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.SocialAction;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

/**
 * AI Bizarre Cocoon 
 * При юзе итема (14832) вызывается рб. Шанс неудачного спауна 8%. Респаун коконов 3 часа.
 * Кокон использует анимацию раскрытия, при вызове рб. (нету анимации скилла при вызове.. хз какой скилл)
 * @author Drizzy
 * @date 25.08.10
 */

public class BizarreCocoon extends DefaultAI
{
	private static final int Growth_Accelerator = 2905;
	private static final int Stakato_Cheif = 25667;
	
	public BizarreCocoon(L2Character actor)
	{
		super(actor);
	}
	
	protected boolean randomAnimation()
	{
		return false;
	}
	
	@Override
	public boolean isGlobalAI()
	{
		return true;
	}	
	
	@Override
	protected boolean randomWalk()
	{
		return false;
	}	

	public boolean isCrestEnable()
	{
		return false;
	}	
	
	@Override
	protected void onEvtSeeSpell(L2Skill skill, L2Character caster)
	{
		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead() || skill == null)
			return;
			
		if(skill.getId() == Growth_Accelerator)
		{
			if(Rnd.chance(92))
			{
				try
				{
					L2Spawn sp = new L2Spawn(NpcTable.getTemplate(Stakato_Cheif));
					Location pos = GeoEngine.findPointToStay(actor.getX(), actor.getY(), actor.getZ(), 0, 0, actor.getReflection().getGeoIndex());
					sp.setLoc(pos);
					L2NpcInstance npc = sp.doSpawn(true);
					actor.broadcastPacket(new SocialAction(actor.getObjectId(), 1));
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, caster, Rnd.get(1, 100));
					actor.doDie(caster);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}					
			}
			else
			{
			    actor.broadcastPacket(new SocialAction(actor.getObjectId(), 1));
				actor.doDie(caster);
			}
		}		
	}
}