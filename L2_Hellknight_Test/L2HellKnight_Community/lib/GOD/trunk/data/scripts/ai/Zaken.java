package ai;

import bosses.ZakenManager;
import javolution.util.FastMap;
import l2rt.common.ThreadPoolManager;
import l2rt.extensions.listeners.DayNightChangeListener;
import l2rt.extensions.listeners.L2ZoneEnterLeaveListener;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.geodata.GeoEngine;
import l2rt.gameserver.instancemanager.InstancedZoneManager;
import l2rt.gameserver.instancemanager.ZoneManager;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.L2Zone.ZoneType;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.network.serverpackets.ExSendUIEvent;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.PlaySound;
import l2rt.gameserver.tables.NpcTable;
import l2rt.util.Location;
import l2rt.util.Rnd;

import java.util.HashMap;

/**
 * Индивидуальное АИ эпик боса Zaken.
 * - Спаунит каждые 2 минуты, 3 моба (охрану).
 * - имеет усиленный реген ночью<BR>
 * - получает 25% пенальти на реген в солнечной комнате
 * - после смерти проигрывает музыку<BR>
 */
public class Zaken extends DefaultAI
{
	//Npc Day and Night
	private static final int[] mobs = { 29023, 29024, 29026, 29027 };
	//Npc Day High
	private static final int[] _mobs = { 29182, 29183, 29184, 29185 };
	//Skills
	private final int FaceChanceNightToDay = 4223;
	private final int FaceChanceDayToNight = 4224;
	private final L2Skill AbsorbHPMP;
	private final L2Skill Hold;
	private final L2Skill DeadlyDualSwordWeapon;
	private final L2Skill DeadlyDualSwordWeaponRangeAttack;
	//Other
	private final float _baseHpReg;
	private final float _baseMpReg;
	private boolean _isInLightRoom = false;	
	private String txt = "";	
	//Listener
	private static final L2Zone _zone = ZoneManager.getInstance().getZoneById(ZoneType.no_restart, 1335, true);
	private ZoneListener _zoneListener = new ZoneListener();
	private NightInvulDayNightListener _timeListener = new NightInvulDayNightListener();
    private boolean spawn = false;

    public Zaken(L2Character actor)
	{
		super(actor);

		HashMap<Integer, L2Skill> skills = getActor().getTemplate().getSkills();

		AbsorbHPMP = skills.get(4218);
		Hold = skills.get(4219);
		DeadlyDualSwordWeapon = skills.get(4220);
		DeadlyDualSwordWeaponRangeAttack = skills.get(4221);

		_baseHpReg = actor.getTemplate().baseHpReg;
		_baseMpReg = actor.getTemplate().baseMpReg;
	}

