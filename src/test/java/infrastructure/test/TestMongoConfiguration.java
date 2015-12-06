package infrastructure.test;

import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories({"infrastructure"})
public class TestMongoConfiguration extends AbstractMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "fongoDatabase";
    }

    public Fongo fongo() {
        return new Fongo("InMemoryMongo");
    }

    @Override
    public Mongo mongo() {
        return fongo().getMongo();
    }

    @Bean
    public DB fongoDB(MongoDbFactory mongoDbFactory) {
        return mongoDbFactory.getDb();
    }

}
