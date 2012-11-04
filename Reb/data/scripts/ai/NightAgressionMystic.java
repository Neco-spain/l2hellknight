package ai;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.ai.Mystic;
import l2r.gameserver.listener.game.OnDayNightChangeListener;
import l2r.gameserver.model.instances.NpcInstance;

/**
 * АИ для мобов, меняющих агресивность в ночное время.<BR>
 * Наследуется на прямую от Mystic.
 *
 * @Author: Death
 * @Date: 23/11/2007
 * @Time: 8:40:10
 */
public class NightAgressionMystic extends Mystic
{
	public NightAgressionMystic(NpcInstance actor)
	{
		super(actor);
		GameTimeController.getInstance().addListener(new NightAgressionDayNightListener());
	}

	private class NightAgressionDayNightListener implements OnDayNightChangeListener
	{
		private NightAgressionDayNightListener()
		{
			if(GameTimeController.getInstance().isNowNight())
				onNight();
			else
				onDay();
		}

		/**
		 * Вызывается, когда на сервере наступает день
		 */
		@Override
		public void onDay()
		{
			getActor().setAggroRange(0);
		}

		/**
		 * Вызывается, когда на сервере наступает ночь
		 */
		@Override
		public void onNight()
		{
			getActor().setAggroRange(-1);
		}
	}
}