package avatar.voting.repository;

import avatar.voting.domain.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

}
