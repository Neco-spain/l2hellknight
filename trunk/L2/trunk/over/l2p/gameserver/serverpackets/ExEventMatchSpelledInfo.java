package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.model.Player;

public class ExEventMatchSpelledInfo extends L2GameServerPacket
{
  private int char_obj_id = 0;
  private List<Effect> _effects;

  public ExEventMatchSpelledInfo()
  {
    _effects = new ArrayList();
  }

  public void addEffect(int skillId, int dat, int duration)
  {
    _effects.add(new Effect(skillId, dat, duration));
  }

  public void addSpellRecivedPlayer(Player cha)
  {
    if (cha != null)
      char_obj_id = cha.getObjectId();
  }

  protected void writeImpl()
  {
    writeEx(4);

    writeD(char_obj_id);
    writeD(_effects.size());
    for (Effect temp : _effects)
    {
      writeD(temp.skillId);
      writeH(temp.dat);
      writeD(temp.duration);
    }
  }

  class Effect
  {
    int skillId;
    int dat;
    int duration;

    public Effect(int skillId, int dat, int duration)
    {
      this.skillId = skillId;
      this.dat = dat;
      this.duration = duration;
    }
  }
}