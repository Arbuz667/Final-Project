package com.softchaos.utils;

public enum LocationType {
    FOREST,
    OCEAN,
    SPACE;

    /** Returns the full-screen background PNG filename for this location. */
    public String backgroundFile() {
        switch (this) {
            case FOREST: return "forest_location.png";
            case OCEAN:  return "sea_location.png";
            case SPACE:  return "space_location.png";
            default:     return "forest_location.png";
        }
    }

    /** Returns the next location in the sequence, or null if this is the last one. */
    public LocationType next() {
        switch (this) {
            case FOREST: return OCEAN;
            case OCEAN:  return SPACE;
            default:     return null;
        }
    }
}
