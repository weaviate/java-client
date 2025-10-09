package io.weaviate.client6.v1.api.rbac;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public record Role(
    @SerializedName("name") String name,
    @SerializedName("permissions") List<Permission> permissions) {

  public Role(String name, Permission... permissions) {
    this(name, Arrays.asList(permissions));
  }

  public static enum CustomTypeAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!Role.class.isAssignableFrom(type.getRawType())) {
        return null;
      }
      var delegate = gson.getDelegateAdapter(this, type);
      return new TypeAdapter<T>() {

        @Override
        public void write(JsonWriter out, T value) throws IOException {
          delegate.write(out, value);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T read(JsonReader in) throws IOException {
          var role = (Role) delegate.read(in);
          if (role.permissions == null) {
            return (T) role;
          }
          return (T) new Role(role.name(), Permission.merge(role.permissions));
        }
      };
    }
  }
}
