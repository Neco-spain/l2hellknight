package net.sf.l2j.gameserver.network.serverpackets;

public class SocialAction extends L2GameServerPacket
{
  private int _charObjId;
  private int _actionId;

  public SocialAction(int playerId, int actionId)
  {
    _charObjId = playerId;
    _actionId = actionId;
  }

  protected final void writeImpl()
  {
    writeC(45);
    writeD(_charObjId);
    writeD(_actionId);
  }

  public String getType()
  {
    return "S.SocialAction";
  }
}