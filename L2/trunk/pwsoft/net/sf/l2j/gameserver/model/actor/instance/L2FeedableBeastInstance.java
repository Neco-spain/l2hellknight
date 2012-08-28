package net.sf.l2j.gameserver.model.actor.instance;

import java.util.logging.Logger;
import javolution.util.FastMap;
import javolution.util.FastTable;
import net.sf.l2j.gameserver.ai.CtrlEvent;
import net.sf.l2j.gameserver.ai.L2CharacterAI;
import net.sf.l2j.gameserver.datatables.NpcTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Character;
import net.sf.l2j.gameserver.model.L2Spawn;
import net.sf.l2j.gameserver.model.base.ClassId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.templates.L2NpcTemplate;
import net.sf.l2j.util.Rnd;

public class L2FeedableBeastInstance extends L2MonsterInstance
{
  public static FastMap<Integer, growthInfo> growthCapableMobs = new FastMap().shared("L2FeedableBeastInstance.growthCapableMobs");
  public static FastMap<Integer, Integer> madCowPolymorph = new FastMap().shared("L2FeedableBeastInstance.madCowPolymorph");
  static FastTable<Integer> tamedBeasts = new FastTable();
  static FastTable<Integer> feedableBeasts = new FastTable();
  public static FastMap<Integer, Integer> feedInfo = new FastMap();
  private static int GOLDEN_SPICE = 0;
  private static int CRYSTAL_SPICE = 1;
  private static int SKILL_GOLDEN_SPICE = 2188;
  private static int SKILL_CRYSTAL_SPICE = 2189;
  private static String[][] text = { { "What did you just do to me?", "You want to tame me, huh?", "Do not give me this. Perhaps you will be in danger.", "Bah bah. What is this unpalatable thing?", "My belly has been complaining. This hit the spot.", "What is this? Can I eat it?", "You don't need to worry about me.", "Delicious food, thanks.", "I am starting to like you!", "Gulp" }, { "I do not think you have given up on the idea of taming me.", "That is just food to me.  Perhaps I can eat your hand too.", "Will eating this make me fat? Ha ha", "Why do you always feed me?", "Do not trust me. I may betray you" }, { "Destroy", "Look what you have done!", "Strange feeling...! Evil intentions grow in my heart...!", "It is happenning!", "This is sad...Good is sad...!" } };

