package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Player;
import l2p.gameserver.model.Summon;
import l2p.gameserver.templates.npc.NpcTemplate;

public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
  private int owner_obj_id;
  private int npc_id;
  private int _type;
  private int curHp;
  private int maxHp;
  private int curMp;
  private int maxMp;
  private int level;
  private int obj_id = 0;
  private String _name;

  public ExPartyPetWindowUpdate(Summon summon)
  {
    obj_id = summon.getObjectId();
    owner_obj_id = summon.getPlayer().getObjectId();
    npc_id = (summon.getTemplate().npcId + 1000000);
    _type = summon.getSummonType();
    _name = summon.getName();
    curHp = (int)summon.getCurrentHp();
    maxHp = summon.getMaxHp();
    curMp = (int)summon.getCurrentMp();
    maxMp = summon.getMaxMp();
    level = summon.getLevel();
  }

  protected final void writeImpl()
  {
    writeEx(25);
    writeD(obj_id);
    writeD(npc_id);
    writeD(_type);
    writeD(owner_obj_id);
    writeS(_name);
    writeD(curHp);
    writeD(maxHp);
    writeD(curMp);
    writeD(maxMp);
    writeD(level);
  }
}