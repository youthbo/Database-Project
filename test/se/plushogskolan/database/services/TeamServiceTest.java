package se.plushogskolan.database.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import se.plushogskolan.database.model.Team;
import se.plushogskolan.database.model.User;
import se.plushogskolan.database.repository.RepositoryException;
import se.plushogskolan.database.repository.TeamRepository;
import se.plushogskolan.database.repository.UserRepository;

@RunWith(MockitoJUnitRunner.class)
public final class TeamServiceTest {
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private  UserRepository userRepository;
    @InjectMocks
    private TeamService teamService;
    
    private static Team team;
	
    @BeforeClass
    public static void setUp() throws RepositoryException{
    	team = new Team("1","Test team 1","Active");
 
    }
	@Test
	public void canAddTeamTest() throws RepositoryException {
		when(teamRepository.exists(team.getName())).thenReturn(false);
		teamService.addTeam(team);
		verify(teamRepository,times(1)).addTeam(team);
	}
	
	@Test
	public void canDeactiveTeamTest() throws RepositoryException{
		when(teamRepository.exists(team.getName())).thenReturn(true);
		doAnswer(new Answer<Team>(){

			@Override
			public Team answer(InvocationOnMock invocation) throws Throwable {
				team = new Team(team.getId(),team.getName(),"Inactive");
				return team;
			}
			
		}).when(teamRepository).deactivateTeam(team.getName());
		teamService.deactivateTeam(team.getName());
		verify(teamRepository).deactivateTeam(team.getName());
		assertEquals(team.getStatus(),"Inactive");
	}
	
	@Test
	public void canGetAllTeamsTest() throws RepositoryException{
		List<Team> teamList = new ArrayList<>();
		teamList.add(new Team("11","team 1","Active"));
		teamList.add(new Team("12","team 2","Active"));
		teamList.add(new Team("13","team 3","Active"));
		when(teamRepository.getAllTeams()).thenReturn(teamList);
		
		List<Team> teamList1 = teamService.getAllTeams();
		assertEquals(teamList1.size(),teamList.size());
		verify(teamRepository,times(1)).getAllTeams();
	}
	
	@Test
	public void canAddUserToTeamTest() throws RepositoryException{
		when(teamRepository.getTeamById(team.getId())).thenReturn(team);
		when(teamRepository.exists(team.getName())).thenReturn(true);
		List<User> userList = new ArrayList<>();
		for (int i=0;i<8;i++){
			userList.add(new User(Integer.toString(i),"fn","ln","Username_"+Integer.toString(i),team.getId(),"Active"));
		}
		when(userRepository.getAllUsersInTeam(team.getId())).thenReturn(userList);
        
		User user = new User("9","fn","ln","Username_11",null,"Active");
		teamService.addUserToTeam(user.getId(), team.getId());
		verify(teamRepository).addUserToTeam(user.getId(), team.getId());
	}
	
	@Test(expected = ServiceException.class)
	public void canNotAddUserToTeamMoreThan10PeopleTest() throws RepositoryException{
		when(teamRepository.getTeamById(team.getId())).thenReturn(team);
		when(teamRepository.exists(team.getName())).thenReturn(true);
		List<User> userList = new ArrayList<>();
		for (int i=0;i<10;i++){
			userList.add(new User(Integer.toString(i),"fn","ln","Username_"+Integer.toString(i),team.getId(),"Active"));
		}
		when(userRepository.getAllUsersInTeam(team.getId())).thenReturn(userList);
        
		User user = new User("11","fn","ln","Username_11",null,"Active");
		teamService.addUserToTeam(user.getId(), team.getId());
	}
	
	@Test
	public void canUpdateTeamTest() throws RepositoryException{
		String old_name = "Test team 1";
		String new_name = "Test team 2";
		when(teamRepository.exists(old_name)).thenReturn(true);
		when(teamRepository.exists(new_name)).thenReturn(false);
		teamService.updateTeam(old_name, new_name);
		verify(teamRepository).updateTeam(old_name, new_name);
	}
    

}
