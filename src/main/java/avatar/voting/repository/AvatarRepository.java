package avatar.voting.repository;

import avatar.voting.domain.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    Avatar findByCandidateEmail(String candidateEmail);

    @Query("FROM Avatar WHERE id = (SELECT MAX(id) FROM Avatar WHERE suggestion_open = true OR vote_open = true)")
    Avatar findLast();
}
