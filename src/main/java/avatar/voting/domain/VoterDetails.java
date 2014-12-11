package avatar.voting.domain;

public class VoterDetails {

    private Long id;
    private String email;

    public VoterDetails(Voter voter) {
        this.id = voter.getId();
        this.email = voter.getEmail();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
