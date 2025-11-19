package io.weaviate.client6.v1.api.collections.generate;

import java.util.ArrayList;
import java.util.List;

import io.weaviate.client6.v1.api.collections.generative.AnthropicGenerative;
import io.weaviate.client6.v1.api.collections.generative.AnyscaleGenerative;
import io.weaviate.client6.v1.api.collections.generative.AwsGenerative;
import io.weaviate.client6.v1.api.collections.generative.CohereGenerative;
import io.weaviate.client6.v1.api.collections.generative.DatabricksGenerative;
import io.weaviate.client6.v1.api.collections.generative.DummyGenerative;
import io.weaviate.client6.v1.api.collections.generative.FriendliaiGenerative;
import io.weaviate.client6.v1.api.collections.generative.GoogleGenerative;
import io.weaviate.client6.v1.api.collections.generative.MistralGenerative;
import io.weaviate.client6.v1.api.collections.generative.NvidiaGenerative;
import io.weaviate.client6.v1.api.collections.generative.OllamaGenerative;
import io.weaviate.client6.v1.api.collections.generative.OpenAiGenerative;
import io.weaviate.client6.v1.api.collections.generative.ProviderMetadata;
import io.weaviate.client6.v1.api.collections.generative.XaiGenerative;
import io.weaviate.client6.v1.api.collections.query.QueryResponse;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoGenerative;
import io.weaviate.client6.v1.internal.grpc.protocol.WeaviateProtoSearchGet;
import io.weaviate.client6.v1.internal.orm.CollectionDescriptor;

