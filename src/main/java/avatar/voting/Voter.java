package avatar.voting;

public class Voter {

    private Long id;
    private String email;

    public Voter(String email) {
        this.email = email;
    }

    public Voter(Long id, String email) {
        this.id = id;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
