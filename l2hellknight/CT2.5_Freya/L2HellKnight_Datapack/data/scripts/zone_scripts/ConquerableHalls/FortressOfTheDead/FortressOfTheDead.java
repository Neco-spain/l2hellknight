package zone_scripts.ConquerableHalls.FortressOfTheDead;

import gnu.trove.TIntIntHashMap;

import l2.hellknight.gameserver.GameTimeController;
import l2.hellknight.gameserver.ThreadPoolManager;
import l2.hellknight.gameserver.datatables.ClanTable;
import l2.hellknight.gameserver.model.L2Clan;
import l2.hellknight.gameserver.model.actor.L2Npc;
import l2.hellknight.gameserver.model.actor.instance.L2PcInstance;
import l2.hellknight.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import l2.hellknight.gameserver.network.clientpackets.Say2;

public final class FortressOfTheDead extends ClanHallSiegeEngine
{		
	private static final String qn = "FortressOfTheDead";
	
	private static final int LIDIA = 35629;
	private static final int ALFRED = 35630;
	private static final int GISELLE = 35631;
	
	private static TIntIntHashMap _damageToLidia = new TIntIntHashMap();
	
	/**
	 * @param questId
	 * @param name
	 * @param descr
	 */
	public FortressOfTheDead(int questId, String name, String descr, final int hallId)
	{
		super(questId, name, descr, hallId);
		addKillId(LIDIA);
		addKillId(ALFRED);
		addKillId(GISELLE);
		
		addSpawnId(LIDIA);
		addSpawnId(ALFRED);
		addSpawnId(GISELLE);
		
		addAttackId(LIDIA);
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == LIDIA)
			broadcastNpcSay(npc, Say2.SHOUT, 1010624);
		else if(npc.getNpcId() == ALFRED)
			broadcastNpcSay(npc, Say2.SHOUT, 1010636);
		else if(npc.getNpcId() == GISELLE)
			broadcastNpcSay(npc, Say2.SHOUT, 1010637);
		return null;
	}
	
	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(!_hall.isInSiege())
			return null;
		
		synchronized(this)
		{
			final L2Clan clan = attacker.getClan();
				
			if(clan != null && checkIsAttacker(clan))
			{
				final int id = clan.getClanId();
				if(id > 0 && _damageToLidia.containsKey(id))
				{
					int newDamage = _damageToLidia.get(id);
					newDamage += damage;
					_damageToLidia.put(id, newDamage);
				}
				else
					_damageToLidia.put(id, damage);
			}
		}
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(!_hall.isInSiege()) return null;
		
		final int npcId = npc.getNpcId();
		
		if(npcId == ALFRED || npcId == GISELLE)
			broadcastNpcSay(npc, Say2.SHOUT, 1010625);
		if(npcId == LIDIA)
		{
			broadcastNpcSay(npc, Say2.SHOUT, 1010639);
			_missionAccomplished = true;
			synchronized(this)
			{
				cancelSiegeTask();
				endSiege();
			}
		}
			
		return null;
	}
			
	@Override
	public L2Clan getWinner()
	{
		int counter = 0;
		int damagest = 0;
		for(int clan : _damageToLidia.keys())
		{
			final int damage = _damageToLidia.get(clan);
			if(damage > counter)
			{
				counter = damage;
				damagest = clan;
			}
		}
		return ClanTable.getInstance().getClan(damagest);
	}
	
	@Override
	public void startSiege()
	{
		/*
		 * Siege must start at night
		 */
		int hoursLeft = (GameTimeController.getInstance().getGameTime() / 60) % 24;
		
		if(hoursLeft < 0 || hoursLeft > 6)
		{
			cancelSiegeTask();
			long scheduleTime = (24 - hoursLeft) * 10 * 60000;
			_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), scheduleTime);
		}
		else
			super.startSiege();
	}
		
	public static void main(String[] args)
	{
		new FortressOfTheDead(-1, qn, "conquerablehalls", FORTRESS_OF_DEAD);
	}
}
