package l2p.gameserver.loginservercon.lspackets;

import l2p.gameserver.Config;
import l2p.gameserver.cache.Msg;
import l2p.gameserver.dao.AccountBonusDAO;
import l2p.gameserver.loginservercon.LoginServerCommunication;
import l2p.gameserver.loginservercon.ReceivablePacket;
import l2p.gameserver.loginservercon.SessionKey;
import l2p.gameserver.loginservercon.gspackets.PlayerInGame;
import l2p.gameserver.model.Player;
import l2p.gameserver.network.GameClient;
import l2p.gameserver.network.GameClient.GameClientState;
import l2p.gameserver.serverpackets.CharacterSelectionInfo;
import l2p.gameserver.serverpackets.LoginFail;
import l2p.gameserver.serverpackets.ServerClose;

public class PlayerAuthResponse extends ReceivablePacket
{
  private String account;
  private boolean authed;
  private int playOkId1;
  private int playOkId2;
  private int loginOkId1;
  private int loginOkId2;
  private double bonus;
  private int bonusExpire;

  public void readImpl()
  {
    account = readS();
    authed = (readC() == 1);
    if (authed)
    {
      playOkId1 = readD();
      playOkId2 = readD();
      loginOkId1 = readD();
      loginOkId2 = readD();
      bonus = readF();
      bonusExpire = readD();
    }
  }

  protected void runImpl()
  {
    SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
    GameClient client = LoginServerCommunication.getInstance().removeWaitingClient(account);
    if (client == null) {
      return;
    }
    if ((authed) && (client.getSessionKey().equals(skey)))
    {
      client.setAuthed(true);
      client.setState(GameClient.GameClientState.AUTHED);
      switch (Config.SERVICES_RATE_TYPE)
      {
      case 0:
        bonus = 1.0D;
        bonusExpire = 0;
        break;
      case 2:
        double[] bonuses = AccountBonusDAO.getInstance().select(account);
        bonus = bonuses[0];
        bonusExpire = (int)bonuses[1];
      }

      client.setBonus(bonus);
      client.setBonusExpire(bonusExpire);

      GameClient oldClient = LoginServerCommunication.getInstance().addAuthedClient(client);
      if (oldClient != null)
      {
        oldClient.setAuthed(false);
        Player activeChar = oldClient.getActiveChar();
        if (activeChar != null)
        {
          activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
          activeChar.logout();
        }
        else
        {
          oldClient.close(ServerClose.STATIC);
        }
      }

      sendPacket(new PlayerInGame(client.getLogin()));

      CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
      client.sendPacket(csi);
      client.setCharSelection(csi.getCharInfo());
    }
    else
    {
      client.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
    }
  }
}