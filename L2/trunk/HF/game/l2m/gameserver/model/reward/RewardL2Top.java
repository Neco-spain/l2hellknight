package l2m.gameserver.model.reward;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.StringTokenizer;
import l2p.commons.configuration.ExProperties;
import l2p.commons.dbutils.DbUtils;
import l2m.gameserver.Config;
import l2m.gameserver.ThreadPoolManager;
import l2m.gameserver.database.DatabaseFactory;
import l2m.gameserver.model.GameObjectsStorage;
import l2m.gameserver.model.Player;
import l2m.gameserver.model.items.ItemInstance;
import l2m.gameserver.model.items.PcInventory;
import l2m.gameserver.utils.ItemFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RewardL2Top
{
  private static final Logger log = LoggerFactory.getLogger(RewardL2Top.class);
  private static boolean Enable;
  private static String Url;
  private static long RefreshTime;
  private static int MinLvl;
  private static boolean RandomItemCount;
  private static int RewardItemId;
  private static int RewardItemCount;
  private BufferedReader reader;
  private static RewardL2Top Instance = new RewardL2Top();

  public static RewardL2Top getInstance()
  {
    return Instance;
  }

  private RewardL2Top()
  {
    loadSettings();

    if (Enable)
    {
      log.info("L2Top Bonus service loaded");
      ThreadPoolManager.getInstance().scheduleAtFixedDelay(new ParseTask(), 60000L, RefreshTime);
    }
  }

  private static void loadSettings()
  {
    ExProperties properties = Config.load("config/services/l2top.ini");

    Enable = properties.getProperty("Enable", false);
    Url = properties.getProperty("Url", "");
    RefreshTime = properties.getProperty("RefreshTime", 10L) * 60L * 1000L;
    MinLvl = properties.getProperty("MinLvl", 1);
    RandomItemCount = properties.getProperty("RandomItemCount", false);
    RewardItemId = properties.getProperty("RewardItemId", 57);
    RewardItemCount = properties.getProperty("RewardItemCount", 1);
  }

  public void resultVotes()
  {
    try
    {
      String line = null;
      BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(Url).openStream(), "UTF8"));
      while ((line = reader.readLine()) != null)
      {
        StringTokenizer st = new StringTokenizer(line, "- :\t");
        while (st.hasMoreTokens())
        {
          try
          {
            Calendar vote_time = Calendar.getInstance();
            vote_time.set(1, Integer.parseInt(st.nextToken()));
            vote_time.set(2, Integer.parseInt(st.nextToken()));
            vote_time.set(5, Integer.parseInt(st.nextToken()));
            vote_time.set(11, Integer.parseInt(st.nextToken()));
            vote_time.set(12, Integer.parseInt(st.nextToken()));
            vote_time.set(13, Integer.parseInt(st.nextToken()));
            vote_time.set(14, 0);

            String name = st.nextToken();
            Player player = GameObjectsStorage.getPlayer(name);
            if (player != null)
              rewardItem(name, vote_time.getTimeInMillis() / 1000L);
          }
          catch (Exception e)
          {
          }
        }
      }
    }
    catch (Exception e)
    {
      log.warn("Error L2Top storing data.");
    }
  }

  public void rewardItem(String name, long time)
  {
    Connection _con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      _con = DatabaseFactory.getInstance().getConnection();
      st = _con.prepareStatement("SELECT * FROM `character_votes` WHERE `name`=? AND `time`=? AND `type`='l2top' LIMIT 1");
      st.setString(1, name);
      st.setLong(2, time);
      rs = st.executeQuery();
      if (!rs.next())
      {
        Player player = GameObjectsStorage.getPlayer(name);
        if ((player != null) && (player.isOnline()))
        {
          player.getInventory().addItem(RewardItemId, RewardItemCount);
          player.sendMessage("\u0412\u0430\u043C \u043D\u0430\u0447\u0438\u0441\u043B\u0435\u043D\u043E " + RewardItemCount + " " + ItemFunctions.createItem(RewardItemId).getName() + " \u0437\u0430 \u0433\u043E\u043B\u043E\u0441\u043E\u0432\u0430\u043D\u0438\u0435 \u0432 \u0440\u0435\u0439\u0442\u0438\u043D\u0433\u0435 L2top.ru");
          st = _con.prepareStatement("INSERT INTO `character_votes` (`name`, `time`, `type`) VALUES (?, ?, 'l2top')");
          st.setString(1, name);
          st.setLong(2, time);
          st.execute();
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      DbUtils.closeQuietly(_con, st, rs);
    }
  }

  public class ParseTask implements Runnable
  {
    public ParseTask()
    {
    }

    public void run()
    {
      try
      {
        resultVotes();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
  }
}