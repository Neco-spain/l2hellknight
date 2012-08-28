package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.templates.L2NpcTemplate;

public class L2ShopSkillInstance extends L2FolkInstance
{
	public L2ShopSkillInstance(int objectId, L2NpcTemplate template)
	  {
	    super(objectId, template);
	  }

	  public String getHtmlPath(int npcId, int val)
	  {
	    String pom = "";
	    if (val == 0)
	    {
	      pom = "" + npcId;
	    }
	    else
	    {
	      pom = npcId + "-" + val;
	    }
	    return "data/html/skillshop/" + pom + ".htm";
	  }
}
