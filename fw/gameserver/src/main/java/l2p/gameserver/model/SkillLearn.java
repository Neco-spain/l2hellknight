package l2p.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 */
public final class SkillLearn implements Comparable<SkillLearn> {
    private final int _id;
    private final int _level;
    private final int _minLevel;
    private final int _cost;
    private final Map<Integer, Long> required_items;
    private final List<Integer> delete_skills;

    public SkillLearn(int id, int lvl, int minLvl, int cost, boolean clicked, Map<Integer, Long> required_items, List<Integer> delete_skills) {
        _id = id;
        _level = lvl;
        _minLevel = minLvl;
        _cost = cost;

        this.required_items = required_items;
        this.delete_skills = delete_skills;
    }

    public int getId() {
        return _id;
    }

    public int getLevel() {
        return _level;
    }

    public int getMinLevel() {
        return _minLevel;
    }

    public int getCost() {
        return _cost;
    }

    @Override
    public int compareTo(SkillLearn o) {
        if (getId() == o.getId())
            return getLevel() - o.getLevel();
        else
            return getId() - o.getId();
    }

    public Map<Integer, Long> getRequiredItems() {
        return Collections.unmodifiableMap(required_items);
    }

    public List<Integer> getDeleteSkills() {
        return Collections.unmodifiableList(delete_skills);
    }
    
    public List<Skill> getRemovedSkillsForPlayer(Player player) {
        List<Skill> skills = new ArrayList<Skill>();
        for(int skill_id : getDeleteSkills()) {
            if(player.getKnownSkill(skill_id) != null) {
                skills.add(player.getKnownSkill(skill_id));
            }
        }
        return skills;
    }
}