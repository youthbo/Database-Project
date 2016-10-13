package se.plushogskolan.database.repository.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.plushogskolan.database.model.User;
import se.plushogskolan.database.repository.RepositoryException;
import se.plushogskolan.database.repository.UserRepository;;

public final class MySQLUserRepository implements UserRepository {

	private final static Mapper<User> mapper = (r -> new User(r.getString(1), r.getString(2), r.getString(3),
			r.getString(4), r.getString(5), r.getString(6)));

	@Override
	public void addUser(User user) throws RepositoryException {
		String insert = "INSERT INTO User (id, firstname, lastname, username, userstatus, teamid) "
				+ "VALUES (?,?,?,?,?,?)";
		try {
			new SQL(insert).param(user.getId()).param(user.getFirstname()).param(user.getLastname())
					.param(user.getUsername()).param(user.getStatus()).param(user.getTeamid()).update();
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't add user \"" + user.getUsername() + "\" to the database", e);
		}
	}

	@Override
	public User getUserById(String id) throws RepositoryException {
		String query = "select * from User where id = ?";
		try {
			User user = new SQL(query).param(id).singleQuery(mapper);
			return user;
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't get user with id: " + id + " from the database", e);
		}
	}

	@Override
	public User getUserByUsername(String username) throws RepositoryException {
		String query = "select * from User where username = ?";

		try {
			User user = new SQL(query).param(username).singleQuery(mapper);
			return user;
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't get user with name: " + username + " from the database", e);
		}

	}

	@Override
	public List<User> getAllUsersInTeam(String teamid) throws RepositoryException {
		List<User> allUsersInTeam = new ArrayList<>();
		String query = "select * from User where teamid = ?";
		try {
			allUsersInTeam = new SQL(query).param(teamid).multiQuery(mapper);
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't get users in team id: " + teamid + " from the database", e);
		}
		return allUsersInTeam;
	}

	@Override
	public void deactivateUser(String username) throws RepositoryException {

		String update = "UPDATE User SET userstatus ='Inactive' WHERE username = ?";
		try {
			new SQL(update).param(username).update();
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't deactivate user:" + username, e);
		}

	}

	@Override
	public void updateUser(User user, String oldUsername) throws RepositoryException {
		// we do not update id here, id must not change for the same user!
		String update = "UPDATE User SET firstname = ?, "
				+ "lastname = ?, username = ?, userstatus = ? , teamid = ? WHERE username = ?";
		try {
			new SQL(update).param(user.getFirstname()).param(user.getLastname()).param(user.getUsername())
					.param(user.getStatus()).param(user.getTeamid()).param(oldUsername).update();
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't update user \"" + user.getUsername() + "\" to the database", e);
		}

	}
}
