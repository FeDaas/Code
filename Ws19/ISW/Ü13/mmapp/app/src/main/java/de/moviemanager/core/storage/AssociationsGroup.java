package de.moviemanager.core.storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import de.associations.AssociationException;
import de.associations.BidirectionalAssociationSet;
import de.associations.shortcuts.IdMapper;
import de.associations.shortcuts.IdUnmapper;
import de.moviemanager.util.FileUtils;
import de.storage.StorageGroup;
import de.util.Pair;
import de.util.StringUtils;

import static de.util.Pair.paired;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

class AssociationsGroup<L, R> extends StorageGroup<BidirectionalAssociationSet<L, R>> {
    private IdMapper<L> leftIdMapper;
    private IdUnmapper<L> leftIdUnmapper;
    private IdMapper<R> rightIdMapper;
    private IdUnmapper<R> rightIdUnmapper;

    private final BidirectionalAssociationSet<L, R> instance;

    public AssociationsGroup(final BidirectionalAssociationSet<L, R> set) {
        super((Class<BidirectionalAssociationSet<L, R>>) set.getClass(), "associations");
        instance = set;
    }

    public void setLeftMapper(final IdMapper<L> mapper, final IdUnmapper<L> unmapper) {
        this.leftIdMapper = requireNonNull(mapper);
        this.leftIdUnmapper = requireNonNull(unmapper);
    }

    public void setRightMapper(final IdMapper<R> mapper, final IdUnmapper<R> unmapper) {
        this.rightIdMapper = requireNonNull(mapper);
        this.rightIdUnmapper = requireNonNull(unmapper);
    }

    @Override
    public String createFileNameFor(final BidirectionalAssociationSet<L, R> asso) {
        return asso.getLeftType().getSimpleName() + "-" + asso.getRightType().getSimpleName() + ".json";
    }

    @Override
    protected void saveToStorage(final File directory,
                                 final BidirectionalAssociationSet<L, R> obj) {
        requireNonNull(leftIdMapper);
        requireNonNull(rightIdMapper);
        try {
            final List<JSONObject> mappedAssociations = obj
                    .getMappedAssociations(leftIdMapper, rightIdMapper)
                    .stream()
                    .map(Pair::toJsonObject)
                    .collect(toList());
            final JSONArray array = new JSONArray(mappedAssociations);
            final List<String> lines = asList(array.toString(2).split("\n"));
            FileUtils.writeLines(directory, lines);
        } catch (Exception e) {
            throw new AssociationException(e);
        }
    }

    @Override
    protected Optional<BidirectionalAssociationSet<L, R>> loadFromStorage(final File directory,
                                                                          final String objName) {
        requireNonNull(leftIdUnmapper);
        requireNonNull(rightIdUnmapper);
        try {
            final List<String> lines = FileUtils.readAllLines(FileUtils.resolve(directory, objName));
            final List<Pair<Integer, Integer>> mapping = loadMappings(lines);

            if(!mapping.isEmpty())
                instance.insertMappedAssociations(mapping, leftIdUnmapper, rightIdUnmapper);
            return Optional.of(instance);
        } catch(JSONException | IOException e) {
            return Optional.of(instance);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    static List<Pair<Integer, Integer>> loadMappings(final List<String> lines) throws JSONException {
        final String jsonString = StringUtils.join("", lines);
        final JSONArray array = new JSONArray(jsonString);
        return range(0, array.length())
                .mapToObj(array::optJSONObject)
                .map(obj -> paired(obj.optInt("first"), obj.optInt("second")))
                .collect(toList());
    }
}

