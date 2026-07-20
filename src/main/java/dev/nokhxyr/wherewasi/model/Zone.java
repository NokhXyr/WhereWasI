package dev.nokhxyr.wherewasi.model;

/**
 * A named area of interest, anchored on the 64x64 cell where the player spent
 * the most time. {@link #covers} lets events be tagged with the zone they fall
 * in, allowing a small radius around the anchor so a "base" spanning a few cells
 * still reads as one place.
 */
public record Zone(
        String id,
        String name,
        String dim,
        int x, int z,
        long millis
) {
    public int cellX() {
        return Math.floorDiv(x, 64);
    }

    public int cellZ() {
        return Math.floorDiv(z, 64);
    }

    public boolean covers(String otherDim, int px, int pz, int cellRadius) {
        if (!dim.equals(otherDim)) {
            return false;
        }
        int dcx = Math.abs(Math.floorDiv(px, 64) - cellX());
        int dcz = Math.abs(Math.floorDiv(pz, 64) - cellZ());
        return dcx <= cellRadius && dcz <= cellRadius;
    }

    public Zone withName(String newName) {
        return new Zone(id, newName, dim, x, z, millis);
    }
}
