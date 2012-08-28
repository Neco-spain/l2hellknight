package l2m.gameserver.network.clientpackets;

import l2m.gameserver.instancemanager.games.HandysBlockCheckerManager;
import l2m.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RequestExCubeGameChangeTeam extends L2GameClientPacket
{
  private static final Logger _log = LoggerFactory.getLogger(RequestExCubeGameChangeTeam.class);
  int _team;
  int _arena;

  protected void readImpl()
  {
    _arena = (readD() + 1);
    _team = readD();
  }

  protected void runImpl()
  {
    if (HandysBlockCheckerManager.getInstance().arenaIsBeingUsed(_arena))
      return;
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if ((activeChar == null) || (activeChar.isDead())) {
      return;
    }
    switch (_team)
    {
    case 0:
    case 1:
      HandysBlockCheckerManager.getInstance().changePlayerToTeam(activeChar, _arena, _team);
      break;
    case -1:
      int team = HandysBlockCheckerManager.getInstance().getHolder(_arena).getPlayerTeam(activeChar);

      if (team <= -1) break;
      HandysBlockCheckerManager.getInstance().removePlayer(activeChar, _arena, team); break;
    default:
      _log.warn("Wrong Team ID: " + _team);
    }
  }
}