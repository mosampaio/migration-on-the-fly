package infrastructure.test;

import infrastructure.migration.Migration;
import infrastructure.migration.MigrationOnTheFly;
import infrastructure.migration.PersonMongodbListener;
import infrastructure.migration.TelephonesMigration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.SortedMap;
import java.util.TreeMap;

@Configuration
@ComponentScan
public class TestMigrationConfiguration {

    private static final int CURRENT_MIGRATION_VERSION = 1;
    private SortedMap<Integer, Migration> migrations = new TreeMap<Integer, Migration>() {{
        put(1, new TelephonesMigration());
    }};

    @Bean
    public PersonMongodbListener personMongodbListener(
            MigrationOnTheFly migrationOnTheFly) {
        return new PersonMongodbListener(migrationOnTheFly, CURRENT_MIGRATION_VERSION);
    }

    @Bean
    public MigrationOnTheFly migrationOnTheFly() {
        return new MigrationOnTheFly(migrations, CURRENT_MIGRATION_VERSION);
    }

}
