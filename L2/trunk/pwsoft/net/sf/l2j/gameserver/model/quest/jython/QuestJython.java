package net.sf.l2j.gameserver.model.quest.jython;

import java.io.File;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.quest.Quest;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

public abstract class QuestJython extends Quest
{
  private static BSFManager _bsf;

  public static void init()
  {
    try
    {
      _bsf = new BSFManager();

      String dataPackDirForwardSlashes = Config.DATAPACK_ROOT.getPath().replaceAll("\\\\", "/");
      String loadingScript = "import sys;sys.path.insert(0,'" + dataPackDirForwardSlashes + "');" + "import data";
      _bsf.exec("jython", "quest", 0, 0, loadingScript);
    }
    catch (BSFException e)
    {
      e.printStackTrace();
    }
  }

  public static boolean reloadQuest(String questFolder)
  {
    try
    {
      _bsf.exec("jython", "quest", 0, 0, "reload(data.jscript." + questFolder + ");");
      return true;
    }
    catch (Exception e)
    {
    }

    return false;
  }

  public QuestJython(int questId, String name, String descr)
  {
    super(questId, name, descr);
  }

  public QuestJython(int questId, String name, String descr, int ex)
  {
    super(questId, name, descr, ex);
  }
}