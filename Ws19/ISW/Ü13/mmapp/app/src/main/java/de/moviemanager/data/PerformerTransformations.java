package de.moviemanager.data;

import java.util.Date;
import java.util.List;

import de.util.operationflow.ReversibleOperations.ReversibleTransformation;

import static de.util.operationflow.ReversibleOperations.reversibleTransformation;

public final class PerformerTransformations {
    private PerformerTransformations() {}

    public static ReversibleTransformation<Performer> setName(String name) {
        return reversibleTransformation(
                Performer::getName,
                Performer::setName,
                name
        );
    }

    public static ReversibleTransformation<Performer> setBirthName(String birthName) {
        return reversibleTransformation(
                Performer::getBirthName,
                Performer::setBirthName,
                birthName
        );
    }

    public static ReversibleTransformation<Performer> setBiography(String bio) {
        return reversibleTransformation(
                Performer::getBiography,
                Performer::setBiography,
                bio
        );
    }

    public static ReversibleTransformation<Performer> setDateOfBirth(Date dateOfBirth) {
        return reversibleTransformation(
                Performer::getDateOfBirth,
                Performer::setDateOfBirth,
                dateOfBirth
        );
    }

    public static ReversibleTransformation<Performer> setRating(double rating){
        return reversibleTransformation(
                Performer::getRating,
                Performer::setRating,
                rating
        );
    }

    public static ReversibleTransformation<Performer> setOccupations(List<String> occupations) {
        return reversibleTransformation(
                Performer::getOccupations,
                Performer::setOccupations,
                occupations
        );
    }

    public static ReversibleTransformation<Performer> setImageId(int id) {
        return reversibleTransformation(
                Performer::getImageId,
                Performer::setImageId,
                id
        );
    }
}
