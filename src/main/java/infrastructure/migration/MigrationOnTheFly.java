package infrastructure.migration;

import com.mongodb.DBObject;

import java.util.Optional;
import java.util.SortedMap;

public class MigrationOnTheFly {

    public static final String MIGRATION_VERSION_FIELD_NAME = "migrationVersion";

    private SortedMap<Integer, Migration> migrations;
    private int currentMigrationVersion;

    public MigrationOnTheFly(SortedMap<Integer, Migration> migrations, int currentMigrationVersion) {
        this.migrations = migrations;
        this.currentMigrationVersion = currentMigrationVersion;
    }

    public void migrate(final DBObject dbObject) {
        final Integer migrationVersion = (Integer) Optional
                .ofNullable(dbObject.get(MIGRATION_VERSION_FIELD_NAME))
                .orElse(0);

        migrations.entrySet()
                .stream()
                .filter(input -> input.getKey() > migrationVersion)
                .forEachOrdered(migrationEntry -> {
                    migrationEntry.getValue().migrate(dbObject);

                });
    }

    public void migrateVersion(DBObject dbObject) {
        dbObject.put(MIGRATION_VERSION_FIELD_NAME, currentMigrationVersion);
    }
}

