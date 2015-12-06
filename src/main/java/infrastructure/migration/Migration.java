package infrastructure.migration;

import com.mongodb.DBObject;

public interface Migration {
    void migrate(DBObject dbObject);
}
