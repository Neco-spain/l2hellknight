package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.base.ClassId;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
  private int _side;
  private int class_id;
  private int curHp;
  private int maxHp;
  private int curCp;
  private int maxCp;
  private int obj_id = 0;
  private String _name;

  public ExOlympiadUserInfo(Player player, int side)
  {
    _side = side;
    obj_id = player.getObjectId();
    class_id = player.getClassId().getId();
    _name = player.getName();
    curHp = (int)player.getCurrentHp();
    maxHp = player.getMaxHp();
    curCp = (int)player.getCurrentCp();
    maxCp = player.getMaxCp();
  }

  protected final void writeImpl()
  {
    writeEx(122);
    writeC(_side);
    writeD(obj_id);
    writeS(_name);
    writeD(class_id);
    writeD(curHp);
    writeD(maxHp);
    writeD(curCp);
    writeD(maxCp);
  }
}