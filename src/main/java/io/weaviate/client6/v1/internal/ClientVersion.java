package io.weaviate.client6.v1.internal;

public class ClientVersion {
    public static final String HEADER_X_WEAVIATE_CLIENT = "X-Weaviate-Client";

    public static String getVersion() {
        return  "weaviate-client-java/" + BuildInfo.VERSION;
    }
}
