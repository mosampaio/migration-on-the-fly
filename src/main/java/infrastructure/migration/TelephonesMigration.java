package infrastructure.migration;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

public class TelephonesMigration implements Migration {
    @Override
    public void migrate(DBObject dbObject) {
        final String telephone = (String) dbObject.get("telephone");

        dbObject.put("telephones", new BasicDBList() {{
            add(telephone);
        }});

        dbObject.removeField("telephone");
    }
}
