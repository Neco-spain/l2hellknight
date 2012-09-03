package ai;

import l2rt.gameserver.ai.Fighter;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Spawn;
import l2rt.gameserver.model.Reflection;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 * Инстант для лабиринта бездны (камалока). Предназначен для снижения статов у рб. При убийстве первой комнаты п. деф рб падает на 1\3. 
 * 2-я комната снижает м.деф на 1\3. 3-я комната снижает п.атк на 1\3.
 */
public class LabyrinthOfTheAbyss extends Fighter
{
	private int FirstInt = 0;
	private int SecondInt = 0;
	private boolean FirstRoom = false;
	private boolean SecondRoom = false;
	private boolean ThirdRoom = false;

	public LabyrinthOfTheAbyss(L2Character actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		int InstanceId = actor.getReflection().getInstancedZoneId();
		System.out.println("Instance Id = " + InstanceId);
		switch(InstanceId)
		{
			case 29:
				checkKillProgress(29, actor);
				break;
			case 39:
				checkKillProgress(39, actor);
				break;
			case 49:
				checkKillProgress(49, actor);
				break;
			case 59:
				checkKillProgress(59, actor);
				break;
			case 69:
				checkKillProgress(69, actor);
				break;
			case 74:
				checkKillProgress(74, actor);
				break;
			case 81:
				checkKillProgress(81, actor);
				break;
			case 83:
				checkKillProgress(83, actor);
				break;
		}
		super.onEvtDead(killer);
	}

	private void checkKillProgress(int Id, L2NpcInstance actor)
	{
		System.out.println("checkKillProgress Run");
		Reflection ref = actor.getReflection();
		for(L2Spawn spawn : ref.getSpawns())
		{
			if(!FirstRoom)
			{
				if(spawn.getNpcId() == getFirstId(Id))
				{
					for(L2NpcInstance npc : spawn.getAllSpawned())
					{
						if(npc.isDead())
						{
							if(FirstInt < 9)
							{
								FirstInt++;
							}
							if(FirstInt == 9)
							{
								FirstInt = 0; // Обнуляем на всякий случай.
								FirstRoom = true;
								FirstRoom(actor, Id);
							}
						}
					}
				}
			}
			if(!SecondRoom)
			{
				if(spawn.getNpcId() == getSecondId(Id))
				{
					for(L2NpcInstance npc : spawn.getAllSpawned())
					{
						if(npc.isDead())
						{
							if(SecondInt < 5)
							{
								SecondInt++;
							}
							if(SecondInt == 5)
							{
								SecondInt = 0; //Обнуляем на всякий случай.
								SecondRoom = true;
								SecondRoom(actor, Id);
							}
						}
					}
				}
			}
			if(!ThirdRoom)
			{
				if(spawn.getNpcId() == getThirdId(Id))
				{
					for(L2NpcInstance npc : spawn.getAllSpawned())
					{
						if(npc.isDead())
						{
							ThirdRoom = true;
							ThirdRoom(actor, Id);
						}
					}
				}
			}
		}
	}

	private void FirstRoom(L2NpcInstance actor, int id)
	{
		System.out.println("FirstRoom Run");
		Reflection ref = actor.getReflection();
		for(L2Spawn spawn : ref.getSpawns())
		{
			if(spawn.getNpcId() == getRBId(id))
			{
				for(L2NpcInstance npc : spawn.getAllSpawned())
				{
					int stat = npc.getTemplate().basePDef / 3;
					npc.getTemplate().basePDef -= stat;
				}
			}
		}
	}

	private void SecondRoom(L2NpcInstance actor, int id)
	{
		System.out.println("SecondRoom Run");
		Reflection ref = actor.getReflection();
		for(L2Spawn spawn : ref.getSpawns())
		{
			if(spawn.getNpcId() == getRBId(id))
			{
				for(L2NpcInstance npc : spawn.getAllSpawned())
				{
					int stat = npc.getTemplate().baseMDef / 3;
					npc.getTemplate().baseMDef -= stat;
				}
			}
		}
	}

	private void ThirdRoom(L2NpcInstance actor, int id)
	{
		System.out.println("ThirdRoom Run");
		Reflection ref = actor.getReflection();
		for(L2Spawn spawn : ref.getSpawns())
		{
			if(spawn.getNpcId() == getRBId(id))
			{
				for(L2NpcInstance npc : spawn.getAllSpawned())
				{
					int stat = npc.getTemplate().basePAtk / 3;
					npc.getTemplate().basePAtk -= stat;
				}
			}
		}
	}

	private int getFirstId(int id)
	{
		switch(id)
		{
			case 29:
				return 22485;
			case 39:
				return 22488;
			case 49:
				return 22491;
			case 59:
				return 22494;
			case 69:
				return 22497;
			case 74:
				return 22501;
			case 81:
				return 22503;
			case 83:
				return 25707;
		}
		return 0;
	}

	private int getSecondId(int id)
	{
		switch(id)
		{
			case 29:
				return 22487;
			case 39:
				return 22490;
			case 49:
				return 22493;
			case 59:
				return 22496;
			case 69:
				return 22499;
			case 74:
				return 22502;
			case 81:
				return 22505;
			case 83:
				return 25708;
		}
		return 0;
	}

	private int getThirdId(int id)
	{
		switch(id)
		{
			case 29:
				return 25616;
			case 39:
				return 25617;
			case 49:
				return 25618;
			case 59:
				return 25619;
			case 69:
				return 25620;
			case 74:
				return 25621;
			case 81:
				return 25622;
			case 83:
				return 25709;
		}
		return 0;
	}

	private int getRBId(int id)
	{
		switch(id)
		{
			case 29:
				return 29129;
			case 39:
				return 29132;
			case 49:
				return 29135;
			case 59:
				return 29138;
			case 69:
				return 29141;
			case 74:
				return 29144;
			case 81:
				return 29147;
			case 83:
				return 25710;
		}
		return 0;
	}
}