package net.sf.l2j.gameserver.instancemanager.games;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.Announcements;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.L2ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.Rnd;
import net.sf.l2j.util.log.AbstractLogger;

public class Lottery
{
  public static final long SECOND = 1000L;
  public static final long MINUTE = 60000L;
  private static Lottery _instance;
  protected static final Logger _log = AbstractLogger.getLogger(Lottery.class.getName());
  private static final String INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)";
  private static final String UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?";
  private static final String UPDATE_LOTTERY = "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?";
  private static final String SELECT_LAST_LOTTERY = "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1";
  private static final String SELECT_LOTTERY_ITEM = "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?";
  private static final String SELECT_LOTTERY_TICKET = "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?";
  protected int _number;
  protected int _prize;
  protected boolean _isSellingTickets;
  protected boolean _isStarted;
  protected long _enddate;

  private Lottery()
  {
    _number = 1;
    _prize = Config.ALT_LOTTERY_PRIZE;
    _isSellingTickets = false;
    _isStarted = false;
    _enddate = System.currentTimeMillis();

    if (Config.ALLOW_LOTTERY) new startLottery().run();
  }

  public static Lottery getInstance()
  {
    if (_instance == null) _instance = new Lottery();

    return _instance;
  }

  public int getId()
  {
    return _number;
  }

  public int getPrize()
  {
    return _prize;
  }

  public long getEndDate()
  {
    return _enddate;
  }

  public void increasePrize(int count)
  {
    _prize += count;
    Connect con = null;
    PreparedStatement st = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?");
      st.setInt(1, getPrize());
      st.setInt(2, getPrize());
      st.setInt(3, getId());
      st.execute();
    }
    catch (SQLException e)
    {
      _log.warning("Lottery: Could not increase current lottery prize: " + e);
    }
    finally
    {
      Close.CS(con, st);
    }
  }

  public boolean isSellableTickets()
  {
    return _isSellingTickets;
  }

  public boolean isStarted()
  {
    return _isStarted;
  }

  public int[] decodeNumbers(int enchant, int type2)
  {
    int[] res = new int[5];
    int id = 0;
    int nr = 1;

    while (enchant > 0)
    {
      int val = enchant / 2;
      if (val != enchant / 2.0D)
      {
        res[id] = nr;
        id++;
      }
      enchant /= 2;
      nr++;
    }

    nr = 17;

    while (type2 > 0)
    {
      int val = type2 / 2;
      if (val != type2 / 2.0D)
      {
        res[id] = nr;
        id++;
      }
      type2 /= 2;
      nr++;
    }

    return res;
  }

  public int[] checkTicket(L2ItemInstance item)
  {
    return checkTicket(item.getCustomType1(), item.getEnchantLevel(), item.getCustomType2());
  }

  public int[] checkTicket(int id, int enchant, int type2)
  {
    int[] res = { 0, 0 };

    Connect con = null;
    PreparedStatement st = null;
    ResultSet rset = null;
    try
    {
      con = L2DatabaseFactory.getInstance().getConnection();
      st = con.prepareStatement("SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?");
      st.setInt(1, id);
      rset = st.executeQuery();

      if (rset.next())
      {
        int curenchant = rset.getInt("number1") & enchant;
        int curtype2 = rset.getInt("number2") & type2;

        if ((curenchant == 0) && (curtype2 == 0))
        {
          Close.SR(st, rset);
          int[] arrayOfInt1 = res;
          return arrayOfInt1;
        }
        int count = 0;

        for (int i = 1; i <= 16; i++)
        {
          int val = curenchant / 2;
          if (val != curenchant / 2.0D) count++;
          int val2 = curtype2 / 2;
          if (val2 != curtype2 / 2.0D) count++;
          curenchant = val;
          curtype2 = val2;
        }

        switch (count)
        {
        case 0:
          break;
        case 5:
          res[0] = 1;
          res[1] = rset.getInt("prize1");
          break;
        case 4:
          res[0] = 2;
          res[1] = rset.getInt("prize2");
          break;
        case 3:
          res[0] = 3;
          res[1] = rset.getInt("prize3");
          break;
        case 1:
        case 2:
        default:
          res[0] = 4;
          res[1] = 200;
        }

      }

      Close.SR(st, rset);
    }
    catch (SQLException e)
    {
      _log.warning("Lottery: Could not check lottery ticket #" + id + ": " + e);
    }
    finally
    {
      Close.CSR(con, st, rset);
    }

    return res;
  }

  private class finishLottery
    implements Runnable
  {
    protected finishLottery()
    {
    }

    public void run()
    {
      int[] luckynums = new int[5];
      int luckynum = 0;

      for (int i = 0; i < 5; i++)
      {
        boolean found = true;

        while (found)
        {
          luckynum = Rnd.get(20) + 1;
          found = false;

          for (int j = 0; j < i; j++) {
            if (luckynums[j] != luckynum) continue; found = true;
          }
        }
        luckynums[i] = luckynum;
      }

      int enchant = 0;
      int type2 = 0;

      for (int i = 0; i < 5; i++)
      {
        if (luckynums[i] < 17) enchant = (int)(enchant + Math.pow(2.0D, luckynums[i] - 1)); else {
          type2 = (int)(type2 + Math.pow(2.0D, luckynums[i] - 17));
        }

      }

      int count1 = 0;
      int count2 = 0;
      int count3 = 0;
      int count4 = 0;

      Connect con = null;
      PreparedStatement st = null;
      ResultSet rset = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?");
        st.setInt(1, getId());
        rset = st.executeQuery();

        while (rset.next())
        {
          int curenchant = rset.getInt("enchant_level") & enchant;
          int curtype2 = rset.getInt("custom_type2") & type2;

          if ((curenchant == 0) && (curtype2 == 0))
            continue;
          int count = 0;

          for (int i = 1; i <= 16; i++)
          {
            int val = curenchant / 2;

            if (val != curenchant / 2.0D) count++;

            int val2 = curtype2 / 2;

            if (val2 != curtype2 / 2.0D) count++;

            curenchant = val;
            curtype2 = val2;
          }

          if (count == 5) count1++;
          else if (count == 4) count2++;
          else if (count == 3) count3++;
          else if (count > 0) count4++;
        }
        Close.SR(st, rset);
      }
      catch (SQLException e)
      {
        Lottery._log.warning("Lottery: Could restore lottery data: " + e);
      }
      finally
      {
        Close.CSR(con, st, rset);
      }

      int prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE;
      int prize1 = 0;
      int prize2 = 0;
      int prize3 = 0;

      if (count1 > 0) prize1 = (int)((getPrize() - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / count1);

      if (count2 > 0) prize2 = (int)((getPrize() - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / count2);

      if (count3 > 0) prize3 = (int)((getPrize() - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / count3);

      int newprize = getPrize() - (prize1 + prize2 + prize3 + prize4);

      if (count1 > 0)
      {
        Announcements.getInstance().announceToAll(SystemMessage.id(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER).addNumber(getId()).addNumber(getPrize()).addNumber(count1));
      }
      else
      {
        Announcements.getInstance().announceToAll(SystemMessage.id(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER).addNumber(getId()).addNumber(getPrize()));
      }
      SystemMessage sm = null;

      Connect con2 = null;
      PreparedStatement st2 = null;
      try
      {
        con2 = L2DatabaseFactory.getInstance().getConnection();
        st2 = con2.prepareStatement("UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?");
        st2.setInt(1, getPrize());
        st2.setInt(2, newprize);
        st2.setInt(3, enchant);
        st2.setInt(4, type2);
        st2.setInt(5, prize1);
        st2.setInt(6, prize2);
        st2.setInt(7, prize3);
        st2.setInt(8, getId());
        st2.execute();
      }
      catch (SQLException e)
      {
        Lottery._log.warning("Lottery: Could not store finished lottery data: " + e);
      }
      finally
      {
        Close.CS(con2, st2);
      }

      ThreadPoolManager.getInstance().scheduleGeneral(new Lottery.startLottery(Lottery.this), 60000L);
      _number += 1;

      _isStarted = false;
    }
  }

  private class stopSellingTickets
    implements Runnable
  {
    protected stopSellingTickets()
    {
    }

    public void run()
    {
      _isSellingTickets = false;

      Announcements.getInstance().announceToAll(Static.LOTTERY_TICKET_SALES_TEMP_SUSPENDED);
    }
  }

  private class startLottery
    implements Runnable
  {
    protected startLottery()
    {
    }

    public void run()
    {
      Connect con = null;
      PreparedStatement st = null;
      ResultSet rset = null;
      try
      {
        con = L2DatabaseFactory.getInstance().getConnection();
        st = con.prepareStatement("SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1");
        rset = st.executeQuery();
        if (rset.next())
        {
          _number = rset.getInt("idnr");

          if (rset.getInt("finished") == 1)
          {
            _number += 1;
            _prize = rset.getInt("newprize");
          }
          else
          {
            _prize = rset.getInt("prize");
            _enddate = rset.getLong("enddate");

            if (_enddate <= System.currentTimeMillis() + 120000L) { new Lottery.finishLottery(Lottery.this).run();
              rset.close();
              st.close();
              return; }
            if (_enddate > System.currentTimeMillis())
            {
              _isStarted = true;
              ThreadPoolManager.getInstance().scheduleGeneral(new Lottery.finishLottery(Lottery.this), _enddate - System.currentTimeMillis());

              if (_enddate > System.currentTimeMillis() + 720000L)
              {
                _isSellingTickets = true;
                ThreadPoolManager.getInstance().scheduleGeneral(new Lottery.stopSellingTickets(Lottery.this), _enddate - System.currentTimeMillis() - 600000L);
              }

              Close.SR(st, rset);
              return;
            }
          }
        }

        Close.SR(st, rset);
      }
      catch (SQLException e)
      {
        Lottery._log.warning("Lottery: Could not restore lottery data: " + e);
      }
      finally
      {
        Close.CSR(con, st, rset);
      }

      _isSellingTickets = true;
      _isStarted = true;

      Announcements.getInstance().announceToAll("Lottery tickets are now available for Lucky Lottery #" + getId() + ".");

      Calendar finishtime = Calendar.getInstance();
      finishtime.setTimeInMillis(_enddate);
      finishtime.set(12, 0);
      finishtime.set(13, 0);

      if (finishtime.get(7) == 1)
      {
        finishtime.set(11, 19);
        _enddate = finishtime.getTimeInMillis();
        _enddate += 604800000L;
      }
      else
      {
        finishtime.set(7, 1);
        finishtime.set(11, 19);
        _enddate = finishtime.getTimeInMillis();
      }

      ThreadPoolManager.getInstance().scheduleGeneral(new Lottery.stopSellingTickets(Lottery.this), _enddate - System.currentTimeMillis() - 600000L);

      ThreadPoolManager.getInstance().scheduleGeneral(new Lottery.finishLottery(Lottery.this), _enddate - System.currentTimeMillis());

      Connect con2 = null;
      PreparedStatement st2 = null;
      try
      {
        con2 = L2DatabaseFactory.getInstance().getConnection();
        st2 = con.prepareStatement("INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)");
        st2.setInt(1, 1);
        st2.setInt(2, getId());
        st2.setLong(3, getEndDate());
        st2.setInt(4, getPrize());
        st2.setInt(5, getPrize());
        st2.execute();
      }
      catch (SQLException e)
      {
        Lottery._log.warning("Lottery: Could not store new lottery data: " + e);
      }
      finally
      {
        Close.CS(con2, st2);
      }
    }
  }
}