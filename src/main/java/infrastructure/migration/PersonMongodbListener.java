package infrastructure.migration;

import domain.Person;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterLoadEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;

public class PersonMongodbListener extends AbstractMongoEventListener<Person> {

    private MigrationOnTheFly migrationOnTheFly;
    private int currentMigrationVersion;

    public PersonMongodbListener(MigrationOnTheFly migrationOnTheFly, int currentMigrationVersion) {
        this.migrationOnTheFly = migrationOnTheFly;
        this.currentMigrationVersion = currentMigrationVersion;
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Person> event) {
        this.migrationOnTheFly.migrateVersion(event.getDBObject());
    }

    @Override
    public void onAfterLoad(AfterLoadEvent<Person> event) {
        this.migrationOnTheFly.migrate(event.getDBObject());
    }
}
