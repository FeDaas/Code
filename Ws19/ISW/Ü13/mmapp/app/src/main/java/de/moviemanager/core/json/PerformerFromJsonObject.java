package de.moviemanager.core.json;

import java.util.Map;

import de.moviemanager.data.Performer;

public class PerformerFromJsonObject extends FromJsonObject<Performer> {
    public PerformerFromJsonObject() {
        super(PerformerFromJsonObject::fromMap);
        registerSetter("name", Performer::setName);
        registerSetter("birthName", Performer::setBirthName);
        registerSetter("biography", Performer::setBiography);
        registerSetter("dateOfBirth", Performer::setDateOfBirth);
        registerSetter("occupations", Performer::setOccupations);
        registerSetter("rating", PerformerFromJsonObject::setRating);
        registerSetter("imageId", Performer::setImageId);

        registerConversion("dateOfBirth", PerformerFromJsonObject::convertStringToDate);
        registerConversion("occupations", PerformerFromJsonObject::convertJSONArrayToListOfStrings);
    }

    private static Performer fromMap(Map<String, Object> map) {
        int id = (int) map.get("id");
        return new Performer(id);
    }

    private static void setRating(Performer performer, Number number) {
        performer.setRating(number.doubleValue());
    }
}
