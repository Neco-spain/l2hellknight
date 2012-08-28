package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestBlock extends L2GameClientPacket
{
  private static final int BLOCK = 0;
  private static final int UNBLOCK = 1;
  private static final int BLOCKLIST = 2;
  private static final int ALLBLOCK = 3;
  private static final int ALLUNBLOCK = 4;
  private String _name;
  private Integer _type;
  private L2PcInstance _target;

  protected void readImpl()
  {
    _type = Integer.valueOf(readD());

    if ((_type.intValue() == 0) || (_type.intValue() == 1))
    {
      _name = readS();
      _target = L2World.getInstance().getPlayer(_name);
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    switch (_type.intValue())
    {
    case 0:
    case 1:
      if (_target == null)
      {
        player.sendPacket(Static.FAILED_TO_REGISTER_TO_IGNORE_LIST);
        return;
      }

      if (_target.isGM())
      {
        player.sendPacket(Static.YOU_MAY_NOT_IMPOSE_A_BLOCK_AN_A_GM);
        return;
      }

      if (_type.intValue() == 0)
        BlockList.addToBlockList(player, _target);
      else
        BlockList.removeFromBlockList(player, _target);
      break;
    case 2:
      BlockList.sendListToOwner(player);
      break;
    case 3:
      player.setMessageRefusal(true);
      player.sendPacket(Static.YOU_ARE_NOW_BLOCKING_EVERYTHING);
      break;
    case 4:
      player.setMessageRefusal(false);
      player.sendPacket(Static.YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING);
    }
  }
}