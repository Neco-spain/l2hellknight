package l2rt.gameserver.network.serverpackets;

import l2rt.Config;
import l2rt.extensions.scripts.Functions;
import l2rt.gameserver.model.L2Character;
import l2rt.gameserver.model.L2Skill;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.tables.SkillTable;

public class MagicSkillUse extends L2GameServerPacket
{
  private int _targetId;
  private int _skillId;
  private int _skillLevel;
  private int _hitTime;
  private long _reuseDelay;
  private int _chaId;
  private int _x;
  private int _y;
  private int _z;
  private int _tx;
  private int _ty;
  private int _tz;

  public int getSkillId()
  {
    return _skillId;
  }

  public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, long reuseDelay)
  {
    _chaId = cha.getObjectId();
    _targetId = target.getObjectId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _hitTime = hitTime;
    _reuseDelay = reuseDelay;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _tx = target.getX();
    _ty = target.getY();
    _tz = target.getZ();
    if ((!Config.SAY_CASTING_SKILL_NAME) || (!cha.isNpc()))
      return;
    L2Skill sk = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
    Functions.npcSay((L2NpcInstance)cha, "Casting " + sk.getName() + "[" + _skillId + "." + _skillLevel + "]");
  }

  public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, long reuseDelay)
  {
    _chaId = cha.getObjectId();
    _targetId = cha.getTargetId();
    _skillId = skillId;
    _skillLevel = skillLevel;
    _hitTime = hitTime;
    _reuseDelay = reuseDelay;
    _x = cha.getX();
    _y = cha.getY();
    _z = cha.getZ();
    _tx = cha.getX();
    _ty = cha.getY();
    _tz = cha.getZ();
    if ((!Config.SAY_CASTING_SKILL_NAME) || (!cha.isNpc()))
      return;
    L2Skill sk = SkillTable.getInstance().getInfo(_skillId, _skillLevel);
    Functions.npcSay((L2NpcInstance)cha, "Casting " + sk.getName() + "[" + _skillId + "." + _skillLevel + "]");
  }

  protected final void writeImpl()
  {
    writeC(72);
    writeD(0); //тип SetupGauge, ID смотрим в пакете SetupGauge тип SetupGauge(0 первый скил, 1 второй скил, 2 пусто, 3 зеленая полоска, 4 рыжая полоска)
    writeD(_chaId);
    writeD(_targetId);
    writeC(0);

    writeD(_skillId);
    writeD(_skillLevel);
    writeD(_hitTime);
    writeD(-1); // TODO: [K1mel] Группа реюза (Используется только итемами)
	
	writeD((int)_reuseDelay);
    writeD(_x);
    writeD(_y);
    writeD(_z);
	writeD(0x00); // unknown size TODO
	/*for (int i = 0; i < size; i++)
	{
		writeH(0); //???
	}*/

    writeD(_tx);
    writeD(_ty);
    writeD(_tz);
  }
}