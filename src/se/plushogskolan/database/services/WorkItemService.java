package se.plushogskolan.database.services;

import java.util.List;

import se.plushogskolan.database.model.User;
import se.plushogskolan.database.model.WorkItem;
import se.plushogskolan.database.model.WorkItemStatus;
import se.plushogskolan.database.repository.ServiceException;
import se.plushogskolan.database.repository.UserRepository;
import se.plushogskolan.database.repository.WorkItemRepository;
import se.plushogskolan.database.repository.mysql.MySQLUserRepository;

public class WorkItemService {

	WorkItemRepository wir;

	public WorkItemService(WorkItemRepository wir) {
		this.wir = wir;
	}

	public void addWorkItem(WorkItem workItem) {
		wir.addWorkItem(workItem);
	}

	public void changeStatus(String id, WorkItemStatus status) {

		wir.changeStatus(id, status.toString());
	}

	public void delete(String id) {
		wir.delete(id);
	}

	public void assignItemToUser(String itemId, String userId) {
		UserRepository userRepository = new MySQLUserRepository();
		UserService us = new UserService(userRepository);
		User user = us.getUserById(userId);
		if ((user != null) && (user.getStatus().equals("Inactive"))) {
			throw new ServiceException("Assign workitem to user failed, user is inactive");
		} else if (user == null) {
			wir.assignItemToUser(itemId, userId);
			return;
		}
		List<WorkItem> items = this.wir.getAllByUser(userId);
		if (items.size() < 5) {
			this.wir.assignItemToUser(itemId, userId);
		} else {
			throw new ServiceException("Assign work item to user failed, user has already 5 workitems.");
		}
	}

	public List<WorkItem> getAllByStatus(WorkItemStatus status) {
		return this.wir.getAllByStatus(status.toString());
	}

	public List<WorkItem> getAllByTeam(String teamId) {
		return this.wir.getAllByTeam(teamId);
	}

	public List<WorkItem> getAllByUser(String id) {
		return this.wir.getAllByUser(id);
	}

	public WorkItem getById(String id) {
		return this.wir.getById(id);
	}
}
