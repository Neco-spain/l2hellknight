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

public class L2TopRU
{
  private static final Logger _log = AbstractLogger.getLogger(L2TopRU.class.getName());

  private static FastList<Config.EventReward> _rewards = Config.L2TOP_ONLINE_REWARDS;
  private static FastList<String> _waiters = new FastList();
  private static String _last = "";
  private static L2TopRU _instance;

  public static L2TopRU getInstance()
  {
    return _instance;
  }

  public static void init() {
    _instance = new L2TopRU();
    _instance.load();
  }

  private void load()
  {
    if ((Config.L2TOP_SERV_ID == 0) || (Config.L2TOP_SERV_KEY.equals("")))
    {
      _log.warning("[ERROR] L2TopRU: load() L2TopServerId = 0 in services.cfg");
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
          URL l2top = new URL("http://l2top.ru/editServ/?adminAct=lastVotes&uid=" + Config.L2TOP_SERV_ID + "&key=" + Config.L2TOP_SERV_KEY + "");
          isr = new InputStreamReader(l2top.openStream());
          br = new BufferedReader(isr);
          int i = 0;
          String line;
          while ((line = br.readLine()) != null)
          {
            i++;
            if (i == 1) {
              continue;
            }
            pre_last = line.substring(0, 19);
            if (pre_last.equals(L2TopRU._last)) {
              break;
            }
            if (i == 2) {
              f_last = pre_last;
            }

            String name = line.substring(19).trim();
            if (name.length() == 0) {
              continue;
            }
            L2TopRU._waiters.add(name);
          }

        }
        catch (Exception e)
        {
          L2TopRU._log.warning("[ERROR] L2TopRU: updateVoters() error: " + e);
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
            L2TopRU._log.warning("[ERROR] L2TopRU: updateVoters()close() error: " + e);
          }
        }
        if (f_last.length() > 0)
          L2TopRU.access$102(f_last);
        L2TopRU.this.setLast();

        if (Config.L2TOP_LOGTYPE > 0)
        {
          long time = (System.currentTimeMillis() - start) / 1000L;
          String result = L2TopRU.access$500() + ", l2top.ru: finished, +" + L2TopRU._waiters.size() + "; time: " + time + "s.";
          switch (Config.L2TOP_LOGTYPE)
          {
          case 1:
            L2TopRU._log.info(result);
            break;
          case 2:
            Log.add(result, "vote_l2top");
          }
        }

        L2TopRU.this.giveRewards();
      }
    }).start();
  }

  private void giveRewards()
  {
    new Thread(new Runnable()
    {
      public void run() {
        String now = L2TopRU.access$500();
        Connect con = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try
        {
          L2PcInstance voter = null;
          Config.EventReward reward = null;
          con = L2DatabaseFactory.getInstance().getConnection();
          con.setTransactionIsolation(1);
          FastList.Node n = L2TopRU._waiters.head(); for (FastList.Node end = L2TopRU._waiters.tail(); (n = n.getNext()) != end; )
          {
            String name = (String)n.getValue();
            if (name == null) {
              continue;
            }
            int char_id = L2TopRU.this.getCharId(con, name);
            if (char_id == 0) {
              continue;
            }
            voter = L2World.getInstance().getPlayer(char_id);
            if (voter == null)
            {
              if (GiveItem.insertOffline(con, char_id, Config.L2TOP_OFFLINE_ITEM, Config.L2TOP_OFFLINE_COUNT, 0, 0, 0, Config.L2TOP_OFFLINE_LOC))
                L2TopRU.this.logVote(con, name, now);
            }
            else
            {
              reward = null;
              FastList.Node k = L2TopRU._rewards.head(); for (FastList.Node endk = L2TopRU._rewards.tail(); (k = k.getNext()) != endk; )
              {
                reward = (Config.EventReward)k.getValue();
                if ((reward == null) || 
                  (Rnd.get(100) >= reward.chance)) continue;
                voter.addItem("L2TopRU.giveItem", reward.id, reward.count, voter, true);
              }
              reward = null;
              L2TopRU.this.logVote(con, name, now);
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
          L2TopRU._log.warning("[ERROR] L2TopRU: giveRewards() error: " + e);
        }
        finally
        {
          L2TopRU._waiters.clear();
          Close.CSR(con, st, rs);
        }
        ThreadPoolManager.getInstance().scheduleGeneral(new L2TopRU.UpdateTask(L2TopRU.this), Config.L2TOP_UPDATE_DELAY);
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
      System.out.println("[ERROR] L2TopRU, logVote() error: " + e);
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
      System.out.println("[ERROR] L2TopRU, getCharId() error: " + e);
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
      st.setInt(1, -1);
      rs = st.executeQuery();
      if (rs.next()) {
        String str = rs.getString("date");
        return str;
      }
    }
    catch (SQLException e)
    {
      System.out.println("[ERROR] L2TopRU, getLast() error: " + e);
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
      st.setInt(1, -1);
      st.setString(2, _last);
      st.setString(3, "l#a#s#t#l#2#t#o#p");
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
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
      L2TopRU.this.updateVoters();

      CustomServerData.getInstance().cacheStat();
    }
  }
}