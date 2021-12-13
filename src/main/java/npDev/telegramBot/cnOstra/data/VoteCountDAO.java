package npDev.telegramBot.cnOstra.data;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.TableUtils;
import npDev.telegramBot.cnOstra.Database;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VoteCountDAO {
    private static Dao<VoteCount, String> dao;

    //	public static VoteCount queryForId(String id) {
//		VoteCount v = null;
//		try {
//			v = getDao().queryForId(id);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return v;
//	}
//	private static List<VoteCount> queryForMatching(VoteCount voteCount) {
//		List<VoteCount> list = new ArrayList<VoteCount>();
//		try {
//			list = getDao().queryForMatchingArgs(voteCount);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return list;
//	}
    public static List<VoteCount> queryForMatchingVoteAndOption(Vote vote, Byte option) {
//		VoteCount vc=new VoteCount(vote, null, option);
//		return queryForMatching(vc);
        List<VoteCount> list = new ArrayList<>();
        try {
            QueryBuilder<VoteCount, String> qb = getDao().queryBuilder();
            qb.where().eq(VoteCount.VOTE, vote.getId()).and().eq(VoteCount.OPTION, option);
            list = getDao().query(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static int countOfVoteUserAndOption(Vote vote, Long user, Byte option) {
        int count = 0;
        try {
            QueryBuilder<VoteCount, String> qb = getDao().queryBuilder();
            qb.setCountOf(true).where().eq(VoteCount.VOTE, vote.getId()).and().eq(VoteCount.USER, user).and().eq(VoteCount.OPTION, option);
            count = (int) getDao().countOf(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
    // public static void create(Message message) {// 用增改替换
    // try {
    // getDao().create(message);
    // } catch (SQLException e) {
    // e.printStackTrace();
    // }
    // }

    public static int countOfVoteAndOption(Vote vote, Byte option) {
        int count = 0;
        try {
            QueryBuilder<VoteCount, String> qb = getDao().queryBuilder();
            qb.setCountOf(true).where().eq(VoteCount.VOTE, vote.getId()).and().eq(VoteCount.OPTION, option);
            count = (int) getDao().countOf(qb.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    //	public static void delete(VoteCount voteCount) {
//		try {
//			getDao().delete(voteCount);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//    static void deleteByVote(Vote vote) {
//        try {
//            DeleteBuilder<VoteCount, String> db = getDao().deleteBuilder();
//            db.where().eq(VoteCount.VOTE, vote.getId());
//            getDao().delete(db.prepare());
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    public static void createOrUpdate(VoteCount voteCount) {
        try {
            voteCount.setId(voteCount.getVote().getId() + "@" + voteCount.getUser());
            getDao().createOrUpdate(voteCount);
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
    private static Dao<VoteCount, String> getDao() throws SQLException {
        if (dao == null) {
            dao = DaoManager.createDao(Database.getConnection(),
                    VoteCount.class);
            TableUtils.createTableIfNotExists(Database.getConnection(),
                    VoteCount.class);// 如果不存在就创建表
        }
        return dao;
    }
}
