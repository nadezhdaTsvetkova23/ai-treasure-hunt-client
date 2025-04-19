package client.map;

import java.util.Objects;

public class Coordinate {
    private final int xCord;
    private final int yCord;

    public Coordinate(int x, int y) {
        this.xCord = x;
        this.yCord = y;
    }

    public int getX() { return xCord; }
    public int getY() { return yCord; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinate that)) return false;
        return xCord == that.xCord && yCord == that.yCord;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCord, yCord);
    }
}
