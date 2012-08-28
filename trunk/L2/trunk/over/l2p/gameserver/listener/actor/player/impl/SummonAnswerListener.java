package l2p.gameserver.listener.actor.player.impl;

import l2p.commons.lang.reference.HardReference;
import l2p.gameserver.listener.actor.player.OnAnswerListener;
import l2p.gameserver.model.Player;
import l2p.gameserver.model.items.PcInventory;
import l2p.gameserver.serverpackets.SystemMessage2;
import l2p.gameserver.serverpackets.components.SystemMsg;
import l2p.gameserver.utils.Location;

public class SummonAnswerListener
  implements OnAnswerListener
{
  private HardReference<Player> _playerRef;
  private Location _location;
  private long _count;

  public SummonAnswerListener(Player player, Location loc, long count)
  {
    _playerRef = player.getRef();
    _location = loc;
    _count = count;
  }

  public void sayYes()
  {
    Player player = (Player)_playerRef.get();
    if (player == null) {
      return;
    }
    player.abortAttack(true, true);
    player.abortCast(true, true);
    player.stopMove();
    if (_count > 0L)
    {
      if (player.getInventory().destroyItemByItemId(8615, _count))
      {
        player.sendPacket(SystemMessage2.removeItems(8615, _count));
        player.teleToLocation(_location);
      }
      else {
        player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
      }
    }
    else player.teleToLocation(_location);
  }

  public void sayNo()
  {
  }
}