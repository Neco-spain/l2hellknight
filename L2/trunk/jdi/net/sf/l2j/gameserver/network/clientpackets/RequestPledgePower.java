package net.sf.l2j.gameserver.network.clientpackets;

import java.util.logging.Logger;
import net.sf.l2j.gameserver.model.L2Clan;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.ManagePledgePower;

public final class RequestPledgePower extends L2GameClientPacket
{
  static Logger _log = Logger.getLogger(ManagePledgePower.class.getName());
  private static final String _C__C0_REQUESTPLEDGEPOWER = "[C] C0 RequestPledgePower";
  private int _rank;
  private int _action;
  private int _privs;

  protected void readImpl()
  {
    _rank = readD();
    _action = readD();
    if (_action == 2)
    {
      _privs = readD();
    }
    else _privs = 0;
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) return;

    if (_action == 2)
    {
      if ((player.getClan() != null) && (player.isClanLeader()))
      {
        if (_rank == 9)
        {
          _privs = ((_privs & 0x8) + (_privs & 0x400) + (_privs & 0x8000));
        }

        player.getClan().setRankPrivs(_rank, _privs);
      }
    }
    else {
      ManagePledgePower mpp = new ManagePledgePower(((L2GameClient)getClient()).getActiveChar().getClan(), _action, _rank);
      player.sendPacket(mpp);
    }
  }

  public String getType()
  {
    return "[C] C0 RequestPledgePower";
  }
}