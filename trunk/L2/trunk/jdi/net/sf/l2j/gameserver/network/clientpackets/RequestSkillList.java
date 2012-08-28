package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;

public final class RequestSkillList extends L2GameClientPacket
{
  private static final String _C__3F_REQUESTSKILLLIST = "[C] 3F RequestSkillList";
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
    cha.sendSkillList();
  }

  public String getType()
  {
    return "[C] 3F RequestSkillList";
  }
}