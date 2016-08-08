import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import static org.hamcrest.CoreMatchers.is;
import static org.neo4j.driver.v1.Values.parameters;
import static org.junit.Assert.assertThat;

/**
 * Created by shinsuke-abe on 2016/08/08.
 */
public class ManualFullTextIndexTest {
    // テストしたいProcedureクラスを指定
    @Rule
    public Neo4jRule neo4j = new Neo4jRule().withProcedure(FullTextIndex.class);

    @Test
    public void shouldAllowIndexingAndFindingANode() throws Throwable {
        // tryブロックでテスト後に確実にドライバーを閉じる
        try(Driver driver = GraphDatabase.driver(neo4j.boltURI(), Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig())) {
            // Neo4jのセッション開始
            Session session = driver.session();

            // データベースを作る
            long nodeId = session.run("CREATE (p:User {name:'Brookreson'}) RETURN id(p)")
                    .single()
                    .get(0).asLong();

            // 前準備となるプロシジャをコールする
            // プロシジャの引数はnodeIDとテキストのリスト
            session.run("CALL example.index({id}, ['name'])", parameters("id", nodeId));

            // テストするプロシジャのコール
            StatementResult result = session.run("CALL example.search('User', 'name:Brook*')");
            assertThat(result.single().get("nodeId").asLong(), is(nodeId));
        }
    }
}
