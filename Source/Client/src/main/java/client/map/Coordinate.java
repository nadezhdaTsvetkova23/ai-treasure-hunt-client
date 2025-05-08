package client.map;

import java.util.ArrayList;
import java.util.List;
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
    
    public List<Coordinate> getAdjacentCoordinates() {
        List<Coordinate> neighbors = new ArrayList<>();
        neighbors.add(new Coordinate(xCord - 1, yCord));
        neighbors.add(new Coordinate(xCord + 1, yCord));
        neighbors.add(new Coordinate(xCord, yCord - 1));
        neighbors.add(new Coordinate(xCord, yCord + 1));
        return neighbors;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinate other = (Coordinate) obj;
        return xCord == other.xCord && yCord == other.yCord;
    }

    @Override
    public int hashCode() {
        return Objects.hash(xCord, yCord);
    }

    
    @Override
    public String toString() {
        return "(" + this.getX() + "," + this.getY() + ")";
    }

}
