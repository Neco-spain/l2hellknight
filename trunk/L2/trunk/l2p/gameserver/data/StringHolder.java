package l2p.gameserver.data;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.Config;
import l2p.gameserver.model.Player;
import l2p.gameserver.utils.Language;

public final class StringHolder extends AbstractHolder
{
  private static final StringHolder _instance = new StringHolder();
  private Map<Language, Map<String, String>> _strings = new HashMap();

  public static StringHolder getInstance()
  {
    return _instance;
  }

  public String getNullable(Player player, String name)
  {
    Language lang = player == null ? Language.ENGLISH : player.getLanguage();
    return get(lang, name);
  }

  public String getNotNull(Player player, String name)
  {
    Language lang = player == null ? Language.ENGLISH : player.getLanguage();

    String text = get(lang, name);
    if ((text == null) && (player != null))
    {
      text = "Not find string: " + name + "; for lang: " + lang;
      ((Map)_strings.get(lang)).put(name, text);
    }

    return text;
  }

  private String get(Language lang, String address)
  {
    Map strings = (Map)_strings.get(lang);

    return (String)strings.get(address);
  }

  public void load()
  {
    for (Language lang : Language.VALUES)
    {
      _strings.put(lang, new HashMap());

      File f = new File(Config.DATAPACK_ROOT, "data/string/strings_" + lang.getShortName() + ".properties");
      if (!f.exists())
      {
        warn("Not find file: " + f.getAbsolutePath());
      }
      else
      {
        LineNumberReader reader = null;
        try
        {
          reader = new LineNumberReader(new FileReader(f));
          String line = null;

          while ((line = reader.readLine()) != null)
          {
            if (line.startsWith("#")) {
              continue;
            }
            StringTokenizer token = new StringTokenizer(line, "=");
            if (token.countTokens() < 2)
            {
              error("Error on line: " + line + "; file: " + f.getName());
              continue;
            }

            String name = token.nextToken();
            String value = token.nextToken();
            while (token.hasMoreTokens()) {
              value = value + "=" + token.nextToken();
            }
            Map strings = (Map)_strings.get(lang);

            strings.put(name, value);
          }
        }
        catch (Exception e)
        {
          error("Exception: " + e, e);
        }
        finally {
          try {
            reader.close(); } catch (Exception e) {
          }
        }
      }
    }
    log();
  }

  public void reload()
  {
    clear();
    load();
  }

  public void log()
  {
    for (Map.Entry entry : _strings.entrySet())
      info("load strings: " + ((Map)entry.getValue()).size() + " for lang: " + entry.getKey());
  }

  public int size()
  {
    return 0;
  }

  public void clear()
  {
    _strings.clear();
  }
}