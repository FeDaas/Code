package de.moviemanager.core.storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import de.moviemanager.core.json.FromJsonObject;
import de.moviemanager.core.json.JsonBridge;
import de.storage.StorageException;
import de.storage.StorageGroup;
import de.moviemanager.util.FileUtils;
import de.util.Identifiable;
import de.util.StringUtils;

import static de.moviemanager.core.json.JsonBridge.fromJson;
import static java.util.Arrays.asList;

class JsonGroup<T extends Identifiable> extends StorageGroup<T> {

    private final String objectPrefix;
    private final Supplier<FromJsonObject<T>> builderSource;

    public JsonGroup(Class<T> clazz, Supplier<FromJsonObject<T>> builderSource) {
        super(clazz, clazz.getSimpleName().toLowerCase() + "s");
        this.objectPrefix = clazz.getSimpleName().toLowerCase();
        this.builderSource = builderSource;
    }

    @Override
    public String createFileNameFor(T object) {
        return objectPrefix + "_" + object.id() + ".json";
    }

    @Override
    protected void saveToStorage(File filePath, T object) {
        Optional<JSONObject> objOpt = JsonBridge.toJson(object);
        if (!objOpt.isPresent())
            throw new StorageException(object + " was not converted to JSON!");

        try {
            FileUtils.writeLines(filePath, asList(objOpt.get().toString(2).split("\n")));
        } catch(IOException | JSONException ioe) {
            throw new StorageException(ioe);
        }
    }

    @Override
    protected Optional<T> loadFromStorage(final File directory, final String objName) {
        final File jsonFile = FileUtils.resolve(directory, objName);

        String jsonString = null;
        try {
            jsonString = StringUtils.join("", FileUtils.readAllLines(jsonFile));
        } catch (IOException e) {
            throw new StorageException(e);
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return fromJson(jsonObject, builderSource);
        } catch (JSONException je) {
            return Optional.empty();
        }
    }
}