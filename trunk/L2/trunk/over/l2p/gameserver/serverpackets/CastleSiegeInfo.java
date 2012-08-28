package l2p.gameserver.serverpackets;

import java.util.Calendar;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2p.gameserver.model.entity.residence.Castle;
import l2p.gameserver.model.entity.residence.ClanHall;
import l2p.gameserver.model.entity.residence.Residence;
import l2p.gameserver.model.pledge.Alliance;
import l2p.gameserver.model.pledge.Clan;
import org.apache.commons.lang3.ArrayUtils;

public class CastleSiegeInfo extends L2GameServerPacket
{
  private long _startTime;
  private int _id;
  private int _ownerObjectId;
  private int _allyId;
  private boolean _isLeader;
  private String _ownerName = "NPC";
  private String _leaderName = "";
  private String _allyName = "";
  private int[] _nextTimeMillis = ArrayUtils.EMPTY_INT_ARRAY;

  public CastleSiegeInfo(Castle castle, Player player)
  {
    this(castle, player);

    CastleSiegeEvent siegeEvent = (CastleSiegeEvent)castle.getSiegeEvent();
    long siegeTimeMillis = castle.getSiegeDate().getTimeInMillis();
    if (siegeTimeMillis == 0L)
      _nextTimeMillis = siegeEvent.getNextSiegeTimes();
    else
      _startTime = (int)(siegeTimeMillis / 1000L);
  }

  public CastleSiegeInfo(ClanHall ch, Player player)
  {
    this(ch, player);

    _startTime = (int)(ch.getSiegeDate().getTimeInMillis() / 1000L);
  }

  protected CastleSiegeInfo(Residence residence, Player player)
  {
    _id = residence.getId();
    _ownerObjectId = residence.getOwnerId();
    Clan owner = residence.getOwner();
    if (owner != null)
    {
      _isLeader = ((player.isGM()) || (owner.getLeaderId(0) == player.getObjectId()));
      _ownerName = owner.getName();
      _leaderName = owner.getLeaderName(0);
      Alliance ally = owner.getAlliance();
      if (ally != null)
      {
        _allyId = ally.getAllyId();
        _allyName = ally.getAllyName();
      }
    }
  }

  protected void writeImpl()
  {
    writeC(201);
    writeD(_id);
    writeD(_isLeader ? 1 : 0);
    writeD(_ownerObjectId);
    writeS(_ownerName);
    writeS(_leaderName);
    writeD(_allyId);
    writeS(_allyName);
    writeD((int)(Calendar.getInstance().getTimeInMillis() / 1000L));
    writeD((int)_startTime);
    if (_startTime == 0L)
      writeDD(_nextTimeMillis, true);
  }
}