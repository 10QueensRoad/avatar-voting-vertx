package avatar.voting.domain;

public class AvatarDetails {
    private Long id;
    private String name;
    private String candidate;
    private String candidateEmail;
    private Boolean suggestionOpen;
    private String suggestionOpenString;
    private Boolean voteOpen;
    private String voteOpenString;
    private Integer numberOfVoters;
    private Integer numberOfSuggestions;

    public AvatarDetails(Avatar avatar) {
        this.id = avatar.getId();
        this.name = avatar.getName();
        this.candidate = avatar.getCandidate();
        this.candidateEmail = avatar.getCandidateEmail();
        this.suggestionOpen = avatar.getSuggestionOpen();
        this.suggestionOpenString = suggestionOpen ? "open" : "close";
        this.voteOpen = avatar.getVoteOpen();
        this.voteOpenString = voteOpen ? "open" : "close";
        this.numberOfVoters = avatar.getVoters().size();
        this.numberOfSuggestions = avatar.getSuggestions().size();
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

    public String getSuggestionOpenString() {
        return suggestionOpenString;
    }

    public Boolean getVoteOpen() {
        return voteOpen;
    }

    public String getVoteOpenString() {
        return voteOpenString;
    }

    public Integer getNumberOfVoters() {
        return numberOfVoters;
    }

    public Integer getNumberOfSuggestions() {
        return numberOfSuggestions;
    }
}
