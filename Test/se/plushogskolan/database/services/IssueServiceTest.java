package se.plushogskolan.database.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import se.plushogskolan.database.model.Issue;
import se.plushogskolan.database.model.WorkItem;
import se.plushogskolan.database.repository.IssueRepository;
import se.plushogskolan.database.repository.RepositoryException;
import se.plushogskolan.database.repository.ServiceException;
import se.plushogskolan.database.repository.WorkItemRepository;

@RunWith(MockitoJUnitRunner.class)
public class IssueServiceTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
    @Mock
    private static IssueRepository issueRepository;
    @Mock
    private static WorkItemRepository workItemRepository;
    
    @InjectMocks
    private static IssueService issueService;
    
    
    
    private static Issue issue;
	
	
    @BeforeClass
    public static void setUp() throws RepositoryException{
    	issue = new Issue("Test issue 1");
 
    }
    	
	@Test
	public void createIssueTest() throws RepositoryException {    
		when(issueRepository.exists(issue)).thenReturn(false);
		issueService.createIssue(issue);	
		verify(issueRepository).createIssue(issue);
	}
	
	@Test(expected=ServiceException.class)
	public void createIssueExceptionTest() throws RepositoryException {
		when(issueRepository.exists(issue)).thenReturn(true);
		issueService.createIssue(issue);
	}
	
	@Test
	public void updateIssueTest() throws RepositoryException{
		when(issueRepository.exists(issue)).thenReturn(true);
		when(issueRepository.getIssueByName("Test issue 1")).thenReturn(null);
		issueService.updateIssue(issue, "Test issue 3");	
		verify(issueRepository).updateIssue(issue,"Test issue 3");
		
		
	}
	
	@Test(expected=ServiceException.class)
	public void updateIssueExceptionTest() throws RepositoryException {
		when(issueRepository.exists(issue)).thenReturn(false);	
		issueService.updateIssue(issue,"Test issue 3");
		
	}
	
	@Test(expected=ServiceException.class)
	public void updateIssueExceptionTest2() throws RepositoryException{
		when(issueRepository.exists(issue)).thenReturn(true);
		Issue issue2 = new Issue("Test issue 2");
		when(issueRepository.getIssueByName("Test issue 2")).thenReturn(issue2);
	    issueService.updateIssue(issue, "Test issue 2");
		
	}
	
	@Test
	public void assignToWorkTest() throws RepositoryException{
		WorkItem workItem = new WorkItem("1","Test work item","Done",null, null);
		when(workItemRepository.getById("1")).thenReturn(workItem);
		issueService.assignToWorkItem(issue, "1");
		verify(issueRepository,times(1)).assignToWorkItem(issue, "1");
		verify(workItemRepository,times(1)).changeStatus("1", "Unstarted");		
	}
	
	@Test(expected=ServiceException.class)
	public void assignToWorkExceptionTest() throws RepositoryException{
		WorkItem workItem = new WorkItem("2","Test work item","Started",null,null);
		when(workItemRepository.getById("2")).thenReturn(workItem);
		issueService.assignToWorkItem(issue, "2");
		thrown.expectMessage("Assign issue to work item failed. Status of work item is not 'Done'");
	}
	
	@Test 
	public void getAllItemWithIssue() throws RepositoryException{
		List<WorkItem> itemList = new ArrayList<>();
		itemList.add(new WorkItem("1","work item 1","Started",null,"1"));
		itemList.add(new WorkItem("2","work item 2","Started",null,"2"));
		itemList.add(new WorkItem("3","work item 3","Started",null,"3"));
		when(issueRepository.getAllItemsWithIssue()).thenReturn(itemList);
		assertEquals(issueService.getAllItemsWithIssue().size(),3);
	}
	
	@Test 
	public void getIssueByNameTest() throws RepositoryException{
		when(issueRepository.getIssueByName("Test issue 1")).thenReturn(issue);
		assertEquals(issueService.getIssueByName("Test issue 1").getDescription(),"Test issue 1");
	}
	
}
