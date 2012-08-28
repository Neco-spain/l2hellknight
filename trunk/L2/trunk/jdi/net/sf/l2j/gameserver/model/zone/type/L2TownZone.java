package net.sf.l2j.gameserver.model.zone.type;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.util.Rnd;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class L2TownZone extends L2ZoneType
{
  private String _townName;
  private int _townId;
  private int _redirectTownId;
  private int _taxById;
  private boolean _noPeace;
  private FastList _spawnLocs;

  public L2TownZone(int id)
  {
    super(id);
    _taxById = 0;
    _spawnLocs = new FastList();
    _redirectTownId = 9;
    _noPeace = false;
  }

  public void setParameter(String s, String s1)
  {
    if (s.equals("name")) {
      _townName = s1;
    }
    else if (s.equals("townId")) {
      _townId = Integer.parseInt(s1);
    }
    else if (s.equals("redirectTownId")) {
      _redirectTownId = Integer.parseInt(s1);
    }
    else if (s.equals("taxById")) {
      _taxById = Integer.parseInt(s1);
    }
    else if (s.equals("noPeace"))
      _noPeace = Boolean.parseBoolean(s1);
    else
      super.setParameter(s, s1);
  }

  public void setSpawnLocs(Node node)
  {
    int[] ai = new int[3];
    Node node1 = node.getAttributes().getNamedItem("X");
    if (node1 != null)
      ai[0] = Integer.parseInt(node1.getNodeValue());
    node1 = node.getAttributes().getNamedItem("Y");
    if (node1 != null)
      ai[1] = Integer.parseInt(node1.getNodeValue());
    node1 = node.getAttributes().getNamedItem("Z");
    if (node1 != null)
      ai[2] = Integer.parseInt(node1.getNodeValue());
    if (ai != null)
      _spawnLocs.add(ai);
  }

  protected void onEnter(L2Character l2character)
  {
    if (((l2character instanceof L2PcInstance)) && (((L2PcInstance)l2character).getSiegeState() != 0) && (Config.ZONE_TOWN == 1))
      return;
    if ((!_noPeace) && (Config.ZONE_TOWN != 2))
      l2character.setInsideZone(2, true);
  }

  protected void onExit(L2Character l2character)
  {
    if (!_noPeace)
      l2character.setInsideZone(2, false);
  }

  protected void onDieInside(L2Character l2character)
  {
  }

  protected void onReviveInside(L2Character l2character)
  {
  }

  public String getName()
  {
    return _townName;
  }

  public int getTownId()
  {
    return _townId;
  }

  public int getRedirectTownId()
  {
    return _redirectTownId;
  }

  public final int[] getSpawnLoc()
  {
    int[] ai = new int[3];
    ai = (int[])(int[])_spawnLocs.get(Rnd.get(_spawnLocs.size()));
    return ai;
  }

  public final int getTaxById()
  {
    return _taxById;
  }
}