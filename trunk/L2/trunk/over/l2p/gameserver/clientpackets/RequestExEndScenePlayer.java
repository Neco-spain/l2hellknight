package l2p.gameserver.clientpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;

public class RequestExEndScenePlayer extends L2GameClientPacket
{
  private int _movieId;

  protected void readImpl()
  {
    _movieId = readD();
  }

  protected void runImpl()
  {
    Player activeChar = ((GameClient)getClient()).getActiveChar();
    if (activeChar == null)
      return;
    if ((!activeChar.isInMovie()) || (activeChar.getMovieId() != _movieId))
    {
      activeChar.sendActionFailed();
      return;
    }
    activeChar.setIsInMovie(false);
    activeChar.setMovieId(0);
    activeChar.decayMe();
    activeChar.spawnMe();
  }
}