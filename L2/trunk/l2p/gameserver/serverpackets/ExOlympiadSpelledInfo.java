package l2p.gameserver.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2p.gameserver.model.Player;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
  private int char_obj_id = 0;
  private List<Effect> _effects;

  public ExOlympiadSpelledInfo()
  {
    _effects = new ArrayList();
  }

  public void addEffect(int skillId, int level, int duration)
  {
    _effects.add(new Effect(skillId, level, duration));
  }

  public void addSpellRecivedPlayer(Player cha)
  {
    if (cha != null)
      char_obj_id = cha.getObjectId();
  }

  protected final void writeImpl()
  {
    writeEx(123);

    writeD(char_obj_id);
    writeD(_effects.size());
    for (Effect temp : _effects)
    {
      writeD(temp.skillId);
      writeH(temp.level);
      writeD(temp.duration);
    }
  }

  class Effect
  {
    int skillId;
    int level;
    int duration;

    public Effect(int skillId, int level, int duration)
    {
      this.skillId = skillId;
      this.level = level;
      this.duration = duration;
    }
  }
}