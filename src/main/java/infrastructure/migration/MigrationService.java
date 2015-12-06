package infrastructure.migration;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

import java.util.logging.Logger;

import static infrastructure.migration.MigrationOnTheFly.MIGRATION_VERSION_FIELD_NAME;
import static org.springframework.util.StringUtils.isEmpty;

//this job will run on start up
public class MigrationService implements Runnable {

    private static final Logger log = Logger.getLogger(MigrationService.class.getName());

    private static final int LIMIT = 20;
    private static final String ID = "_id";
    private static final String $_AND = "$and";
    private static final String $_LT = "$lt";
    private static final String $_EXISTS = "$exists";
    private static final String $_OR = "$or";

    private String optimisticLockVersionFieldName;
    private MigrationOnTheFly migrationOnTheFly;
    private DB db;
    private String collectionName;
    private int currentMigrationVersion;

    public MigrationService(String optimisticLockVersionFieldName, MigrationOnTheFly migrationOnTheFly, DB db, String collectionName, int currentMigrationVersion) {
        this.optimisticLockVersionFieldName = optimisticLockVersionFieldName;
        this.migrationOnTheFly = migrationOnTheFly;
        this.db = db;
        this.collectionName = collectionName;
        this.currentMigrationVersion = currentMigrationVersion;
    }

    @Override
    public void run() {
        while (countToBeMigrated() > 0) {
            findToBeMigrated().iterator().forEachRemaining(dbObject -> {
                migrateAndUpdateInMongo(dbObject);
            });
        }
    }

    boolean migrateAndUpdateInMongo(DBObject dbObject) {
        migrationOnTheFly.migrate(dbObject);
        migrationOnTheFly.migrateVersion(dbObject);
        return updateInMongo(dbObject);
    }

    private boolean updateInMongo(DBObject entityDbObject) {
        WriteResult writeResult = db.getCollection(collectionName).update(queryToBeUpdated(entityDbObject), entityDbObject, false, false);
        boolean result = writeResult.getN() == 1;
        final Object id = entityDbObject.get(ID);
        if (result) {
            log.info(String.format("The entity with id %s was migrated to version %s", id, currentMigrationVersion));
        } else {
            log.warning(String.format("The entity with id %s was NOT migrated to version %s [optimistic lock version probably has changed]. Will try again later.", id, currentMigrationVersion));
        }
        return result;
    }

    private Iterable<DBObject> findToBeMigrated() {
        return db.getCollection(collectionName).find(queryToBeMigrated(currentMigrationVersion)).limit(LIMIT);
    }

    private long countToBeMigrated() {
        return db.getCollection(collectionName).count(queryToBeMigrated(currentMigrationVersion));
    }

    private BasicDBObject queryToBeUpdated(DBObject dbObject){
        if (isEmpty(optimisticLockVersionFieldName)) {
            return new BasicDBObject(ID, dbObject.get(ID));
        } else {
            return new BasicDBObject($_AND, new BasicDBList() {{
                add(new BasicDBObject(ID, dbObject.get(ID)));
                add(new BasicDBObject(optimisticLockVersionFieldName, dbObject.get(optimisticLockVersionFieldName)));
            }});
        }
    }

    private BasicDBObject queryToBeMigrated(final int lastMigrationVersion) {
        return new BasicDBObject($_OR, new BasicDBList() {{
            add(new BasicDBObject(MIGRATION_VERSION_FIELD_NAME, new BasicDBObject($_LT, lastMigrationVersion)));
            add(new BasicDBObject(MIGRATION_VERSION_FIELD_NAME, new BasicDBObject($_EXISTS, false)));
        }});
    }
}
