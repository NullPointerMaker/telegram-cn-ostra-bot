package npDev.telegramBot.cnOstra.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.table.TableUtils;
import npDev.telegramBot.cnOstra.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VoteDAO {
    private static Dao<Vote, String> dao;

    public static Vote queryForId(String id) {
        Vote v = null;
        try {
            v = getDao().queryForId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return v;
    }

    private static List<Vote> queryForMatching(Vote vote) {
        List<Vote> list = new ArrayList<>();
        try {
            list = getDao().queryForMatchingArgs(vote);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // public static void create(Message message) {// 用增改替换
    // try {
    // getDao().create(message);
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }

    public static Vote queryForReportedMessage(String reportedMessage) {
        Vote r = new Vote();
        r.setReportedMessage(reportedMessage);
        List<Vote> list = queryForMatching(r);
        if (list.size() == 0) {
            r = null;
        } else {
            r = list.get(0);
        }
        return r;
    }

//	public static void delete(Vote vote) {
//		try {
//			getDao().delete(vote);
//			VoteCountDao.deleteByVote(vote);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}

    public static void createOrUpdate(Vote vote) {
        try {
            getDao().createOrUpdate(vote);
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
    private static Dao<Vote, String> getDao() throws SQLException {
        if (dao == null) {
            dao = DaoManager.createDao(Database.getConnection(),
                    Vote.class);
            TableUtils.createTableIfNotExists(Database.getConnection(),
                    Vote.class);// 如果不存在就创建表
        }
        return dao;
    }
}
