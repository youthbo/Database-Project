package se.plushogskolan.database.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
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
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import se.plushogskolan.database.model.User;
import se.plushogskolan.database.model.WorkItem;
import se.plushogskolan.database.model.WorkItemStatus;
import se.plushogskolan.database.repository.RepositoryException;
import se.plushogskolan.database.repository.UserRepository;
import se.plushogskolan.database.repository.WorkItemRepository;

@RunWith(MockitoJUnitRunner.class)
public final class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private WorkItemRepository workItemRepository;
	@InjectMocks
	private UserService userService;
	
	private static User user1;
	private static User user2;
	
	@BeforeClass
	public static void setUp(){
		user1 = new User("1","Anna","Karlsson","Annakarlsson","1","Active");
		user2 = new User("2","Anna","Karlsson","Anka",null,"Active");
	}
	
	@Test
	public void addUserWithNameLessThan10Test()  {
	   try{
		userService.addUser(user2); 
	   }catch(ServiceException e){
		   assertEquals(e.getMessage(),"Username must be at least 10 characters long!");
	   }
	}
	
	@Test
	public void addUserToTeamMoreThan10PeopleTest() throws RepositoryException{
		List<User> userList = new ArrayList<>();
		for (int i=0;i<10;i++){
			userList.add(new User(Integer.toString(i),"fn","ln","Username_"+Integer.toString(i),"1","Active"));
		}
		when(userRepository.getAllUsersInTeam("1")).thenReturn(userList);
		try{
		   userService.addUser(user1);
		}catch(ServiceException e){
			assertEquals(e.getMessage(),"This team already has 10 users! (But it is allowed to have MAX 10 users in one team)");
		}
	}
	
	@Test
	public void canUpdateUserTest() throws RepositoryException{
		User user3 = new User("3","Hans","Xie","HansXiexxx","1","Active");
		when(userRepository.getUserByUsername("AnnaKarlsson")).thenReturn(user1);
		userService.updateUser(user3, "AnnaKarlsson");
		verify(userRepository).updateUser(user3, "AnnaKarlsson");
	}
	
	@Test
	public void canDeactiveUserTest() throws RepositoryException{
		when(userRepository.getUserByUsername("Annakarlsson")).thenReturn(user1);
		List<WorkItem> result = new ArrayList<>();
		result.add(new WorkItem("1","item 1","Started",user1.getId(),null));
		result.add(new WorkItem("2","item 2","Done",user1.getId(),null));
		result.add(new WorkItem("3","item 3","UnStarted",user1.getId(),null));

		when(workItemRepository.getAllByUser(user1.getId())).thenReturn(result);
			
		List<WorkItem> result1 = new ArrayList<>();
		doAnswer(new Answer<List<WorkItem>>(){
			@Override
			public List<WorkItem> answer(InvocationOnMock invocation) throws Throwable {
				String status = (String) invocation.getArguments()[1];
				String itemId = (String) invocation.getArguments()[0];
				result1.add(new WorkItem(itemId,"item"+itemId,status,user1.getId(),null));
				
				return result1;	
			}
			
		}).when(workItemRepository).changeStatus(Mockito.anyString(), Mockito.eq(WorkItemStatus.Unstarted.toString()));
		
		doNothing().when(workItemRepository).removeUserId(Mockito.anyString());
		userService.deactivateUser("Annakarlsson");
		assertEquals(result1.size(),3);
		assertEquals(result1.get(0).getStatus(),"Unstarted");
		verify(workItemRepository,times(3)).changeStatus(Mockito.anyString(), Mockito.eq(WorkItemStatus.Unstarted.toString()));
		verify(workItemRepository,times(3)).removeUserId(Mockito.anyString());
	}
	
	@Test
	public void canGetAllUsersInTeamTest() throws RepositoryException{
		List<User> userList = new ArrayList<>();
		for (int i=0;i<3;i++){
			userList.add(new User(Integer.toString(i),"firstname","lastname","Username_"+i,"1","Active"));
		}
		for (int i=0;i<2;i++){
			userList.add(new User(Integer.toString(i+3),"firstname","lastname","Username_"+i,"2","Active"));
		}
		when(userRepository.getAllUsersInTeam("2")).thenAnswer(new Answer<List<User>>(){

			@Override
			public List<User> answer(InvocationOnMock invocation) throws Throwable {
                List<User> result = new ArrayList<>();
                String teamId = (String)invocation.getArguments()[0];
                for (User user:userList){
                	if (user.getTeamid().equals(teamId)){
                		result.add(user);
                	}
                } 
				return result;
			}		
		});
		List<User> result = userService.getAllUsersInTeam("2");
		assertEquals(result.size(),2);
	}

}
