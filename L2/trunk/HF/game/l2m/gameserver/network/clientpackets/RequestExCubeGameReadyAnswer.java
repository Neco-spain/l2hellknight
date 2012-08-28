package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestExCubeGameReadyAnswer extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestExCubeGameReadyAnswer.class);
  int _arena;
  int _answer;

  protected void readImpl()
  {
    _arena = (readD() + 1);
    _answer = readD();
  }

  public void runImpl()
  {
    Player player = ((GameClient)getClient()).getActiveChar();
    if (player == null)
      return;
    switch (_answer)
    {
    case 0:
      break;
    case 1:
      HandysBlockCheckerManager.getInstance().increaseArenaVotes(_arena);
      break;
    default:
      _log.warn("Unknown Cube Game Answer ID: " + _answer);
    }
  }
}