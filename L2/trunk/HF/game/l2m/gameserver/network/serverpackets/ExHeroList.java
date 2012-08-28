package l2m.gameserver.network.serverpackets;

import java.util.Map;
import l2m.gameserver.model.entity.Hero;
import l2m.gameserver.templates.StatsSet;

public class ExHeroList extends L2GameServerPacket
{
  private Map<Integer, StatsSet> _heroList;

  public ExHeroList()
  {
    _heroList = Hero.getInstance().getHeroes();
  }

  protected final void writeImpl()
  {
    writeEx(121);

    writeD(_heroList.size());
    for (StatsSet hero : _heroList.values())
    {
      writeS(hero.getString("char_name"));
      writeD(hero.getInteger("class_id"));
      writeS(hero.getString("clan_name", ""));
      writeD(hero.getInteger("clan_crest", 0));
      writeS(hero.getString("ally_name", ""));
      writeD(hero.getInteger("ally_crest", 0));
      writeD(hero.getInteger("count"));
    }
  }
}