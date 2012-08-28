package l2p.gameserver.model.entity.events.objects;

import l2p.gameserver.model.pledge.Clan;

public class AuctionSiegeClanObject extends SiegeClanObject
{
  private long _bid;

  public AuctionSiegeClanObject(String type, Clan clan, long param)
  {
    this(type, clan, param, System.currentTimeMillis());
  }

  public AuctionSiegeClanObject(String type, Clan clan, long param, long date)
  {
    super(type, clan, param, date);
    _bid = param;
  }

  public long getParam()
  {
    return _bid;
  }

  public void setParam(long param)
  {
    _bid = param;
  }
}