package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.accounts.Account;
import l2m.loginserver.gameservercon.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveAccountPoint extends ReceivablePacket
{
  public static final Logger _log = LoggerFactory.getLogger(ChangeAccessLevel.class);
  private String account;
  private int point;

  protected void readImpl()
  {
    account = readS();
    point = readD();
  }

  protected void runImpl()
  {
    Account acc = new Account(account);
    acc.setAccountPoints(point);
  }
}