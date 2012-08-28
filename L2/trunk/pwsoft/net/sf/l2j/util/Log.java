package net.sf.l2j.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.mysql.Close;
import net.sf.l2j.mysql.Connect;
import net.sf.l2j.util.log.AbstractLogger;

public class Log
{
  private static final Logger _log = AbstractLogger.getLogger(Log.class.getName());
  public static final int TRADE = 1;
  public static final int WAREHOUSE = 2;
  public static final int MULTISELL = 3;
  public static final int PRIVATE_STORE = 4;
  public static final int SHOP = 5;
  public static final int PICKUP = 6;
  public static final int DIEDROP = 7;
  private static File item_trade = null;
  private static File item_wh = null;
  private static File item_ms = null;
  private static File item_ps = null;
  private static File item_shop = null;
  private static File item_pickup = null;
  private static File item_didrop = null;
  private static File item_all = null;

  private static Lock itemLock = new ReentrantLock();

  public static void add(String text, String cat)
  {
    Lock print = new ReentrantLock();
    print.lock();
    try {
      new File("log/").mkdirs();

      File file = new File(new StringBuilder().append("log/").append(cat != null ? cat : "_all").append(".txt").toString());

      if (!file.exists()) {
        try {
          file.createNewFile();
        } catch (IOException e) {
          _log.warning(new StringBuilder().append("saving ").append(cat != null ? cat : "all").append(" log failed, can't create file: ").append(e).toString());
        }

      }

      FileWriter save = null;
      TextBuilder msgb = new TextBuilder();
      try {
        save = new FileWriter(file, true);

        msgb.append(new StringBuilder().append(text).append("\n").toString());
        save.write(msgb.toString());
      } catch (IOException e1) {
        _log.warning(new StringBuilder().append("saving ").append(cat != null ? cat : "all").append(" log failed: ").append(e).toString());
        e.printStackTrace();
      } finally {
        try {
          if (save != null) {
            save.close();
          }
          msgb.clear();
          msgb = null;
          save = null;
        } catch (Exception e1) {
        }
      }
    } catch (Exception e) {
    }
    finally {
      print.unlock();
    }
  }

  public static void addDonate(L2PcInstance player, String action, int price)
  {
    Date date = new Date();
    SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");

    Connect con = null;
    PreparedStatement statement = null;
    try {
      con = L2DatabaseFactory.getInstance().getConnection();
      statement = con.prepareStatement("INSERT INTO `zz_donate_log` (`id`,`date`,`time`,`login`,`name`,`action`,`payment`) VALUES (NULL,?,?,?,?,?,?)");
      statement.setString(1, datef.format(date).toString());
      statement.setString(2, timef.format(date).toString());
      statement.setString(3, player.getAccountName());
      statement.setString(4, player.getName());
      statement.setString(5, action);
      statement.setInt(6, price);
      statement.execute();
    } catch (Exception e) {
      _log.warning(new StringBuilder().append("Donate: logAction() error: ").append(e).toString());
    } finally {
      Close.CS(con, statement);
    }
  }

