package net.sf.l2j.gameserver.network;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.LoginServerThread.SessionKey;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class HwidDisconnection
  implements Runnable
{
  private L2PcInstance _activeChar;
  public int baseresult;
  public int clientresult = -1;

  public HwidDisconnection(L2PcInstance activeChar)
  {
    _activeChar = activeChar;
  }

  public void run() {
    if (_activeChar == null) {
      return;
    }
    int ishwidguard = 0;

    Connection con = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      PreparedStatement preparedstatement1 = con.prepareStatement("SELECT HWIDBlock FROM accounts WHERE login=?");
      preparedstatement1.setString(1, _activeChar.getAccountName());
      ResultSet resultset1 = preparedstatement1.executeQuery();
      resultset1.next();

      PreparedStatement preparedstatement2 = con.prepareStatement("SELECT HWIDBlockON FROM accounts WHERE login=?");
      preparedstatement2.setString(1, _activeChar.getAccountName());
      ResultSet resultset2 = preparedstatement2.executeQuery();
      resultset2.next();

      baseresult = resultset1.getInt(1);
      if ((_activeChar.getClient() != null) && (_activeChar.getClient().getSessionId() != null)) {
        clientresult = _activeChar.getClient().getSessionId().clientKey;
      }
      ishwidguard = resultset2.getInt(1);

      preparedstatement1.close();
      resultset1.close();
      preparedstatement2.close();
      resultset2.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally {
      try {
        con.close(); } catch (Exception e) { e.printStackTrace();
      }
    }
    if (ishwidguard == 1)
    {
      if (baseresult == clientresult)
      {
        _activeChar.setClientKey(true);
      }
      else
      {
        _activeChar.sendMessage("\u042D\u0442\u043E \u0430\u043A\u043A\u0430\u0443\u043D\u0442 \u043F\u0440\u0438\u0432\u044F\u0437\u0430\u043D \u043A \u0434\u0440\u0443\u0433\u043E\u043C\u0443 \u043A\u043E\u043C\u043F\u044C\u044E\u0442\u0435\u0440\u0443");
        _activeChar.closeNetConnection(false);
      }
    }
  }
}