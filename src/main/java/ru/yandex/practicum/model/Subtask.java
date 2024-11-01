package ru.yandex.practicum.model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * @author vsmordvincev
 */
public class Subtask extends Task {
    private final int epicId;

    public Subtask(String title, String description, Status status, int epicId, Duration duration,
                   LocalDateTime startTime) {
        super(title, description, status, duration, startTime);
        this.epicId = epicId;
    }

      public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", duration=" + getDuration() +
                ", startTime=" + getStartTime() +
                ", endTime=" + getEndTime() +
                ", epicId=" + epicId +
                '}';
    }

}
