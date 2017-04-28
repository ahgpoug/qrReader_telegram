package telegram.objects;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String taskName;
    private String groupName;
    private String expDate;


    public Task(String id, String taskName, String groupName, String expDate){
        this.id = id;
        this.taskName = taskName;
        this.groupName = groupName;
        this.expDate = expDate;
    }

    public String getId() {
        return id;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getExpDate() {
        return expDate;
    }
}
