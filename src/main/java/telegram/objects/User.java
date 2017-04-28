package telegram.objects;

public class User {
    private String id;
    private String alias;
    private int state;

    public User(String id, String alias, int state) {
        this.id = id;
        this.alias = alias;
        this.state = state;
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

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setState(int state) {
        this.state = state;
    }
}
