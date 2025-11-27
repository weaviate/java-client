package io.weaviate.client6.v1.internal.json;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.jparams.junit4.JParamsTestRunner;
import com.jparams.junit4.data.DataMethod;

import io.weaviate.client6.v1.api.cluster.NodeVerbosity;
import io.weaviate.client6.v1.api.collections.CollectionConfig;
import io.weaviate.client6.v1.api.collections.Encoding;
import io.weaviate.client6.v1.api.collections.Generative;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.Quantization;
import io.weaviate.client6.v1.api.collections.ReferenceProperty;
import io.weaviate.client6.v1.api.collections.Reranker;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.client6.v1.api.collections.VectorConfig;
import io.weaviate.client6.v1.api.collections.Vectors;
import io.weaviate.client6.v1.api.collections.WeaviateObject;
import io.weaviate.client6.v1.api.collections.data.BatchReference;
import io.weaviate.client6.v1.api.collections.data.ObjectReference;
import io.weaviate.client6.v1.api.collections.data.ReferenceAddManyResponse;
import io.weaviate.client6.v1.api.collections.quantizers.PQ;
import io.weaviate.client6.v1.api.collections.rerankers.CohereReranker;
import io.weaviate.client6.v1.api.collections.vectorindex.Distance;
import io.weaviate.client6.v1.api.collections.vectorindex.Dynamic;
import io.weaviate.client6.v1.api.collections.vectorindex.Flat;
import io.weaviate.client6.v1.api.collections.vectorindex.Hnsw;
import io.weaviate.client6.v1.api.collections.vectorindex.MultiVector;
import io.weaviate.client6.v1.api.collections.vectorindex.MultiVector.Aggregation;
import io.weaviate.client6.v1.api.collections.vectorizers.Img2VecNeuralVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Multi2VecClipVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.SelfProvidedVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecCohereVectorizer;
import io.weaviate.client6.v1.api.collections.vectorizers.Text2VecWeaviateVectorizer;
import io.weaviate.client6.v1.api.rbac.AliasesPermission;
import io.weaviate.client6.v1.api.rbac.BackupsPermission;
import io.weaviate.client6.v1.api.rbac.ClusterPermission;
import io.weaviate.client6.v1.api.rbac.CollectionsPermission;
import io.weaviate.client6.v1.api.rbac.DataPermission;
import io.weaviate.client6.v1.api.rbac.GroupsPermission;
import io.weaviate.client6.v1.api.rbac.NodesPermission;
import io.weaviate.client6.v1.api.rbac.ReplicatePermission;
import io.weaviate.client6.v1.api.rbac.Role;
import io.weaviate.client6.v1.api.rbac.RolesPermission;
import io.weaviate.client6.v1.api.rbac.RolesPermission.Scope;
import io.weaviate.client6.v1.api.rbac.TenantsPermission;
import io.weaviate.client6.v1.api.rbac.UsersPermission;
import io.weaviate.client6.v1.api.rbac.groups.GroupType;

