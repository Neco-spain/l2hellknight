package l2p.gameserver.data.xml.parser;

import gnu.trove.TIntObjectHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import l2p.commons.data.xml.AbstractDirParser;
import l2p.gameserver.Config;
import l2p.gameserver.data.xml.holder.SkillAcquireHolder;
import l2p.gameserver.model.SkillLearn;

/**
 * @author: VISTALL
 * @date:  20:55/30.11.2010
 */
public final class SkillAcquireParser extends AbstractDirParser<SkillAcquireHolder>
{
	private static final SkillAcquireParser _instance = new SkillAcquireParser();

	public static SkillAcquireParser getInstance()
	{
		return _instance;
	}

	protected SkillAcquireParser()
	{
		super(SkillAcquireHolder.getInstance());
	}

	@Override
	public File getXMLDir()
	{
		return new File(Config.DATAPACK_ROOT, "data/skill_tree/");
	}

	@Override
	public boolean isIgnored(File b)
	{
		return false;
	}

	@Override
	public String getDTDFileName()
	{
		return "tree.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator iterator = rootElement.elementIterator("certification_skill_tree"); iterator.hasNext();)
			getHolder().addAllCertificationLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator iterator = rootElement.elementIterator("sub_unit_skill_tree"); iterator.hasNext();)
			getHolder().addAllSubUnitLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator iterator = rootElement.elementIterator("pledge_skill_tree"); iterator.hasNext();)
			getHolder().addAllPledgeLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator iterator = rootElement.elementIterator("collection_skill_tree"); iterator.hasNext();)
			getHolder().addAllCollectionLearns(parseSkillLearn((Element) iterator.next()));

		for(Iterator iterator = rootElement.elementIterator("fishing_skill_tree"); iterator.hasNext();)
		{
			Element nxt = (Element) iterator.next();
			for(Iterator classIterator = nxt.elementIterator("race"); classIterator.hasNext();)
			{
				Element classElement = (Element) classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllFishingLearns(race, learns);
			}
		}

		for(Iterator iterator = rootElement.elementIterator("transfer_skill_tree"); iterator.hasNext();)
		{
			Element nxt = (Element) iterator.next();
			for(Iterator classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = (Element) classIterator.next();
				int classId = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllTransferLearns(classId, learns);
			}
		}

		for(Iterator iterator = rootElement.elementIterator("normal_skill_tree"); iterator.hasNext();)
		{
			TIntObjectHashMap<List<SkillLearn>> map = new TIntObjectHashMap<List<SkillLearn>>();
			Element nxt = (Element) iterator.next();
			for(Iterator classIterator = nxt.elementIterator("class"); classIterator.hasNext();)
			{
				Element classElement = (Element) classIterator.next();
				int classId = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);

				map.put(classId, learns);
			}

			getHolder().addAllNormalSkillLearns(map);
		}

		for(Iterator iterator = rootElement.elementIterator("transformation_skill_tree"); iterator.hasNext();)
		{
			Element nxt = (Element) iterator.next();
			for(Iterator classIterator = nxt.elementIterator("race"); classIterator.hasNext();)
			{
				Element classElement = (Element) classIterator.next();
				int race = Integer.parseInt(classElement.attributeValue("id"));
				List<SkillLearn> learns = parseSkillLearn(classElement);
				getHolder().addAllTransformationLearns(race, learns);
			}
		}
	}

	private List<SkillLearn> parseSkillLearn(Element tree)
	{
		List<SkillLearn> skillLearns = new ArrayList<SkillLearn>();
		for(Iterator iterator = tree.elementIterator("skill"); iterator.hasNext();)
		{
			Element element = (Element) iterator.next();

			int id = Integer.parseInt(element.attributeValue("id"));
			int level = Integer.parseInt(element.attributeValue("level"));
			int cost = element.attributeValue("cost") == null ? 0 : Integer.parseInt(element.attributeValue("cost"));
			int min_level = Integer.parseInt(element.attributeValue("min_level"));
			int item_id = element.attributeValue("item_id") == null ? 0 : Integer.parseInt(element.attributeValue("item_id"));
			long item_count = element.attributeValue("item_count") == null ? 1 : Long.parseLong(element.attributeValue("item_count"));
			boolean clicked = element.attributeValue("clicked") != null && Boolean.parseBoolean(element.attributeValue("clicked"));

			skillLearns.add(new SkillLearn(id, level, min_level, cost, item_id, item_count, clicked));
		}

		return skillLearns;
	}
}
