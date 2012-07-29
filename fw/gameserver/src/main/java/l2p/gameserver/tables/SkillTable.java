package l2p.gameserver.tables;

import gnu.trove.map.hash.TIntIntHashMap;
import l2p.gameserver.data.xml.holder.SkillsHolder;
import l2p.gameserver.model.Skill;
import l2p.gameserver.skills.SkillsEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SkillTable {
    private static final Logger _log = LoggerFactory.getLogger(SkillTable.class);

    private static final SkillTable _instance = new SkillTable();

    private Map<Integer, Skill> _skills;
    public Map<Integer, Integer> identifySkills = new HashMap<Integer, Integer>();

    private TIntIntHashMap _maxLevelsTable;
    private TIntIntHashMap _baseLevelsTable;

    public static final SkillTable getInstance() {
        return _instance;
    }

    @Deprecated
    public void load() {
        _skills = SkillsEngine.getInstance().loadAllSkills();
        Map<Integer, Skill> newSkills = SkillsHolder.getInstance().getSkillList();
        List<Integer> hashes = new ArrayList<Integer>();
        hashes.addAll(SkillsHolder.getInstance().getSkillList().keySet());
        Collections.sort(hashes);
        for (int hash : hashes) {
            if (_skills.containsKey(hash)) {
                _log.warn("Duplicate skills: id: " + (hash - (hash % 1000)) / 1000 + " lvl: " + hash % 1000);
            } else {
                _skills.put(hash, newSkills.get(hash));
            }
        }
        //_skills.putAll(SkillsHolder.getInstance().getSkillList());
        makeLevelsTable();
    }

    public void reload() {
        load();
    }

    public Skill getInfo(int skillId, int level) {
        return _skills.get(getSkillHashCode(skillId, level));
    }

    public int getMaxLevel(int skillId) {
        return _maxLevelsTable.get(skillId);
    }

    public int getBaseLevel(int skillId) {
        return _baseLevelsTable.get(skillId);
    }

    public static int getSkillHashCode(Skill skill) {
        return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel());
    }

    public static int getSkillHashCode(int skillId, int skillLevel) {
        return skillId * 1000 + skillLevel;
    }

    private void makeLevelsTable() {
        _maxLevelsTable = new TIntIntHashMap();
        _baseLevelsTable = new TIntIntHashMap();
        for (Skill s : _skills.values()) {
            int skillId = s.getId();
            int level = s.getLevel();
            int maxLevel = _maxLevelsTable.get(skillId);
            if (level > maxLevel)
                _maxLevelsTable.put(skillId, level);
            if (_baseLevelsTable.get(skillId) == 0)
                _baseLevelsTable.put(skillId, s.getBaseLevel());
        }
    }
}