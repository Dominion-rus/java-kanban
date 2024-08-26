package org.practicum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vsmordvincev
 */
public class Epic extends Task{
    private final List<Integer> subtaskIds;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        this.subtaskIds = new ArrayList<>();
    }

      public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    public void updateStatus(List<Subtask> subtasks){
        boolean allDone = true;
        boolean allNew = true;

        if (subtaskIds.isEmpty() || subtasks.isEmpty()) {
            this.status = Status.NEW;
            return;
        }
        for (Subtask subtask : subtasks) {
            if (subtaskIds.contains(subtask.getId())) {
                if (subtask.getStatus() != Status.DONE) {
                    allDone = false;
                }
                if (subtask.getStatus() != Status.NEW) {
                    allNew = false;
                }
            }
        }
        if (allDone) {
            this.status = Status.DONE;
        } else if (allNew) {
            this.status = Status.NEW;
        } else {
            this.status = Status.IN_PROGRESS;
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                '}';
    }

}

