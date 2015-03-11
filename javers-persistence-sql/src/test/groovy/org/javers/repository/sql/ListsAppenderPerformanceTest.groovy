package org.javers.repository.sql
import org.h2.tools.Server
import org.javers.core.Javers
import org.javers.core.metamodel.object.InstanceIdDTO
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode
import java.sql.Connection
import java.sql.DriverManager

import static org.javers.core.JaversBuilder.javers

class ListsAppenderPerformanceTest extends Specification {

    Connection dbConnection;

    Javers javers;
    Javers levenshteinJavers;

    def setup() {
        Server.createTcpServer().start()
        dbConnection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/mem:test")
//        dbConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pawel.szymczyk", "pawel.szymczyk", "")

        dbConnection.setAutoCommit(false)

        def connectionProvider = { dbConnection } as ConnectionProvider

        def sqlRepository = SqlRepositoryBuilder
                .sqlRepository()
                .withConnectionProvider(connectionProvider)
                .withDialect(DialectName.POSTGRES).build()
        javers = javers().registerJaversRepository(sqlRepository).build()
        levenshteinJavers = javers().registerJaversRepository(sqlRepository).withLevensteinListAppender().build()

        clearTables()
    }

    @Unroll
    def "should find change history when all list elements changed"() {
        given:
        PerformanceEntity entity = new PerformanceEntity();
        entity.generateRandomList(listElements)

        when:
        commits.times {
            entity.changeAll()
            javers.commit("author", entity)
            dbConnection.commit()
        }

        def start = System.currentTimeMillis()
        javers.getChangeHistory(InstanceIdDTO.instanceId(entity.id, PerformanceEntity.class), 1000)
        stop("lists_with_random_int.txt", "Dummy", start, listElements, commits)

        then:
        true

        where:
        listElements | commits
        200          | 100
        400          | 100
        600          | 100
        800          | 100
        1000         | 100
        3000         | 100
        5000         | 100
        200          | 1000
        400          | 1000
        600          | 1000
        800          | 1000
        1000         | 1000
        3000         | 1000
        5000         | 1000
    }

    @Unroll
    def "Levenstein should find change history when all list elements changed"() {
        given:
        PerformanceEntity entity = new PerformanceEntity();
        entity.generateRandomList(listElements)

        when:
        commits.times {
            entity.changeAll()
            levenshteinJavers.commit("author", entity)
            dbConnection.commit()
        }

        def start = System.currentTimeMillis()
        levenshteinJavers.getChangeHistory(InstanceIdDTO.instanceId(entity.id, PerformanceEntity.class), 1000)
        stop("lists_with_random_int.txt", "Levenstein", start, listElements, commits)

        then:
        true

        where:
        listElements | commits
        200          | 100
        400          | 100
        600          | 100
        800          | 100
        1000         | 100
        3000         | 100
        5000         | 100
        200          | 1000
        400          | 1000
        600          | 1000
        800          | 1000
        1000         | 1000
        3000         | 1000
        5000         | 1000
    }

    @Unroll
    def "should find change history when firstly element remove"() {
        given:
        PerformanceEntity entity = new PerformanceEntity();
        entity.generateRandomList(listElements)

        when:
        commits.times {
            entity.removeElement(10)
            javers.commit("author", entity)
            dbConnection.commit()
        }

        def start = System.currentTimeMillis()
        javers.getChangeHistory(InstanceIdDTO.instanceId(entity.id, PerformanceEntity.class), 1000)
        stop("lists_with_random_int_remove.txt", "Dummy", start, listElements, commits)

        then:
        true

        where:
        listElements | commits
        200          | 100
        400          | 100
        600          | 100
        800          | 100
        1000         | 100
        3000         | 100
        5000         | 100
    }

    @Unroll
    def "Levenstein should find change history when firstly element remove"() {
        given:
        PerformanceEntity entity = new PerformanceEntity();
        entity.generateRandomList(listElements)

        when:
        commits.times {
            entity.removeElement(10)
            levenshteinJavers.commit("author", entity)
            dbConnection.commit()
        }

        def start = System.currentTimeMillis()
        levenshteinJavers.getChangeHistory(InstanceIdDTO.instanceId(entity.id, PerformanceEntity.class), 1000)
        stop("lists_with_random_int_remove.txt", "Levenstein", start, listElements, commits)

        then:
        true

        where:
        listElements | commits
        200          | 100
        400          | 100
        600          | 100
        800          | 100
        1000         | 100
        3000         | 100
        5000         | 100
    }


    def stop(String fileName, String message, long start, int elements, int commits) {
        def stop = System.currentTimeMillis()

        def opAvg = (stop - start) / commits

        def f = new File(fileName)

        f.append("""$message, elements in list: $elements, commits: $commits total time: """
                + round(stop - start) + """ ms, average $opAvg\n""")
    }

    String round(def what) {
        new BigDecimal(what).setScale(2, RoundingMode.HALF_UP).toString()
    }

    def produce(int startingId, int n) {

        PerformanceEntity root = new PerformanceEntity(id: startingId)
        root.generateRandomList()

        def range = startingId + 1..startingId + n
        def children = range.collect {
            new PerformanceEntity(id: it)
        }

        root.refs = children
        root
    }

    def clearTables() {
        execute("delete  from jv_snapshot;")
        execute("delete  from jv_commit;")
        execute("delete  from jv_global_id;")
        execute("delete  from jv_cdo_class;")
    }

    def execute(String sql) {
        def stmt = dbConnection.createStatement()
        stmt.executeUpdate(sql)
        stmt.close()
    }

}