package net.sf.l2j.gameserver.script;

import javax.script.ScriptContext;
import net.sf.l2j.gameserver.scripting.L2ScriptEngineManager;

public class Expression
{
  private final ScriptContext _context;
  private final String _lang;
  private final String _code;

  public static Object eval(String lang, String code)
  {
    try
    {
      return L2ScriptEngineManager.getInstance().eval(lang, code);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public static Object eval(ScriptContext context, String lang, String code)
  {
    try
    {
      return L2ScriptEngineManager.getInstance().eval(lang, code, context);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }return null;
  }

  public static Expression create(ScriptContext context, String lang, String code)
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

  private Expression(ScriptContext pContext, String pLang, String pCode)
  {
    _context = pContext;
    _lang = pLang;
    _code = pCode;
  }

  public <T> void addDynamicVariable(String name, T value)
  {
    try
    {
      _context.setAttribute(name, value, 100);
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
      _context.removeAttribute(name, 100);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}