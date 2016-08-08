import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.PerformsWrites;
import org.neo4j.procedure.Procedure;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.neo4j.helpers.collection.MapUtil.stringMap;

/**
 * Created by shinsuke-abe on 2016/08/08.
 */
public class FullTextIndex {
    // 全文検索インデックスの設定
    private static final Map<String, String> FULL_TEXT = stringMap(IndexManager.PROVIDER, "lucene", "type", "fulltext");

    // このクラスで実行されるすべてのプロシジャでコンテキストとして必要になるフィールド
    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    // 全文検索インデックスのサーチを自力で行う検索プロシジャ
    // 戻り値はレコードのStreamで、レコードはプロシジャごとに定義する
    // publicでnon-finalなフィールドを持つクラスでなければダメ > プリミティブとかでは返せない
    // プロシジャのパラメータ名はNameアノテーションで指定
    // パラメータに使えるのは、String,Long,Double,Number,Boolean,Map,List,Object
    @Procedure("example.search") // プロシジャのnamespace指定
    @PerformsWrites
    public Stream<SearchHit> search(@Name("label") String label, @Name("query") String query) {
        String index = indexName(label);

        // インデックスがなければ空Streamを返す
        if(!db.index().existsForNodes(index)) {
            log.debug("Skipping index query since index does not exist: `%s`", index);
            return Stream.empty();
        }

        return db.index()
                .forNodes(index)
                .query(query)
                .stream()
                .map(SearchHit::new);
    }

    // 全文検索インデックスをマニュアルで作り直すプロシジャ
    // 戻り値はvoidにしてもいい
    @Procedure("example.index")
    @PerformsWrites
    public void index(@Name("nodeId") long nodeId, @Name("properties") List<String> propKeys) {
        Node node = db.getNodeById(nodeId);

        Set<Map.Entry<String, Object>> properties = node.getProperties(propKeys.toArray(new String[0])).entrySet();

        for(Label label: node.getLabels()) {
            Index<Node> index = db.index().forNodes(indexName(label.name()), FULL_TEXT);

            index.remove(node);

            for(Map.Entry<String, Object> property: properties) {
                index.add(node, property.getKey(), property.getValue());
            }
        }
    }

    // 検索結果のレコードマッピングを返すためのクラス
    // レコードに使えるタイプはLong,Double,Number,Boolean,Node,Relationship,Path,Map,List,Object
    // Node,Relationship,PathはNeo4jのクラス
    public static class SearchHit {
        public long nodeId;

        public SearchHit(Node node) {
            this.nodeId = node.getId();
        }
    }

    private String indexName(String label) {
        return "label-" + label;
    }
}
