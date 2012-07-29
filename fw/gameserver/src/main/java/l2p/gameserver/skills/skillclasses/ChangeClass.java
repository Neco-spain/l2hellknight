package l2p.gameserver.skills.skillclasses;

import java.util.List;
import l2p.gameserver.model.*;
import l2p.gameserver.templates.StatsSet;

/**
 * Created by IntelliJ IDEA.
 * User: Cain
 * Date: 27.02.12
 * Time: 18:36
 * To change this template use File | Settings | File Templates.
 */
public class ChangeClass extends Skill
{
    public ChangeClass(StatsSet set)
    {
        super(set);
        _classIndex = set.getInteger("class_index");
    }

    public void useSkill(Creature caster, List targets)
    {
        Player activeChar = caster.getPlayer();
        activeChar.changeClass(activeChar, _classIndex);
    }

    private int _classIndex;
}
