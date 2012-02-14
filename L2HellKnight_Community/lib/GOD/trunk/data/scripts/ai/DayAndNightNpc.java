package ai;

import l2rt.extensions.listeners.DayNightChangeListener;
import l2rt.extensions.listeners.PropertyCollection;
import l2rt.gameserver.GameTimeController;
import l2rt.gameserver.ai.DefaultAI;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.instances.L2NpcInstance;

/**
 *  NPC AI
 *  В ночное время исчезает.
 *  В дневное время спавнится.
 */
public class DayAndNightNpc extends DefaultAI implements PropertyCollection
{
	private L2NpcInstance actor = getActor();

	public DayAndNightNpc(L2Character actor)
	{
		super(actor);
		GameTimeController.getInstance().getListenerEngine().addPropertyChangeListener(GameTimeControllerDayNightChange, new NightAgressionDayNightListener());
	}

	private class NightAgressionDayNightListener extends DayNightChangeListener
	{
		private NightAgressionDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
				switchToNight();
			else
				switchToDay();
		}

		/**
		 * Исчезает, когда на сервере наступает ночь
		 */
		@Override
		public void switchToNight()
		{
			if(actor != null)
				actor.decayMe();
		}

		/**
		 * Спавнится, когда на сервере наступает день
		 */
		@Override
		public void switchToDay()
		{
			if(actor == null)
				actor.spawnMe();
		}
	}
}