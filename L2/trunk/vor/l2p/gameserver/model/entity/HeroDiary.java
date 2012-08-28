package l2p.gameserver.model.entity;

import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import l2p.gameserver.model.Player;
import l2p.gameserver.serverpackets.components.CustomMessage;
import l2p.gameserver.utils.HtmlUtils;

public class HeroDiary
{
  private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:** dd.MM.yyyy");
  public static final int ACTION_RAID_KILLED = 1;
  public static final int ACTION_HERO_GAINED = 2;
  public static final int ACTION_CASTLE_TAKEN = 3;
  private int _id;
  private long _time;
  private int _param;

  public HeroDiary(int id, long time, int param)
  {
    _id = id;
    _time = time;
    _param = param;
  }

  public Map.Entry<String, String> toString(Player player)
  {
    CustomMessage message = null;
    switch (_id)
    {
    case 1:
      message = new CustomMessage("l2p.gameserver.model.entity.Hero.RaidBossKilled", player, new Object[0]).addString(HtmlUtils.htmlNpcName(_param));
      break;
    case 2:
      message = new CustomMessage("l2p.gameserver.model.entity.Hero.HeroGained", player, new Object[0]);
      break;
    case 3:
      message = new CustomMessage("l2p.gameserver.model.entity.Hero.CastleTaken", player, new Object[0]).addString(HtmlUtils.htmlResidenceName(_param));
      break;
    default:
      return null;
    }

    return new AbstractMap.SimpleEntry(SIMPLE_FORMAT.format(Long.valueOf(_time)), message.toString());
  }
}