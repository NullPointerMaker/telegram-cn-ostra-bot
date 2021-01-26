package npDev.telegramBot.cnOstra.shell;

import com.pengrad.telegrambot.model.Message;
import npDev.telegramBot.cnOstra.data.Member;
import npDev.telegramBot.cnOstra.data.MemberDAO;
import npDev.telegramBot.shell.MessageShell;

public class MessageOstra extends MessageShell {
    public MessageOstra(Message message) {
        super(message);
    }

    public Member getMember() {
        Member member = MemberDAO.queryForChatAndUser(getChat().getID(), getFrom().getID());
        if (member == null) {
            member=new Member(getChat().getID(), getFrom().getID());
        }
        return member;
    }
    public void logMember(int preHours){
        if(!getChat().isGroupChat()||getFrom().isNull()){
            return;
        }
        Member member = MemberDAO.queryForChatAndUser(getChat().getID(), getFrom().getID());
        if(member==null){
            member=new Member(getChat().getID(),getFrom().getID());
            MemberDAO.create(member);
        }else if(getDate()-member.getRecent()>preHours*60*60){
            member.setLast(member.getRecent());
            member.setRecent(getDate());
            MemberDAO.update(member);
        }
    }
}