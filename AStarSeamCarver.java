package seamcarving;

import astar.AStarGraph;
import astar.AStarSolver;
import astar.WeightedEdge;
import edu.princeton.cs.algs4.Picture;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AStarSeamCarver implements SeamCarver {
    private Picture picture;
    private Pixel start;
    private Pixel stop;

    public AStarSeamCarver(Picture picture) {
        if (picture == null) {
            throw new NullPointerException("Picture cannot be null.");
        }
        this.picture = new Picture(picture);
        start = new Pixel(-1, -1, 0);
        stop = new Pixel(-2, -2, 0);
    }

    public Picture picture() {
        return new Picture(picture);
    }

    public void setPicture(Picture picture) {
        this.picture = picture;
    }

    public int width() {
        return picture.width();
    }

    public int height() {
        return picture.height();
    }

    public Color get(int x, int y) {
        return picture.get(x, y);
    }

    //Discussed rotation method with another student named Jia Ling. Besides theory, no code was discussed.
    private Picture rotateClockwise() {
        Picture rotated = new Picture(height(), width());
        for (int row = 0; row < this.height(); row++) {
            for (int col = 0; col < this.width(); col++) {
                rotated.set(height() - 1 - row, col, get(col, row));
            }
        }
        return rotated;
    }

    public double energy(int x, int y) {
        if (!inBounds(x, y)) {
            throw new IndexOutOfBoundsException("Energy does not exist");
        }
        // if pixel is not in bounds throw index out of bounds exception
        Color leftPixel;
        if (inBounds(x - 1, y)) {
            leftPixel = get(x - 1, y);
        } else {
            leftPixel = get(width() - 1, y);
        }
        Color rightPixel;
        if (inBounds(x + 1, y)) {
            rightPixel = get(x + 1, y);
        } else {
            rightPixel = get(0, y);
        }
        Color upperPixel;
        if (inBounds(x, y - 1)) {
            upperPixel = get(x, y - 1);
        } else {
            upperPixel = get(x, height() - 1);
        }
        Color lowerPixel;
        if (inBounds(x, y + 1)) {
            lowerPixel = get(x, y + 1);
        } else {
            lowerPixel = get(x, 0);
        }
        double xGradRedDiffSquared = Math.pow(rightPixel.getRed() - leftPixel.getRed(), 2);
        double xGradGreenDiffSquared = Math.pow(rightPixel.getGreen() - leftPixel.getGreen(), 2);
        double xGradBlueDiffSquared = Math.pow(rightPixel.getBlue() - leftPixel.getBlue(), 2);
        double yGradRedDiffSquared = Math.pow(upperPixel.getRed() - lowerPixel.getRed(), 2);
        double yGradGreenDiffSquared = Math.pow(upperPixel.getGreen() - lowerPixel.getGreen(), 2);
        double yGradBlueDiffSquared = Math.pow(upperPixel.getBlue() - lowerPixel.getBlue(), 2);
        double energy = Math.sqrt(xGradRedDiffSquared + xGradGreenDiffSquared + xGradBlueDiffSquared
                + yGradRedDiffSquared + yGradGreenDiffSquared + yGradBlueDiffSquared);
        return energy;
    }

    public int[] findHorizontalSeam() {
        // Rotate picture so that we can use the vertical implementation of neighbors
        this.picture = rotateClockwise();
        MakeGraph input = new MakeGraph();
        AStarSolver findSeam = new AStarSolver(input, start, stop, Double.POSITIVE_INFINITY);
        List<Pixel> solution = findSeam.solution();
        int[] horizontalSeam = new int[solution.size() - 2];
        for (int i = 0; i < solution.size() - 2; i++) {
            horizontalSeam[i] = width() - 1 - solution.get(i + 1).getX();
        }
        return horizontalSeam;
    }

    public int[] findVerticalSeam() {
        MakeGraph input = new MakeGraph();
        AStarSolver findSeam = new AStarSolver(input, start, stop, 100);
        List<Pixel> solution = findSeam.solution();
        int[] verticalSeam = new int[height()];
        for (int i = 0; i < solution.size() - 2; i++) {
            verticalSeam[i] = solution.get(i + 1).getX();
        }
        return verticalSeam;
    }

    private class Pixel {
        private int x;
        private int y;
        private double energy;

        public Pixel(int x, int y) {
            this(x, y, energy(x, y));
        }

        public Pixel(int x, int y, double energy) {
            this.x = x;
            this.y = y;
            this.energy = energy;
        }

        public double getEnergy() {
            return this.energy;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public String toString() {
            return "(" + this.x + ", " + this.y + ")";
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Pixel) {
                return (this.x == ((Pixel) other).x && this.y == ((Pixel) other).y);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    public class MakeGraph implements AStarGraph<Pixel> {

        public List<WeightedEdge<Pixel>> neighbors(Pixel p) {
            List<WeightedEdge<Pixel>> neighborEdges = new ArrayList<>();
            if (p.equals(start)) {
                for (int i = 0; i < width(); i++) {
                    double energyOfNeighbor = energy(i, 0);
                    neighborEdges.add(new WeightedEdge<>(p, new Pixel(i, 0), energyOfNeighbor));
                }
            } else if (p.getX() == -2 && p.getY() == -2) {
                return neighborEdges;
            } else if (p.getY() == height() - 1) {
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(-2, -2, 0), 0));
            } else if (width() == 1) {
                double energyOfSNeighbor = energy(p.getX(), p.getY() + 1);
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX(), p.getY() + 1), energyOfSNeighbor));
            } else if (p.getX() == width() - 1) {
                double energyOfSWNeighbor = energy(p.getX() - 1, p.getY() + 1);
                double energyOfSNeighbor = energy(p.getX(), p.getY() + 1);

                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX() - 1, p.getY() + 1), energyOfSWNeighbor));
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX(), p.getY() + 1), energyOfSNeighbor));
            } else if (p.getX() == 0) {
                double energyOfSENeighbor = energy(p.getX() + 1, p.getY() + 1);
                double energyOfSNeighbor = energy(p.getX(), p.getY() + 1);
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX() + 1, p.getY() + 1), energyOfSENeighbor));
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX(), p.getY() + 1), energyOfSNeighbor));
            } else {
                double energyOfSWNeighbor = energy(p.getX() - 1, p.getY() + 1);
                double energyOfSENeighbor = energy(p.getX() + 1, p.getY() + 1);
                double energyOfSNeighbor = energy(p.getX(), p.getY() + 1);
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX() + 1, p.getY() + 1), energyOfSENeighbor));
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX(), p.getY() + 1), energyOfSNeighbor));
                neighborEdges.add(new WeightedEdge<>(p, new Pixel(p.getX() - 1, p.getY() + 1), energyOfSWNeighbor));
            }
            return neighborEdges;
        }


        public double estimatedDistanceToGoal(Pixel s, Pixel goal) {
            return 0.0;
        }

    }
}
