package l2rt.gameserver.model.quest;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.instances.L2NpcInstance;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class QuestTimer
{
	public class ScheduleTimerTask implements Runnable
	{
		public void run()
		{
			if(!isActive())
				return;

			L2Player pl = getPlayer();
			if(pl != null && getQuest() != null && getQuest().getName() != null && getName() != null)
				pl.processQuestEvent(getQuest().getName(), getName(), getNpc());
			cancel();
		}
	}

	private boolean _isActive = true;
	private String _name;
	private L2NpcInstance _npc;
	private long _time;
	private long _ownerStoreId = 0;
	private Quest _quest;
	private ScheduledFuture<?> _schedular;

	public QuestTimer(Quest quest, String name, long time, L2NpcInstance npc, L2Player player)
	{
		_name = name;
		_quest = quest;
		_ownerStoreId = player == null ? 0 : player.getStoredId();
		_npc = npc;
		_time = time;
		_schedular = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleTimerTask(), time); // Prepare auto end task
	}

	public void cancel()
	{
		_isActive = false;

		if(_schedular != null)
		{
			// Запоминаем оставшееся время, для возможности возобновления таска
			_time = _schedular.getDelay(TimeUnit.SECONDS);
			_schedular.cancel(false);
		}

		getQuest().removeQuestTimer(this);
	}

	public final boolean isActive()
	{
		return _isActive;
	}

	public final String getName()
	{
		return _name;
	}

	public final L2NpcInstance getNpc()
	{
		return _npc;
	}

	public final L2Player getPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(_ownerStoreId);
	}

	public final Quest getQuest()
	{
		return _quest;
	}

	// Проверяет, совпадают ли указанные параметры с параметрами этого таймера
	public boolean isMatch(Quest quest, String name, L2Player player)
	{
		return quest != null && name != null && quest == getQuest() && name.equalsIgnoreCase(getName()) && player == getPlayer();
	}

	@Override
	public final String toString()
	{
		return _name;
	}

	public long getTime()
	{
		return _time;
	}
}