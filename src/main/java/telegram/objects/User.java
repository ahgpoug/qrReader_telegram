package telegram.objects;

public class User {
    private String id;
    private String alias;
    private int state;
    private String taskId;

    public User(String id, String alias, int state, String taskId) {
        this.id = id;
        this.alias = alias;
        this.state = state;
        this.taskId = taskId;
    }

    public String getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public int getState() {
        return state;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
