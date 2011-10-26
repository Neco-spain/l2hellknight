import sys
from java.util import Iterator
from l2.hellknight import L2DatabaseFactory
from l2.hellknight.gameserver.model.quest import State
from l2.hellknight.gameserver.model.quest import QuestState
from l2.hellknight.gameserver.model.quest.jython import QuestJython as JQuest
 
qn = "50300_PKlist"
 
NPC         = 36601
QuestId     = 50300
MIN_LEVEL   = 0
MAX_LEVEL   = 86
QuestName   = "PKlist"
QuestDesc   = "custom"
InitialHtml = "1.htm"
DonateMaster  = "This ain't no free service nubblet! Now get out of my hare... Get it, 'hare'? - That gets me every time!"
 
 
class Quest (JQuest) :
 
	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)
 
	def onEvent(self,event,st):
		htmltext = event
		level = st.getPlayer().getLevel()
		levelup = 86 - level
		if level < MIN_LEVEL :
			return"<html><head><body>No quicky for you! - Your to young shorty!</body></html>"
		if level > MAX_LEVEL :
		    return"<html><head><body>No quicky for you! - Your to old fatty!</body></html>"
 
		else:
            		#PK INFO
			if event == "01":
			 con = L2DatabaseFactory.getInstance().getConnection()
			 total_asesinados = 0
			 htmltext_ini = "<html><head><title>PG-L2 PK INFO</title></head><body><table width=300><tr><td><font color =\"FF00FF\">Pos.</td><td><center><font color =\"FFFF00\">*** Player ***</color></center></td><td><center>*** Kill's ***</center></td></tr>"
			 htmltext_info =""
			 color = 1
			 pos = 0
			 pks = con.prepareStatement("SELECT char_name,pkkills FROM characters WHERE pkkills>0 and accesslevel=0 order by pkkills desc limit 50")
			 rs = pks.executeQuery()
			 while (rs.next()) :
			   char_name = rs.getString("char_name")
			   char_pkkills = rs.getString("pkkills")
			   total_asesinados = total_asesinados + int(char_pkkills)
			   pos = pos + 1
			   posstr = str(pos)
			   if color == 1:
			      color_text = "<font color =\"00FFFF\">"
			      color = 2
			      htmltext_info = htmltext_info + "<tr><td><center><font color =\"FF00FF\">" + posstr + "</td><td><center>" + color_text + char_name +"</center></td><td><center>" + char_pkkills + "</center></td></tr>"
			   elif color == 2:
			      color_text = "<font color =\"FF0000\">"
			      color = 1
			      htmltext_info = htmltext_info + "<tr><td><center><font color =\"FF00FF\">" + posstr + "</td><td><center>" + color_text + char_name +"</center></td><td><center>" + char_pkkills + "</center></td></tr>"
			 htmltext_end = "</table><center><font color=\"FFFFFF\">" + "A Total of " + str(total_asesinados) + " Pk's.</center></body></html>"
			 htmltext_pklist = htmltext_ini + htmltext_info + htmltext_end
			 L2DatabaseFactory.close(con)
			 return htmltext_pklist

            		#PVP INFO
			if event == "02":
			 con = L2DatabaseFactory.getInstance().getConnection()
			 total_asesinados = 0
			 htmltext_ini = "<html><head><title>PG-L2 PVP INFO</title></head><body><table width=300><tr><td><font color =\"FF00FF\">Pos.</td><td><center><font color =\"FFFF00\">*** Player ***</color></center></td><td><center>*** Kill's ***</center></td></tr>"
			 htmltext_info =""
			 color = 1
			 pos = 0
			 pks = con.prepareStatement("SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 and accesslevel=0 order by pvpkills desc limit 50")
			 rs = pks.executeQuery()
			 while (rs.next()) :
			   char_name = rs.getString("char_name")
			   char_pvpkills = rs.getString("pvpkills")
			   total_asesinados = total_asesinados + int(char_pvpkills)
			   pos = pos + 1
			   posstr = str(pos)
			   if color == 1:
			      color_text = "<font color =\"00FFFF\">"
			      color = 2
			      htmltext_info = htmltext_info + "<tr><td><center><font color =\"FF00FF\">" + posstr + "</td><td><center>" + color_text + char_name +"</center></td><td><center>" + char_pvpkills + "</center></td></tr>"
			   elif color == 2:
			      color_text = "<font color =\"FF0000\">"
			      color = 1
			      htmltext_info = htmltext_info + "<tr><td><center><font color =\"FF00FF\">" + posstr + "</td><td><center>" + color_text + char_name +"</center></td><td><center>" + char_pvpkills + "</center></td></tr>"
			 htmltext_end = "</table><center><font color=\"FFFFFF\">" + "A Total of " + str(total_asesinados) + " PvP's.</center></body></html>"
			 htmltext_pklist = htmltext_ini + htmltext_info + htmltext_end
			 L2DatabaseFactory.close(con)
			 return htmltext_pklist

			if htmltext != event:
 
				st.exitQuest(1)
 
		return htmltext
 
 
 
	def onFirstTalk (self,npc,player):
 
	   st = player.getQuestState(qn)
 
	   if not st : st = self.newQuestState(player)

	   return InitialHtml

 
QUEST = Quest(QuestId,str(QuestId) + "_" + QuestName,QuestDesc)
QUEST.addStartNpc(NPC)
QUEST.addFirstTalkId(NPC)
QUEST.addTalkId(NPC)
print "# PVP PK INFO Loaded"