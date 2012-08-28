package scripts.autoevents.openseason;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class OpenSeason
{
  private static OpenSeason _instance = new OpenSeason();

  public static OpenSeason getEvent() {
    return _instance;
  }

  public void regPlayer(L2PcInstance player)
  {
  }

  public void increasePoints(int teamId)
  {
  }

  public int getTeam(int playerId)
  {
    return 0;
  }
}