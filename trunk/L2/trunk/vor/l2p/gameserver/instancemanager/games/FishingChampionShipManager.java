package l2p.gameserver.instancemanager.games;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import l2p.commons.dbutils.DbUtils;
import l2p.commons.threading.RunnableImpl;
import l2p.commons.util.Rnd;
import l2p.gameserver.Config;
import l2p.gameserver.ThreadPoolManager;
import l2p.gameserver.data.xml.holder.ItemHolder;
import l2p.gameserver.database.DatabaseFactory;
import l2p.gameserver.instancemanager.ServerVariables;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.instances.NpcInstance;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.NpcHtmlMessage;
import l2p.gameserver.serverpackets.SystemMessage;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.templates.item.ItemTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FishingChampionShipManager
{
  private static final Logger _log = LoggerFactory.getLogger(FishingChampionShipManager.class);

  private static final FishingChampionShipManager _instance = new FishingChampionShipManager();

  private long _enddate = 0L;
  private List<String> _playersName = new ArrayList();
  private List<String> _fishLength = new ArrayList();
  private List<String> _winPlayersName = new ArrayList();
  private List<String> _winFishLength = new ArrayList();
  private List<Fisher> _tmpPlayers = new ArrayList();
  private List<Fisher> _winPlayers = new ArrayList();
  private double _minFishLength = 0.0D;
  private boolean _needRefresh = true;

  public static final FishingChampionShipManager getInstance()
  {
    return _instance;
  }

  private FishingChampionShipManager()
  {
    restoreData();
    refreshWinResult();
    recalculateMinLength();
    if (_enddate <= System.currentTimeMillis())
    {
      _enddate = System.currentTimeMillis();
      new finishChamp(null).run();
    }
    else {
      ThreadPoolManager.getInstance().schedule(new finishChamp(null), _enddate - System.currentTimeMillis());
    }
  }

  private void setEndOfChamp() {
    Calendar finishtime = Calendar.getInstance();
    finishtime.setTimeInMillis(_enddate);
    finishtime.set(12, 0);
    finishtime.set(13, 0);
    finishtime.add(5, 6);
    finishtime.set(7, 3);
    finishtime.set(11, 19);
    _enddate = finishtime.getTimeInMillis();
  }

  private void restoreData()
  {
    _enddate = ServerVariables.getLong("fishChampionshipEnd", 0L);
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("SELECT `PlayerName`, `fishLength`, `rewarded` FROM fishing_championship");
      ResultSet rs = statement.executeQuery();
      while (rs.next())
      {
        int rewarded = rs.getInt("rewarded");
        if (rewarded == 0)
          _tmpPlayers.add(new Fisher(rs.getString("PlayerName"), rs.getDouble("fishLength"), 0));
        if (rewarded > 0)
          _winPlayers.add(new Fisher(rs.getString("PlayerName"), rs.getDouble("fishLength"), rewarded));
      }
      rs.close();
    }
    catch (SQLException e)
    {
      _log.warn(new StringBuilder().append("Exception: can't get fishing championship info: ").append(e.getMessage()).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  public synchronized void newFish(Player pl, int lureId)
  {
    if (!Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
      return;
    double p1 = Rnd.get(60, 80);
    if ((p1 < 90.0D) && (lureId > 8484) && (lureId < 8486))
    {
      long diff = Math.round(90.0D - p1);
      if (diff > 1L)
        p1 += Rnd.get(1L, diff);
    }
    double len = Rnd.get(100, 999) / 1000.0D + p1;
    if (_tmpPlayers.size() < 5)
    {
      for (Fisher fisher : _tmpPlayers)
        if (fisher.getName().equalsIgnoreCase(pl.getName()))
        {
          if (fisher.getLength() < len)
          {
            fisher.setLength(len);
            pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.ResultImproveOn", pl, new Object[0]));
            recalculateMinLength();
          }
          return;
        }
      _tmpPlayers.add(new Fisher(pl.getName(), len, 0));
      pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.YouInAPrizeList", pl, new Object[0]));
      recalculateMinLength();
    }
    else if (_minFishLength < len)
    {
      for (Fisher fisher : _tmpPlayers)
        if (fisher.getName().equalsIgnoreCase(pl.getName()))
        {
          if (fisher.getLength() < len)
          {
            fisher.setLength(len);
            pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.ResultImproveOn", pl, new Object[0]));
            recalculateMinLength();
          }
          return;
        }
      Fisher minFisher = null;
      double minLen = 99999.0D;
      for (Fisher fisher : _tmpPlayers)
        if (fisher.getLength() < minLen)
        {
          minFisher = fisher;
          minLen = minFisher.getLength();
        }
      _tmpPlayers.remove(minFisher);
      _tmpPlayers.add(new Fisher(pl.getName(), len, 0));
      pl.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.games.FishingChampionShipManager.YouInAPrizeList", pl, new Object[0]));
      recalculateMinLength();
    }
  }

  private void recalculateMinLength()
  {
    double minLen = 99999.0D;
    for (Fisher fisher : _tmpPlayers)
      if (fisher.getLength() < minLen)
        minLen = fisher.getLength();
    _minFishLength = minLen;
  }

  public long getTimeRemaining()
  {
    return (_enddate - System.currentTimeMillis()) / 60000L;
  }

  public String getWinnerName(int par)
  {
    if (_winPlayersName.size() >= par)
      return (String)_winPlayersName.get(par - 1);
    return "\u2014";
  }

  public String getCurrentName(int par)
  {
    if (_playersName.size() >= par)
      return (String)_playersName.get(par - 1);
    return "\u2014";
  }

  public String getFishLength(int par)
  {
    if (_winFishLength.size() >= par)
      return (String)_winFishLength.get(par - 1);
    return "0";
  }

  public String getCurrentFishLength(int par)
  {
    if (_fishLength.size() >= par)
      return (String)_fishLength.get(par - 1);
    return "0";
  }

  public void getReward(Player pl)
  {
    String filename = "fisherman/championship/getReward.htm";
    NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
    html.setFile(filename);
    pl.sendPacket(html);
    for (Fisher fisher : _winPlayers)
      if ((fisher._name.equalsIgnoreCase(pl.getName())) && 
        (fisher.getRewardType() != 2))
      {
        int rewardCnt = 0;
        for (int x = 0; x < _winPlayersName.size(); x++) {
          if (((String)_winPlayersName.get(x)).equalsIgnoreCase(pl.getName()))
            switch (x)
            {
            case 0:
              rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_1;
              break;
            case 1:
              rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_2;
              break;
            case 2:
              rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_3;
              break;
            case 3:
              rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_4;
              break;
            case 4:
              rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_5;
            }
        }
        fisher.setRewardType(2);
        if (rewardCnt > 0)
        {
          SystemMessage smsg = new SystemMessage(53).addItemName(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).addNumber(rewardCnt);
          pl.sendPacket(smsg);
          pl.getInventory().addItem(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM, rewardCnt);
          pl.sendItemList(false);
        }
      }
  }

  public void showMidResult(Player pl)
  {
    if (_needRefresh)
    {
      refreshResult();
      ThreadPoolManager.getInstance().schedule(new needRefresh(null), 60000L);
    }
    NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
    String filename = "fisherman/championship/MidResult.htm";
    html.setFile(filename);
    String str = null;
    for (int x = 1; x <= 5; x++)
    {
      str = new StringBuilder().append(str).append("<tr><td width=70 align=center>").append(x).append(pl.isLangRus() ? " \u041C\u0435\u0441\u0442\u043E:" : " Position:").append("</td>").toString();
      str = new StringBuilder().append(str).append("<td width=110 align=center>").append(getCurrentName(x)).append("</td>").toString();
      str = new StringBuilder().append(str).append("<td width=80 align=center>").append(getCurrentFishLength(x)).append("</td></tr>").toString();
    }
    html.replace("%TABLE%", str);
    html.replace("%prizeItem%", ItemHolder.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
    html.replace("%prizeFirst%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_1));
    html.replace("%prizeTwo%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_2));
    html.replace("%prizeThree%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_3));
    html.replace("%prizeFour%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_4));
    html.replace("%prizeFive%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_5));
    pl.sendPacket(html);
  }

  public void showChampScreen(Player pl, NpcInstance npc)
  {
    NpcHtmlMessage html = new NpcHtmlMessage(pl.getObjectId());
    String filename = "fisherman/championship/champScreen.htm";
    html.setFile(filename);
    String str = null;
    for (int x = 1; x <= 5; x++)
    {
      str = new StringBuilder().append(str).append("<tr><td width=70 align=center>").append(x).append(pl.isLangRus() ? " \u041C\u0435\u0441\u0442\u043E:" : " Position:").append("</td>").toString();
      str = new StringBuilder().append(str).append("<td width=110 align=center>").append(getWinnerName(x)).append("</td>").toString();
      str = new StringBuilder().append(str).append("<td width=80 align=center>").append(getFishLength(x)).append("</td></tr>").toString();
    }
    html.replace("%TABLE%", str);
    html.replace("%prizeItem%", ItemHolder.getInstance().getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM).getName());
    html.replace("%prizeFirst%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_1));
    html.replace("%prizeTwo%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_2));
    html.replace("%prizeThree%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_3));
    html.replace("%prizeFour%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_4));
    html.replace("%prizeFive%", String.valueOf(Config.ALT_FISH_CHAMPIONSHIP_REWARD_5));
    html.replace("%refresh%", String.valueOf(getTimeRemaining()));
    html.replace("%objectId%", String.valueOf(npc.getObjectId()));
    pl.sendPacket(html);
  }

  public void shutdown()
  {
    ServerVariables.set("fishChampionshipEnd", _enddate);
    Connection con = null;
    PreparedStatement statement = null;
    try
    {
      con = DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("DELETE FROM fishing_championship");
      statement.execute();
      statement.close();

      for (Fisher fisher : _winPlayers)
      {
        statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
        statement.setString(1, fisher.getName());
        statement.setDouble(2, fisher.getLength());
        statement.setInt(3, fisher.getRewardType());
        statement.execute();
        statement.close();
      }
      for (Fisher fisher : _tmpPlayers)
      {
        statement = con.prepareStatement("INSERT INTO fishing_championship(PlayerName,fishLength,rewarded) VALUES (?,?,?)");
        statement.setString(1, fisher.getName());
        statement.setDouble(2, fisher.getLength());
        statement.setInt(3, 0);
        statement.execute();
        statement.close();
      }
    }
    catch (SQLException e)
    {
      _log.warn(new StringBuilder().append("Exception: can't update player vitality: ").append(e.getMessage()).toString());
    }
    finally
    {
      DbUtils.closeQuietly(con, statement);
    }
  }

  private synchronized void refreshResult()
  {
    _needRefresh = false;
    _playersName.clear();
    _fishLength.clear();
    Fisher fisher1 = null;
    Fisher fisher2 = null;
    for (int x = 0; x <= _tmpPlayers.size() - 1; x++) {
      for (int y = 0; y <= _tmpPlayers.size() - 2; y++)
      {
        fisher1 = (Fisher)_tmpPlayers.get(y);
        fisher2 = (Fisher)_tmpPlayers.get(y + 1);
        if (fisher1.getLength() >= fisher2.getLength())
          continue;
        _tmpPlayers.set(y, fisher2);
        _tmpPlayers.set(y + 1, fisher1);
      }
    }
    for (int x = 0; x <= _tmpPlayers.size() - 1; x++)
    {
      _playersName.add(((Fisher)_tmpPlayers.get(x))._name);
      _fishLength.add(String.valueOf(((Fisher)_tmpPlayers.get(x)).getLength()));
    }
  }

  private void refreshWinResult()
  {
    _winPlayersName.clear();
    _winFishLength.clear();
    Fisher fisher1 = null;
    Fisher fisher2 = null;
    for (int x = 0; x <= _winPlayers.size() - 1; x++) {
      for (int y = 0; y <= _winPlayers.size() - 2; y++)
      {
        fisher1 = (Fisher)_winPlayers.get(y);
        fisher2 = (Fisher)_winPlayers.get(y + 1);
        if (fisher1.getLength() >= fisher2.getLength())
          continue;
        _winPlayers.set(y, fisher2);
        _winPlayers.set(y + 1, fisher1);
      }
    }
    for (int x = 0; x <= _winPlayers.size() - 1; x++)
    {
      _winPlayersName.add(((Fisher)_winPlayers.get(x))._name);
      _winFishLength.add(String.valueOf(((Fisher)_winPlayers.get(x)).getLength()));
    }
  }

  private class Fisher
  {
    private double _length = 0.0D;
    private String _name;
    private int _reward = 0;

    public Fisher(String name, double length, int rewardType)
    {
      setName(name);
      setLength(length);
      setRewardType(rewardType);
    }

    public void setLength(double value)
    {
      _length = value;
    }

    public void setName(String value)
    {
      _name = value;
    }

    public void setRewardType(int value)
    {
      _reward = value;
    }

    public String getName()
    {
      return _name;
    }

    public int getRewardType()
    {
      return _reward;
    }

    public double getLength()
    {
      return _length;
    }
  }

  private class needRefresh extends RunnableImpl
  {
    private needRefresh()
    {
    }

    public void runImpl()
      throws Exception
    {
      FishingChampionShipManager.access$802(FishingChampionShipManager.this, true);
    }
  }

  private class finishChamp extends RunnableImpl
  {
    private finishChamp()
    {
    }

    public void runImpl()
      throws Exception
    {
      _winPlayers.clear();
      for (FishingChampionShipManager.Fisher fisher : _tmpPlayers)
      {
        fisher.setRewardType(1);
        _winPlayers.add(fisher);
      }
      _tmpPlayers.clear();
      FishingChampionShipManager.this.refreshWinResult();
      FishingChampionShipManager.this.setEndOfChamp();
      shutdown();
      _log.info("Fishing Championship Manager : start new event period.");
      ThreadPoolManager.getInstance().schedule(new finishChamp(FishingChampionShipManager.this), _enddate - System.currentTimeMillis());
    }
  }
}