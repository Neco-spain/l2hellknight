package net.sf.l2j.gameserver.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;

public class Log
{
  private static final Logger _log = Logger.getLogger(Log.class.getName());

  public static final void add(String text, String cat)
  {
    String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());

    new File("logs/game").mkdirs();
    try
    {
      File file = new File(new StringBuilder().append("logs/game/").append(cat != null ? cat : "_all").append(".txt").toString());
      FileWriter save = new FileWriter(file, true);
      String out = new StringBuilder().append("[").append(date).append("] '---': ").append(text).append("\n").toString();
      save.write(out);
      save.flush();
      save.close();
      save = null;
      file = null;
    }
    catch (IOException e)
    {
      _log.warning(new StringBuilder().append("saving chat log failed: ").append(e).toString());
      e.printStackTrace();
    }

    if (cat != null)
      add(text, null);
  }

  @Deprecated
  public static final void addEvent(L2PcInstance pc, String text) {
    String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
    String filedate = new SimpleDateFormat("yyMMdd_H").format(new Date());

    new File("logs/game").mkdirs();
    File file = new File(new StringBuilder().append("logs/game/actions_").append(filedate).append(".txt").toString());
    FileWriter save = null;
    try
    {
      save = new FileWriter(file, true);
      String out = new StringBuilder().append("[").append(date).append("] '<").append(pc.getName()).append(">': ").append(text).append("\n").toString();
      save.write(out);
    }
    catch (IOException e1)
    {
      _log.warning(new StringBuilder().append("saving actions log failed: ").append(e).toString());
      e.printStackTrace();
    }
    finally {
      try {
        save.close(); } catch (Exception e1) {
      }
    }
  }

  @Deprecated
  public static final void Assert(boolean exp) {
    Assert(exp, "");
  }

  public static final void Assert(boolean exp, String cmt)
  {
    if ((exp) || (!Config.ASSERT)) {
      return;
    }

    System.out.println(new StringBuilder().append("Assertion error [").append(cmt).append("]").toString());
    Thread.dumpStack();
  }
}