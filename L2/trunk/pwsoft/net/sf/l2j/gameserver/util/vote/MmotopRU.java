package net.sf.l2j.gameserver.util.vote;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastList.Node;
import net.sf.l2j.Config;
import net.sf.l2j.Config.EventReward;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.datatables.CustomServerData;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.util.GiveItem;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class MmotopRU
{
  private static final Logger _log = AbstractLogger.getLogger(MmotopRU.class.getName());

  private static FastList<Config.EventReward> _rewards = Config.MMOTOP_ONLINE_REWARDS;
  private static FastList<String> _waiters = new FastList();
  private static String _last = "";
  private static MmotopRU _instance;

  public static MmotopRU getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new MmotopRU();
    _instance.load();
  }

  private void load()
  {
    if (Config.MMOTOP_STAT_LINK.equals(""))
    {
      _log.warning("[ERROR] MmotopRU: load() MmotopStatLink is empty; in services.cfg");
      return;
    }
    _last = getLast();
    updateVoters();
  }

  private void updateVoters()
  {
    new Thread(new Runnable()
    {
      public void run() {
        long start = System.currentTimeMillis();
        String pre_last = "";
        String f_last = "";

        BufferedReader br = null;
        InputStreamReader isr = null;
        try
        {
          URL l2top = new URL(Config.MMOTOP_STAT_LINK);
          isr = new InputStreamReader(l2top.openStream());
          br = new BufferedReader(isr);
          String line;
          while ((line = br.readLine()) != null)
          {
            String[] tmp = line.split("\t");
            pre_last = tmp[1];
            if (pre_last.equals(MmotopRU._last)) {
              break;
            }
            f_last = pre_last;
            String name = tmp[3];

            if (name.length() == 0) {
              continue;
            }
            MmotopRU._waiters.add(name);
          }

        }
        catch (Exception e)
        {
          MmotopRU._log.warning("[ERROR] MmotopRU: updateVoters() error: " + e);
        }
        finally
        {
          try
          {
            if (isr != null)
            {
              isr.close();
              isr = null;
            }
            if (br != null)
            {
              br.close();
              br = null;
            }
          }
          catch (Exception e)
          {
            MmotopRU._log.warning("[ERROR] MmotopRU: updateVoters()close() error: " + e);
          }
        }
        if (f_last.length() > 0)
          MmotopRU.access$102(f_last);
        MmotopRU.this.setLast();

        if (Config.MMOTOP_LOGTYPE > 0)
        {
          long time = (System.currentTimeMillis() - start) / 1000L;
          String result = MmotopRU.access$500() + ", mmotop.ru: finished, +" + MmotopRU._waiters.size() + "; time: " + time + "s.";
          switch (Config.MMOTOP_LOGTYPE)
          {
          case 1:
            MmotopRU._log.info(result);
            break;
          case 2:
            Log.add(result, "vote_mmotop");
          }
        }

        MmotopRU.this.giveRewards();
      }
    }).start();
  }

  private void giveRewards()
  {
    new Thread(new Runnable()
    {
      public void run() {
        String now = MmotopRU.access$500();
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try
        {
          L2PcInstance voter = null;
          Config.EventReward reward = null;
          con = L2DatabaseFactory.getInstance().getConnection();
          con.setTransactionIsolation(1);
          FastList.Node n = MmotopRU._waiters.head(); for (FastList.Node end = MmotopRU._waiters.tail(); (n = n.getNext()) != end; )
          {
            String name = (String)n.getValue();
            if (name == null) {
              continue;
            }
            int char_id = MmotopRU.this.getCharId(con, name);
            if (char_id == 0) {
              continue;
            }
            voter = L2World.getInstance().getPlayer(char_id);
            if (voter == null)
            {
              if (GiveItem.insertOffline(con, char_id, Config.MMOTOP_OFFLINE_ITEM, Config.MMOTOP_OFFLINE_COUNT, 0, 0, 0, Config.MMOTOP_OFFLINE_LOC))
                MmotopRU.this.logVote(con, name, now);
            }
            else
            {
              reward = null;
              FastList.Node k = MmotopRU._rewards.head(); for (FastList.Node endk = MmotopRU._rewards.tail(); (k = k.getNext()) != endk; )
              {
                reward = (Config.EventReward)k.getValue();
                if ((reward == null) || 
                  (Rnd.get(100) >= reward.chance)) continue;
                voter.addItem("MmotopRU.giveItem", reward.id, reward.count, voter, true);
              }
              reward = null;
              MmotopRU.this.logVote(con, name, now);
            }

            try
            {
              Thread.sleep(1L);
            } catch (InterruptedException ex) {
            }
          }
          voter = null;
          reward = null;
        }
        catch (SQLException e)
        {
          MmotopRU._log.warning("[ERROR] MmotopRU: giveRewards() error: " + e);
        }
        finally
        {
          MmotopRU._waiters.clear();
          Close.CSR(con, st, rs);
        }
        ThreadPoolManager.getInstance().scheduleGeneral(new MmotopRU.UpdateTask(MmotopRU.this), Config.MMOTOP_UPDATE_DELAY);
      }
    }).start();
  }

  private void logVote(Connect con, String name, String date)
  {
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      st = con.prepareStatement("INSERT INTO `z_vote_logs` (`date`, `name`) VALUES (?, ?)");
      st.setString(1, date);
      st.setString(2, name);
      st.execute();
    }
    catch (SQLException e)
    {
      System.out.println("[ERROR] MmotopRU, logVote() error: " + e);
    }
    finally
    {
      Close.SR(st, rs);
    }
  }

  private int getCharId(Connect con, String name)
  {
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      if (Config.VS_VREF)
      {
        st = con.prepareStatement("SELECT * FROM `z_vote_names` WHERE `from` = ? LIMIT 1");
        st.setString(1, name);
        rs = st.executeQuery();
        if (rs.next()) {
          name = rs.getString("to");
        }
        Close.SR(st, rs);
      }

      st = con.prepareStatement("SELECT obj_Id FROM `characters` WHERE `char_name` = ? LIMIT 0,1");
      st.setString(1, name);
      rs = st.executeQuery();
      if (rs.next()) {
        int i = rs.getInt("obj_Id");
        return i;
      }
    }
    catch (SQLException e)
    {
      System.out.println("[ERROR] MmotopRU, getCharId() error: " + e);
    }
    finally
    {
      Close.SR(st, rs);
    }
    return 0;
  }

  private String getLast()
  {
    Connect con = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      con.setTransactionIsolation(1);

      st = con.prepareStatement("SELECT date FROM `z_vote_logs` WHERE `id` = ?");
      st.setInt(1, -2);
      rs = st.executeQuery();
      if (rs.next()) {
        String str = rs.getString("date");
        return str;
      }
    }
    catch (SQLException e)
    {
      System.out.println("[ERROR] MmotopRU, getLast() error: " + e);
    }
    finally
    {
      Close.CSR(con, st, rs);
    }
    return "";
  }

  private void setLast()
  {
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("REPLACE INTO `z_vote_logs` (`id`, `date`, `name`) VALUES (?, ?, ?)");
      st.setInt(1, -2);
      st.setString(2, _last);
      st.setString(3, "l#a#s#t#m#m#o#t#o#p");
      st.execute();
    }
    catch (SQLException e)
    {
      _log.warning("setLast() error: " + e);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  private static String getDate()
  {
    Date date = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    return sdf.format(date);
  }

  class UpdateTask
    implements Runnable
  {
    UpdateTask()
    {
    }

    public void run()
    {
      MmotopRU.this.updateVoters();

      CustomServerData.getInstance().cacheStat();
    }
  }
}