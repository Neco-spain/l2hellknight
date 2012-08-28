package net.sf.l2j.gameserver.util.protection;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.util.TimeLogger;
import net.sf.l2j.util.Util;
import net.sf.l2j.util.log.AbstractLogger;

public class GameGuardMain extends GameGuard
{
  private static final Logger _log = AbstractLogger.getLogger(GameGuardMain.class.getName());
  private static Map<L2GameClient, L2PcInstance> _clients = new ConcurrentHashMap();

  private static GameGuardMain _instance = null;

  public static GameGuardMain load() {
    _instance = new GameGuardMain();
    return _instance;
  }

  public void startSession(L2GameClient client)
  {
    _clients.put(client, client.getActiveChar());
  }

  public void closeSession(L2GameClient client)
  {
    _clients.remove(client);
  }

  public boolean checkGameGuardReply(L2GameClient client, int[] reply)
  {
    try {
      if ((reply[3] & 0x4) == 4) {
        client.punishClient();
        return false;
      }

      if (!acceptHwId(client, getHwid(reply[1]))) {
        client.punishClient();
        return false;
      }

      reply[3] &= -256;
      client.getSessionId().clientKey = reply[0];

      if (Config.GAMEGUARD_KEY != reply[3]) {
        client.punishClient();
        return false;
      }

      return true; } catch (Exception e) {
    }
    return false;
  }

  private boolean acceptHwId(L2GameClient client, String hwid)
  {
    if ((client.getMyHWID().equalsIgnoreCase("none")) || (client.getMyHWID().equalsIgnoreCase(hwid))) {
      client.setHWID(hwid);
      return true;
    }

    return false;
  }

  private String getHwid(int hwid) {
    if (Config.VS_HWID) {
      return Util.md5(String.format("%X", new Object[] { Integer.valueOf(hwid) }));
    }

    return "none";
  }

  public void startCheckTask()
  {
    _log.info(TimeLogger.getLogTime() + "Game Guard: loaded.");
    if (Config.GAMEGUARD_INTERVAL > 0) {
      ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheckTask(), Config.GAMEGUARD_INTERVAL);
      _log.info(TimeLogger.getLogTime() + "Game Guard Cron Task: initialized.");
    }
  }

  static class GameGuardCheckTask
    implements Runnable
  {
    public void run()
    {
      new Thread(new Runnable() {
        public void run() { L2GameClient client;
          L2PcInstance player;
          try { client = null;
            player = null;
            for (Map.Entry entry : GameGuardMain._clients.entrySet()) {
              client = (L2GameClient)entry.getKey();
              player = (L2PcInstance)entry.getValue();
              if ((client == null) || (player == null) || 
                (player.isDeleting()) || (player.isInOfflineMode()))
              {
                continue;
              }
              if (!client.isAuthedGG()) {
                client.punishClient();
                continue;
              }

              client.sendPacket(Static.GAME_GUARD);
            }
          } catch (Exception e) {
          }
          ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardMain.GameGuardCheckTask(), Config.GAMEGUARD_INTERVAL);
        }
      }).start();
    }
  }
}