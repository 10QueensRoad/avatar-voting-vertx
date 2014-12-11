package avatar.voting.domain;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class Suggestion {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 500)
    private String name;

    @OneToOne
    private Voter suggester;

    @ManyToOne
    private Avatar avatar;

    @OneToMany(mappedBy = "suggestion", cascade = CascadeType.ALL)
    private Set<Voter> voters = new LinkedHashSet<>();

    private Suggestion() {
    }

    public Suggestion(String name, Voter suggester, Avatar avatar) {
        this.name = name;
        this.suggester = suggester;
        this.avatar = avatar;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getVotes() {
        return getVoters().size();
    }

    public Set<Voter> getVoters() {
        return voters;
    }

    public void voted(Voter voter) {
        if (voters.contains(voter)) {
            voter.undo(this);
        } else {
            voter.voted(this);
        }
    }

    void addVoter(Voter voter) {
        this.voters.add(voter);
    }

    void removeVoter(Voter voter) {
        this.voters.remove(voter);
    }

    public Long getSuggesterId() {
        return suggester.getId();
    }

    public String getSuggesterEmail() {
        return suggester.getEmail();
    }
}
