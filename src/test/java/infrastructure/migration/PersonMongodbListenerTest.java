package infrastructure.migration;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import domain.Person;
import infrastructure.repository.PersonRepository;
import infrastructure.test.PersonMongodbListener;
import infrastructure.test.PersonWithOldSchemaRepository;
import infrastructure.test.TestMigrationConfiguration;
import infrastructure.test.TestMongoConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestMongoConfiguration.class, TestMigrationConfiguration.class})
public class PersonMongodbListenerTest {

    @Rule
    public FongoRule fongo = new FongoRule();
    private DB db;
    private DBCollection collection;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private PersonWithOldSchemaRepository personWithOldSchemaRepository;

    @Before
    public void setUp() {
        db = fongo.getDB();
        collection = db.getCollection(Person.COLLECTION_NAME);
    }

    @Test
    public void shouldMigrateOnRead() {
        //given
        final String id = UUID.randomUUID().toString();
        final String telephone = "14159363485";
        final PersonMongodbListener personWithOldSchema = new PersonMongodbListener(id, "Marcos", telephone);
        personWithOldSchemaRepository.save(personWithOldSchema);

        //when
        final Person person = personRepository.findOne(id);

        //then
        assertThat(person.getTelephones(), is(asList(telephone)));

    }
}
