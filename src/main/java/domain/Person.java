package domain;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = Person.COLLECTION_NAME)
public class Person {
    public static final String COLLECTION_NAME = "people";

    private String id;
    private String name;
    private List<String> telephones;

    public Person(String id, String name, List<String> telephones){
        this.id = this.id;
        this.name = name;
        this.telephones = telephones;
    }

    public String getName() {
        return name;
    }

    public List<String> getTelephones() {
        return telephones;
    }
}
