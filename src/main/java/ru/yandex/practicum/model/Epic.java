package ru.yandex.practicum.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author vsmordvincev
 */
public class Epic extends Task {
    private final List<Integer> subtaskIds;
    private LocalDateTime endTime;

    public Epic(String title, String description) {
        super(title, description, Status.NEW, null, null);
        this.subtaskIds = new ArrayList<>();
        this.endTime = null;
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }


    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);

    }

    public void removeSubtask(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);

    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }


    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                ", endTime=" + endTime +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

}

