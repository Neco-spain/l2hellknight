/*     */ package quests._640_TheZeroHour;
/*     */ 
/*     */ import l2rt.Config;
import l2rt.extensions.scripts.ScriptFile;
import l2rt.gameserver.model.instances.L2NpcInstance;
import l2rt.gameserver.model.quest.Quest;
import l2rt.gameserver.model.quest.QuestState;
import quests._109_InSearchOfTheNest._109_InSearchOfTheNest;
/*     */ 
/*     */ public class _640_TheZeroHour extends Quest
/*     */   implements ScriptFile
/*     */ {
/*     */   private static final int KAHMAN = 31554;
/*     */   private static final int FANG = 14859;
/*     */   private static final int Reward = 14849;
/*     */   private static final int QUEEN = 25671;
/*     */ 
/*     */   public void onLoad()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void onReload()
/*     */   {
/*     */   }
/*     */ 
/*     */   public void onShutdown()
/*     */   {
/*     */   }
/*     */ 
/*     */   public _640_TheZeroHour()
/*     */   {
/*  29 */     super(true);
/*  30 */     addStartNpc(31554);
/*  31 */     addKillId(new int[] { 25671 });
/*  32 */     addQuestItem(new int[] { 14859 });
/*     */   }
/*     */ 
/*     */   public String onEvent(String event, QuestState st, L2NpcInstance npc)
/*     */   {
/*  38 */     int cond = st.getInt("cond");
/*  39 */     String htmltext = event;
/*     */ 
/*  41 */     if (event.equals("31554-02.htm"))
/*     */     {
/*  43 */       st.set("cond", "1");
/*  44 */       st.setState(2);
/*  45 */       st.playSound(SOUND_ACCEPT);
/*     */     }
/*  47 */     else if (event.equals("reward"))
/*     */     {
/*  49 */       if (st.getQuestItemsCount(14859) >= 1L)
/*     */       {
/*  51 */         htmltext = "31554-06.htm";
/*  52 */         st.takeItems(14859, 1L);
/*  53 */         st.giveItems(14849, 1L);
/*     */       }
/*     */       else {
/*  56 */         htmltext = "Not enough fang!";
/*     */       }
/*     */     }
/*  58 */     return htmltext;
/*     */   }
/*     */ 
/*     */   public String onTalk(L2NpcInstance npc, QuestState st)
/*     */   {
/*  64 */     String htmltext = "noquest";
/*  65 */     int cond = st.getCond();
/*  66 */     int npcId = npc.getNpcId();
/*     */ 
/*  68 */     QuestState InSearchOfTheNest = st.getPlayer().getQuestState(_109_InSearchOfTheNest.class);
/*  69 */     if (npcId == 31554)
/*     */     {
/*  71 */       if (cond == 0)
/*     */       {
/*  73 */         if (st.getPlayer().getLevel() >= 81)
/*     */         {
/*  75 */           if ((InSearchOfTheNest != null) && (InSearchOfTheNest.isCompleted()))
/*  76 */             htmltext = "31554-01.htm";
/*     */           else
/*  78 */             htmltext = "31554-00.htm";
/*     */         }
/*     */         else
/*  81 */           htmltext = "31554-03.htm";
/*     */       }
/*  83 */       else if (cond == 1)
/*  84 */         htmltext = "31554-04.htm";
/*     */     }
/*  86 */     return htmltext;
/*     */   }
/*     */ 
/*     */   public String onKill(L2NpcInstance npc, QuestState st)
/*     */   {
/*  92 */     int npcId = npc.getNpcId();
/*  93 */     int cond = st.getInt("cond");
/*     */ 
/*  95 */     if ((npcId == 25671) && (cond >= 1))
/*     */     {
/*  97 */       st.giveItems(14859, (int)Config.RATE_QUESTS_DROP);
/*  98 */       st.playSound(SOUND_ITEMGET);
/*     */     }
/* 100 */     return null;
/*     */   }
/*     */ }

/* Location:           C:\Users\Baltasar\Desktop\scripts.jar
 * Qualified Name:     quests._640_TheZeroHour._640_TheZeroHour
 * JD-Core Version:    0.6.0
 */