package scripts.script;

import org.apache.bsf.BSFManager;

public class Expression
{
  private final BSFManager _context;
  private final String _lang;
  private final String _code;

  public static Object eval(String lang, String code)
  {
    try
    {
      return new BSFManager().eval(lang, "eval", 0, 0, code);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public static Object eval(BSFManager context, String lang, String code)
  {
    try
    {
      return context.eval(lang, "eval", 0, 0, code);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public static Expression create(BSFManager context, String lang, String code)
  {
    try
    {
      return new Expression(context, lang, code);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  private Expression(BSFManager pContext, String pLang, String pCode)
  {
    _context = pContext;
    _lang = pLang;
    _code = pCode;
  }

  public <T> void addDynamicVariable(String name, T value, Class<T> type)
  {
    try
    {
      _context.declareBean(name, value, type);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void removeDynamicVariable(String name)
  {
    try
    {
      _context.undeclareBean(name);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}