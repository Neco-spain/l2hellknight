package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.accounts.Account;
import l2m.loginserver.gameservercon.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChangePassword extends ReceivablePacket
{
  public static final Logger _log = LoggerFactory.getLogger(ChangePassword.class);
  private String _account;
  private String _oldPass;
  private String _newPass;
  private String _hwid;

  protected void readImpl()
  {
    _account = readS();
    _oldPass = readS();
    _newPass = readS();
    _hwid = readS();
  }

  protected void runImpl()
  {
    Account acc = new Account(_account);
    acc.restore();

    acc.update();
  }
}