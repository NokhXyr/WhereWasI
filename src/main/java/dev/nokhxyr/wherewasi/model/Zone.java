package dev.nokhxyr.wherewasi.model;

/**
 * A claimed rectangular area, defined by two opposite corners (a block-coordinate
 * bounding box, chunk-aligned when created from two points). Events whose position
 * falls inside the box are tagged with the zone.
 */
public record Zone(
        String id,
        String name,
        String dim,
        int minX, int minZ,
        int maxX, int maxZ,
        long millis
) {
    public boolean covers(String otherDim, int px, int pz) {
        return dim.equals(otherDim) && px >= minX && px <= maxX && pz >= minZ && pz <= maxZ;
    }

    public int centerX() {
        return Math.floorDiv(minX + maxX, 2);
    }

    public int centerZ() {
        return Math.floorDiv(minZ + maxZ, 2);
    }

    public int chunksWide() {
        return (maxX - minX + 1) / 16;
    }

    public int chunksDeep() {
        return (maxZ - minZ + 1) / 16;
    }

    public long areaBlocks() {
        return (long) (maxX - minX + 1) * (maxZ - minZ + 1);
    }

    public Zone withName(String newName) {
        return new Zone(id, newName, dim, minX, minZ, maxX, maxZ, millis);
    }

    public Zone withBounds(int nMinX, int nMinZ, int nMaxX, int nMaxZ) {
        return new Zone(id, name, dim, nMinX, nMinZ, nMaxX, nMaxZ, millis);
    }

    /**
     * Chunk-aligned bounding box spanning two world points — this is what "claims
     * the chunks between the two corners". Returns {@code [minX, minZ, maxX, maxZ]}.
     */
    public static int[] boxFromPoints(int x1, int z1, int x2, int z2) {
        int minX = (Math.min(x1, x2) >> 4) << 4;
        int minZ = (Math.min(z1, z2) >> 4) << 4;
        int maxX = ((Math.max(x1, x2) >> 4) << 4) + 15;
        int maxZ = ((Math.max(z1, z2) >> 4) << 4) + 15;
        return new int[]{minX, minZ, maxX, maxZ};
    }
}
