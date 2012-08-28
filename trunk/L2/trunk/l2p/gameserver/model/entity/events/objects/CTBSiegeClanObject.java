package l2p.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2p.gameserver.dao.SiegePlayerDAO;
import l2p.gameserver.model.GameObjectsStorage;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.SiegeEvent;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.pledge.Clan;

public class CTBSiegeClanObject extends SiegeClanObject
{
  public static final long serialVersionUID = 1L;
  private List<Integer> _players = new ArrayList();
  private long _npcId;

  public CTBSiegeClanObject(String type, Clan clan, long param, long date)
  {
    super(type, clan, param, date);
    _npcId = param;
  }

  public CTBSiegeClanObject(String type, Clan clan, long param)
  {
    this(type, clan, param, System.currentTimeMillis());
  }

  public void select(Residence r)
  {
    _players.addAll(SiegePlayerDAO.getInstance().select(r, getObjectId()));
  }

  public List<Integer> getPlayers()
  {
    return _players;
  }

  public void setEvent(boolean start, SiegeEvent event)
  {
    for (Iterator i$ = getPlayers().iterator(); i$.hasNext(); ) { int i = ((Integer)i$.next()).intValue();

      Player player = GameObjectsStorage.getPlayer(i);
      if (player != null)
      {
        if (start)
          player.addEvent(event);
        else
          player.removeEvent(event);
        player.broadcastCharInfo();
      }
    }
  }

  public boolean isParticle(Player player)
  {
    return _players.contains(Integer.valueOf(player.getObjectId()));
  }

  public long getParam()
  {
    return _npcId;
  }

  public void setParam(int npcId)
  {
    _npcId = npcId;
  }
}