package com.spring.task.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.object.MappingSqlQuery;
import org.springframework.stereotype.Repository;

import com.spring.task.boot.ApplicationInitializer;
import com.spring.task.dao.TaskDAO;
import com.spring.task.dao.UserDAO;
import com.spring.task.domain.Task;
import com.spring.task.domain.User;

@Repository("TaskJdbcDAO")
public class TaskJdbcDAO implements TaskDAO {

	private static final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);
	
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private RowMapper<Task> taskRowMapper;
	private SimpleJdbcCall createTaskStoredProc;
	private SimpleJdbcCall updateTaskStoredProc;
	private SimpleJdbcCall findTasksByStatusAssigneeIdStoredProc;
	private SimpleJdbcCall tasksCountFunction;
	private SimpleJdbcCall tasksByStatusStoredProc;
	private SimpleJdbcCall openTasksByAssigneeIdStoreProc;
	private UserDAO userDAO;

	@Autowired
	public TaskJdbcDAO(DataSource dataSource, @Qualifier("UserJdbcDAO") UserDAO userDAO) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.jdbcTemplate.setResultsMapCaseInsensitive(true);
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		this.createTaskStoredProc = new SimpleJdbcCall(dataSource).withProcedureName("CREATE_TASK").declareParameters(
				new SqlOutParameter("v_newID", Types.INTEGER), new SqlParameter("v_name", Types.VARCHAR),
				new SqlParameter("v_STATUS", Types.VARCHAR), new SqlParameter("v_priority", Types.INTEGER),
				new SqlParameter("v_createdUserId", Types.INTEGER), new SqlParameter("v_createdDate", Types.DATE),
				new SqlParameter("v_assignedUserId", Types.INTEGER), new SqlParameter("v_comment", Types.VARCHAR));
		this.updateTaskStoredProc = new SimpleJdbcCall(dataSource).withProcedureName("UPDATE_TASK").declareParameters(
				new SqlParameter("v_id", Types.INTEGER), new SqlParameter("v_name", Types.VARCHAR),
				new SqlParameter("v_STATUS", Types.VARCHAR), new SqlParameter("v_priority", Types.INTEGER),
				new SqlParameter("v_assignedUserId", Types.INTEGER), new SqlParameter("v_comment", Types.VARCHAR));

		this.tasksCountFunction = new SimpleJdbcCall(jdbcTemplate).withFunctionName("get_user_id");
		// this.tasksCountFunction = new
		// SimpleJdbcCall(jdbcTemplate).withFunctionName("GET_TASK_COUNT");
		// .declareParameters(new SqlParameter("v_status",
		// Types.VARCHAR)).withReturnValue();

		this.taskRowMapper = new RowMapper<Task>() {

			@Override
			public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
				Task task = new Task();
				task.setId(rs.getLong("id"));
				Long assigneeId = rs.getLong("assignee_user_id");
				if (assigneeId != null)
					task.setAssignee(userDAO.findById(assigneeId));
				task.setComments(rs.getString("comments"));
				task.setCompletedDate(rs.getDate("completed_date"));
				task.setCreatedBy(userDAO.findById(rs.getLong("created_user_id")));
				task.setCreatedDate(rs.getDate("created_date"));
				task.setName(rs.getString("name"));
				task.setPriority(rs.getInt("priority"));
				task.setStatus(rs.getString("status"));
				return task;
			}

		};

		this.tasksByStatusStoredProc = new SimpleJdbcCall(dataSource).withProcedureName("GET_TASKS_BY_STATUS")
				.returningResultSet("tasks", this.taskRowMapper);

		this.openTasksByAssigneeIdStoreProc = new SimpleJdbcCall(dataSource)
				.withProcedureName("GET_TASKS_BY_STATUS_ASSIGNEE_ID").returningResultSet("tasks", this.taskRowMapper);

		this.userDAO = userDAO;
	}

	@Override
	public void createTask(Task task) {

		SqlParameterSource inParams = new MapSqlParameterSource().addValue("v_name", task.getName())
				.addValue("v_STATUS", task.getStatus()).addValue("v_priority", task.getPriority())
				.addValue("v_createdUserId", task.getCreatedBy().getId())
				.addValue("v_createdDate", task.getCreatedDate())
				.addValue("v_assignedUserId", task.getAssignee() == null ? null : task.getAssignee().getId())
				.addValue("v_comment", task.getComments());

		Map<String, Object> out = createTaskStoredProc.execute(inParams);

		task.setId(Long.valueOf(out.get("v_newID").toString()));
	}

	@Override
	public Task findById(Long taskId) {
		MappingSqlQuery<Task> query = new MappingSqlQuery<Task>() {

			@Override
			protected Task mapRow(ResultSet rs, int rowNum) throws SQLException {
				return taskRowMapper.mapRow(rs, rowNum);
			}

		};
		query.setJdbcTemplate(jdbcTemplate);
		query.setSql("select id, name, status, priority, created_user_id, created_date"
				+ ", assignee_user_id, completed_date, comments from TASK where id = ?");
		query.declareParameter(new SqlParameter("id", Types.INTEGER));

		return query.findObject(taskId);

	}

	@Override
	public List<Task> findByAssignee(Long assigneeId) {
		MappingSqlQuery<Task> query = new MappingSqlQuery<Task>() {

			@Override
			protected Task mapRow(ResultSet rs, int rowNum) throws SQLException {
				return taskRowMapper.mapRow(rs, rowNum);
			}

		};
		query.setJdbcTemplate(jdbcTemplate);
		query.setSql("select id, name, status, priority, created_user_id, created_date"
				+ ", assignee_user_id, completed_date, comments from TASK where assignee_user_id = ?");
		query.declareParameter(new SqlParameter("id", Types.INTEGER));

		return query.execute(assigneeId);
	}

	@Override
	public List<Task> findByAssignee(String assigneeUserName) {
		User assignee = userDAO.findByUserName(assigneeUserName);
		return findByAssignee(assignee.getId());
	}

	@Override
	public List<Task> findAllTasks() {
		MappingSqlQuery<Task> query = new MappingSqlQuery<Task>() {

			@Override
			protected Task mapRow(ResultSet rs, int rowNum) throws SQLException {
				return taskRowMapper.mapRow(rs, rowNum);
			}
		};
		query.setJdbcTemplate(jdbcTemplate);
		query.setSql("select id, name, status, priority, created_user_id, created_date"
				+ ", assignee_user_id, completed_date, comments from task");
		// query.declareParameter(new SqlParameter("id", Types.INTEGER));

		return query.execute();
	}

	@Override
	public int findAllTasksCount() {
		String sql = "SELECT COUNT(*) AS Total FROM task";
		logger.debug(sql);
		int total = jdbcTemplate.queryForObject(sql, null, Integer.class);
		return total;

	}

	@Override
	public List<Task> findAllOpenTasks() {

		return (List<Task>) this.tasksByStatusStoredProc.execute("Open").get("tasks");
	}

	@Override
	public List<Task> findAllCompletedTasks() {
		return (List<Task>) this.tasksByStatusStoredProc.execute("Closed").get("tasks");
	}

	@Override
	public int findAllOpenTasksCount() {
		String sql = "SELECT COUNT(*) AS Total FROM task where status = 'Open'";
		logger.debug(sql);
		int total = jdbcTemplate.queryForObject(sql, null, Integer.class);
		return total;
	}

	@Override
	public int findAllCompletedTasksCount() {
		String sql = "SELECT COUNT(*) AS Total FROM task where status = 'Closed'";
		logger.debug(sql);
		int total = jdbcTemplate.queryForObject(sql, null, Integer.class);
		return total;
	}

	@Override
	public List<Task> findOpenTasksByAssignee(Long assigneeId) {

		SimpleJdbcCall procCall = new SimpleJdbcCall(jdbcTemplate.getDataSource())
				.withProcedureName("GET_TASKS_BY_STATUS_ASSIGNEE_ID").returningResultSet("RESULT", taskRowMapper);

		SqlParameterSource inParams = new MapSqlParameterSource().addValue("v_status", "Open").addValue("v_assignee_id",
				assigneeId);

		Map<String, Object> out = procCall.execute(inParams);
		return (List<Task>) out.get("RESULT");
	}

	@Override
	public List<Task> findOpenTasksByAssignee(String assigneeUserName) {
		User assignee = userDAO.findByUserName(assigneeUserName);
		return findOpenTasksByAssignee(assignee.getId());
	}

	@Override
	public List<Task> findCompletedTasksByAssignee(Long assigneeId) {
		SimpleJdbcCall procCall = new SimpleJdbcCall(jdbcTemplate.getDataSource())
				.withProcedureName("GET_TASKS_BY_STATUS_ASSIGNEE_ID").returningResultSet("RESULT", taskRowMapper);

		SqlParameterSource inParams = new MapSqlParameterSource().addValue("v_status", "Closed").addValue("v_assignee_id",
				assigneeId);

		Map<String, Object> out = procCall.execute(inParams);
		return (List<Task>) out.get("RESULT");
	}

	@Override
	public List<Task> findCompletedTasksByAssignee(String assigneeUserName) {
		User assignee = userDAO.findByUserName(assigneeUserName);
		return findCompletedTasksByAssignee(assignee.getId());
	}

	@Override
	public void deleteTask(Task task) {
		String sql = "DELETE FROM task WHERE id= :v_id";
		SqlParameterSource inParams = new MapSqlParameterSource().addValue("v_id", task.getId());
		namedParameterJdbcTemplate.update(sql, inParams);
	}

	@Override
	public void updateTask(Task task) {
		SqlParameterSource inParams = new MapSqlParameterSource().addValue("v_id", task.getId())
				.addValue("v_name", task.getName()).addValue("v_STATUS", task.getStatus())
				.addValue("v_priority", task.getPriority())
				.addValue("v_assignedUserId", task.getAssignee() == null ? null : task.getAssignee().getId())
				.addValue("v_comment", task.getComments());

		updateTaskStoredProc.execute(inParams);

	}

	@Override
	public void addFile(Long taskId, String fileName) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteFile(Long taskId, Long fileId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAllFiles(Long taskId) {
		// TODO Auto-generated method stub

	}

}
