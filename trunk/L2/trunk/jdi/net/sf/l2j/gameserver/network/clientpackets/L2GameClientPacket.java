package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.Config;
import net.sf.l2j.gameserver.GameTimeController;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.L2GameClient.GameClientState;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import org.mmocore.network.ReceivablePacket;

public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
  private static final Logger _log = Logger.getLogger(L2GameClientPacket.class.getName());

  protected boolean read()
  {
    try
    {
      readImpl();
      return true;
    }
    catch (Throwable t)
    {
      _log.severe("Client: " + ((L2GameClient)getClient()).toString() + " - Failed reading: " + getType());
      t.printStackTrace();
    }
    return false;
  }

  protected abstract void readImpl();

  public void run()
  {
    try
    {
      if (GameTimeController.getGameTicks() - ((L2GameClient)getClient()).packetsSentStartTick > 10)
      {
        ((L2GameClient)getClient()).packetsSentStartTick = GameTimeController.getGameTicks();
        ((L2GameClient)getClient()).packetsSentInSec = 0;
      }
      else
      {
        L2GameClient tmp53_50 = ((L2GameClient)getClient()); tmp53_50.packetsSentInSec = (byte)(tmp53_50.packetsSentInSec + 1);
        if (((L2GameClient)getClient()).packetsSentInSec > 12)
        {
          if (((L2GameClient)getClient()).packetsSentInSec < 100)
            sendPacket(new ActionFailed());
          return;
        }

      }

      if ((((L2GameClient)getClient()).getEnterWorld()) && (Config.ENTER_WORLD_FIX) && (((L2GameClient)getClient()).getState() == L2GameClient.GameClientState.IN_GAME))
      {
        if ((this instanceof EnterWorld))
        {
          ((L2GameClient)getClient()).setEnterWorld(false);
        }
        else if (!(this instanceof RequestManorList))
        {
          if (!(this instanceof GameGuardReply))
          {
            _log.severe(((L2GameClient)getClient()).getActiveChar().getName() + "  : fail wait EnterWorld - packet name " + getClass().getName());
            ((L2GameClient)getClient()).MyOnForcedDisconnection();
            return;
          }
        }
      }

      runImpl();
      if (((this instanceof MoveBackwardToLocation)) || ((this instanceof AttackRequest)) || ((this instanceof RequestMagicSkillUse)))
      {
        if (((L2GameClient)getClient()).getActiveChar() != null)
          ((L2GameClient)getClient()).getActiveChar().onActionRequest();
      }
    }
    catch (Throwable t)
    {
      _log.severe("Client: " + ((L2GameClient)getClient()).toString() + " - Failed running: " + getType());
      t.printStackTrace();
    }
  }

  protected abstract void runImpl();

  protected final void sendPacket(L2GameServerPacket gsp) {
    ((L2GameClient)getClient()).sendPacket(gsp);
  }

  public abstract String getType();
}