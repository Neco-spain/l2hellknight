package l2p.gameserver.data.xml.holder;

import l2p.commons.data.xml.AbstractHolder;
import l2p.gameserver.model.Skill;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : Ragnarok
 * @date : 02.05.12  16:05
 */
public class SkillsHolder extends AbstractHolder {
    private static final SkillsHolder _instance = new SkillsHolder();

    private static final Map<Integer, Skill> skillList = new HashMap<Integer, Skill>();

    @Override
    public int size() {
        return skillList.size();
    }

    @Override
    public void clear() {
        skillList.clear();
    }

    public static SkillsHolder getInstance() {
        return _instance;
    }

    public void addSkill(Skill skill) {
        int hashCode = getSkillHashCode(skill);
        skillList.put(hashCode, skill);
    }

    public Map<Integer, Skill> getSkillList() {
        return Collections.unmodifiableMap(skillList);
    }

    private int getSkillHashCode(Skill skill) {
        return getSkillHashCode(skill.getId(), skill.getLevel());
    }

    private int getSkillHashCode(int skillId, int skillLevel) {
        return skillId * 1000 + skillLevel;
    }
}
