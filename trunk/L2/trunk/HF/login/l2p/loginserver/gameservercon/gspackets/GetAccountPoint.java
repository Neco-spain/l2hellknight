package l2m.loginserver.gameservercon.gspackets;

import l2m.loginserver.accounts.Account;
import l2m.loginserver.gameservercon.ReceivablePacket;
import l2m.loginserver.gameservercon.lspackets.SetAccountPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAccountPoint extends ReceivablePacket
{
  public static final Logger _log = LoggerFactory.getLogger(ChangeAccessLevel.class);
  private String account;

  protected void readImpl()
  {
    account = readS();
  }

  protected void runImpl()
  {
    Account acc = new Account(account);
    sendPacket(new SetAccountPoint(account, acc.getAccountPoints()));
  }
}