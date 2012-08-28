package net.sf.l2j.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.ThreadPoolManager;
import net.sf.l2j.gameserver.ai.CtrlIntention;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.model.L2CharPosition;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public class MoveBackwardToLocation extends L2GameClientPacket
{
  private int _targetX;
  private int _targetY;
  private int _targetZ;
  private int _moveMovement;

  protected void readImpl()
  {
    _targetX = readD();
    _targetY = readD();
    _targetZ = readD();
    try {
      _moveMovement = readD();
    }
    catch (BufferUnderflowException e) {
      if (Config.KICK_L2WALKER) {
        L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();

        player.kick();
      }
    }
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }

    if (System.currentTimeMillis() - player.gCPBJ() < 111L) {
      player.sendActionFailed();
      return;
    }
    player.sCPBJ();

    if (_moveMovement == 0)
    {
      player.sendActionFailed();
      return;
    }

    if ((player.isOutOfControl()) || (player.isUltimate()) || (player.isParalyzed()))
    {
      player.sendActionFailed();
      return;
    }

    if (player.isInBoat()) {
      player.setInBoat(false);
    }

    if (player.getTeleMode() > 0) {
      if (player.getTeleMode() == 1) {
        player.setTeleMode(0);
      }
      player.sendActionFailed();
      player.teleToLocation(_targetX, _targetY, _targetZ, false);
      return;
    }

    player.updateLastTeleport(false);

    ThreadPoolManager.getInstance().executePathfind(new StartMoveTask(player, new L2CharPosition(_targetX, _targetY, _targetZ, 0)));
  }
  public static class StartMoveTask implements Runnable {
    private L2PcInstance _player;
    private L2CharPosition _loc;

    public StartMoveTask(L2PcInstance player, L2CharPosition loc) {
      _player = player;
      _loc = loc;
    }

    public void run() {
      _player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _loc);
    }
  }
}