package intelligence.GrandBosses;

import l2.hellknight.Config;
import l2.hellknight.bflmpsvz.a.L2AttackableAIScript;

import l2.hellknight.gameserver.instancemanager.ZoneManager;
import l2.hellknight.gameserver.model.actor.L2Character;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.zone.L2ZoneType;
import l2.hellknight.gameserver.network.serverpackets.ExStartScenePlayer;

public class Lindvior extends L2AttackableAIScript
{
	private static L2ZoneType _Zone;
	
	public Lindvior(int id, String name, String descr)
	{
		super(id, name, descr);
		_Zone = ZoneManager.getInstance().getZoneById(11040);
		startQuestTimer("lindvior_visit", (Config.PRVNI_SCENA_PO_STARTU_SERVERU), null, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("lindvior_visit"))
		{
			if (_Zone == null)
				return null;
			
			for (L2Character visitor : _Zone.getCharactersInsideArray())
			{
				if (!(visitor instanceof L2PcInstance))
					continue;
				
				((L2PcInstance)visitor).showQuestMovie(ExStartScenePlayer.LINDVIOR);
			} //21600000
			startQuestTimer("lindvior_visit", (Config.INTERVAL_MEZI_SCENAMA), null, null);
			
			return null;
		}

		return super.onAdvEvent(event, npc, player);
	}	
	
	public static void main(String[] args)
	{
		new Lindvior(-1, "Lindvior", "ai");
		if (Config.ENABLE_LOADING_INFO_FOR_SCRIPTS)
			_log.info("Loaded GrandBoss: Lindvior");
	}
}