/** Unit tests for custom POJO-to-JSON serialization. */
@RunWith(JParamsTestRunner.class)
public class JSONTest {
  public static Object[][] testCases() {
    return new Object[][] {
        // Vectorizer.CustomTypeAdapterFactory
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorIndexConfig": {},
                  "vectorizer": {"none": {}}
                }
                  """,
        },
        {
            VectorConfig.class,
            Img2VecNeuralVectorizer.of(i2v -> i2v.imageFields("jpeg", "png")),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorIndexConfig": {},
                  "vectorizer": {
                    "img2vec-neural": {
                      "imageFields": ["jpeg", "png"]
                    }
                  }
                }
                    """,
        },
        {
            VectorConfig.class,
            Multi2VecClipVectorizer.of(m2v -> m2v
                .inferenceUrl("http://example.com")
                .imageField("img", 1f)
                .textField("txt", 2f)),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorIndexConfig": {},
                  "vectorizer": {
                    "multi2vec-clip": {
                      "inferenceUrl": "http://example.com",
                      "imageFields": ["img"],
                      "textFields": ["txt"],
                      "weights": {
                        "imageWeights": [1.0],
                        "textWeights": [2.0]
                      }
                    }
                  }
                }
                    """,
        },
        {
            VectorConfig.class,
            Text2VecCohereVectorizer.of(),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorIndexConfig": {},
                  "vectorizer": {
                    "text2vec-cohere": {
                      "vectorizeClassName": false,
                      "sourceProperties": []
                    }
                  }
                }
                    """,
        },
        {
            VectorConfig.class,
            Text2VecWeaviateVectorizer.of(t2v -> t2v
                .baseUrl("http://example.com")
                .dimensions(4)
                .model("very-good-model")),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorIndexConfig": {},
                  "vectorizer": {
                    "text2vec-weaviate": {
                      "baseURL": "http://example.com",
                      "dimensions": 4,
                      "model": "very-good-model",
                      "sourceProperties": []

                    }
                  }
                }
                    """,
        },

        // VectorIndex.CustomTypeAdapterFactory
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .vectorIndex(Flat.of(flat -> flat
                    .vectorCacheMaxObjects(100)))),
            """
                {
                  "vectorIndexType": "flat",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {"vectorCacheMaxObjects": 100}
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .vectorIndex(Dynamic.of(idx -> idx
                    .hnsw(Hnsw.of(hnsw -> hnsw
                        .ef(1)
                        .efConstruction(2)))
                    .flat(Flat.of(flat -> flat
                        .vectorCacheMaxObjects(100)))
                    .threshold(5)))),
            """
                {
                  "vectorIndexType": "dynamic",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "flat": {"vectorCacheMaxObjects": 100},
                    "hnsw": {"ef": 1, "efConstruction": 2},
                    "threshold": 5
                  }
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .quantization(Quantization.pq(pq -> pq
                    .centroids(8)
                    .encoderDistribution(PQ.EncoderDistribution.NORMAL)
                    .encoderType(PQ.EncoderType.TILE)
                    .segments(16)
                    .trainingLimit(1024)
                    .bitCompression(true)))),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "pq": {
                      "enabled": true,
                      "centroids": 8,
                      "encoder_distribution": "normal",
                      "encoder_type": "tile",
                      "segments": 16,
                      "training_limit": 1024,
                      "bit_compression": true
                    }
                  }
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .quantization(Quantization.sq(sq -> sq
                    .rescoreLimit(10)
                    .trainingLimit(1024)
                    .cache(true)))),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "sq": {
                      "enabled": true,
                      "rescore_limit": 10,
                      "training_limit": 1024,
                      "cache": true
                    }
                  }
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .quantization(Quantization.rq(rq -> rq
                    .rescoreLimit(10)
                    .bits(8)))),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "rq": {
                      "enabled": true,
                      "rescore_limit": 10,
                      "bits": 8
                    }
                  }
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .quantization(Quantization.bq(bq -> bq
                    .rescoreLimit(10)
                    .cache(true)))),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "bq": {
                      "enabled": true,
                      "rescore_limit": 10,
                      "cache": true
                    }
                  }
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .quantization(Quantization.uncompressed())),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "skipDefaultQuantization": true
                  }
                }
                """,
        },
        {
            VectorConfig.class,
            SelfProvidedVectorizer.of(none -> none
                .vectorIndex(Hnsw.of(hnsw -> hnsw
                    .distance(Distance.DOT)
                    .ef(1)
                    .efConstruction(2)
                    .maxConnections(3)
                    .vectorCacheMaxObjects(4)
                    .cleanupIntervalSeconds(5)
                    .dynamicEfMin(6)
                    .dynamicEfMax(7)
                    .dynamicEfFactor(8)
                    .flatSearchCutoff(9)
                    .skipVectorization(true)
                    .filterStrategy(Hnsw.FilterStrategy.ACORN)))),
            """
                {
                  "vectorIndexType": "hnsw",
                  "vectorizer": {"none": {}},
                  "vectorIndexConfig": {
                    "distance": "dot",
                    "ef": 1,
                    "efConstruction": 2,
                    "maxConnections": 3,
                    "vectorCacheMaxObjects": 4,
                    "cleanupIntervalSeconds": 5,
                    "dynamicEfMin": 6,
                    "dynamicEfMax": 7,
                    "dynamicEfFactor": 8,
                    "flatSearchCutoff": 9,
                    "skip": true,
                    "filterStrategy":"acorn"
                  }
                }
                """,
        },

        // Vectors.CustomTypeAdapterFactory
        {
            Vectors.class,
            Vectors.of(new float[] { 1f, 2f }),
            "{\"default\": [1.0, 2.0]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of(new float[][] { { 1f, 2f }, { 3f, 4f } }),
            "{\"default\": [[1.0, 2.0], [3.0, 4.0]]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of("custom", new float[] { 1f, 2f }),
            "{\"custom\": [1.0, 2.0]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            Vectors.of("custom", new float[][] { { 1f, 2f }, { 3f, 4f } }),
            "{\"custom\": [[1.0, 2.0], [3.0, 4.0]]}",
            (CustomAssert) JSONTest::compareVectors,
        },
        {
            Vectors.class,
            new Vectors(
                Vectors.of("1d", new float[] { 1f, 2f }),
                Vectors.of("2d", new float[][] { { 1f, 2f }, { 3f, 4f } })),
            "{\"1d\": [1.0, 2.0], \"2d\": [[1.0, 2.0], [3.0, 4.0]]}",
            (CustomAssert) JSONTest::compareVectors,
        },

        // WeaviateCollection.CustomTypeAdapterFactory
        {
            CollectionConfig.class,
            CollectionConfig.of("Things", things -> things
                .description("A collection of things")
                .properties(
                    Property.text("shape"),
                    Property.text("custom_id", p -> p.tokenization(Tokenization.WORD)),
                    Property.integer("size"))
                .references(
                    ReferenceProperty.to("owner", "Person", "Company"))
                .vectorConfig(
                    VectorConfig.img2vecNeural("v-shape",
                        i2v -> i2v.imageFields("img")))),
            """
                {
                  "class": "Things",
                  "description": "A collection of things",
                  "properties": [
                    {"name": "shape", "dataType": ["text"]},
                    {"name": "size", "dataType": ["int"]},
                    {"name": "custom_id", "dataType": ["text"], tokenization: "word"},
                    {"name": "owner", "dataType": ["Person", "Company"]}
                  ],
                  "vectorConfig": {
                    "v-shape": {
                      "vectorIndexType": "hnsw",
                      "vectorIndexConfig": {},
                      "vectorizer": {"img2vec-neural": {
                        "imageFields": ["img"]
                      }}
                    }
                  }
                }
                """,
        },

        // Reference.TYPE_ADAPTER
        {
            ObjectReference.class,
            ObjectReference.uuid("id-1"),
            "{\"beacon\": \"weaviate://localhost/id-1\"}",
        },
        {
            ObjectReference.class,
            ObjectReference.collection("Doodlebops", "id-1"),
            "{\"beacon\": \"weaviate://localhost/Doodlebops/id-1\"}",
        },

        // WriteWeaviateObject.CustomTypeAdapterFactory.INSTANCE
        {
            new TypeToken<WeaviateObject<Map<String, Object>>>() {
            },
            new WeaviateObject<>(
                "thing-1",
                "Things",
                /* tenant */ null,
                Map.of("title", "ThingOne"),
                /* vectors */ null,
                /* creationTimeUnix */ null,
                /* lastUpdateTimeUnix */ null,
                /* queryMetadata */ null,
                Map.of("hasRef", List.of(ObjectReference.uuid("ref-1")))),

            """
                {
                  "class": "Things",
                  "properties": {
                    "title": "ThingOne",
                    "hasRef": [{"beacon": "weaviate://localhost/ref-1"}]
                  },
                  "id": "thing-1"
                }
                  """,
        },

        // Reranker.CustomTypeAdapterFactory
        {
            Reranker.class,
            Reranker.cohere(rerank -> rerank
                .model(CohereReranker.RERANK_ENGLISH_V2)),
            """
                {
                  "reranker-cohere": {
                    "model": "rerank-english-v2.0"
                  }
                }
                  """,
        },

        // BatchReference.CustomTypeAdapterFactory
        {
            BatchReference.class,
            new BatchReference("FromCollection", "fromProperty", "from-uuid",
                ObjectReference.collection("ToCollection", "to-uuid")),
            """
                {
                  "from": "weaviate://localhost/FromCollection/from-uuid/fromProperty",
                  "to": "weaviate://localhost/ToCollection/to-uuid"
                }
                  """,
        },

        // Role.CustomTypeAdapterFactory & Permission.CustomTypeAdapterFactory
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new AliasesPermission(
                        "CollectionAlias",
                        "Collection",
                        List.of(
                            AliasesPermission.Action.CREATE,
                            AliasesPermission.Action.READ,
                            AliasesPermission.Action.UPDATE,
                            AliasesPermission.Action.DELETE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_aliases",
                      "aliases": {
                        "alias": "CollectionAlias",
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "read_aliases",
                      "aliases": {
                        "alias": "CollectionAlias",
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "update_aliases",
                      "aliases": {
                        "alias": "CollectionAlias",
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "delete_aliases",
                      "aliases": {
                        "alias": "CollectionAlias",
                        "collection": "Collection"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new BackupsPermission(
                        "Collection",
                        List.of(BackupsPermission.Action.MANAGE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "manage_backups",
                      "backups": {
                        "collection": "Collection"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new ClusterPermission(
                        List.of(ClusterPermission.Action.READ)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    { "action": "read_cluster" }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new CollectionsPermission(
                        "Collection",
                        List.of(
                            CollectionsPermission.Action.CREATE,
                            CollectionsPermission.Action.READ,
                            CollectionsPermission.Action.UPDATE,
                            CollectionsPermission.Action.DELETE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_collections",
                      "collections": {
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "read_collections",
                      "collections": {
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "update_collections",
                      "collections": {
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "delete_collections",
                      "collections": {
                        "collection": "Collection"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new DataPermission(
                        "Collection",
                        List.of(
                            DataPermission.Action.CREATE,
                            DataPermission.Action.READ,
                            DataPermission.Action.UPDATE,
                            DataPermission.Action.DELETE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_data",
                      "data": {
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "read_data",
                      "data": {
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "update_data",
                      "data": {
                        "collection": "Collection"
                      }
                    },
                    {
                      "action": "delete_data",
                      "data": {
                        "collection": "Collection"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new GroupsPermission(
                        "friend-group",
                        GroupType.OIDC,
                        List.of(
                            GroupsPermission.Action.READ,
                            GroupsPermission.Action.ASSIGN_AND_REVOKE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "read_groups",
                      "groups": {
                        "group": "friend-group",
                        "groupType": "oidc"
                      }
                    },
                    {
                      "action": "assign_and_revoke_groups",
                      "groups": {
                        "group": "friend-group",
                        "groupType": "oidc"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new NodesPermission(
                        "Collection",
                        NodeVerbosity.MINIMAL,
                        List.of(NodesPermission.Action.READ)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "read_nodes",
                      "nodes": {
                        "collection": "Collection",
                        "verbosity": "minimal"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new ReplicatePermission(
                        "Collection",
                        "shard-123",
                        List.of(
                            ReplicatePermission.Action.CREATE,
                            ReplicatePermission.Action.READ,
                            ReplicatePermission.Action.UPDATE,
                            ReplicatePermission.Action.DELETE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_replicate",
                      "replicate": {
                        "collection": "Collection",
                        "shard": "shard-123"
                      }
                    },
                    {
                      "action": "read_replicate",
                      "replicate": {
                        "collection": "Collection",
                        "shard": "shard-123"
                      }
                    },
                    {
                      "action": "update_replicate",
                      "replicate": {
                        "collection": "Collection",
                        "shard": "shard-123"
                      }
                    },
                    {
                      "action": "delete_replicate",
                      "replicate": {
                        "collection": "Collection",
                        "shard": "shard-123"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new RolesPermission(
                        "rock-n-role",
                        Scope.MATCH,
                        List.of(
                            RolesPermission.Action.CREATE,
                            RolesPermission.Action.READ,
                            RolesPermission.Action.UPDATE,
                            RolesPermission.Action.DELETE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_roles",
                      "roles": {
                        "role": "rock-n-role",
                        "scope": "match"
                      }
                    },
                    {
                      "action": "read_roles",
                      "roles": {
                        "role": "rock-n-role",
                        "scope": "match"
                      }
                    },
                    {
                      "action": "update_roles",
                      "roles": {
                        "role": "rock-n-role",
                        "scope": "match"
                      }
                    },
                    {
                      "action": "delete_roles",
                      "roles": {
                        "role": "rock-n-role",
                        "scope": "match"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new TenantsPermission(
                        "Collection",
                        "TeenageMutenantNinjaTurtles",
                        List.of(
                            TenantsPermission.Action.CREATE,
                            TenantsPermission.Action.READ,
                            TenantsPermission.Action.UPDATE,
                            TenantsPermission.Action.DELETE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_tenants",
                      "tenants": {
                        "collection": "Collection",
                        "tenant": "TeenageMutenantNinjaTurtles"
                      }
                    },
                    {
                      "action": "read_tenants",
                      "tenants": {
                        "collection": "Collection",
                        "tenant": "TeenageMutenantNinjaTurtles"
                      }
                    },
                    {
                      "action": "update_tenants",
                      "tenants": {
                        "collection": "Collection",
                        "tenant": "TeenageMutenantNinjaTurtles"
                      }
                    },
                    {
                      "action": "delete_tenants",
                      "tenants": {
                        "collection": "Collection",
                        "tenant": "TeenageMutenantNinjaTurtles"
                      }
                    }
                  ]
                }
                  """
        },
        {
            Role.class,
            new Role(
                "rock-n-role",
                List.of(
                    new UsersPermission(
                        "john-doe",
                        List.of(
                            UsersPermission.Action.CREATE,
                            UsersPermission.Action.READ,
                            UsersPermission.Action.UPDATE,
                            UsersPermission.Action.DELETE,
                            UsersPermission.Action.ASSIGN_AND_REVOKE)))),
            """
                {
                  "name": "rock-n-role",
                  "permissions": [
                    {
                      "action": "create_users",
                      "users": {
                        "users": "john-doe"
                      }
                    },
                    {
                      "action": "read_users",
                      "users": {
                        "users": "john-doe"
                      }
                    },
                    {
                      "action": "update_users",
                      "users": {
                        "users": "john-doe"
                      }
                    },
                    {
                      "action": "delete_users",
                      "users": {
                        "users": "john-doe"
                      }
                    },
                    {
                      "action": "assign_and_revoke_users",
                      "users": {
                        "users": "john-doe"
                      }
                    }
                  ]
                }
                  """
        },
        {
            MultiVector.class,
            MultiVector.of(
                mv -> mv
                    .aggregation(Aggregation.MAX_SIM)
                    .encoding(Encoding.muvera(
                        enc -> enc
                            .dprojections(1)
                            .repetitions(2)
                            .ksim(3)))),
            """
                {
                  "enabled": true,
                  "aggregation": "MAX_SIM",
                  "muvera": {
                    "enabled": true,
                    "dprojections": 1,
                    "repetitions": 2,
                    "ksim": 3
                  }
                }
                  """
        },

        // Generative.CustomTypeAdapterFactory
        {
            Generative.class,
            Generative.anyscale(cfg -> cfg
                .baseUrl("https://example.com")
                .model("example-model")
                .temperature(3f)),
            """
                {
                  "generative-anyscale": {
                    "baseURL": "https://example.com",
                    "temperature": 3.0,
                    "model": "example-model"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.anthropic(cfg -> cfg
                .baseUrl("https://example.com")
                .topK(1)
                .maxTokens(2)
                .temperature(3f)
                .model("example-model")
                .stopSequences("stop", "halt")),
            """
                {
                  "generative-anthropic": {
                    "baseURL": "https://example.com",
                    "topK": 1,
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "model": "example-model",
                    "stopSequences": ["stop", "halt"]
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.awsBedrock(
                "aws-region",
                "example-model",
                cfg -> cfg
                    .model("example-model"),
                    .temperature(0.7f)
                    .maxTokenCount(100)
                    .topK(50)
                    .topP(0.9f)
                    .stopSequences("STOP", "END")),
            """
                {
                  "generative-aws": {
                    "model": "example-model",
                    "region": "aws-region",
                    "service": "bedrock",
                    "temperature": 0.7,
                    "maxTokenCount": 100,
                    "topK": 50,
                    "topP": 0.9,
                    "stopSequences": ["STOP", "END"]
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.awsSagemaker(
                "aws-region",
                "https://example.com",
                cfg -> cfg
                    .endpoint("https://example.com")
                    .targetModel("custom-model")
                    .targetVariant("variant-1")
                    .maxTokensToSample(200)
                    .stopSequences("STOP")),
            """
                {
                  "generative-aws": {
                    "endpoint": "https://example.com",
                    "region": "aws-region",
                    "service": "sagemaker",
                    "targetModel": "custom-model",
                    "targetVariant": "variant-1",
                    "maxTokensToSample": 200,
                    "stopSequences": ["STOP"]
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.cohere(cfg -> cfg
                .topK(1)
                .maxTokens(2)
                .temperature(3f)
                .model("example-model")
                .returnLikelihoodsProperty("likelihood")
                .stopSequences("stop", "halt")),
            """
                {
                  "generative-cohere": {
                    "k": 1,
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "model": "example-model",
                    "returnLikelihoods": "likelihood",
                    "stopSequences": ["stop", "halt"]
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.databricks(
                "https://example.com",
                cfg -> cfg
                    .topK(1)
                    .maxTokens(2)
                    .temperature(3f)
                    .topP(4f)),
            """
                {
                  "generative-databricks": {
                    "endpoint": "https://example.com",
                    "topK": 1,
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "topP": 4.0
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.friendliai(cfg -> cfg
                .baseUrl("https://example.com")
                .maxTokens(2)
                .temperature(3f)
                .model("example-model")),
            """
                {
                  "generative-friendliai": {
                    "baseURL": "https://example.com",
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "model": "example-model"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.mistral(cfg -> cfg
                .baseUrl("https://example.com")
                .maxTokens(2)
                .temperature(3f)
                .model("example-model")),
            """
                {
                  "generative-mistral": {
                    "baseURL": "https://example.com",
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "model": "example-model"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.nvidia(cfg -> cfg
                .baseUrl("https://example.com")
                .maxTokens(2)
                .temperature(3f)
                .topP(0.95f)
                .model("example-model")),
            """
                {
                  "generative-nvidia": {
                    "baseURL": "https://example.com",
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "topP": 0.95,
                    "model": "example-model"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.googleVertex(
                "google-project",
                cfg -> cfg
                    .apiEndpoint("https://example.com")
                    .maxTokens(2)
                    .temperature(3f)
                    .topK(4)
                    .topP(5f)
                    .modelId("example-model")
                    .endpointId("endpoint-123")
                    .region("us-central1")
                    .model("gemini-pro")),
            """
                {
                  "generative-google": {
                    "apiEndpoint": "https://example.com",
                    "maxOutputTokens": 2,
                    "temperature": 3.0,
                    "topK": 4,
                    "topP": 5,
                    "projectId": "google-project",
                    "modelId": "example-model",
                    "endpointId": "endpoint-123",
                    "region": "us-central1",
                    "model": "gemini-pro"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.ollama(cfg -> cfg
                .apiEndpoint("https://example.com")
                .model("example-model")),
            """
                {
                  "generative-ollama": {
                    "apiEndpoint": "https://example.com",
                    "model": "example-model"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.xai(cfg -> cfg
                .baseUrl("https://example.com")
                .maxTokens(2)
                .temperature(3f)
                .topP(0.9f)
                .model("example-model")),
            """
                {
                  "generative-xai": {
                    "baseURL": "https://example.com",
                    "maxTokens": 2,
                    "temperature": 3.0,
                    "topP": 0.9,
                    "model": "example-model"
                  }
                }
                  """,
        },
        {
            Generative.class,
            Generative.openai(cfg -> cfg
                .baseUrl("https://example.com")
                .frequencyPenalty(1f)
                .presencePenalty(2f)
                .temperature(3f)
                .topP(4f)
                .maxTokens(5)
                .model("o3-mini")),
            """
                {
                  "generative-openai": {
                    "baseURL": "https://example.com",
                    "frequencyPenalty": 1.0,
                    "presencePenalty": 2.0,
                    "temperature": 3.0,
                    "topP": 4.0,
                    "maxTokens": 5,
                    "model": "o3-mini"
                  }
                }
                  """
        },
        {
            Generative.class,
            Generative.azure(
                "azure-resource",
                "azure-deployment",
                cfg -> cfg
                    .baseUrl("https://example.com")
                    .frequencyPenalty(1f)
                    .presencePenalty(2f)
                    .temperature(3f)
                    .topP(4f)
                    .maxTokens(5)),
            """
                {
                  "generative-openai": {
                    "baseURL": "https://example.com",
                    "frequencyPenalty": 1.0,
                    "presencePenalty": 2.0,
                    "temperature": 3.0,
                    "topP": 4.0,
                    "maxTokens": 5,
                    "resourceName": "azure-resource",
                    "deploymentId": "azure-deployment"
                  }
                }
                  """
        },
    };
  }

  @Test
  @DataMethod(source = JSONTest.class, method = "testCases")
  public void test_serialize(Object cls, Object in, String want) {
    String got;
    if (cls instanceof TypeToken typeToken) {
      got = JSON.serialize(in, typeToken);
    } else {
      got = JSON.serialize(in);
    }
    assertEqualJson(want, got);

  }

  @FunctionalInterface
  private interface CustomAssert extends BiConsumer<Object, Object> {
  }

  @Test
  @SuppressWarnings("unchecked")
  @DataMethod(source = JSONTest.class, method = "testCases")
  public void test_deserialize(Object target, Object want, String in, CustomAssert assertion) {

    Object got;
    if (target instanceof Class targetClass) {
      got = JSON.deserialize(in, targetClass);
    } else if (target instanceof TypeToken targetToken) {
      got = JSON.deserialize(in, targetToken);
    } else {
      throw new IllegalArgumentException("target must be either Class<?> or TypeToken<?>");
    }

    if (assertion != null) {
      assertion.accept(got, want);
    } else {
      Assertions.assertThat(got).isEqualTo(want);
    }
  }

  private static void assertEqualJson(String want, String got) {
    var wantJson = JsonParser.parseString(want);
    var gotJson = JsonParser.parseString(got);
    Assertions.assertThat(gotJson).isEqualTo(wantJson);
  }

  /**
   * Custom assert function that uses deep array equality
   * to correctly compare float[] and float[][] nested in the object.
   */
  private static void compareVectors(Object got, Object want) {
    Assertions.assertThat(got)
        .usingRecursiveComparison()
        .withEqualsForType(Arrays::equals, float[].class)
        .withEqualsForType(Arrays::deepEquals, float[][].class)
        .isEqualTo(want);
  }

  @Test
  public void test_ReferenceAddManyResponse_CustomDeserializer() {
    var json = """
        [
          {
            "result": { "status": "SUCCESS", "errors": {} }
          },
          {
            "result": { "status": "FAILED", "errors": {
              "error": [ "oops" ]
            }}
          }
        ]
          """;

    var got = JSON.deserialize(json, ReferenceAddManyResponse.class);

    Assertions.assertThat(got.errors())
        .as("response contains 1 error")
        .hasSize(1);
  }

  @Test
  public void test_CollectionConfig_read_empty() {
    var json = """
        { "class": "BarebonesCollection" }
        """;
    Assertions.assertThatCode(() -> JSON.deserialize(json, CollectionConfig.class))
        .as("deserialize CollectionConfig with no properties")
        .doesNotThrowAnyException();
  }
}
