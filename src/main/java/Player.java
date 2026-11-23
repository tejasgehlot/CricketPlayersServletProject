public class Player
{
    private final String name;
    private final int age;
    private final String state;
    private final String playerType;

    public Player(String name, int age, String state, String playerType) {
        this.name = name;
        this.age = age;
        this.state = state;
        this.playerType = playerType;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getState() {
        return state;
    }

    public String getPlayerType() {
        return playerType;
    }

}