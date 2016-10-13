package se.plushogskolan.database.repository.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.plushogskolan.database.model.Issue;
import se.plushogskolan.database.model.WorkItem;
import se.plushogskolan.database.repository.IssueRepository;
import se.plushogskolan.database.repository.RepositoryException;

public final class MySQLIssueRepository implements IssueRepository {

	private final static Mapper<Issue> mapper = (r -> new Issue(r.getString(1), r.getString(2)));

	private final static Mapper<WorkItem> itemMapper = (r -> new WorkItem(r.getString(1), r.getString(2),
			r.getString(3), r.getString(4), r.getString(5)));

	@Override
	public void createIssueAndAssignToWorkItem(Issue issue, String itemId) throws RepositoryException {
		String create_issue_sql = "insert into issue values(?,?)";
		String assign_issue_sql = "update workitem set issueid=? where id=?";

		try {
			SQL sql = new SQL(create_issue_sql);
			sql.param(issue.getId()).param(issue.getDescription()).update();
			sql = new SQL(assign_issue_sql);
			sql.param(issue.getId()).param(itemId).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can't create issue and assign to work item", e);
		}

	}

	@Override
	public void updateIssue(Issue issue, String new_description) throws RepositoryException {
		String update_sql = "update issue set description = ? where description = ?";
		SQL sql = new SQL(update_sql);
		try {
			sql.param(new_description).param(issue.getDescription()).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can't Update issue.", e);
		}
	}

	@Override
	public List<WorkItem> getAllItemsWithIssue() throws RepositoryException {
		String sql = "select * from workitem where issueid is not null";
		List<WorkItem> itemList = new ArrayList<>();
		try {
			itemList = new SQL(sql).multiQuery(itemMapper);
		} catch (SQLException e) {
			throw new RepositoryException("Can not get all items with issue.", e);
		}
		return itemList;
	}

	@Override
	public boolean exists(Issue issue) throws RepositoryException {
		String sql = "select * from issue where description = ?";
		try {
			return new SQL(sql).param(issue.getDescription()).exists();
		} catch (SQLException e) {
			throw new RepositoryException("Can't check if issue exists", e);
		}

	}

	@Override
	public void createIssue(Issue issue) throws RepositoryException {
		String create_issue_sql = "insert into issue values(?,?)";
		SQL sql = new SQL(create_issue_sql);
		try {
			sql.param(issue.getId()).param(issue.getDescription()).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can't create issue", e);
		}

	}

	@Override
	public void assignToWorkItem(Issue issue, String itemid) throws RepositoryException {
		String assign_issue_sql = "update workitem set issueid=? where id=?";
		SQL sql = new SQL(assign_issue_sql);
		try {
			sql.param(issue.getId()).param(itemid).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can't assign issue to work item", e);
		}

	}

	@Override
	public Issue getIssueByName(String name) throws RepositoryException {
		String sql = "select * from issue where description =?";
		Issue issue;
		try {
			issue = new SQL(sql).param(name).singleQuery(mapper);
		} catch (SQLException e) {
			throw new RepositoryException("Can't get issue by name", e);
		}
		return issue;
	}
}
