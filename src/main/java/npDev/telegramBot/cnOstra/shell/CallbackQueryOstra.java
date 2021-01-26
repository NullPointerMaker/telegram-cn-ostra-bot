package npDev.telegramBot.cnOstra.shell;

import com.pengrad.telegrambot.model.CallbackQuery;
import npDev.telegramBot.cnOstra.data.Member;
import npDev.telegramBot.cnOstra.data.MemberDAO;
import npDev.telegramBot.shell.CallbackQueryShell;


public class CallbackQueryOstra extends CallbackQueryShell {
    public CallbackQueryOstra(CallbackQuery callbackQuery) {
        super(callbackQuery);
    }

    public Member getMember() {
        Member member = MemberDAO.queryForChatAndUser(getMessage().getChat().getID(), getFrom().getID());
        if (member == null) {
            member = new Member(getMessage().getChat().getID(), getFrom().getID());
        }
        return member;
    }
}
