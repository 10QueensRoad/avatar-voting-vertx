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

    @OneToMany(mappedBy = "suggestion")
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
        return voters.size();
    }

    public void voted(Voter voter) {
        if (voters.contains(voter)) {
            throw new RuntimeException("Already voted!");
        }
        this.voters.add(voter);
    }

    public String getSuggesterEmail() {
        return suggester.getEmail();
    }
}
