package ai;

import l2rt.config.ConfigSystem;
import l2rt.extensions.listeners.DayNightChangeListener;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.gameserver.Announcements;
import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.instancemanager.HellboundManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.util.Rnd;

/**
 *         AI Shadai для Hellbound.
 *         Раз в сутки по ночам спавнится на определённом месте на острове с шансом 40%.
 *         На оффе некоторые ждут по 2 недели.
 */
public class Shadai extends DefaultAI implements PropertyCollection
{
	private L2Player self;
	L2Player player = self;
	private boolean _tmp1 = false;

	public Shadai(L2Character actor)
	{
		super(actor);
		GameTimeController.getInstance().getListenerEngine().addPropertyChangeListener(GameTimeControllerDayNightChange, new NightInvulDayNightListener());
		actor.decayMe();

		/*if(GameTimeController.getInstance().isNowNight() && !_tmp1)
		{
			_tmp1 = true;
			if(Rnd.chance(Config.CHANCE_SPAWN_SHADAI))
				actor.spawnMe();
		}*/
	}

	private class NightInvulDayNightListener extends DayNightChangeListener
	{
		private NightInvulDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
				switchToNight();
			else
				switchToDay();
		}

		public void switchToNight()
		{
			spawn_despawn(getActor(), true);
		}

		public void switchToDay()
		{
			spawn_despawn(getActor(), false);
		}
	}

	private void spawn_despawn(L2NpcInstance actor, boolean Night)
	{
        int hLevel = HellboundManager.getInstance().getLevel();
        if (hLevel >= 9)
        {
            if(Night)
            {
                if( !actor.isVisible() && Rnd.chance(ConfigSystem.getInt("ChanceSpawnShadai")))
                {
                    actor.spawnMe();
                    if( ConfigSystem.getBoolean("AnnounceShadaiSpawn"))
                    {
                        Announcements.getInstance().announceByCustomMessage("ai.Shadai.announce", null);
                    }
                }
            }
            else
            {
                if(actor.isVisible())
                    actor.decayMe();
            }
        }
	}

	public boolean isGlobalAI()
	{
		return true;
	}
	
	protected boolean randomWalk()
	{
		return false;
	}
}