package scripts.script;

import org.apache.bsf.BSFManager;
import org.w3c.dom.Node;

public abstract class Parser
{
  public abstract void parseScript(Node paramNode, BSFManager paramBSFManager);
}