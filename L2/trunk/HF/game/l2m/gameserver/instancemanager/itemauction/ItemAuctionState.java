package l2m.gameserver.instancemanager.itemauction;

import l2p.commons.lang.ArrayUtils;

public enum ItemAuctionState
{
  CREATED, 
  STARTED, 
  FINISHED;

  public static final ItemAuctionState stateForStateId(int stateId)
  {
    return (ItemAuctionState)ArrayUtils.valid(values(), stateId);
  }
}