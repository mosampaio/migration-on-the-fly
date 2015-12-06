package infrastructure.test;

import org.springframework.data.repository.CrudRepository;

public interface PersonWithOldSchemaRepository extends CrudRepository<PersonWithOldSchema, String> {
}
