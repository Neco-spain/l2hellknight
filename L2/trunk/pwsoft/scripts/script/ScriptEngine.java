package scripts.script;

import java.util.Hashtable;
import scripts.script.faenor.FaenorInterface;

public class ScriptEngine
{
  protected EngineInterface _utils = new FaenorInterface();
  public static final Hashtable<String, ParserFactory> parserFactories = new Hashtable();

  protected static Parser createParser(String name)
    throws ParserNotCreatedException
  {
    ParserFactory s = (ParserFactory)parserFactories.get(name);
    if (s == null)
    {
      try
      {
        Class.forName("scripts.script." + name);

        s = (ParserFactory)parserFactories.get(name);
        if (s == null)
        {
          throw new ParserNotCreatedException();
        }

      }
      catch (ClassNotFoundException e)
      {
        throw new ParserNotCreatedException();
      }
    }
    return s.create();
  }
}