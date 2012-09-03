package l2rt.gameserver.xml.loader;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.tables.SkillTable;
import l2rt.gameserver.templates.L2Item;
import l2rt.gameserver.templates.L2Weapon;
import l2rt.gameserver.templates.L2Weapon.WeaponType;
import l2rt.gameserver.templates.StatsSet;
import l2rt.gameserver.xml.XmlUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.io.File;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * @author : Ragnarok
 * @date : 09.01.11    14:12
 */
public class XmlWeaponLoader {
    private static XmlWeaponLoader ourInstance = new XmlWeaponLoader();
    private ConcurrentHashMap<Integer, L2Weapon> weapons = new ConcurrentHashMap<Integer, L2Weapon>();
    private Logger log = Logger.getLogger(XmlWeaponLoader.class.getName());

    public static XmlWeaponLoader getInstance() {
        return ourInstance;
    }

    private XmlWeaponLoader() {
        load();
    }

    private void load() {
        weapons.clear();
        File file = new File(ConfigSystem.get("DatapackRoot") + "/data/stats/items/weapon");
        int rare = 0;
        int pvp = 0;
        int sa = 0;
		String fna = "";
        try {
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".xml")) {
					fna = f.getName();
                    Document doc = XmlUtils.readFile(f);
                    Element list = doc.getRootElement();
                    for(Element weapon : list.elements("weapon")){
                        StatsSet set = new StatsSet();
                        WeaponType type = WeaponType.valueOf(weapon.attributeValue("type"));
                        int id = XmlUtils.getIntValue(weapon, "id", 0);
                        if(id == 0)
                            continue;
                        set.set("class", "EQUIPMENT");
                        set.set("item_id", id);
                        set.set("name", weapon.attributeValue("name"));
                        for(Iterator<Element> i = weapon.elementIterator("set"); i.hasNext();) {
                            Element e = i.next();
                            if(e.attributeValue("name").equals("isRare") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                                rare++;
                            if(e.attributeValue("name").equals("isPvP") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                                pvp++;
                            if(e.attributeValue("name").equals("isSa") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                                sa++;
                            set.set(e.attributeValue("name"), e.attributeValue("val"));
                        }
                        if(type == WeaponType.NONE) { // TODO: избавиться от mask() в типах, и скиллах. Сделать более понятным для конечного пользователя.
                            set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
                            set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
                        } else {
                            set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
                            set.set("type2", L2Item.TYPE2_WEAPON);
                        }
                        L2Item.Bodypart bodypart = L2Item.Bodypart.NONE;
                        try{
                            bodypart = set.getEnum("bodypart", L2Item.Bodypart.class, L2Item.Bodypart.NONE);
                        } catch (IllegalArgumentException eee) {
                            log.warning(set.getString("item_id") + " " + set.getString("name") + " " + set.getString("bodypart", "!(!I"));
                        }
                        if(type == WeaponType.PET) {
                            set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
                            if(bodypart == L2Item.Bodypart.WOLF)
                                set.set("type2", L2Item.TYPE2_PET_WOLF);
                            else if(bodypart == L2Item.Bodypart.GWOLF)
                                set.set("type2", L2Item.TYPE2_PET_GWOLF);
                            else if(bodypart == L2Item.Bodypart.HATCHLING)
                                set.set("type2", L2Item.TYPE2_PET_HATCHLING);
                            else
                                set.set("type2", L2Item.TYPE2_PET_STRIDER);
                            set.set("bodypart", "RHAND");
                        }


                        Element skills = weapon.element("skills");
                        if(skills != null) {
                            String sk = "";
                            String lvl = "";
                            for(Iterator<Element> i = skills.elementIterator("skill"); i.hasNext();) {
                                Element skill = i.next();
                                sk += skill.attributeValue("id") + ";";
                                lvl += skill.attributeValue("lvl") + ";";
                            }
                            set.set("skill_id", sk);
                            set.set("skill_level", lvl);
                        }

                        Element enchant4_skill = weapon.element("enchant4_skill");
                        if(enchant4_skill != null) {
                            set.set("enchant4_skill_id", enchant4_skill.attributeValue("id"));
                            set.set("enchant4_skill_lvl", enchant4_skill.attributeValue("lvl"));
                        }

                        L2Weapon weap = new L2Weapon(type, set);
                        if(weap.isPvP()) {
                            switch(type) {
                                case BOW:
                                case CROSSBOW:
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3655, 1)); // PvP Weapon - Rapid Fire
                                    break;
                                case BIGSWORD:
                                case BIGBLUNT:
                                case ANCIENTSWORD:
                                    if(weap.isMageSA())
                                        weap.attachSkill(SkillTable.getInstance().getInfo(3654, 1)); // PvP Weapon - Casting
                                    else
                                        weap.attachSkill(SkillTable.getInstance().getInfo(3653, 1)); // PvP Weapon - Attack Chance
                                    break;
                                case SWORD:
                                case BLUNT:
                                case RAPIER:
                                    if(weap.isMageSA())
                                        weap.attachSkill(SkillTable.getInstance().getInfo(3654, 1)); // PvP Weapon - Casting
                                    else
                                        weap.attachSkill(SkillTable.getInstance().getInfo(3650, 1)); // PvP Weapon - CP Drain
                                    break;
                                case FIST:
                                case DUALFIST:
                                case DAGGER:
                                case DUALDAGGER:
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3651, 1)); // PvP Weapon - Cancel
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3652, 1)); // PvP Weapon - Ignore Shield Defense
                                    break;
                                case POLE:
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3653, 1)); // PvP Weapon - Attack Chance
                                    break;
                                case DUAL:
                                    weap.attachSkill(SkillTable.getInstance().getInfo(3656, 1)); // PvP Weapon - Decrease Range
                                    break;
                            }
                        }
                        weapons.put(id, weap);
                    }
                }
            }
            log.info("XmlWeaponLoader: Loaded " + weapons.size() + " Weapons");
            log.info("XmlWeaponLoader: Loaded " + rare + " Rare Weapons");
            log.info("XmlWeaponLoader: Loaded " + pvp + " PvP Weapons");
            log.info("XmlWeaponLoader: Loaded " + sa + " SA Weapons");
        } catch (DocumentException e) {
			log.info("Error in"+fna);
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<Integer, L2Weapon> getWeapons() {
        return weapons;
    }
}