    @Override
	protected void thinkAttack()
	{
		L2NpcInstance actor = getActor();
		if(actor == null)
			return;

        int instanceId = actor.getReflection().getInstancedZoneId();
        if(spawn == false)
        {
            if(instanceId == 515 || instanceId == 516)
            {
                spawnGuards(mobs[Rnd.get(mobs.length)]);
				spawnGuards(mobs[Rnd.get(mobs.length)]);
				spawnGuards(mobs[Rnd.get(mobs.length)]);
                spawn = true;
                ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(), 120000);
            }
            if(instanceId == 517)
            {
                spawnGuards(_mobs[Rnd.get(_mobs.length)]);
				spawnGuards(_mobs[Rnd.get(mobs.length)]);
				spawnGuards(_mobs[Rnd.get(mobs.length)]);
                spawn = true;
                ThreadPoolManager.getInstance().scheduleGeneral(new SpawnTask(), 120000);
            }
        }
		super.thinkAttack();
	}



    private void spawnGuards(int mob)
    {
        L2NpcInstance actor = getActor();
        ZakenManager.ZakenInstanceInfo instanceInfo = ZakenManager.instances.get(actor.getReflectionId());
        Location loc = instanceInfo.getZakenLoc();
        Location pos = GeoEngine.findPointToStay(loc.x, loc.y, loc.z, 200, 200, actor.getReflection().getGeoIndex());
		try
		{
			L2Spawn spawn = new L2Spawn(NpcTable.getTemplate(mob));
			actor.getReflection().addSpawn(spawn);
			spawn.setReflection(actor.getReflectionId());
			spawn.setRespawnDelay(0, 0);
			spawn.setLocation(0);
			spawn.setLoc(pos);
			spawn.doSpawn(true);
			spawn.stopRespawn();
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}	
    }

    @Override
	protected boolean createNewTask()
	{
		clearTasks();
		L2Character target;
		if((target = prepareTarget()) == null)
			return false;

		L2NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;

		int rnd_per = Rnd.get(100);

		double distance = actor.getDistance(target);

		if(!actor.isAMuted() && rnd_per < 75)
			return chooseTaskAndTargets(null, target, distance);

		FastMap<L2Skill, Integer> d_skill = new FastMap<L2Skill, Integer>();

		addDesiredSkill(d_skill, target, distance, DeadlyDualSwordWeapon);
		addDesiredSkill(d_skill, target, distance, DeadlyDualSwordWeaponRangeAttack);
		addDesiredSkill(d_skill, target, distance, Hold);
		addDesiredSkill(d_skill, target, distance, AbsorbHPMP);

		L2Skill r_skill = selectTopSkill(d_skill);

		return chooseTaskAndTargets(r_skill, target, distance);
	}

	/**
	 * Метод вызываемый при смерте закена.
	 */
	@Override
	protected void onEvtDead(L2Character killer)
	{
		L2NpcInstance actor = getActor();
		if(actor != null)
			actor.broadcastPacket(new PlaySound(1, "BS02_D", 1, actor.getObjectId(), actor.getLoc()));

		String name = InstancedZoneManager.getInstance().getById(getActor().getReflection().getInstancedZoneId()).get(0).getName();
		L2Player player = killer.getPlayer();
		L2Party party = player.getParty();
		Reflection r = player.getReflection();		
		if(party.isInCommandChannel())
		{
			r.clearReflection(10, true);
			L2CommandChannel cc = party.getCommandChannel();
			for(L2Player member : cc.getMembers())
			{
				member.sendPacket(new ExSendUIEvent(member, true, true, 0, 10, txt));
				member.setVar(name, String.valueOf(System.currentTimeMillis()));
			}
		}
		else
		{
			r.clearReflection(10, true);
			for(L2Player member : party.getPartyMembers())
			{
				member.sendPacket(new ExSendUIEvent(member, true, true, 0, 10, txt));
				member.setVar(name, String.valueOf(System.currentTimeMillis()));
			}			
		}
        ZakenManager.instances.remove(r.getId());
		super.onEvtDead(killer);
	}

	/**
	 * Запуск АИ
	 */
	@Override
	public void startAITask()
	{
		if(_aiTask == null)
		{
			GameTimeController.getInstance().getListenerEngine().addPropertyChangeListener(PropertyCollection.GameTimeControllerDayNightChange, _timeListener);
			_zone.getListenerEngine().addMethodInvokedListener(_zoneListener);
		}
		super.startAITask();
	}

	/**
	 * Остановка АИ.
	 */
	@Override
	public void stopAITask()
	{
		if(_aiTask != null)
		{
			GameTimeController.getInstance().getListenerEngine().removePropertyChangeListener(PropertyCollection.GameTimeControllerDayNightChange, _timeListener);
			_zone.getListenerEngine().removeMethodInvokedListener(_zoneListener);
		}
		super.stopAITask();
	}

	/**
	 * Листенер времени суток. (День, ночь)
	 */
	private class NightInvulDayNightListener extends DayNightChangeListener
	{
		private NightInvulDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
				switchToNight();
			else
				switchToDay();
		}

		/**
		 * Вызывается, когда на сервере наступает ночь
		 */
		@Override
		public void switchToNight()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
			{
				if(_isInLightRoom)
				{
					actor.getTemplate().baseHpReg = (float) (_baseHpReg * 7.5);
					actor.getTemplate().baseMpReg = (float) (_baseMpReg * 7.5);
				}
				else
				{
					actor.getTemplate().baseHpReg = (float) (_baseHpReg * 10.);
					actor.getTemplate().baseMpReg = (float) (_baseMpReg * 10.);
				}
				actor.broadcastPacket(new MagicSkillUse(actor, actor, FaceChanceDayToNight, 1, 1100, 0));
			}
		}

		/**
		 * Вызывается, когда на сервере наступает день
		 */
		@Override
		public void switchToDay()
		{
			L2NpcInstance actor = getActor();
			if(actor != null)
			{
				actor.getTemplate().baseHpReg = _baseHpReg;
				actor.getTemplate().baseMpReg = _baseMpReg;
				actor.broadcastPacket(new MagicSkillUse(actor, actor, FaceChanceNightToDay, 1, 1100, 0));
			}
		}
	}
	
	/**
	 * Листенер зон (вход - выход).
	 */
	private class ZoneListener extends L2ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(L2Zone zone, L2Object object)
		{
			L2NpcInstance actor = getActor();
			if(actor == null)
				return;
			actor.getTemplate().baseHpReg = (float) (_baseHpReg * 0.75);
			actor.getTemplate().baseMpReg = (float) (_baseMpReg * 0.75);
			_isInLightRoom = true;
		}

		@Override
		public void objectLeaved(L2Zone zone, L2Object object)
		{
			L2NpcInstance actor = getActor();
			if(actor == null)
				return;
			actor.getTemplate().baseHpReg = _baseHpReg;
			actor.getTemplate().baseMpReg = _baseMpReg;
			_isInLightRoom = false;
		}
	}

    private class SpawnTask implements Runnable {

        public SpawnTask() {
        }

        @Override
        public void run() {
            spawn = false;
        }
    }
}