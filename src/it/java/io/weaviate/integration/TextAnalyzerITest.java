package io.weaviate.integration;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;

import io.weaviate.ConcurrentTest;
import io.weaviate.client6.v1.api.WeaviateClient;
import io.weaviate.client6.v1.api.collections.Property;
import io.weaviate.client6.v1.api.collections.TextAnalyzer;
import io.weaviate.client6.v1.api.collections.Tokenization;
import io.weaviate.client6.v1.api.collections.query.Filter;
import io.weaviate.containers.Container;
import io.weaviate.containers.Weaviate;

public class TextAnalyzerITest extends ConcurrentTest {
    private static final WeaviateClient client = Container.WEAVIATE.getClient();

    @BeforeClass
    public static void __() {
        Weaviate.Version.V137.orSkip();
    }

    @Test
    public void testAsciiFoldRoundTripsThroughConfigAndAffectsFilters() throws Exception {
        var nsAccent = ns("AccentFolding");
        client.collections.create(nsAccent, c -> c
                .properties(
                        Property.text("text_default",
                                p -> p.tokenization(Tokenization.WORD)),
                        Property.text("text_folded",
                                p -> p.tokenization(Tokenization.WORD)
                                        .textAnalyzer(TextAnalyzer.of(t -> t.foldAscii(true)))),
                        Property.text("text_folded_keep_e",
                                p -> p.tokenization(Tokenization.WORD)
                                        .textAnalyzer(TextAnalyzer.of(t -> t
                                                .foldAscii(true)
                                                .keepAscii("é"))))));

        var products = client.collections.use(nsAccent);

        // ---- Layer 1: schema round-trip -------------------------------------
        var config = products.config.get();
        Assertions.assertThat(config).isPresent();
        var props = config.get().properties();

        var textDefault = props.stream()
                .filter(p -> p.propertyName().equals("text_default")).findFirst().orElseThrow();
        var textFolded = props.stream()
                .filter(p -> p.propertyName().equals("text_folded")).findFirst().orElseThrow();
        var textFoldedKeepE = props.stream()
                .filter(p -> p.propertyName().equals("text_folded_keep_e")).findFirst().orElseThrow();

        Assertions.assertThat(textDefault.textAnalyzer())
                .as("default property has no textAnalyzer config")
                .isNull();

        Assertions.assertThat(textFolded.textAnalyzer())
                .as("text_folded persists asciiFold=true")
                .isNotNull()
                .satisfies(ta -> {
                    Assertions.assertThat(ta.foldAscii()).isTrue();
                });

        Assertions.assertThat(textFoldedKeepE.textAnalyzer())
                .as("text_folded_keep_e persists asciiFold=true and asciiFoldIgnore=[é]")
                .isNotNull()
                .satisfies(ta -> {
                    Assertions.assertThat(ta.foldAscii()).isTrue();
                    Assertions.assertThat(ta.keepAscii()).containsExactly("é");
                });

        // ---- Layer 2: behavioral --------------------------------------------
        products.data.insert(Map.of(
                "text_default", "Café Crème Bio",
                "text_folded", "Café Crème Bio",
                "text_folded_keep_e", "Café Crème Bio"));

        // "cafe" (lowercase, no accents) must match only the fully-folded property.
        var defaultMatches = products.query.fetchObjects(
                q -> q.filters(Filter.property("text_default").eq("cafe")));
        Assertions.assertThat(defaultMatches.objects())
                .as("text_default has no folding, 'cafe' should not match 'Café Crème Bio'")
                .isEmpty();

        var foldedMatches = products.query.fetchObjects(
                q -> q.filters(Filter.property("text_folded").eq("cafe")));
        Assertions.assertThat(foldedMatches.objects())
                .as("text_folded has asciiFold=true, 'cafe' must match 'Café Crème Bio'")
                .hasSize(1);

        var keepEMatches = products.query.fetchObjects(
                q -> q.filters(Filter.property("text_folded_keep_e").eq("cafe")));
        Assertions.assertThat(keepEMatches.objects())
                .as("text_folded_keep_e preserves é, 'cafe' must NOT match 'Café Crème Bio'")
                .isEmpty();

        // The exact accented form matches everywhere.
        for (String prop : new String[] { "text_default", "text_folded", "text_folded_keep_e" }) {
            var hits = products.query.fetchObjects(
                    q -> q.filters(Filter.property(prop).eq("Café")));
            Assertions.assertThat(hits.objects())
                    .as("'Café' (exact) should match on %s regardless of folding", prop)
                    .hasSize(1);
        }
    }

    @Test
    public void testStopwordPresetRoundTripsThroughConfig() throws Exception {
        var nsStop = ns("StopwordPreset");
        client.collections.create(nsStop, c -> c
                .properties(
                        Property.text("name_en",
                                p -> p.tokenization(Tokenization.WORD)
                                        .textAnalyzer(TextAnalyzer.of(t -> t.stopwordPreset("en")))),
                        Property.text("name_none",
                                p -> p.tokenization(Tokenization.WORD)
                                        .textAnalyzer(TextAnalyzer.of(t -> t.stopwordPreset("none"))))));

        var products = client.collections.use(nsStop);
        var config = products.config.get();
        Assertions.assertThat(config).isPresent();
        var props = config.get().properties();

        var nameEn = props.stream()
                .filter(p -> p.propertyName().equals("name_en")).findFirst().orElseThrow();
        var nameNone = props.stream()
                .filter(p -> p.propertyName().equals("name_none")).findFirst().orElseThrow();

        Assertions.assertThat(nameEn.textAnalyzer())
                .as("name_en persists stopwordPreset=en")
                .isNotNull()
                .satisfies(ta -> Assertions.assertThat(ta.stopwordPreset()).isEqualTo("en"));

        Assertions.assertThat(nameNone.textAnalyzer())
                .as("name_none persists stopwordPreset=none")
                .isNotNull()
                .satisfies(ta -> Assertions.assertThat(ta.stopwordPreset()).isEqualTo("none"));
    }
}
