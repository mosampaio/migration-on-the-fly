package infrastructure.migration;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MigrationOnTheFlyTest {
    private MigrationOnTheFly migrationOnTheFly;
    private SortedMap<Integer, Migration> migrations;
    private Integer currentMigrationVersionNumber;
    private Migration firstMigration;
    private Migration secondMigration;

    @Before
    public void setUp() throws Exception {
        migrations = new TreeMap<Integer, Migration>(){{
            put(1, dbObject -> dbObject.put("name", "Marcelo"));
            put(2, dbObject -> dbObject.put("hairColor", "white"));
        }};
        currentMigrationVersionNumber = 2;

        migrationOnTheFly = new MigrationOnTheFly(migrations, currentMigrationVersionNumber);
    }

    @Test
    public void shouldRunEveryMigrationIfDBObjectDoesNotHaveMigrationVersion() throws Exception {
        //given
        DBObject dbObject = new BasicDBObject();

        //when
        migrationOnTheFly.migrate(dbObject);

        //then
        assertThat(dbObject.get("name"), is("Marcelo"));
        assertThat(dbObject.get("hairColor"), is("white"));
    }

    @Test
    public void shouldOnlyRunMigrationsLaterThanDBObjectsMigrationVersion() throws Exception {
        //given
        DBObject dbObject = new BasicDBObject();
        dbObject.put("migrationVersion", 1);

        //when
        migrationOnTheFly.migrate(dbObject);

        //then
        assertThat(dbObject.get("name"), is(nullValue()));
        assertThat(dbObject.get("hairColor"), is("white"));
    }

    @Test
    public void shouldUpdateMigrationVersion() throws Exception {
        //given
        DBObject dbObject = new BasicDBObject();
        dbObject.put("migrationVersion", 1);

        //when
        migrationOnTheFly.migrateVersion(dbObject);

        //then
        assertThat((Integer) dbObject.get("migrationVersion"), is(currentMigrationVersionNumber));
    }
}

