package l2rt.gameserver.model.instances;

import l2rt.common.ThreadPoolManager;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2ObjectsStorage;
import l2rt.gameserver.model.L2Player;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.network.serverpackets.MagicSkillUse;
import l2rt.gameserver.network.serverpackets.SkillList;
import l2rt.gameserver.tables.SkillTable;
import l2rt.util.GArray;
import l2rt.util.Rnd;

import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author SYS
 */
public class L2AgathionInstance
{
	protected static final Logger _log = Logger.getLogger(L2AgathionInstance.class.getName());

	public static final int BEASTLY_AGATHION_ID = 16026;
	public static final int RAINBOW_AGATHION_ID = 16027;
	public static final int CASTLE_LORD_AGATHION_ID = 16028;
	public static final int FORTRESS_AGATHION_ID = 16029;
	public static final int LITTLE_ANGEL_AGATHION_ID = 16031;
	public static final int LITTLE_DEVIL_AGATHION_ID = 16032;
	public static final int RUDOLPH_AGATHION_ID = 16033;

	public static final int BABY_PANDA_AGATHION_ID = 1505;
	public static final int BAMBOO_PANDA_AGATHION_ID = 1506;
	public static final int SEXY_PANDA_AGATHION_ID = 1507;

	public static final int CHARMING_CUPID_AGATHION_ID = 1508;
	public static final int NAUGHTY_CUPID_AGATHION_ID = 1509;

	public static final int MAJO_AGATHION_ID = 1501;
	public static final int GOLD_MAJO_AGATHION_ID = 1502;
	public static final int BLACK_MAJO_AGATHION_ID = 1503;

	public static final int PLAIPITAK_AGATHION_ID = 1504;

	public static final int WHITE_MANEKI_AGATHION_ID = 1510;
	public static final int BLACK_MANEKI_AGATHION_ID = 1511;
	public static final int BROWN_MANEKI_AGATHION_ID = 1512;

	public static final int BAT_DROVE_AGATHION_ID = 1513;

	public static final int PEGASUS_AGATHION_ID = 1514;

	public static final int YELLOW_ROBED_TOJIGONG_AGATHION_ID = 1515;
	public static final int BLUE_ROBED_TOJIGONG_AGATHION_ID = 1516;
	public static final int GREEN_ROBED_TOJIGONG_AGATHION_ID = 1517;

	public static final int BUGBEAR_AGATHION_ID = 1518;

	private long ownerStoreId = 0, targetStoreId = 0;
	private int _id;

	/** скилы агнишена */
	private GArray<L2Skill> _skills = new GArray<L2Skill>();
	/** скилы, добавляемые хозяину */
	private GArray<L2Skill> _addSkills = new GArray<L2Skill>();

	private Future<?> _actionTask;

