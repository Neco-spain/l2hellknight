package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.components.NpcString;

public class ExSendUIEvent extends NpcStringContainer
{
  private int _objectId;
  private boolean _isHide;
  private boolean _isIncrease;
  private int _startTime;
  private int _endTime;

  public ExSendUIEvent(Player player, boolean isHide, boolean isIncrease, int startTime, int endTime, String[] params)
  {
    this(player, isHide, isIncrease, startTime, endTime, NpcString.NONE, params);
  }

  public ExSendUIEvent(Player player, boolean isHide, boolean isIncrease, int startTime, int endTime, NpcString npcString, String[] params)
  {
    super(npcString, params);
    _objectId = player.getObjectId();
    _isHide = isHide;
    _isIncrease = isIncrease;
    _startTime = startTime;
    _endTime = endTime;
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(142);
    writeD(_objectId);
    writeD(_isHide ? 1 : 0);
    writeD(0);
    writeD(0);
    writeS(_isIncrease ? "1" : "0");
    writeS(String.valueOf(_startTime / 60));
    writeS(String.valueOf(_startTime % 60));
    writeS(String.valueOf(_endTime / 60));
    writeS(String.valueOf(_endTime % 60));
    writeElements();
  }
}