import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.PerformsWrites;
import org.neo4j.procedure.Procedure;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by shinsuke-abe on 2016/08/08.
 */
public class ListNearbyScore {
    @Context
    public GraphDatabaseService db;

    @Context
    public Log log;

    @Procedure("myprop.list_nearby_sore")
    @PerformsWrites
    public Stream<CalculatedScore> listNearbyScore(@Name("baseList") List<String> baseList, @Name("targetList") List<String> targetList) {
        return Stream.of(new CalculatedScore(baseList.stream().sorted().filter(item -> targetList.contains(item)).count()));
    }

    public static class CalculatedScore {
        public Long score;

        public CalculatedScore(Long score) {
            this.score = score;
        }
    }
}
