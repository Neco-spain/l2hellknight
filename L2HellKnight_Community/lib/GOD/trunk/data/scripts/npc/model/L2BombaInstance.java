package npc.model;

import javolution.util.FastList;
import l2rt.gameserver.model.*;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2NpcTemplate;

public class L2BombaInstance extends L2NpcInstance
{

    public L2BombaInstance(int objectId, L2NpcTemplate template)
    {
        super(objectId, template);
    }

    @SuppressWarnings("unchecked")
	public void reduceCurrentHp(double i, L2Character attacker, boolean awake, boolean standUp, boolean directHp)
    {
        L2Skill skill = SkillTable.getInstance().getInfo(4571, 7);
        FastList targets = new FastList();
        targets.addAll(L2World.getAroundNpc(this, skill.getSkillRadius(), 200));
        if(getCurrentHp() - i < 1.0D)
            super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, directHp);
    }
}
