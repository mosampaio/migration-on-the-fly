package infrastructure.migration;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MigrationServiceTest {

    private static final String COLLECTION_NAME = "people";
    private static final String OPTIMISTIC_LOCK_VERSION_FIELD_NAME = "version";

    @Rule
    public FongoRule fongo = new FongoRule();

    private DBCollection collection;
    private DB db;
    private MigrationOnTheFly migrationOnTheFly;
    private MigrationService migrationService;
    private int currentMigrationVersion = 2;
    private SortedMap<Integer, Migration> migrations = new TreeMap<Integer, Migration>() {{
        put(1, dbObject -> dbObject.put("foo", "bar"));
        put(2, dbObject -> dbObject.put("newField", "newValue"));
    }};

    @Before
    public void setUp() throws Exception {
        db = fongo.getDB();
        collection = db.getCollection(COLLECTION_NAME);

        this.migrationOnTheFly = new MigrationOnTheFly(migrations, currentMigrationVersion);

        this.migrationService = new MigrationService(
                OPTIMISTIC_LOCK_VERSION_FIELD_NAME, migrationOnTheFly, db, COLLECTION_NAME, 2
        );
    }

    @Test
    public void shouldNotMigrateIfSomeoneHasAlreadyMigratedIt() throws Exception {
        //given
        collection.save(new BasicDBObject(){{
            put("_id", UUID.randomUUID().toString());
            put("version", 1);
        }});

        //migration loads
        final DBObject dbObjectUser1 = db.getCollection(COLLECTION_NAME).findOne();

        //user 2 loads
        final DBObject dbObjectUser2 = db.getCollection(COLLECTION_NAME).findOne();

        //user 2 updates before migration
        db.getCollection(COLLECTION_NAME).update(dbObjectUser2, new BasicDBObject("$inc", new BasicDBObject("version", 1)));

        //migration process
        migrationService.migrateAndUpdateInMongo(dbObjectUser1);

        //then
        final DBObject one = db.getCollection(COLLECTION_NAME).findOne();
        final Integer migrationField = (Integer) one.get("migrationVersion");
        assertThat(migrationField, is(nullValue()));
    }

    @Test
    public void shouldTryAgainIfSomeoneHasAlreadyMigratedIt() throws Exception {
        //given
        collection.save(new BasicDBObject(){{
            put("_id", UUID.randomUUID().toString());
            put("version", 1);
        }});

        //migration loads
        final DBObject dbObjectUser1 = db.getCollection(COLLECTION_NAME).findOne();

        //user 2 loads
        final DBObject dbObjectUser2 = db.getCollection(COLLECTION_NAME).findOne();

        //user 2 updates before migration
        db.getCollection(COLLECTION_NAME).update(dbObjectUser2, new BasicDBObject("$inc", new BasicDBObject("version", 1)));

        //migration process
        migrationService.run();

        //then
        final DBObject one = db.getCollection(COLLECTION_NAME).findOne();
        final Integer migrationField = (Integer) one.get("migrationVersion");
        assertThat(migrationField, is(nullValue()));
    }
}