  public static void init()
  {
    new File("log/items/").mkdirs();

    Date date = new Date();
    SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd_HH-mm_");
    String time = datef.format(date).toString();

    item_trade = new File(new StringBuilder().append("log/items/").append(time).append("trade.txt").toString());
    if (!item_trade.exists()) {
      try {
        item_trade.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_trade: ").append(e).toString());
        return;
      }
    }
    item_wh = new File(new StringBuilder().append("log/items/").append(time).append("warehouse.txt").toString());
    if (!item_wh.exists()) {
      try {
        item_wh.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_wh: ").append(e).toString());
        return;
      }
    }
    item_ms = new File(new StringBuilder().append("log/items/").append(time).append("multisell.txt").toString());
    if (!item_ms.exists()) {
      try {
        item_ms.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_ms: ").append(e).toString());
        return;
      }
    }
    item_ps = new File(new StringBuilder().append("log/items/").append(time).append("private_store.txt").toString());
    if (!item_ps.exists()) {
      try {
        item_ps.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_ps: ").append(e).toString());
        return;
      }
    }
    item_shop = new File(new StringBuilder().append("log/items/").append(time).append("shop.txt").toString());
    if (!item_shop.exists()) {
      try {
        item_shop.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_shop: ").append(e).toString());
        return;
      }
    }
    item_pickup = new File(new StringBuilder().append("log/items/").append(time).append("pickup.txt").toString());
    if (!item_pickup.exists()) {
      try {
        item_pickup.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_pickup: ").append(e).toString());
        return;
      }
    }
    item_didrop = new File(new StringBuilder().append("log/items/").append(time).append("diedrop.txt").toString());
    if (!item_didrop.exists()) {
      try {
        item_didrop.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_didrop: ").append(e).toString());
        return;
      }
    }
    item_all = new File(new StringBuilder().append("log/items/").append(time).append("all.txt").toString());
    if (!item_all.exists())
      try {
        item_all.createNewFile();
      } catch (IOException e) {
        _log.warning(new StringBuilder().append("Log [ERROR], can't create item_all: ").append(e).toString());
        return;
      }
  }

  public static void item(String data, int type)
  {
    if (!Config.LOG_ITEMS) {
      return;
    }

    itemLock.lock();
    try {
      File file = null;
      FileWriter save = null;
      try
      {
        switch (type) {
        case 1:
          file = item_trade;
          break;
        case 2:
          file = item_wh;
          break;
        case 3:
          file = item_ms;
          break;
        case 4:
          file = item_ps;
          break;
        case 5:
          file = item_shop;
          break;
        case 6:
          file = item_pickup;
          break;
        case 7:
          file = item_didrop;
        }

        save = new FileWriter(file, true);

        save.write(data);

        if (save != null) {
          save.close();
        }

        file = item_all;
        save = new FileWriter(file, true);

        save.write(data);
      } catch (IOException e1) {
        _log.warning(new StringBuilder().append("Log [ERROR], saving ").append(file.getName()).append(" log failed: ").append(e).toString());
        e.printStackTrace();
      } finally {
        try {
          if (save != null) {
            save.close();
          }

          save = null;
        } catch (Exception e1) {
        }
      }
    } catch (Exception e) {
    }
    finally {
      itemLock.unlock();
    }
  }

  public static void banHWID(String hwid, String ip, String account)
  {
    Lock print = new ReentrantLock();
    print.lock();
    try {
      File file = new File("lameguard/banned_hwid.txt");
      FileWriter save = null;
      try {
        save = new FileWriter(file, true);
        save.write(new StringBuilder().append(hwid).append("\t# added : ").append(getTime()).append(" ip: ").append(ip).append("; account: ").append(account).append("; reason: admin_hwidban\n").toString());
      } catch (IOException e1) {
        _log.warning(new StringBuilder().append("saving banHWID ").append(hwid).append(" failed: ").append(e).toString());
        e.printStackTrace();
      } finally {
        try {
          if (save != null) {
            save.close();
          }
          save = null;
        } catch (Exception e1) {
        }
      }
    } catch (Exception e) {
    }
    finally {
      print.unlock();
    }
  }

  public static void addToPath(File file, String text) {
    Lock print = new ReentrantLock();
    print.lock();
    try {
      FileWriter save = null;
      TextBuilder msgb = new TextBuilder();
      try {
        save = new FileWriter(file, true);

        msgb.append(new StringBuilder().append(text).append("\n").toString());
        save.write(msgb.toString());
      } catch (IOException e1) {
        _log.warning(new StringBuilder().append("saving ").append(file.getName()).append(" failed: ").append(e).toString());
        e.printStackTrace();
      } finally {
        try {
          if (save != null) {
            save.close();
          }
          msgb.clear();
          msgb = null;
          save = null;
        } catch (Exception e1) {
        }
      }
    } catch (Exception e) {
    }
    finally {
      print.unlock();
    }
  }

  public static String getTime() {
    Date date = new Date();
    SimpleDateFormat datef = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS, ");
    return datef.format(date).toString();
  }
}