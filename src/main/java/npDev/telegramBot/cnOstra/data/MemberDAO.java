package npDev.telegramBot.cnOstra.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import npDev.telegramBot.cnOstra.Database;

import java.sql.SQLException;

public class MemberDAO {
    private static Dao<Member, String> dao;
//	private static List<Member> queryForMatching(Member member) {List<Member> list = new ArrayList<Member>();try {list = getDao().queryForMatchingArgs(member);} catch (SQLException e) {e.printStackTrace();}return list;}

    public static Member queryForId(String id) {
        Member v = null;
        try {
            v = getDao().queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return v;
    }

    //	public static Member queryForChatAndUser(long chatID,int userID) {Member r = new Member(chatID,userID);List<Member> list=queryForMatching(r);if(list.size()==0){r.resetFlag();}else{r=list.get(0);}return r;}
    public static Member queryForChatAndUser(long chatID, int userID) {
        Member m = new Member(chatID, userID);
        return queryForId(m.getId());
    }

public static void create(Member member) {try {getDao().create(member);} catch (SQLException e) {e.printStackTrace();}}
public static void update(Member member) {try {getDao().update(member);} catch (SQLException e) {e.printStackTrace();}}
//static void delete(Member member) {try {getDao().delete(member);} catch (SQLException e) {e.printStackTrace();}}

    public static void createOrUpdate(Member member) {
        try {
            //member.setId(member.getChat()+"@"+member.getUser());
            //member.setLast((int) (System.currentTimeMillis()/1000));
            getDao().createOrUpdate(member);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * 全局唯一DAO
     *
     * @return dao
     * @throws SQLException 异常
     */
    private static Dao<Member, String> getDao() throws SQLException {
        if (dao == null) {
            dao = DaoManager.createDao(Database.getConnection(),
                    Member.class);
            TableUtils.createTableIfNotExists(Database.getConnection(),
                    Member.class);// 如果不存在就创建表
        }
        return dao;
    }
}
