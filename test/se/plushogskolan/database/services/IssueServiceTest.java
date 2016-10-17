package se.plushogskolan.database.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import se.plushogskolan.database.model.Issue;
import se.plushogskolan.database.model.WorkItem;
import se.plushogskolan.database.repository.IssueRepository;
import se.plushogskolan.database.repository.RepositoryException;
import se.plushogskolan.database.repository.WorkItemRepository;

@RunWith(MockitoJUnitRunner.class)
public final class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;
    @Mock
    private WorkItemRepository workItemRepository;
    
    @InjectMocks
    private IssueService issueService;
    
    private static Issue issue;
    private static WorkItem workItem;
	
	
    @BeforeClass
    public static void setUp() throws RepositoryException{
    	issue = new Issue("Test issue 1");
    	workItem = new WorkItem("1","Test work item","Done",null, null);
 
    }
    	
	@Test
	public void canCreateIssueTest() throws RepositoryException {    
		when(issueRepository.exists(issue)).thenReturn(false);
		issueService.createIssue(issue);	
		verify(issueRepository).createIssue(issue);
	}
	
	@Test(expected=ServiceException.class)
	public void canNotCreateExistIssueTest() throws RepositoryException {
		when(issueRepository.exists(issue)).thenReturn(true);
		issueService.createIssue(issue);
	}
	
	@Test
	public void canUpdateIssueTest() throws RepositoryException{
		when(issueRepository.exists(issue)).thenReturn(true);
		when(issueRepository.getIssueByName("Test issue 1")).thenReturn(null);
		issueService.updateIssue(issue, "Test issue 3");	
		verify(issueRepository).updateIssue(issue,"Test issue 3");		
	}
	
	
	@Test(expected=ServiceException.class)
	public void canNotUpdateIssueIfSameNameExistsTest() throws RepositoryException{
		when(issueRepository.exists(issue)).thenReturn(true);
		Issue issue2 = new Issue("Test issue 2");
		when(issueRepository.getIssueByName("Test issue 2")).thenReturn(issue2);
	    issueService.updateIssue(issue, "Test issue 2");	
	}
	
	@Test
	public void canAssignToWorkTest() throws RepositoryException{
		when(workItemRepository.getById("1")).thenReturn(workItem);
		doAnswer(new Answer<WorkItem>(){
			@Override
			public WorkItem answer(InvocationOnMock invocation) throws Throwable {
				Issue issue1 = (Issue) invocation.getArguments()[0];
				String itemId = (String) invocation.getArguments()[1];
				workItem = new WorkItem(itemId,workItem.getTitle(),workItem.getStatus(),workItem.getUserId(),issue1.getId());
				return workItem;
			}		
		}).when(issueRepository).assignToWorkItem(issue, "1");
		
		doAnswer(new Answer<WorkItem>(){
			@Override
			public WorkItem answer(InvocationOnMock invocation) throws Throwable {
				String itemId = (String) invocation.getArguments()[0];
				String status = (String) invocation.getArguments()[1];
				workItem = new WorkItem(itemId,workItem.getTitle(),status,workItem.getUserId(),workItem.getIssueId());
				return workItem;
			}	
		}).when(workItemRepository).changeStatus("1", "Unstarted") ;
			
		issueService.assignToWorkItem(issue, "1");
		verify(issueRepository,times(1)).assignToWorkItem(issue, "1");
		verify(workItemRepository,times(1)).changeStatus("1", "Unstarted");	
		assertNotNull(workItem.getIssueId());
		assertEquals(workItem.getStatus(),"Unstarted");
	}
	
	@Test(expected=ServiceException.class)
	public void canNotAssignToWorkItemNotDoneTest() throws RepositoryException{
		WorkItem workItem = new WorkItem("2","Test work item","Started",null,null);
		when(workItemRepository.getById("2")).thenReturn(workItem);
		issueService.assignToWorkItem(issue, "2");
	}
	
	@Test 
	public void canGetAllItemWithIssue() throws RepositoryException{
		List<WorkItem> itemList = new ArrayList<>();
		itemList.add(new WorkItem("1","work item 1","Start",null,null));
		itemList.add(new WorkItem("2","work item 2","Unstarted",null,null));
		itemList.add(new WorkItem("3","work item 3","Done",null,"1"));
		itemList.add(new WorkItem("3","work item 3","Done",null,"2"));
		when(issueRepository.getAllItemsWithIssue()).thenAnswer(new Answer<List<WorkItem>>(){
			@Override
			public List<WorkItem> answer(InvocationOnMock invocation) throws Throwable {
				List<WorkItem> result=new ArrayList<>();
				for (WorkItem item:itemList){
					if (item.getIssueId()!=null){
						result.add(item);
					}
				}
				return result;
			}
			
		});
		List<WorkItem> resultList = issueService.getAllItemsWithIssue();
		assertEquals(resultList.size(),2);
	}
	
}