	public L2AgathionInstance(final L2Player owner, final int id)
	{
		ownerStoreId = owner.getStoredId();
		_id = id;

		switch(id)
		{
			case BEASTLY_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(5413, 1)); // See a summoned Agathion perform tricks. Beast Farm.
				break;
			case RAINBOW_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(5414, 1)); // See a summoned Agathion perform tricks. Rainbow Clan Hall.
				break;
			case CASTLE_LORD_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(5415, 1)); // See a summoned Agathion perform tricks. Castle.
				break;
			case FORTRESS_AGATHION_ID:
				_addSkills.add(SkillTable.getInstance().getInfo(5458, 1)); // See a summoned Agathion perform tricks. Fortress.
				break;
			case LITTLE_ANGEL_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(5535, 1)); // Little Angel Agathion Cuteness Attack
				break;
			case LITTLE_DEVIL_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(5536, 1)); // Little Devil Agathion Cuteness Attack
				break;
			case RUDOLPH_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(5537, 1)); // Rudolph Agathion Cuteness Attack
				break;
			case BABY_PANDA_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23010, 1)); // Baby Panda Agathion Cute Trick
				break;
			case BAMBOO_PANDA_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23011, 1)); // Bamboo Panda Agathion Cute Trick
				break;
			case SEXY_PANDA_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23012, 1)); // Sexy Panda Agathion Cute Trick
				break;
			case CHARMING_CUPID_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23025, 1)); // Charming Cupid Agathion Cute Trick
				break;
			case NAUGHTY_CUPID_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23026, 1)); // Naughty Cupid Agathion Cute Trick
				break;
			case MAJO_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23000, 1)); // Majo Agathion Cute Trick
				break;
			case GOLD_MAJO_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23001, 1)); // Gold Majo Agathion Cute Trick
				break;
			case BLACK_MAJO_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23002, 1)); // Black Majo Agathion Cute Trick
				break;
			case PLAIPITAK_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23003, 1)); // Plaipitak Agathion Cute Trick
				break;
			case WHITE_MANEKI_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23027, 1)); // White Maneki Neko Agathion Cute Trick I
				break;
			case BLACK_MANEKI_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23028, 1)); // Black Maneki Neko Agathion Cute Trick I
				break;
			case BROWN_MANEKI_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23029, 1)); // Brown Maneki Neko Agathion Cute Trick I
				break;
			case BAT_DROVE_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23033, 1)); // One-Eyed Bat Drove Agathion Cute Trick
				break;
			case PEGASUS_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23036, 1)); // Pegasus Agathion Cute Trick
				break;
			case YELLOW_ROBED_TOJIGONG_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23039, 1)); // Yellow-Robed Tojigong Agathion Cute Trick
				break;
			case BLUE_ROBED_TOJIGONG_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23040, 1)); // Blue-Robed Tojigong Agathion Cute Trick
				break;
			case GREEN_ROBED_TOJIGONG_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23041, 1)); // Green-Robed Tojigong Agathion Cute Trick
				break;
			case BUGBEAR_AGATHION_ID:
				_skills.add(SkillTable.getInstance().getInfo(23045, 1)); // Bugbear Agathion Cute Trick
				break;
			default:
				return;
		}

		// Выдаем скилы хозяину
		for(L2Skill s : _addSkills)
			owner.addSkill(s);

		owner.sendPacket(new SkillList(owner));
	}

	public void doAction(L2Character target)
	{
		L2Player owner = getPlayer();
		if(targetStoreId == target.getStoredId() || owner == null || owner == target)
			return;
		stopAction();
		targetStoreId = target.getStoredId();
		switch(_id)
		{
			case BEASTLY_AGATHION_ID:
			case RAINBOW_AGATHION_ID:
			case CASTLE_LORD_AGATHION_ID:
			case LITTLE_ANGEL_AGATHION_ID:
			case LITTLE_DEVIL_AGATHION_ID:
			case RUDOLPH_AGATHION_ID:
				_actionTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new Action(), 0, 10000, false);
				break;
			case FORTRESS_AGATHION_ID:
				_actionTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new Action(), 0, 8000, false);
				break;
		}
	}

	public int getId()
	{
		return _id;
	}

	public void stopAction()
	{
		targetStoreId = 0;
		if(_actionTask != null)
		{
			_actionTask.cancel(false);
			_actionTask = null;
		}
	}

	public void deleteMe()
	{
		stopAction();

		L2Player owner = getPlayer();
		if(owner != null)
		{
			// Забираем скилы у хозяина
			for(L2Skill s : _addSkills)
				owner.removeSkill(s);
			owner.sendPacket(new SkillList(owner));
		}

		ownerStoreId = 0;
		_skills = null;
		_addSkills = null;
	}

	private class Action implements Runnable
	{
		public void run()
		{
			L2Player owner = getPlayer();
			if(owner == null)
			{
				deleteMe();
				return;
			}

			L2Character target = getTarget();

			L2Skill skill = _skills.get(Rnd.get(_skills.size()));
			if(owner.isDead() || target == null || target.isDead() || !owner.isInRangeZ(target, skill.getCastRange()))
			{
				stopAction();
				if(owner.isDead())
				{
					owner.setAgathion(0);
					owner.broadcastUserInfo(true);
				}
				return;
			}
			try
			{
				if(Rnd.chance(50) && skill.checkCondition(owner, target, false, false, true))
				{
					owner.altUseSkill(skill, target);
					owner.broadcastPacket(new MagicSkillUse(owner, target, skill.getId(), 1, 0, 0));
				}
			}
			catch(final Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
			finally
			{
				_actionTask = ThreadPoolManager.getInstance().scheduleAi(new Action(), (long) (6. * owner.calculateAttackDelay()), true);
			}
		}
	}

	public L2Player getPlayer()
	{
		return L2ObjectsStorage.getAsPlayer(ownerStoreId);
	}

	public L2Player getTarget()
	{
		return L2ObjectsStorage.getAsPlayer(targetStoreId);
	}
}