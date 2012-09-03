package l2rt.gameserver.xml.loader;

import l2rt.config.ConfigSystem;
import l2rt.gameserver.templates.L2Armor;
import l2rt.gameserver.templates.L2Armor.ArmorType;
import l2rt.gameserver.templates.L2Item;
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
 * @date : 28.12.10    8:25
 */
public final class XmlArmorLoader {
    private static XmlArmorLoader ourInstance = new XmlArmorLoader();
    private ConcurrentHashMap<Integer, L2Armor> armors = new ConcurrentHashMap<Integer, L2Armor>();
    private Logger log = Logger.getLogger(XmlArmorLoader.class.getName());

    public static XmlArmorLoader getInstance() {
        return ourInstance;
    }

    private XmlArmorLoader() {
        armors.clear();
        load();
    }

    private void load() {
        File file = new File(ConfigSystem.get("DatapackRoot") + "/data/stats/items/armor");
        int rare = 0;
        int pvp = 0;
        try {
            for (File f : file.listFiles()) {
                if (f.getName().endsWith(".xml")) {
                    Document doc = XmlUtils.readFile(f);
                    Element list = doc.getRootElement();
                    for (Element armor : list.elements("armor")) {
                        StatsSet set = new StatsSet();
                        ArmorType type = ArmorType.valueOf(armor.attributeValue("type"));
                        int id = XmlUtils.getIntValue(armor, "id", 0);
                        if(id == 0)
                            continue;
                        set.set("class", "EQUIPMENT");
                        set.set("item_id", id);
                        set.set("name", armor.attributeValue("name"));
                        for(Iterator<Element> i = armor.elementIterator("set"); i.hasNext();) {
                            Element e = i.next();
                            if(e.attributeValue("name").equals("isRare") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                                rare++;
                            if(e.attributeValue("name").equals("isPvP") && e.attributeValue("val").equalsIgnoreCase("true")) // просто счетчик, для интереса
                                pvp++;
                            set.set(e.attributeValue("name"), e.attributeValue("val"));
                        }
                        L2Item.Bodypart bodypart = set.getEnum("bodypart", L2Item.Bodypart.class, L2Item.Bodypart.NONE);
                        if (bodypart == L2Item.Bodypart.NECK || (bodypart.getVal() & L2Item.Bodypart.LEAR.getVal()) != 0 || (bodypart.getVal() & L2Item.Bodypart.LFINGER.getVal()) != 0) {
                            set.set("type1", L2Item.TYPE1_WEAPON_RING_EARRING_NECKLACE);
                            set.set("type2", L2Item.TYPE2_ACCESSORY);
                        } else if (bodypart == L2Item.Bodypart.HAIR|| bodypart == L2Item.Bodypart.FACE|| bodypart == L2Item.Bodypart.HAIRALL) {
                            set.set("type1", L2Item.TYPE1_OTHER);
                            set.set("type2", L2Item.TYPE2_OTHER);
                        } else {
                            set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
                            set.set("type2", L2Item.TYPE2_SHIELD_ARMOR);
                        }
                        if(type == ArmorType.PET) {
                            set.set("type1", L2Item.TYPE1_SHIELD_ARMOR);
                            if(bodypart == L2Item.Bodypart.WOLF) {
                                set.set("type2", L2Item.TYPE2_PET_WOLF);
                                set.set("bodypart", "CHEST");
                            } else if(bodypart == L2Item.Bodypart.GWOLF) {
                                set.set("type2", L2Item.TYPE2_PET_GWOLF);
                                set.set("bodypart", "CHEST");
                            } else if(bodypart == L2Item.Bodypart.HATCHLING) {
                                set.set("type2", L2Item.TYPE2_PET_HATCHLING);
                                set.set("bodypart", "CHEST");
                            } else if(bodypart == L2Item.Bodypart.PENDANT) {
                                set.set("type2", L2Item.TYPE2_PENDANT);
                                set.set("bodypart", "NECK");
                            } else if(bodypart == L2Item.Bodypart.BABY) {
                                set.set("type2", L2Item.TYPE2_PET_BABY);
                                set.set("bodypart", "CHEST");
                            } else {
                                set.set("type2", L2Item.TYPE2_PET_STRIDER);
                                set.set("bodypart", "CHEST");
                            }
                        }

                        Element skills = armor.element("skills");
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

                        Element enchant4_skill = armor.element("enchant4_skill");
                        if(enchant4_skill != null) {
                            set.set("enchant4_skill_id", enchant4_skill.attributeValue("id"));
                            set.set("enchant4_skill_lvl", enchant4_skill.attributeValue("lvl"));
                        }
                        L2Armor arm = new L2Armor(type, set);
                        armors.put(id, arm);
                    }
                }
            }


            log.info("XmlArmorLoader: Loaded " + armors.size() + " Armors");
            log.info("XmlArmorLoader: Loaded " + rare + " Rare Armors");
            log.info("XmlArmorLoader: Loaded " + pvp + " PvP Armors");
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<Integer, L2Armor> getArmors() {
        return armors;
    }
}
