package avatar.voting.domain;

import javax.persistence.*;

@Entity
public class Voter {

    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String email;

    @ManyToOne
    private Avatar avatar;

    @ManyToOne
    private Suggestion suggestion;

    private Voter() {
    }

    public Voter(Avatar avatar, String email) {
        this.avatar = avatar;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void voted(Suggestion suggestion) {
        if (hasVoted()) {
            throw new RuntimeException("Already voted!");
        }
        suggestion.addVoter(this);
        this.suggestion = suggestion;
    }

    public void undo(Suggestion suggestion) {
        if (!suggestion.getId().equals(this.suggestion.getId())) {
            throw new RuntimeException("Unable to undo with wrong suggestion!");
        }
        suggestion.removeVoter(this);
        this.suggestion = null;
    }

    private boolean hasVoted() {
        return suggestion != null;
    }
}
