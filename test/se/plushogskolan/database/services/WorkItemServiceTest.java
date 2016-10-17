package se.plushogskolan.database.services;

import static org.junit.Assert.assertEquals;
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
public class WorkItemServiceTest {
	@Mock
	private UserRepository userRepository;
	@Mock
	private WorkItemRepository workItemRepository;
	@InjectMocks
	private WorkItemService workItemService;

	private static WorkItem workItem;

	@BeforeClass
	public static void setUp() {
		workItem = new WorkItem("1", "Work Item 1", "Unstarted", "1", null);
	}

	@Test
	public void canAddWorkItemTest() {
		workItemService.addWorkItem(workItem);
		verify(workItemRepository).addWorkItem(workItem);
	}

	@Test
	public void canChangeStatusTest() {
		workItemService.changeStatus(workItem.getId(), WorkItemStatus.Started);
		verify(workItemRepository).changeStatus(workItem.getId(), WorkItemStatus.Started.toString());
	}

	@Test
	public void canDeleteTest() {
		when(workItemRepository.getById(Mockito.anyString())).thenReturn(workItem);
		workItemService.delete("1");
		verify(workItemRepository).delete("1");
	}

	@Test
	public void canAssignItemToUserTest() throws RepositoryException {
		User user = new User("1", "Anna", "Karlsson", "AnnaKarlsson", "1", "Active");
		List<WorkItem> itemList = new ArrayList<>();
		itemList.add(new WorkItem("2", "Work Item 2", "Unstarted", "1", null));
		when(userRepository.getUserById("1")).thenReturn(user);
		when(workItemRepository.getAllByUser(user.getId())).thenReturn(itemList);
		workItemService.assignItemToUser(workItem.getId(), user.getId());
		verify(workItemRepository).assignItemToUser(workItem.getId(), user.getId());
	}

	@Test(expected = ServiceException.class)
	public void canNotAssignItemToInactiveUserTest() throws RepositoryException {
		User user = new User("1", "Anna", "Karlsson", "AnnaKarlsson", "1", "Inactive");
		when(userRepository.getUserById("1")).thenReturn(user);
		workItemService.assignItemToUser(workItem.getId(), user.getId());
	}

	@Test(expected = ServiceException.class)
	public void userCanHaveMax5ItemTest() throws RepositoryException {
		User user = new User("1", "Anna", "Karlsson", "AnnaKarlsson", "1", "Active");
		when(userRepository.getUserById(user.getId())).thenReturn(user);
		List<WorkItem> itemList = new ArrayList<WorkItem>();
		for (int i = 0; i < 5; i++) {
			itemList.add(new WorkItem(Integer.toString(i), "Work Item " + i, "Started", user.getId(), null));
		}
		when(workItemRepository.getAllByUser(user.getId())).thenReturn(itemList);
		workItemService.assignItemToUser(workItem.getId(), user.getId());
	}

	@Test
	public void canGetAllByStatus() {
		List<WorkItem> itemList = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			itemList.add(new WorkItem(Integer.toString(i), "WorkItem", "Unstarted", null, null));
		}
		for (int i = 0; i < 2; i++) {
			itemList.add(new WorkItem(Integer.toString(i + 3), "WorkItem", "Done", null, null));
		}
		when(workItemRepository.getAllByStatus("Done")).thenAnswer(new Answer<List<WorkItem>>() {

			@Override
			public List<WorkItem> answer(InvocationOnMock invocation) throws Throwable {
				List<WorkItem> result = new ArrayList<>();
				String status = (String) invocation.getArguments()[0];
				for (WorkItem item : itemList) {
					if (item.getStatus().equals(status)) {
						result.add(item);
					}
				}
				return result;
			}
		});
		List<WorkItem> result = workItemService.getAllByStatus(WorkItemStatus.Done);
		assertEquals(result.size(), 2);
	}

	@Test
	public void canGetWorkItemByTeam() {
		List<WorkItem> itemList = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			itemList.add(new WorkItem(Integer.toString(i), "WorkItem", "Unstarted", "1", null));
		}
		for (int i = 0; i < 2; i++) {
			itemList.add(new WorkItem(Integer.toString(i + 3), "WorkItem", "Done", "2", null));
		}
		for (int i = 0; i < 2; i++) {
			itemList.add(new WorkItem(Integer.toString(i + 5), "WorkItem", "Done", "3", null));
		}
		User user1 = new User("1", "fn", "ln", "username01", "2", "Active");
		when(workItemRepository.getAllByTeam("2")).thenAnswer(new Answer<List<WorkItem>>() {

			@Override
			public List<WorkItem> answer(InvocationOnMock invocation) throws Throwable {
				List<WorkItem> result = new ArrayList<>();
				for (WorkItem item : itemList) {
					if (item.getUserId().equals(user1.getId())) {
						result.add(item);
					}
				}
				return result;
			}
		});
		List<WorkItem> result = workItemService.getAllByTeam("2");
		assertEquals(result.size(), 3);
	}

}
