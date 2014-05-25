package org.javers.repository.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import org.javers.common.collections.Function;
import org.javers.common.collections.Lists;
import org.javers.common.collections.Optional;
import org.javers.core.commit.Commit;
import org.javers.core.commit.CommitId;
import org.javers.core.json.JsonConverter;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.object.GlobalCdoId;
import org.javers.core.metamodel.object.InstanceId;
import org.javers.repository.api.JaversRepository;
import org.javers.repository.mongo.model.MongoCdoSnapshots;
import org.javers.repository.mongo.model.MongoChange;
import org.javers.repository.mongo.model.MongoCommit;
import org.javers.repository.mongo.model.MongoHeadId;
import org.javers.repository.mongo.model.MongoSnapshot;

import java.util.Collections;
import java.util.List;

public class MongoRepository implements JaversRepository {

    private DB mongo;
    private ModelMapper2 mapper;
    private JsonConverter jsonConverter;

    public MongoRepository(DB mongo) {
        this.mongo = mongo;
    }

    public MongoRepository(DB mongo, JsonConverter jsonConverter) {
        this.mongo = mongo;
        this.mapper = new ModelMapper2(jsonConverter);
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void persist(Commit commit) {
        persistCommit(commit);
        persistSnapshots(commit);
        persistChanges(commit);
        persistHeadId(commit);
    }

    private void persistCommit(Commit commit) {
        MongoCommit mongoCommit = mapper.toMongoCommit(commit);

//        mongo.getCollection(MongoCommit.COLLECTION_NAME)
//                .save(mongoCommit);
    }

    private void persistSnapshots(Commit commit) {

        DBCollection collection = mongo.getCollection(MongoCdoSnapshots.COLLECTION_NAME);

        for (CdoSnapshot snapshot: commit.getSnapshots()) {

            BasicDBObject globalCdoId = new BasicDBObject(MongoCdoSnapshots.GLOBAL_CDO_ID,
                    BasicDBObjectBuilder.start()
                            .add("entity", snapshot.getGlobalId().getCdoClass().getName())
                            .add("cdoId", snapshot.getGlobalId().getCdoId())
                            .get());

            DBObject mongoCdoSnapshots = collection
                    .findOne(globalCdoId);

            if (mongoCdoSnapshots == null) {
                collection.save(mapper.toMongoSnaphot(snapshot));
            } else {
                MongoCdoSnapshots snapshots = new MongoCdoSnapshots(mongoCdoSnapshots);
                snapshots.addSnapshot(new MongoSnapshot((DBObject) JSON.parse(jsonConverter.toJson(snapshot))));
                collection.findAndModify(globalCdoId, snapshots);
            }
        }
    }

    private void persistChanges(Commit commit) {
        MongoChange mongoChange = mapper.toMongoChange(commit);

//        mongo.getCollection(MongoChange.COLLECTION_NAME)
//                .save(mongoChange);

    }

    private void persistHeadId(Commit commit) {
        DBCollection headIdCollection = mongo.getCollection(MongoHeadId.COLLECTION_NAME);

        DBObject oldHeadId = headIdCollection.findOne();
        MongoHeadId newHeadId = new MongoHeadId(jsonConverter.toJson(commit.getId()));

        if (oldHeadId == null) {
            headIdCollection.save(newHeadId);
        } else {
            headIdCollection.findAndModify(oldHeadId, newHeadId);
        }
    }

    @Override
    public List<CdoSnapshot> getStateHistory(GlobalCdoId globalId, int limit) {
        return getStateHistory(new BasicDBObject("globalCdoId",
                (DBObject) JSON.parse(jsonConverter.toJson(globalId))), limit);
    }

    @Override
    public List<CdoSnapshot> getStateHistory(InstanceId.InstanceIdDTO dtoId, int limit) {
        DBObject dbObject = new BasicDBObject("globalCdoId", BasicDBObjectBuilder.start()
                .append("cdoId", dtoId.getCdoId())
                .append("entity", dtoId.getEntity().getName()).get());

        return getStateHistory(dbObject, limit);
    }

    private List<CdoSnapshot> getStateHistory(DBObject id, int limit) {

        DBObject mongoCdoSnapshots = mongo.getCollection(MongoCdoSnapshots.COLLECTION_NAME)
                .findOne(id);

        if (mongoCdoSnapshots == null) {
            return Collections.EMPTY_LIST;
        }

        final MongoCdoSnapshots cdoSnapshots = new MongoCdoSnapshots(mongoCdoSnapshots);

        return Lists.transform(cdoSnapshots.getLatest(limit),
                new Function<MongoSnapshot, CdoSnapshot>() {
                    @Override
                    public CdoSnapshot apply(MongoSnapshot input) {
                        return mapper.toCdoSnapshot(input, cdoSnapshots.getGlobalCdoId());
                    }
                });
    }

    @Override
    public Optional<CdoSnapshot> getLatest(GlobalCdoId globalId) {
        return getLatest(new BasicDBObject("globalCdoId", (DBObject) JSON.parse(jsonConverter.toJson(globalId))));
    }

    @Override
    public Optional<CdoSnapshot> getLatest(InstanceId.InstanceIdDTO dtoId) {

        DBObject dbObject = new BasicDBObject("globalCdoId", BasicDBObjectBuilder.start()
                .append("cdoId", dtoId.getCdoId())
                .append("entity", dtoId.getEntity().getName()).get());

        return getLatest(dbObject);
    }

    private Optional<CdoSnapshot> getLatest(DBObject id) {

        DBObject mongoCdoSnapshots = mongo.getCollection(MongoCdoSnapshots.COLLECTION_NAME)
                .findOne(id);

        if (mongoCdoSnapshots == null) {
            return Optional.empty();
        }

        MongoCdoSnapshots cdoSnapshots = new MongoCdoSnapshots(mongoCdoSnapshots);

        MongoSnapshot latest = cdoSnapshots.getLatest();

        return Optional.of(mapper.toCdoSnapshot(latest, cdoSnapshots.getGlobalCdoId()));
    }


    @Override
    public CommitId getHeadId() {
        DBObject headId = mongo.getCollection(MongoHeadId.COLLECTION_NAME).findOne();

        if (headId == null) {
            return null;
        }

        return jsonConverter.fromJson(headId.get(MongoHeadId.KEY).toString(), CommitId.class);
    }

    @Override
    public void setJsonConverter(JsonConverter jsonConverter) {
        this.mapper = new ModelMapper2(jsonConverter);
        this.jsonConverter = jsonConverter;
    }
}
