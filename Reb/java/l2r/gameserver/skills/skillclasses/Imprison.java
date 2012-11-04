package l2r.gameserver.skills.skillclasses;

import java.util.List;

import l2r.gameserver.model.Creature;
import l2r.gameserver.model.Player;
import l2r.gameserver.model.Skill;
import l2r.gameserver.network.serverpackets.Say2;
import l2r.gameserver.network.serverpackets.components.ChatType;
import l2r.gameserver.templates.StatsSet;
import l2r.gameserver.utils.AutoBan;
import l2r.gameserver.utils.TimeUtils;

public class Imprison extends Skill
{
	public Imprison(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(Creature activeChar, List<Creature> targets)
	{
		for(Creature target : targets)
		{
			if(target != null)
			{
                if (!target.isPlayer())
					continue;

                Player player = target.getPlayer();
				AutoBan.doJainPlayer(player, (int)getPower() * 1000L, false);
				player.sendPacket(new Say2(0, ChatType.TELL, "♦", "Персонаж " + activeChar.getName() + " наложил на Вас проклятие заточения. Вы посажены в тюрьму на срок " + TimeUtils.minutesToFullString((int)getPower() / 60)));
			}
		}
	}
}