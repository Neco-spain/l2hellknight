package l2p.gameserver.serverpackets;

import l2p.gameserver.model.Creature;

import java.util.ArrayList;
import java.util.List;

public class ExAbnormalStatusUpdateFromTarget extends L2GameServerPacket {

    private List<Effect> _effects;
    private int char_obj_id = 0;

    public ExAbnormalStatusUpdateFromTarget(Creature target) {
        char_obj_id = target.getObjectId();
        _effects = new ArrayList<Effect>();
    }

    @Override
    protected final void writeImpl() {
        writeEx(0xE5);
        writeD(char_obj_id);
        writeH(_effects.size());
        for (Effect temp : _effects) {
            writeD(temp.skillId);
            writeH(temp.level);
            writeD(temp.alterData);// Unknown (GOD)
            writeD(temp.duration);
            writeD(temp.effectorId);// Effector ID
        }
    }

    public void addSpelledEffect(int displayId, int displayLevel, int alterData, int duration, int effectorId) {
        _effects.add(new Effect(displayId, displayLevel, alterData, duration, effectorId));
    }

    class Effect {
        public int skillId;
        public int level;
        public int alterData;
        public int duration;
        public int effectorId;

        public Effect(int skillId, int level, int alterData, int duration, int effectorId) {
            this.skillId = skillId;
            this.level = level;
            this.alterData = alterData;
            this.duration = duration;
            this.effectorId = effectorId;
        }
    }
}