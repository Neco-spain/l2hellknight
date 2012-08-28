package net.sf.l2j.gameserver.instancemanager;

import java.io.PrintStream;
import java.util.logging.Logger;
import javolution.util.FastList;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.zone.type.L2OlympiadStadiumZone;

public class OlympiadStadiaManager
{
  protected static Logger _log = Logger.getLogger(OlympiadStadiaManager.class.getName());
  private static OlympiadStadiaManager _instance;
  private FastList<L2OlympiadStadiumZone> _olympiadStadias;

  public static final OlympiadStadiaManager getInstance()
  {
    if (_instance == null)
    {
      System.out.println("Initializing OlympiadStadiaManager");
      _instance = new OlympiadStadiaManager();
    }
    return _instance;
  }

  public void addStadium(L2OlympiadStadiumZone arena)
  {
    if (_olympiadStadias == null) {
      _olympiadStadias = new FastList();
    }
    _olympiadStadias.add(arena);
  }

  public final L2OlympiadStadiumZone getStadium(L2Character character)
  {
    for (L2OlympiadStadiumZone temp : _olympiadStadias) {
      if (temp.isCharacterInZone(character)) return temp;
    }
    return null;
  }

  @Deprecated
  public final L2OlympiadStadiumZone getOlympiadStadiumById(int olympiadStadiumId) {
    for (L2OlympiadStadiumZone temp : _olympiadStadias)
      if (temp.getStadiumId() == olympiadStadiumId) return temp;
    return null;
  }
}