  public L2FeedableBeastInstance(int objectId, L2NpcTemplate template)
  {
    super(objectId, template);

    growthCapableMobs.put(Integer.valueOf(21451), new growthInfo(0, new int[][][] { { { 21452, 21453, 21454, 21455 } }, { { 21456, 21457, 21458, 21459 } } }, 100));

    growthCapableMobs.put(Integer.valueOf(21452), new growthInfo(1, new int[][][] { { { 21460, 21462 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21453), new growthInfo(1, new int[][][] { { { 21461, 21463 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21454), new growthInfo(1, new int[][][] { { { 21460, 21462 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21455), new growthInfo(1, new int[][][] { { { 21461, 21463 } }, new int[0][] }, 40));

    growthCapableMobs.put(Integer.valueOf(21456), new growthInfo(1, new int[][][] { new int[0][], { { 21464, 21466 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21457), new growthInfo(1, new int[][][] { new int[0][], { { 21465, 21467 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21458), new growthInfo(1, new int[][][] { new int[0][], { { 21464, 21466 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21459), new growthInfo(1, new int[][][] { new int[0][], { { 21465, 21467 } } }, 40));

    growthCapableMobs.put(Integer.valueOf(21460), new growthInfo(2, new int[][][] { { { 21468, 21824 }, { 16017, 16018 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21461), new growthInfo(2, new int[][][] { { { 21469, 21825 }, { 16017, 16018 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21462), new growthInfo(2, new int[][][] { { { 21468, 21824 }, { 16017, 16018 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21463), new growthInfo(2, new int[][][] { { { 21469, 21825 }, { 16017, 16018 } }, new int[0][] }, 25));

    growthCapableMobs.put(Integer.valueOf(21464), new growthInfo(2, new int[][][] { new int[0][], { { 21468, 21824 }, { 16017, 16018 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21465), new growthInfo(2, new int[][][] { new int[0][], { { 21469, 21825 }, { 16017, 16018 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21466), new growthInfo(2, new int[][][] { new int[0][], { { 21468, 21824 }, { 16017, 16018 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21467), new growthInfo(2, new int[][][] { new int[0][], { { 21469, 21825 }, { 16017, 16018 } } }, 25));

    growthCapableMobs.put(Integer.valueOf(21470), new growthInfo(0, new int[][][] { { { 21471, 21472, 21473, 21474 } }, { { 21475, 21476, 21477, 21478 } } }, 100));

    growthCapableMobs.put(Integer.valueOf(21471), new growthInfo(1, new int[][][] { { { 21479, 21481 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21472), new growthInfo(1, new int[][][] { { { 21481, 21482 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21473), new growthInfo(1, new int[][][] { { { 21479, 21481 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21474), new growthInfo(1, new int[][][] { { { 21480, 21482 } }, new int[0][] }, 40));

    growthCapableMobs.put(Integer.valueOf(21475), new growthInfo(1, new int[][][] { new int[0][], { { 21483, 21485 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21476), new growthInfo(1, new int[][][] { new int[0][], { { 21484, 21486 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21477), new growthInfo(1, new int[][][] { new int[0][], { { 21483, 21485 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21478), new growthInfo(1, new int[][][] { new int[0][], { { 21484, 21486 } } }, 40));

    growthCapableMobs.put(Integer.valueOf(21479), new growthInfo(2, new int[][][] { { { 21487, 21826 }, { 16013, 16014 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21480), new growthInfo(2, new int[][][] { { { 21488, 21827 }, { 16013, 16014 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21481), new growthInfo(2, new int[][][] { { { 21487, 21826 }, { 16013, 16014 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21482), new growthInfo(2, new int[][][] { { { 21488, 21827 }, { 16013, 16014 } }, new int[0][] }, 25));

    growthCapableMobs.put(Integer.valueOf(21483), new growthInfo(2, new int[][][] { new int[0][], { { 21487, 21826 }, { 16013, 16014 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21484), new growthInfo(2, new int[][][] { new int[0][], { { 21488, 21827 }, { 16013, 16014 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21485), new growthInfo(2, new int[][][] { new int[0][], { { 21487, 21826 }, { 16013, 16014 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21486), new growthInfo(2, new int[][][] { new int[0][], { { 21488, 21827 }, { 16013, 16014 } } }, 25));

    growthCapableMobs.put(Integer.valueOf(21489), new growthInfo(0, new int[][][] { { { 21490, 21491, 21492, 21493 } }, { { 21494, 21495, 21496, 21497 } } }, 100));

    growthCapableMobs.put(Integer.valueOf(21490), new growthInfo(1, new int[][][] { { { 21498, 21500 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21491), new growthInfo(1, new int[][][] { { { 21499, 21501 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21492), new growthInfo(1, new int[][][] { { { 21498, 21500 } }, new int[0][] }, 40));
    growthCapableMobs.put(Integer.valueOf(21493), new growthInfo(1, new int[][][] { { { 21499, 21501 } }, new int[0][] }, 40));

    growthCapableMobs.put(Integer.valueOf(21494), new growthInfo(1, new int[][][] { new int[0][], { { 21502, 21504 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21495), new growthInfo(1, new int[][][] { new int[0][], { { 21503, 21505 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21496), new growthInfo(1, new int[][][] { new int[0][], { { 21502, 21504 } } }, 40));
    growthCapableMobs.put(Integer.valueOf(21497), new growthInfo(1, new int[][][] { new int[0][], { { 21503, 21505 } } }, 40));

    growthCapableMobs.put(Integer.valueOf(21498), new growthInfo(2, new int[][][] { { { 21506, 21828 }, { 16015, 16016 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21499), new growthInfo(2, new int[][][] { { { 21507, 21829 }, { 16015, 16016 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21500), new growthInfo(2, new int[][][] { { { 21506, 21828 }, { 16015, 16016 } }, new int[0][] }, 25));
    growthCapableMobs.put(Integer.valueOf(21501), new growthInfo(2, new int[][][] { { { 21507, 21829 }, { 16015, 16016 } }, new int[0][] }, 25));

    growthCapableMobs.put(Integer.valueOf(21502), new growthInfo(2, new int[][][] { new int[0][], { { 21506, 21828 }, { 16015, 16016 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21503), new growthInfo(2, new int[][][] { new int[0][], { { 21507, 21829 }, { 16015, 16016 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21504), new growthInfo(2, new int[][][] { new int[0][], { { 21506, 21828 }, { 16015, 16016 } } }, 25));
    growthCapableMobs.put(Integer.valueOf(21505), new growthInfo(2, new int[][][] { new int[0][], { { 21507, 21829 }, { 16015, 16016 } } }, 25));

    madCowPolymorph.put(Integer.valueOf(21824), Integer.valueOf(21468));
    madCowPolymorph.put(Integer.valueOf(21825), Integer.valueOf(21469));
    madCowPolymorph.put(Integer.valueOf(21826), Integer.valueOf(21487));
    madCowPolymorph.put(Integer.valueOf(21827), Integer.valueOf(21488));
    madCowPolymorph.put(Integer.valueOf(21828), Integer.valueOf(21506));
    madCowPolymorph.put(Integer.valueOf(21829), Integer.valueOf(21507));
    Integer localInteger1;
    Integer localInteger2;
    for (Integer i = Integer.valueOf(16013); i.intValue() <= 16018; localInteger2 = i = Integer.valueOf(i.intValue() + 1)) {
      tamedBeasts.add(i);

      localInteger1 = i;
    }

    for (Integer i = Integer.valueOf(16013); i.intValue() <= 16019; localInteger2 = i = Integer.valueOf(i.intValue() + 1)) {
      feedableBeasts.add(i);

      localInteger1 = i;
    }

    for (Integer i = Integer.valueOf(21451); i.intValue() <= 21507; localInteger2 = i = Integer.valueOf(i.intValue() + 1)) {
      feedableBeasts.add(i);

      localInteger1 = i;
    }

    for (Integer i = Integer.valueOf(21824); i.intValue() <= 21829; localInteger2 = i = Integer.valueOf(i.intValue() + 1)) {
      feedableBeasts.add(i);

      localInteger1 = i;
    }
  }

  private void spawnNext(L2PcInstance player, int growthLevel, int food)
  {
    int npcId = getNpcId();
    int nextNpcId = 0;

    if (growthLevel == 2)
    {
      if (Rnd.chance(50)) {
        if (player.getClassId().isMage())
          nextNpcId = ((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food][1][1];
        else {
          nextNpcId = ((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food][1][0];
        }
      }
      else if (player.getClassId().isMage())
        nextNpcId = ((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food][0][1];
      else {
        nextNpcId = ((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food][0][0];
      }
    }
    else {
      nextNpcId = ((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food][0][Rnd.get(((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food][0].length)];
    }

    feedInfo.remove(Integer.valueOf(getObjectId()));

    if (((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).growth_level == 0)
      onDecay();
    else {
      deleteMe();
    }

    if ((!player.isPetSummoned()) && (tamedBeasts.contains(Integer.valueOf(nextNpcId)))) {
      L2TamedBeastInstance oldTrained = player.getTrainedBeast();
      if (oldTrained != null) {
        oldTrained.doDespawn();
      }

      L2NpcTemplate template = NpcTable.getInstance().getTemplate(nextNpcId);
      L2TamedBeastInstance nextNpc = new L2TamedBeastInstance(IdFactory.getInstance().getNextId(), template);
      nextNpc.setCurrentHp(nextNpc.getMaxHp());
      nextNpc.setCurrentMp(nextNpc.getMaxMp());
      nextNpc.setFoodType(food == 0 ? SKILL_GOLDEN_SPICE : SKILL_CRYSTAL_SPICE);
      nextNpc.setHome(getX(), getY(), getZ());
      nextNpc.setRunning();
      nextNpc.spawnMe(getX(), getY(), getZ());
      nextNpc.setOwner(player);
      nextNpc.setRunning();

      int objectId = nextNpc.getObjectId();

      String text = "";
      switch (Rnd.get(10)) {
      case 0:
        text = player.getName() + ", will you show me your hideaway?";
        break;
      case 1:
        text = player.getName() + ", whenever I look at spice, I think about you.";
        break;
      case 2:
        text = player.getName() + ", you do not need to return to the village.  I will give you strength.";
        break;
      case 3:
        text = "Thanks, " + player.getName() + ".  I hope I can help you";
        break;
      case 4:
        text = player.getName() + ", what can I do to help you?";
      }

      broadcastPacket(new CreatureSay(objectId, 0, nextNpc.getName(), text));
    }
    else
    {
      L2Spawn spawn = spawn(nextNpcId, getX(), getY(), getZ());
      if (spawn == null) {
        return;
      }

      L2MonsterInstance nextNpc = (L2MonsterInstance)spawn.getLastSpawn();

      feedInfo.put(Integer.valueOf(nextNpc.getObjectId()), Integer.valueOf(player.getObjectId()));
      broadcastPacket(new CreatureSay(nextNpc.getObjectId(), 0, nextNpc.getName(), text[growthLevel][Rnd.get(text[growthLevel].length)]));
      nextNpc.setRunning();
      nextNpc.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, Integer.valueOf(99999));
    }
  }

  public boolean doDie(L2Character killer)
  {
    if (!super.doDie(killer)) {
      return false;
    }

    feedInfo.remove(Integer.valueOf(getObjectId()));
    return true;
  }

  public L2Spawn spawn(int npcId, int x, int y, int z)
  {
    try
    {
      L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
      if (template == null) {
        return null;
      }
      L2Spawn spawn = new L2Spawn(template);
      spawn.setId(npcId);

      spawn.setHeading(0);
      spawn.setLocx(x);
      spawn.setLocy(y);
      spawn.setLocz(z + 20);
      spawn.stopRespawn();
      spawn.spawnOne();
      return spawn;
    } catch (Exception e1) {
      _log.warning("Could not spawn Npc " + npcId);
    }
    return null;
  }

  public void onSkillUse(L2PcInstance player, int skill_id)
  {
    int npcId = getNpcId();

    if (!feedableBeasts.contains(Integer.valueOf(npcId))) {
      return;
    }
    if ((skill_id != SKILL_GOLDEN_SPICE) && (skill_id != SKILL_CRYSTAL_SPICE)) {
      return;
    }

    int food = GOLDEN_SPICE;
    if (skill_id == SKILL_CRYSTAL_SPICE) {
      food = CRYSTAL_SPICE;
    }

    int objectId = getObjectId();

    broadcastPacket(new SocialAction(objectId, 2));

    if (growthCapableMobs.containsKey(Integer.valueOf(npcId)))
    {
      if (((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).spice[food].length == 0) {
        return;
      }

      int growthLevel = ((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).growth_level;

      if (growthLevel > 0)
      {
        if ((feedInfo.get(Integer.valueOf(objectId)) != null) && (((Integer)feedInfo.get(Integer.valueOf(objectId))).intValue() != player.getObjectId())) {
          return;
        }

      }

      if (Rnd.chance(((growthInfo)growthCapableMobs.get(Integer.valueOf(npcId))).growth_chance))
        spawnNext(player, growthLevel, food);
    }
    else if ((tamedBeasts.contains(Integer.valueOf(npcId))) && 
      (skill_id == ((L2TamedBeastInstance)this).getFoodType())) {
      ((L2TamedBeastInstance)this).onReceiveFood();
      String[] mytext = { "Refills! Yeah!", "I am such a gluttonous beast, it is embarrassing! Ha ha", "Your cooperative feeling has been getting better and better.", "I will help you!", "The weather is really good.  Wanna go for a picnic?", "I really like you! This is tasty...", "If you do not have to leave this place, then I can help you.", "What can I helped you with?", "I am not here only for food!", "Yam, yam, yam, yam, yam!" };

      broadcastPacket(new CreatureSay(objectId, 0, getName(), mytext[Rnd.get(mytext.length)]));
    }
  }

  private class growthInfo
  {
    public int growth_level;
    public int growth_chance;
    public int[][][] spice;

    public growthInfo(int level, int[][][] sp, int chance)
    {
      growth_level = level;
      spice = sp;
      growth_chance = chance;
    }
  }
}