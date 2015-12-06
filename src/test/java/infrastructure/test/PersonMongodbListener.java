package infrastructure.test;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "people")
public class PersonMongodbListener {
    private String id;
    private String name;
    private String telephone;

    public PersonMongodbListener(String id, String name, String telephone) {
        this.id = id;
        this.name = name;
        this.telephone = telephone;
    }
}
