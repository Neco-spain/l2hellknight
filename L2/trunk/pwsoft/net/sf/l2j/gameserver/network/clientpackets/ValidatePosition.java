package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class ValidatePosition extends L2GameClientPacket
{
  private int _x;
  private int _y;
  private int _z;
  private int _heading;
  private int _data;

  protected void readImpl()
  {
    _x = readD();
    _y = readD();
    _z = readD();
    _heading = readD();
    _data = readD();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if ((player.isTeleporting()) || (player.inObserverMode())) {
      return;
    }

    if ((Config.ALLOW_FALL) && (player.isFalling(_z))) {
      return;
    }
    int realX = player.getX();
    int realY = player.getY();
    int realZ = player.getZ();

    int dx = _x - realX;
    int dy = _y - realY;
    int dz = _z - realZ;
    double diffSq = dx * dx + dy * dy;

    if ((player.isFlying()) || (player.isInWater()))
    {
      realZ = _z;
      player.setXYZ(realX, realY, _z);
      if (diffSq > 90000.0D)
      {
        player.sendPacket(new ValidateLocation(player));
      }

    }
    else if (diffSq < 360000.0D)
    {
      if (Config.COORD_SYNCHRONIZE == -1)
      {
        player.setXYZ(realX, realY, _z);
        return;
      }
      if (Config.COORD_SYNCHRONIZE == 1)
      {
        if ((!player.isMoving()) || (!player.validateMovementHeading(_heading)))
        {
          if (diffSq < 2500.0D)
          {
            player.setXYZ(realX, realY, _z);
          }
          else player.setXYZ(_x, _y, _z);
        }
        else {
          player.setXYZ(realX, realY, _z);
        }

        player.setHeading(_heading);
        return;
      }

      if ((Config.GEODATA > 0) && ((diffSq > 250000.0D) || (Math.abs(dz) > 200)))
      {
        if ((Math.abs(dz) > 200) && (Math.abs(dz) < 1500) && (Math.abs(_z - player.getClientZ()) < 800))
        {
          player.setXYZ(realX, realY, _z);
          realZ = _z;
        } else {
          player.sendPacket(new ValidateLocation(player));
        }
      }
    }

    player.setClientX(_x);
    player.setClientY(_y);
    player.setClientZ(_z);
    player.setClientHeading(_heading);
    player.setLastServerPosition(realX, realY, realZ);
  }
}