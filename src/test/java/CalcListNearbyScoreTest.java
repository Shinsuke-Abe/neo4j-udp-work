import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

/**
 * Created by shinsuke-abe on 2016/08/08.
 */
public class CalcListNearbyScoreTest {
    @Rule
    public Neo4jRule neo4j = new Neo4jRule().withProcedure(ListNearbyScore.class);

    @Test
    public void リストの一致する個数をスコアとして返す() throws Throwable {
        try(Driver driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig())) {
            List<String> list1 = new ArrayList<>(Arrays.asList("要素1", "要素2", "要素3"));
            List<String> list2 = new ArrayList<>(Arrays.asList("要素2", "要素3", "要素4", "要素5"));

            Session session = driver.session();

            StatementResult result = session.run("CALL myprop.list_nearby_score", parameters("baseList", list1, "targetList", list2));
            assertThat(result.single().get("score").asLong(), is(2L));
        }
    }
}
