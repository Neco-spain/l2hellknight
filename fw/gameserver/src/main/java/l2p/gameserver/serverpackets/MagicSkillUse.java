package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

public class MagicSkillUse extends L2GameServerPacket {
    private int _targetId;
    private int _skillId;
    private int _skillLevel;
    private int _hitTime;
    private int _reuseDelay;
    private int _chaId, _x, _y, _z, _tx, _ty, _tz;
    private int setupType;
    private int reuseGroupId;

    public MagicSkillUse(Creature cha, Creature target, int setupType, int skillId, int skillLevel, int hitTime, long reuseDelay, int reuseGroupId) {
        _chaId = cha.getObjectId();
        _targetId = target.getObjectId();
        _skillId = skillId;
        _skillLevel = skillLevel;
        _hitTime = hitTime;
        _reuseDelay = (int) reuseDelay;
        _x = cha.getX();
        _y = cha.getY();
        _z = cha.getZ();
        _tx = target.getX();
        _ty = target.getY();
        _tz = target.getZ();

        this.setupType = setupType;
        this.reuseGroupId = reuseGroupId;
    }

    @Override
    protected final void writeImpl() {
        writeC(0x48);

        writeD(setupType);
        writeD(_chaId);
        writeD(_targetId);

        writeC(0x00);

        writeD(_skillId);
        writeD(_skillLevel);
        writeD(_hitTime);

        writeD(reuseGroupId);// Reuse Group

        writeD(_reuseDelay);
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(0x00); // unknown
        writeD(_tx);
        writeD(_ty);
        writeD(_tz);
    }
}