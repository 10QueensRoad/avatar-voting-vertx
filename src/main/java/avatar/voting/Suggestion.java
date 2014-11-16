package avatar.voting;

public class Suggestion {

    private Long id;

    private String name;

    private Integer votes;

    public Suggestion(String name) {
        this.name = name;
        this.votes = 0;
    }

    public Suggestion(Long id, String name, Integer votes) {
        this.id = id;
        this.name = name;
        this.votes = votes;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getVotes() {
        return votes;
    }

    public void voted() {
        ++this.votes;
    }
}
