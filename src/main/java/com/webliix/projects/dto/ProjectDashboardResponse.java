package com.webliix.projects.dto;

import lombok.Data;

@Data
public class ProjectDashboardResponse {

    private long totalProjects;
    private long planningProjects;
    private long inProgressProjects;
    private long completedProjects;
    private long cancelledProjects;
    private long overdueProjects;
    private long totalTasks;
    private long completedTasks;
    private long overdueTasks;
    private long activeMilestones;
}