public record GenerativeResponse<PropertiesT>(
    float took,
    List<GenerativeObject<PropertiesT>> objects,
    TaskOutput generative) {
  static <PropertiesT> GenerativeResponse<PropertiesT> unmarshal(
      WeaviateProtoSearchGet.SearchReply reply,
      CollectionDescriptor<PropertiesT> collection) {
    var objects = reply
        .getResultsList()
        .stream()
        .map(result -> {
          var object = QueryResponse.unmarshalResultObject(
              result.getProperties(), result.getMetadata(), collection);
          TaskOutput generative = null;
          if (result.hasGenerative()) {
            generative = GenerativeResponse.unmarshalTaskOutput(result.getGenerative());
          }
          return new GenerativeObject<>(
              object.properties(),
              object.metadata(),
              generative);
        })
        .toList();

    TaskOutput summary = null;
    if (reply.hasGenerativeGroupedResults()) {
      summary = GenerativeResponse.unmarshalTaskOutput(reply.getGenerativeGroupedResults());
    }
    return new GenerativeResponse<>(reply.getTook(), objects, summary);
  }

  static TaskOutput unmarshalTaskOutput(List<WeaviateProtoGenerative.GenerativeReply> values) {
    if (values.isEmpty()) {
      return null;
    }
    var generative = values.get(0);

    var metadata = generative.getMetadata();
    ProviderMetadata providerMetadata = null;
    if (metadata.hasDummy()) {
      providerMetadata = new DummyGenerative.Metadata();
    } else if (metadata.hasAws()) {
      providerMetadata = new AwsGenerative.Metadata();
    } else if (metadata.hasAnthropic()) {
      var anthropic = metadata.getAnthropic();
      var usage = anthropic.getUsage();
      providerMetadata = new AnthropicGenerative.Metadata(new AnthropicGenerative.Metadata.Usage(
          usage.getInputTokens(),
          usage.getOutputTokens()));
    } else if (metadata.hasAnyscale()) {
      providerMetadata = new AnyscaleGenerative.Metadata();
    } else if (metadata.hasCohere()) {
      var cohere = metadata.getCohere();
      var apiVersion = cohere.getApiVersion();
      var billedUnits = cohere.getBilledUnits();
      var tokens = cohere.getTokens();
      providerMetadata = new CohereGenerative.Metadata(
          new CohereGenerative.Metadata.ApiVersion(
              apiVersion.hasVersion() ? apiVersion.getVersion() : null,
              apiVersion.hasIsDeprecated() ? apiVersion.getIsDeprecated() : null,
              apiVersion.hasIsExperimental() ? apiVersion.getIsExperimental() : null),
          new CohereGenerative.Metadata.BilledUnits(billedUnits.getInputTokens(),
              billedUnits.hasOutputTokens() ? billedUnits.getOutputTokens() : null,
              billedUnits.hasSearchUnits() ? billedUnits.getSearchUnits() : null,
              billedUnits.hasClassifications() ? billedUnits.getClassifications() : null),
          new CohereGenerative.Metadata.Tokens(
              tokens.hasInputTokens() ? tokens.getInputTokens() : null,
              tokens.hasOutputTokens() ? tokens.getOutputTokens() : null),
          new ArrayList<>(cohere.getWarnings().getValuesList()));
    } else if (metadata.hasDatabricks()) {
      var databricks = metadata.getDatabricks();
      var usage = databricks.getUsage();
      providerMetadata = new DatabricksGenerative.Metadata(new ProviderMetadata.Usage(
          usage.hasPromptTokens() ? usage.getPromptTokens() : null,
          usage.hasCompletionTokens() ? usage.getCompletionTokens() : null,
          usage.hasTotalTokens() ? usage.getTotalTokens() : null));
    } else if (metadata.hasFriendliai()) {
      var friendliai = metadata.getFriendliai();
      var usage = friendliai.getUsage();
      providerMetadata = new FriendliaiGenerative.Metadata(new ProviderMetadata.Usage(
          usage.hasPromptTokens() ? usage.getPromptTokens() : null,
          usage.hasCompletionTokens() ? usage.getCompletionTokens() : null,
          usage.hasTotalTokens() ? usage.getTotalTokens() : null));
    } else if (metadata.hasMistral()) {
      var mistral = metadata.getMistral();
      var usage = mistral.getUsage();
      providerMetadata = new MistralGenerative.Metadata(new ProviderMetadata.Usage(
          usage.hasPromptTokens() ? usage.getPromptTokens() : null,
          usage.hasCompletionTokens() ? usage.getCompletionTokens() : null,
          usage.hasTotalTokens() ? usage.getTotalTokens() : null));
    } else if (metadata.hasNvidia()) {
      var nvidia = metadata.getNvidia();
      var usage = nvidia.getUsage();
      providerMetadata = new NvidiaGenerative.Metadata(new ProviderMetadata.Usage(
          usage.hasPromptTokens() ? usage.getPromptTokens() : null,
          usage.hasCompletionTokens() ? usage.getCompletionTokens() : null,
          usage.hasTotalTokens() ? usage.getTotalTokens() : null));
    } else if (metadata.hasOllama()) {
      providerMetadata = new OllamaGenerative.Metadata();
    } else if (metadata.hasOpenai()) {
      var openai = metadata.getOpenai();
      var usage = openai.getUsage();
      providerMetadata = new OpenAiGenerative.Metadata(new ProviderMetadata.Usage(
          usage.hasPromptTokens() ? usage.getPromptTokens() : null,
          usage.hasCompletionTokens() ? usage.getCompletionTokens() : null,
          usage.hasTotalTokens() ? usage.getTotalTokens() : null));
    } else if (metadata.hasGoogle()) {
      var google = metadata.getGoogle();
      var tokens = google.getMetadata().getTokenMetadata();
      var usage = google.getUsageMetadata();
      providerMetadata = new GoogleGenerative.Metadata(
          new GoogleGenerative.Metadata.TokenMetadata(
              new GoogleGenerative.Metadata.TokenCount(
                  tokens.getInputTokenCount().hasTotalBillableCharacters()
                      ? tokens.getInputTokenCount().getTotalBillableCharacters()
                      : null,
                  tokens.getInputTokenCount().hasTotalTokens()
                      ? tokens.getInputTokenCount().getTotalTokens()
                      : null),
              new GoogleGenerative.Metadata.TokenCount(
                  tokens.getOutputTokenCount().hasTotalBillableCharacters()
                      ? tokens.getOutputTokenCount().getTotalBillableCharacters()
                      : null,
                  tokens.getOutputTokenCount().hasTotalTokens()
                      ? tokens.getOutputTokenCount().getTotalTokens()
                      : null)),
          new GoogleGenerative.Metadata.Usage(
              usage.hasPromptTokenCount() ? usage.getPromptTokenCount() : null,
              usage.hasCandidatesTokenCount() ? usage.getCandidatesTokenCount() : null,
              usage.hasTotalTokenCount() ? usage.getTotalTokenCount() : null));
    } else if (metadata.hasXai()) {
      var xai = metadata.getXai();
      var usage = xai.getUsage();
      providerMetadata = new XaiGenerative.Metadata(new ProviderMetadata.Usage(
          usage.hasPromptTokens() ? usage.getPromptTokens() : null,
          usage.hasCompletionTokens() ? usage.getCompletionTokens() : null,
          usage.hasTotalTokens() ? usage.getTotalTokens() : null));
    }

    GenerativeDebug debug = null;
    if (generative.getDebug() != null && generative.getDebug().getFullPrompt() != null) {
      debug = new GenerativeDebug(generative.getDebug().getFullPrompt());
    }
    return new TaskOutput(generative.getResult(), providerMetadata, debug);
  }

  static TaskOutput unmarshalTaskOutput(WeaviateProtoGenerative.GenerativeResult result) {
    return unmarshalTaskOutput(result.getValuesList());
  }
}
