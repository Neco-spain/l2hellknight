package l2p.gameserver.loginservercon.gspackets;

import l2p.gameserver.loginservercon.SendablePacket;
import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.network.GameClient;

public class PlayerAuthRequest extends SendablePacket
{
  private String account;
  private int playOkID1;
  private int playOkID2;
  private int loginOkID1;
  private int loginOkID2;

  public PlayerAuthRequest(GameClient client)
  {
    account = client.getLogin();
    playOkID1 = client.getSessionKey().playOkID1;
    playOkID2 = client.getSessionKey().playOkID2;
    loginOkID1 = client.getSessionKey().loginOkID1;
    loginOkID2 = client.getSessionKey().loginOkID2;
  }

  protected void writeImpl()
  {
    writeC(2);
    writeS(account);
    writeD(playOkID1);
    writeD(playOkID2);
    writeD(loginOkID1);
    writeD(loginOkID2);
  }
}