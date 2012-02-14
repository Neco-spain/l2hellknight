package l2rt.gameserver.model.entity.Auction;

import l2rt.common.ThreadPoolManager;

public class AutoEndTask implements Runnable
{
	private final Auction auction;

	public AutoEndTask(Auction auction)
	{
		this.auction = auction;
	}

	public void run()
	{
		try
		{
			long timeRemaining = auction.getTimeRemaining();
			if(timeRemaining > 1800000) // 30 mins or more
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(auction), timeRemaining - 1800000);
			else if(timeRemaining > 600000) // 30 - 10 mins
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(auction), 60000);
			else if(timeRemaining > 0) // 10 - 0 mins
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(auction), 5000);
			else
				auction.endAuction();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}