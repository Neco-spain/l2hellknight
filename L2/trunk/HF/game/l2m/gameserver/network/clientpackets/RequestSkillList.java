package l2m.gameserver.network.clientpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.network.GameClient;
import l2m.gameserver.network.serverpackets.SkillList;

public final class RequestSkillList extends L2GameClientPacket
{
  private static final String _C__50_REQUESTSKILLLIST = "[C] 50 RequestSkillList";

  protected void readImpl()
  {
  }

  protected void runImpl()
  {
    Player cha = ((GameClient)getClient()).getActiveChar();

    if (cha != null)
      cha.sendPacket(new SkillList(cha));
  }

  public String getType()
  {
    return "[C] 50 RequestSkillList";
  }
}