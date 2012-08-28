package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;
import net.sf.l2j.gameserver.model.entity.Hero;
import net.sf.l2j.gameserver.templates.StatsSet;

public class ExHeroList extends L2GameServerPacket
{
  private static final String _S__FE_23_EXHEROLIST = "[S] FE:23 ExHeroList";
  private Map<Integer, StatsSet> _heroList;

  public ExHeroList()
  {
    _heroList = Hero.getInstance().getHeroes();
  }

  protected void writeImpl()
  {
    writeC(254);
    writeH(35);
    writeD(_heroList.size());

    for (Integer heroId : _heroList.keySet())
    {
      StatsSet hero = (StatsSet)_heroList.get(heroId);
      writeS(hero.getString("char_name"));
      writeD(hero.getInteger("class_id"));
      writeS(hero.getString("clan_name", ""));
      writeD(hero.getInteger("clan_crest", 0));
      writeS(hero.getString("ally_name", ""));
      writeD(hero.getInteger("ally_crest", 0));
      writeD(hero.getInteger("count"));
    }
  }

  public String getType()
  {
    return "[S] FE:23 ExHeroList";
  }
}