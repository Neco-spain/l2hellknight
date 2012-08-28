package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestSkillList extends L2GameClientPacket
{
  private int _unk1;
  private int _unk2;
  private int _unk3;

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    L2PcInstance cha = ((L2GameClient)getClient()).getActiveChar();

    if (cha == null) {
      return;
    }
    if (System.currentTimeMillis() - cha.gCPAK() < 200L) {
      return;
    }
    cha.sCPAK();

    cha.sendSkillList();
  }

  public String getType()
  {
    return "C.SkillList";
  }
}