package se.plushogskolan.database.repository.mysql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import se.plushogskolan.database.model.Team;
import se.plushogskolan.database.repository.RepositoryException;
import se.plushogskolan.database.repository.TeamRepository;

public final class MySQLTeamRepository implements TeamRepository {

	private final static Mapper<Team> mapper = (r -> new Team(r.getString(1), r.getString(2), r.getString(3)));

	@Override
	public void addTeam(Team team) throws RepositoryException {

		String insert = "INSERT INTO Team ( id, teamname, teamstatus) VALUES (?,?,?)";
		try {
			new SQL(insert).param(team.getId()).param(team.getName()).param(team.getStatus()).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can not add team:" + team.getName(), e);
		}

	}

	@Override
	public void updateTeam(String oldName, String newName) throws RepositoryException {
		String update = "UPDATE Team SET teamname = ? WHERE teamname = ?";
		try {
			new SQL(update).param(newName).param(oldName).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can not update team " + oldName, e);
		}
	}

	@Override
	public void deactivateTeam(String name) throws RepositoryException {
		String deactivate = "UPDATE Team SET teamstatus = ? WHERE teamname = ?";
		try {
			new SQL(deactivate).param("Inactive").param(name).update();
		} catch (SQLException e) {
			throw new RepositoryException("Can not deactivate team:", e);
		}
	}

	@Override
	public List<Team> getAllTeams() throws RepositoryException {
		String sql = "select * from Team";
		List<Team> teamList = new ArrayList<>();
		try {
			teamList = new SQL(sql).multiQuery(mapper);
			return teamList;
		} catch (SQLException e) {
			throw new RepositoryException("Can not get all teams", e);
		}

	}

	@Override
	public void addUserToTeam(String userid, String teamid) throws RepositoryException {

		String addUserToTeam = "UPDATE User SET teamid = ? WHERE id = ?";
		try {
			new SQL(addUserToTeam).param(teamid).param(userid).update();
		} catch (SQLException e) {
			throw new RepositoryException("Couldn't add user:" + userid + " to team:" + teamid, e);
		}

	}

	@Override
	public boolean exists(String teamname) throws RepositoryException {
		try {
			String checkIfTeamExists = "select * from Team where teamname=?";
			return new SQL(checkIfTeamExists).param(teamname).exists();
		} catch (SQLException e) {
			throw new RepositoryException("Can not check if team:" + teamname + " exists", e);
		}

	}

	@Override
	public Team getTeamById(String teamId) throws RepositoryException {
		try {
			String sql = "select * from Team where id=?";
			Team team = new SQL(sql).param(teamId).singleQuery(mapper);
			return team;
		} catch (SQLException e) {
			throw new RepositoryException("Can not get team by id:" + teamId, e);
		}
	}

}
