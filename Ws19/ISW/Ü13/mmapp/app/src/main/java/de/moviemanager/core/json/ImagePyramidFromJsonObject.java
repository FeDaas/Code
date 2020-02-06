package de.moviemanager.core.json;

import java.util.Map;

import de.moviemanager.data.ImagePyramid;

public class ImagePyramidFromJsonObject extends FromJsonObject<ImagePyramid>{
    public ImagePyramidFromJsonObject() {
        super(ImagePyramidFromJsonObject::fromMap);

        registerSetter("prefix", ImagePyramid::setPrefix);
    }

    private static ImagePyramid fromMap(Map<String, Object> map) {
        int id = (int) map.get("id");
        return new ImagePyramid(id);
    }
}
