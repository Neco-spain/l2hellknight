package l2m.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import l2m.gameserver.data.xml.holder.ResidenceHolder;
import l2m.gameserver.model.entity.events.impl.SiegeEvent;
import l2m.gameserver.model.entity.residence.Fortress;
import l2m.gameserver.model.pledge.Clan;

public class ExShowFortressInfo extends L2GameServerPacket
{
  private List<FortressInfo> _infos = Collections.emptyList();

  public ExShowFortressInfo()
  {
    List forts = ResidenceHolder.getInstance().getResidenceList(Fortress.class);
    _infos = new ArrayList(forts.size());
    for (Fortress fortress : forts)
    {
      Clan owner = fortress.getOwner();
      _infos.add(new FortressInfo(owner == null ? "" : owner.getName(), fortress.getId(), fortress.getSiegeEvent().isInProgress(), owner == null ? 0 : (int)((System.currentTimeMillis() - fortress.getOwnDate().getTimeInMillis()) / 1000L)));
    }
  }

  protected final void writeImpl()
  {
    writeEx(21);
    writeD(_infos.size());
    for (FortressInfo _info : _infos)
    {
      writeD(_info._id);
      writeS(_info._owner);
      writeD(_info._status);
      writeD(_info._siege);
    }
  }
  static class FortressInfo {
    public int _id;
    public int _siege;
    public String _owner;
    public boolean _status;

    public FortressInfo(String owner, int id, boolean status, int siege) { _owner = owner;
      _id = id;
      _status = status;
      _siege = siege;
    }
  }
}