package net.sf.l2j.loginserver.gameserverpackets;

import java.util.logging.Logger;
import net.sf.l2j.loginserver.GameServerTable;
import net.sf.l2j.loginserver.GameServerTable.GameServerInfo;
import net.sf.l2j.loginserver.clientpackets.ClientBasePacket;

public class ServerStatus extends ClientBasePacket
{
  protected static Logger _log = Logger.getLogger(ServerStatus.class.getName());

  public static final String[] STATUS_STRING = { "Auto", "Good", "Normal", "Full", "Down", "Gm Only" };
  public static final int SERVER_LIST_STATUS = 1;
  public static final int SERVER_LIST_CLOCK = 2;
  public static final int SERVER_LIST_SQUARE_BRACKET = 3;
  public static final int MAX_PLAYERS = 4;
  public static final int TEST_SERVER = 5;
  public static final int STATUS_AUTO = 0;
  public static final int STATUS_GOOD = 1;
  public static final int STATUS_NORMAL = 2;
  public static final int STATUS_FULL = 3;
  public static final int STATUS_DOWN = 4;
  public static final int STATUS_GM_ONLY = 5;
  public static final int ON = 1;
  public static final int OFF = 0;

  public ServerStatus(byte[] decrypt, int serverId)
  {
    super(decrypt);

    GameServerTable.GameServerInfo gsi = GameServerTable.getInstance().getRegisteredGameServerById(serverId);
    if (gsi != null)
    {
      int size = readD();
      for (int i = 0; i < size; i++)
      {
        int type = readD();
        int value = readD();
        switch (type)
        {
        case 1:
          gsi.setStatus(value);
          break;
        case 2:
          gsi.setShowingClock(value == 1);
          break;
        case 3:
          gsi.setShowingBrackets(value == 1);
          break;
        case 5:
          gsi.setTestServer(value == 1);
          break;
        case 4:
          gsi.setMaxPlayers(value);
        }
      }
    }
  }
}