package net.sf.l2j.gameserver.script;

import javax.script.ScriptContext;
import org.w3c.dom.Node;

public abstract class Parser
{
  public abstract void parseScript(Node paramNode, ScriptContext paramScriptContext);
}