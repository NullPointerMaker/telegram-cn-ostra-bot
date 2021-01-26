package npDev.telegramBot.cnOstra.data;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "member")
public class Member {
    static final String FLAG = "m_flag", LAST = "m_last", RECENT = "m_recent";
    //static final String CHAT="m_chat",USER="m_user",STATUS="m_status";
    @DatabaseField(id = true)
    private String id;//chat@user
    //	@DatabaseField(columnName=STATUS)
//	private byte status;
    @DatabaseField(columnName = FLAG)
    private int flag;//警告次数
    @DatabaseField(columnName = LAST)
    private int last;//上次更新status时间/上次发言时间
    @DatabaseField(columnName = RECENT)
    private int recent;//最近发言时间

    @SuppressWarnings("unused")
    Member() {
    }

    public Member(long chatID, int userID) {
        setId(chatID, userID);
    }

    String getId() {
        return id;
    }

    @SuppressWarnings("unused")
    void setId(String id) {
        this.id = id;
    }

    void setId(long chatID, int userID) {
        this.id = chatID + "@" + userID;
    }

    //	public long getChatID() {
//		return Long.parseLong(id.split("@")[0]);
//	}
//	public int getUserID() {
//		return Integer.parseInt(id.split("@")[1]);
//	}
    public int getFlag() {
        return flag;
    }

    @SuppressWarnings("unused")
    void setFlag(int flag) {
        this.flag = flag;
    }

    public int getLast() {
        return last;
    }
    public void setLast(int last) {
        this.last = last;
    }

    public int getRecent() {
        return recent;
    }

    public void setRecent(int recent) {
        this.recent = recent;
    }

//    public void log(int recent, int preHours) {if (recent - getRecent() > preHours * 60 * 60) {this.last=this.recent;this.recent=recent;MemberDAO.createOrUpdate(this); } }

    public void resetFlag() {//重设警告次数
        flag = 0;
    }

    public void addFlag() {//增加警告次数
        flag++;
    }

    public void setInvalid() {//重设权限
        recent = last = 1;
    }

    public boolean isValid(int voteTime, int preHours) {//有效时间内有发言
        int validTime = voteTime - preHours * 60 * 60;//有效起始时间
        return (getRecent() >= validTime && getRecent() <= voteTime)
                || (getLast() >= validTime && getLast() <= voteTime);
    }
    //	public byte getStatus() {
//		return status;
//	}
//	public void setStatus(byte status) {
//		this.status = status;
//	}
//	public Status getStatus(TelegramBot bot){
//		if(System.currentTimeMillis()/1000-getLast()>Configration.getCacheHours()*3600){
//			GetChatMember request = new GetChatMember(getChat(), getUser());
//			GetChatMemberResponse response = bot.execute(request);
//			if(response.isOk()){
//				ChatMember chatMember=response.chatMember();
//				Status sta = chatMember.status();
//				System.out.println("status: "+sta);
//				setStatus(sta);
//				System.out.println("can_send_messages: "+chatMember.canSendMessages());
//				if (sta.equals(Status.restricted) &&chatMember.canSendMessages()) {//被限制但能发消息
//					setStatus(Status.member);
//					MemberDao.createOrUpdate(this);
//				}
//			}
//		}
//		return Status.values()[getStatus()];
//	}
//	private void setStatus(Status status) {
//		setStatus((byte)status.ordinal());
//	}
}