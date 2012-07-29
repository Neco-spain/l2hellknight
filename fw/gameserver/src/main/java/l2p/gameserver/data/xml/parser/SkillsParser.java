package l2p.gameserver.data.xml.parser;

import l2p.commons.data.xml.AbstractDirParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SkillsHolder;
import l2p.gameserver.model.Skill;
import l2p.gameserver.skills.effects.EffectTemplate;
import l2p.gameserver.templates.StatsSet;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;

/**
 * @author : Ragnarok
 * @date : 02.05.12  16:05
 */
public class SkillsParser extends AbstractDirParser<SkillsHolder> {
    private static SkillsParser ourInstance = new SkillsParser();

    protected SkillsParser() {
        super(SkillsHolder.getInstance());
    }

    public static SkillsParser getInstance() {
        return ourInstance;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/skills/");
    }

    @Override
    public boolean isIgnored(File f) {
        return false;
    }

    @Override
    public String getDTDFileName() {
        return "skills.dtd";
    }

    @Override
    protected void readData(Element rootElement) throws Exception {
        for (Iterator iterator = rootElement.elementIterator("skill"); iterator.hasNext(); ) {
            Element skillElement = (Element) iterator.next();
            int skillId = Integer.parseInt(skillElement.attributeValue("id"));
            String skillName = skillElement.attributeValue("name");
            int level = Integer.parseInt(skillElement.attributeValue("level"));

            StatsSet sets = new StatsSet();
            sets.put("skill_id", skillId);
            sets.put("level", level);
            sets.put("base_level", level);
            sets.put("name", skillName);

            for (Iterator setIterator = skillElement.elementIterator("set"); setIterator.hasNext(); ) {
                Element set = (Element) setIterator.next();
                String name = set.attributeValue("name");
                String val = set.attributeValue("val");
                sets.put(name, val);
            }

            Skill.SkillType skillEnum = sets.getEnum("skillType", Skill.SkillType.class);
            Skill skill = skillEnum.makeSkill(sets);
            Element effectElement = skillElement.element("effect");
            if (effectElement != null) {
                EffectTemplate effectTemplate = parseEffectTemplate(effectElement);
                skill.attach(effectTemplate);
            }
            getHolder().addSkill(skill);
        }
    }

    private EffectTemplate parseEffectTemplate(Element effectElement) {
        StatsSet sets = new StatsSet();

        sets.set("name", effectElement.attributeValue("name"));
        if (effectElement.attributeValue("applyOnCaster") != null)
            sets.set("applyOnCaster", effectElement.attributeValue("applyOnCaster"));
        if (effectElement.attributeValue("time") != null)
            sets.set("time", effectElement.attributeValue("time"));

        for (Iterator setIterator = effectElement.elementIterator("def"); setIterator.hasNext(); ) {
            Element set = (Element) setIterator.next();
            String name = set.attributeValue("name");
            String val = set.attributeValue("val");
            sets.put(name, val);
        }

        EffectTemplate effectTemplate = new EffectTemplate(sets);
        return effectTemplate;
    }
}
