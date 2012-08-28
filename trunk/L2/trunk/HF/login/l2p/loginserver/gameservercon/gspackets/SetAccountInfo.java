package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.accounts.Account;
import l2m.loginserver.accounts.SessionManager;
import l2m.loginserver.accounts.SessionManager.Session;
import l2m.loginserver.gameservercon.GameServer;
import l2m.loginserver.gameservercon.ReceivablePacket;
import org.apache.commons.lang3.ArrayUtils;

public class SetAccountInfo extends ReceivablePacket
{
  private String _account;
  private int _size;
  private int[] _deleteChars;

  protected void readImpl()
  {
    _account = readS();
    _size = readC();
    int size = readD();
    if ((size > 7) || (size <= 0)) {
      _deleteChars = ArrayUtils.EMPTY_INT_ARRAY;
    }
    else {
      _deleteChars = new int[size];
      for (int i = 0; i < _deleteChars.length; i++)
        _deleteChars[i] = readD();
    }
  }

  protected void runImpl()
  {
    GameServer gs = getGameServer();
    if (gs.isAuthed())
    {
      SessionManager.Session session = SessionManager.getInstance().getSessionByName(_account);
      if (session == null)
        return;
      session.getAccount().addAccountInfo(gs.getId(), _size, _deleteChars);
    }
  }
}