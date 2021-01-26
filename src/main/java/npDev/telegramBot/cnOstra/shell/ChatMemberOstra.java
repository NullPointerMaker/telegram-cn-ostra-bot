package npDev.telegramBot.cnOstra.shell;

import com.pengrad.telegrambot.model.ChatMember;
import npDev.telegramBot.cnOstra.Bot;
import npDev.telegramBot.cnOstra.data.Member;
import npDev.telegramBot.cnOstra.data.MemberDAO;
import npDev.telegramBot.shell.ChatMemberShell;


public class ChatMemberOstra extends ChatMemberShell {
    public ChatMemberOstra(ChatMember chatMember, long chatID) {
        super(chatMember, chatID);
    }

    public static ChatMemberOstra cast(ChatMemberShell shell) {
        return Bot.GSON.fromJson(Bot.GSON.toJson(shell), ChatMemberOstra.class);
    }

    public Member getMember() {
        Member member = MemberDAO.queryForChatAndUser(getChatID(), getUser().getID());
        if (member == null) {
            member = new Member(getChatID(), getUser().getID());
        }
        return member;
    }
}
