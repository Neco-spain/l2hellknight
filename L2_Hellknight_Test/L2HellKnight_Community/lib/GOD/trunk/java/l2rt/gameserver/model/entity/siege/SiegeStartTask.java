package l2rt.gameserver.model.entity.siege;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.network.serverpackets.SystemMessage;

import java.util.Calendar;

public class SiegeStartTask implements Runnable
{
	private Siege _siege;

	public SiegeStartTask(Siege siege)
	{
		_siege = siege;
	}

	public void run()
	{
		if(_siege.isInProgress())
			return;

		try
		{
			long timeRemaining = _siege.getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
			if(timeRemaining > 86400000)
				ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(_siege), timeRemaining - 86400000); // Prepare task for 24 before siege start to end registration
			else if(timeRemaining <= 86400000 && timeRemaining > 3600000)
			{
				checkRegistrationOver();
				ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(_siege), timeRemaining - 3600000); // Prepare task for 1 hr left before siege start.
			}
			else if(timeRemaining <= 3600000 && timeRemaining > 600000)
			{
				_siege.announceToPlayer(new SystemMessage(Math.round(timeRemaining / 60000) + " minute(s) until " + _siege.getSiegeUnit().getName() + " siege begin."), false, false);
				ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(_siege), timeRemaining - 600000); // Prepare task for 10 minute left.
			}
			else if(timeRemaining <= 600000 && timeRemaining > 300000)
			{
				checkRegistrationOver();
				_siege.announceToPlayer(new SystemMessage(Math.round(timeRemaining / 60000) + " minute(s) until " + _siege.getSiegeUnit().getName() + " siege begin."), false, false);
				ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(_siege), timeRemaining - 300000); // Prepare task for 5 minute left.
			}
			else if(timeRemaining <= 300000 && timeRemaining > 10000)
			{
				checkRegistrationOver();
				_siege.announceToPlayer(new SystemMessage(Math.round(timeRemaining / 60000) + " minute(s) until " + _siege.getSiegeUnit().getName() + " siege begin."), false, false);
				ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(_siege), timeRemaining - 10000); // Prepare task for 10 seconds count down
			}
			else if(timeRemaining <= 10000 && timeRemaining > 0)
			{
				checkRegistrationOver();
				_siege.announceToPlayer(new SystemMessage(_siege.getSiegeUnit().getName() + " siege " + Math.round(timeRemaining / 1000) + " second(s) to start!"), false, false);
				ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStartTask(_siege), timeRemaining); // Prepare task for second count down
			}
			else
				_siege.startSiege();
		}
		catch(Throwable t)
		{}
	}

	private void checkRegistrationOver()
	{
		if(!_siege.isRegistrationOver() && _siege.getSiegeRegistrationEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis() <= 10000)
		{
			_siege.announceToPlayer(new SystemMessage(SystemMessage.THE_REGISTRATION_TERM_FOR_S1_HAS_ENDED).addString(_siege.getSiegeUnit().getName()), false, false);
			_siege.setRegistrationOver(true);
			_siege.getDatabase().clearSiegeClan(SiegeClanType.DEFENDER_WAITING);
			_siege.getDatabase().clearSiegeClan(SiegeClanType.DEFENDER_REFUSED);
		}
	}
}