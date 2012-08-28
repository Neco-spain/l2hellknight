package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends L2GameClientPacket
{
  public void readImpl()
  {
  }

  public void runImpl()
  {
    L2PcInstance pl = ((L2GameClient)getClient()).getActiveChar();
    if (pl == null) {
      return;
    }
    if (System.currentTimeMillis() - pl.gCPAT() < 500L) {
      return;
    }
    pl.sCPAT();

    pl.sendPacket(new SkillCoolTime(pl));
  }
}