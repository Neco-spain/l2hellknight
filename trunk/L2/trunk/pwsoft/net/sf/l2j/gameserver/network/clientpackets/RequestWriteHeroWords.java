package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.cache.Static;
import net.sf.l2j.gameserver.model.BlockList;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.actor.instance.L2PcInstance;
import net.sf.l2j.gameserver.network.L2GameClient;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.util.Log;
import net.sf.l2j.util.TimeLogger;

public final class RequestWriteHeroWords extends L2GameClientPacket
{
  private String _text;

  protected void readImpl()
  {
    _text = readS();
  }

  protected void runImpl()
  {
    L2PcInstance player = ((L2GameClient)getClient()).getActiveChar();
    if (player == null) {
      return;
    }
    if (!player.isHero()) {
      return;
    }
    if ((!player.isGM()) && (System.currentTimeMillis() - player.gCPBH() < 5000L))
    {
      player.sendPacket(Static.HERO_DELAY);
      return;
    }
    player.sCPBH();

    if (_text.length() > Config.MAX_CHAT_LENGTH) {
      _text = _text.substring(0, Config.MAX_CHAT_LENGTH);
    }
    _text = _text.replaceAll("\n", "");
    _text = _text.replace("\n", "");
    _text = _text.replace("n\\", "");
    _text = _text.replace("\r", "");
    _text = _text.replace("r\\", "");

    _text = ltrim(_text);
    _text = rtrim(_text);
    _text = itrim(_text);
    _text = lrtrim(_text);

    if (_text.isEmpty()) {
      return;
    }
    if ((Config.USE_CHAT_FILTER) && (!_text.startsWith(".")))
    {
      String[] badwords = { "\u0445\u0443\u0439", "\u043A\u0443\u0439", "\u043F\u0438\u0437\u0434", "\u0431\u043B\u044F\u0442", "\u0431\u043B\u044F\u0434", "\u0448\u043B\u044E\u0445", "\u0445\u0443\u0435", "\u0445\u0443\u0438", "\u0445\u0443\u044F", "\u043F\u0438\u0441\u0434", "\u0435\u0431\u0430\u0442", "\u0435\u0431\u043B\u0430", "\u0435\u0431\u0430\u043B", "\u0435\u0431\u0443\u0442", "\u0435\u0431\u043B", "\u043F\u0438\u0434\u043E", "\u043F\u0438\u0434\u0430", "\u0433\u0430\u043D\u0434", "eba", "ebu", "\u0433\u043D\u0438\u0434\u0430", "\u0437\u0430\u043B\u0443\u043F\u0430", "\u043C\u0443\u0434\u0438\u043B", "\u043C\u0443\u0434\u0430", "\u0432\u044B\u0435\u0431" };

      String wordn = "";
      String wordf = "";
      String newTextt = "";
      String delims = "[ ]+";
      String[] tokens = _text.split(delims);
      for (int i = 0; i < tokens.length; i++)
      {
        wordn = tokens[i];
        String word = wordn.toLowerCase();
        word = word.replace("a", "\u0430");
        word = word.replace("c", "\u0441");
        word = word.replace("s", "\u0441");
        word = word.replace("e", "\u0435");
        word = word.replace("k", "\u043A");
        word = word.replace("m", "\u043C");
        word = word.replace("o", "\u043E");
        word = word.replace("0", "\u043E");
        word = word.replace("x", "\u0445");
        word = word.replace("uy", "\u0443\u0439");
        word = word.replace("y", "\u0443");
        word = word.replace("u", "\u0443");
        word = word.replace("\u0451", "\u0435");
        word = word.replace("9", "\u044F");
        word = word.replace("3", "\u0437");
        word = word.replace("z", "\u0437");
        word = word.replace("d", "\u0434");
        word = word.replace("p", "\u043F");
        word = word.replace("i", "\u0438");
        word = word.replace("ya", "\u044F");
        word = word.replace("ja", "\u044F");
        for (String pattern : badwords)
        {
          if (word.matches(".*" + pattern + ".*"))
          {
            newTextt = word.replace(pattern, "-_-");
            break;
          }

          newTextt = wordn;
        }
        wordf = wordf + newTextt + " ";
      }
      _text = wordf.replace("null", "");
    }

    CreatureSay cs = new CreatureSay(player.getObjectId(), 17, player.getName(), _text);
    for (L2PcInstance pchar : L2World.getInstance().getAllPlayers()) {
      if (!BlockList.isBlocked(pchar, player))
        pchar.sendPacket(cs);
    }
    Log.add(TimeLogger.getTime() + player.getName() + ": " + _text, "hero_chat");
  }

  public static String ltrim(String source)
  {
    return source.replaceAll("^\\s+", "");
  }

  public static String rtrim(String source)
  {
    return source.replaceAll("\\s+$", "");
  }

  public static String itrim(String source)
  {
    return source.replaceAll("\\b\\s{2,}\\b", " ");
  }

  public static String trim(String source)
  {
    return itrim(ltrim(rtrim(source)));
  }

  public static String lrtrim(String source) {
    return ltrim(rtrim(source));
  }

  public String getType()
  {
    return "C.WriteHeroWords";
  }
}