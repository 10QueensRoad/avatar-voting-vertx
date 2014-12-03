package avatar.voting.domain;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Entity
public class Avatar {

    @Id
    @GeneratedValue
    private Long id;

    @Column(length = 500, nullable = true)
    private String name;

    @Column
    private String candidate;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Column(name = "suggestion_open")
    private Boolean suggestionOpen;

    @Column(name = "vote_open")
    private Boolean voteOpen;

    @OneToMany(mappedBy = "avatar", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Voter> voters = new LinkedHashSet<>();

    @OneToMany(mappedBy = "avatar", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Suggestion> suggestions = new LinkedHashSet<>();

    private Avatar() {
    }

    public Avatar(String candidate, String candidateEmail) {
        this.candidate = candidate;
        this.candidateEmail = candidateEmail;
        this.suggestionOpen = true;
        this.voteOpen = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCandidate() {
        return candidate;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public Boolean getSuggestionOpen() {
        return suggestionOpen;
    }

    public Boolean getVoteOpen() {
        return voteOpen;
    }

    public Set<Voter> getVoters() {
        return voters;
    }

    public Set<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void addVoter(Voter voter) {
        this.voters.add(voter);
    }

    public Suggestion addSuggestion(Voter suggester, String suggestionName) {
        if (suggestions.stream().anyMatch(suggestion -> suggestion.getName().equals(suggestionName))) {
            throw new RuntimeException("Suggested name exists!");
        }
        Suggestion suggestion = new Suggestion(suggestionName, suggester, this);
        suggestions.add(suggestion);
        return suggestion;
    }

    public Suggestion vote(Suggestion suggestion, Voter voter) {
        Optional<Suggestion> nullableSuggestion = suggestions.stream().filter(suggest -> suggest.equals(suggestion))
                .findFirst();
        nullableSuggestion.ifPresent(suggest -> suggest.voted(voter));
        return nullableSuggestion.orElseThrow(() -> new RuntimeException("Suggestion does not exist"));
    }

    public void setSuggestionOpen(Boolean suggestionOpen) {
        this.suggestionOpen = suggestionOpen;
    }

    public void setVoteOpen(Boolean voteOpen) {
        this.voteOpen = voteOpen;
    }
}
