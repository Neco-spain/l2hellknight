package net.sf.l2j.util.log;

import java.awt.Container;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.model.L2World;

public class PwLogger extends DefaultLogger
{
  private static ConsoleFrame frame = null;
  private static PwLogger log;
  private static String _startTime;
  private static long _memory;
  private static int _online = 0;
  private static int _traders = 0;
  private static boolean _loaded;
  private static final String F_WARNING = "<font color=#e15b21>%msg%</font>";

  public static PwLogger init()
  {
    log = new PwLogger("pwLogger");
    return log;
  }

  public PwLogger(String name)
  {
    super(name);
    SimpleDateFormat datef = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    _startTime = datef.format(new Date()).toString();

    Runtime r = Runtime.getRuntime();
    _memory = (r.totalMemory() - r.freeMemory()) / 1024L / 1024L;

    frame = new ConsoleFrame();
  }

  public static void startRefresTask()
  {
    ThreadPoolManager.getInstance().scheduleGeneral(new RefreshInfo(), 5000L);
  }

  public static void setLoaded()
  {
    _loaded = true;
  }

  public Logger get(String name)
  {
    return log;
  }

  public static void refreshInfo()
  {
    frame.refreshInfo();
  }

  public void config(String msg)
  {
    frame.validateConsoleText(msg);
  }

  public void severe(String msg)
  {
    frame.validateConsoleText(msg);
  }

  public void warning(String msg)
  {
    frame.validateConsoleText(msg);
  }

  public void error(String msg) {
    frame.validateConsoleText(msg);
  }

  public void info(String msg)
  {
    frame.validateConsoleText(msg);
  }

  public void finest(String msg)
  {
  }

  public void fine(String msg)
  {
  }

  public void finer(String msg)
  {
  }

  public void log(Level level, String msg)
  {
    switch (level.intValue())
    {
    case 1000:
      severe(msg);
      break;
    case 900:
      warning(msg);
      break;
    case 800:
      info(msg);
      break;
    case 700:
      config(msg);
      break;
    case 500:
      fine(msg);
      break;
    case 300:
      finest(msg);
    }
  }

  public void log(Level level, String msg, Throwable thrown)
  {
    log(level, msg + " [" + thrown + "]");
  }

  public static String getTime()
  {
    Date date = new Date();
    SimpleDateFormat datef = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS ");
    return datef.format(date).toString();
  }

  static class ConsoleFrame extends JFrame
  {
    private static JTextPane logArea = null;
    private static JTextArea preText = null;
    private static JTextPane infoArea = null;
    private static JScrollPane areaScrollPane = null;

    public ConsoleFrame() {
      super();

      setSize(780, 480);

      preText = new JTextArea();
      preText.setEditable(false);
      preText.setFocusable(true);

      areaScrollPane = new JScrollPane(preText);

      areaScrollPane.setWheelScrollingEnabled(true);

      areaScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("\u041B\u043E\u0433 \u0441\u0435\u0440\u0432\u0435\u0440\u0430"), BorderFactory.createEmptyBorder(1, 1, 1, 1)));

      areaScrollPane.setPreferredSize(new Dimension(640, 480));
      areaScrollPane.setMinimumSize(new Dimension(640, 480));
      getContentPane().add(areaScrollPane, "West");

      infoArea = new JTextPane();
      infoArea.setContentType("text/html");
      infoArea.setText("\u0412\u0440\u0435\u043C\u044F \u0437\u0430\u043F\u0443\u0441\u043A\u0430: <br>" + PwLogger._startTime + "<br>\u041E\u043D\u043B\u0430\u0439\u043D: 0 <br>\u0422\u043E\u0440\u0433\u043E\u0432\u0446\u044B: 0<br>\u041F\u0430\u043C\u044F\u0442\u044C: " + PwLogger._memory);
      infoArea.setLayout(new BoxLayout(infoArea, 3));
      infoArea.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

      infoArea.setPreferredSize(new Dimension(130, 480));
      infoArea.setMinimumSize(new Dimension(130, 480));
      getContentPane().add(infoArea, "Center");

      pack();
      setVisible(true);
    }

    public void validateConsoleText(String msg)
    {
      preText.append("" + PwLogger.getTime() + "" + msg + "\n");
      preText.setCaretPosition(preText.getText().length());

      getContentPane().validate();
    }

    public void refreshInfo()
    {
      infoArea.setText("\u0412\u0440\u0435\u043C\u044F \u0437\u0430\u043F\u0443\u0441\u043A\u0430: <br>" + PwLogger._startTime + "<br>\u041E\u043D\u043B\u0430\u0439\u043D: " + PwLogger._online + " <br>\u0422\u043E\u0440\u0433\u043E\u0432\u0446\u044B: " + PwLogger._traders + "<br>\u041F\u0430\u043C\u044F\u0442\u044C: " + PwLogger._memory);
      getContentPane().validate();
    }
  }

  static class RefreshInfo
    implements Runnable
  {
    public void run()
    {
      try
      {
        if (PwLogger._loaded)
        {
          PwLogger.access$102(L2World.getInstance().getAllPlayersCount());
          PwLogger.access$202(L2World.getInstance().getAllOfflineCount());
        }

        PwLogger.access$302((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024L / 1024L);
        PwLogger.refreshInfo();
      } catch (Throwable e) {
      }
      ThreadPoolManager.getInstance().scheduleGeneral(new RefreshInfo(), 5000L);
    }
  }
}