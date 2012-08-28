package l2m.gameserver.network.serverpackets;

import l2m.gameserver.model.Player;
import l2m.gameserver.model.Summon;

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
  private int _summonObjectId;
  private int _ownerObjectId;
  private String _summonName;

  public ExPartyPetWindowDelete(Summon summon)
  {
    _summonObjectId = summon.getObjectId();
    _summonName = summon.getName();
    _ownerObjectId = summon.getPlayer().getObjectId();
  }

  protected final void writeImpl()
  {
    writeEx(106);
    writeD(_summonObjectId);
    writeD(_ownerObjectId);
    writeS(_summonName);
  }
}