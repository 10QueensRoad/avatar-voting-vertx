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

    public Voter(String email) {
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Voter voter = (Voter) o;

        if (id != null ? !id.equals(voter.id) : voter.id != null) return false;

        return true;
    }
}
