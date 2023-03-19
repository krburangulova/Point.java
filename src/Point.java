import java.util.ArrayList;
import java.util.List;

record Point(double x, double y) {

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    static Point ZERO = new Point(0, 0);

    public Point add(final Point other) {
        return new Point(x + other.getX(), y + other.getY());
    }

    public Point subtract(final Point other) {
        return new Point(x - other.getX(), y - other.getY());
    }

    public Point multiply(final double factor) {
        return new Point(x * factor, y * factor);
    }

    public Point divide(final double divisor) {
        return new Point(x / divisor, y / divisor);
    }

    public Point rotate(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double newX = x * cos - y * sin;
        double newY = x * sin + y * cos;
        return new Point(newX, newY);
    }

    static double dotProduct(final Point a, final Point b) {
        return a.getX() * b.getX() + a.getY() * b.getY();
    }

    static double distance(final Point a, final Point b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void validatePoints(List<Point> points) {
        if (points.size() < 2) {
            throw new IllegalArgumentException("At least two points are required");
        }
    }

    private void validateCoefficient(double coefficient) {
        if (coefficient == 0) {
            throw new IllegalArgumentException("Coefficient cannot be zero");
        }
    }
}

abstract class Shape {
    public abstract Point center();

    public abstract double perimeter();

    public abstract double area();

    public abstract void translate(final Point newCenter);

    public abstract void rotate(final double angle);

    public abstract void scale(final double coefficient);
}

class Ellipse extends Shape {

    protected Point focus1;
    protected Point focus2;
    protected Point barycenter;
    protected double perifocalDistance;

    public Ellipse(final Point A, final Point B, final double distance) {
        focus1 = A;
        focus2 = B;
        perifocalDistance = distance;
        barycenter = A.add(B).divide(2.0);
    }

    public List<Point> focuses() {
        List<Point> foci = new ArrayList<>();
        foci.add(focus1);
        foci.add(focus2);
        return foci;
    }

    public double focalDistance() {
        return Point.distance(focus1, barycenter);
    }

    public double majorSemiAxis() {
        return focalDistance() + perifocalDistance;
    }

    public double minorSemiAxis() {
        double dst = focalDistance();
        double mjrAxis = majorSemiAxis();
        return Math.sqrt(Math.pow(mjrAxis, 2) - Math.pow(dst, 2));
    }

    public double eccentricity() {
        return focalDistance() / majorSemiAxis();
    }

    public Point center() {
        return barycenter;
    }

    public double perimeter() {
        double a = majorSemiAxis();
        double b = minorSemiAxis();
        return 4 * (Math.PI * a * b + Math.pow((a - b), 2)) / (a + b);
    }

    public double area() {
        return Math.PI * majorSemiAxis() * minorSemiAxis();
    }

    public void translate(final Point newCenter) {
        Point oth = newCenter.subtract(barycenter);
        focus1 = focus1.add(oth);
        focus2 = focus2.add(oth);
        barycenter = newCenter;
    }

    public void rotate(final double angle) {
        focus1 = focus1.rotate(angle);
        focus2 = focus2.rotate(angle);
    }

    public void scale(final double coefficient) {
        perifocalDistance *= Math.abs(coefficient);
        Point prevBarycenter = barycenter;
        translate(Point.ZERO);
        focus1 = focus1.multiply(coefficient);
        focus2 = focus2.multiply(coefficient);
        translate(prevBarycenter);
    }
}

class Circle extends Ellipse {

    public Circle(Point center, double radius) {
        super(center, center, radius);
    }

    public double radius() {
        return perifocalDistance;
    }
}

class Rectangle extends Shape {

    protected Point pointA;
    protected Point pointB;
    protected Point barycenter;
    protected double side1;
    protected double side2;

    public Rectangle(final Point pointA, final Point pointB, final double side1) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.side1 = side1;
        this.side2 = calculateSide2();
    }

    private double calculateSide2() {
        double distance = Point.distance(pointA, pointB);
        return Math.sqrt(side1 * side1 + distance * distance);
    }

    public List<Point> vertices() {
        Point center = new Point((pointA.getX() + pointB.getX()) / 2,
                (pointA.getY() + pointB.getY()) / 2);
        double halfSide1 = side1 / 2;
        double halfSide2 = side2 / 2;

        Point vertex1 = new Point(center.getX() - halfSide1, center.getY() - halfSide2);
        Point vertex2 = new Point(center.getX() - halfSide1, center.getY() + halfSide2);
        Point vertex3 = new Point(center.getX() + halfSide1, center.getY() + halfSide2);
        Point vertex4 = new Point(center.getX() + halfSide1, center.getY() - halfSide2);

        List<Point> vertices = new ArrayList<>();
        vertices.add(vertex1);
        vertices.add(vertex2);
        vertices.add(vertex3);
        vertices.add(vertex4);

        // Sort vertices in counter-clockwise order starting from pointA
        vertices.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.getY() - pointA.getY(), p1.getX() - pointA.getX());
            double angle2 = Math.atan2(p2.getY() - pointA.getY(), p2.getX() - pointA.getX());
            return Double.compare(angle1, angle2);
        });

        return vertices;
    }

    public double firstSide() {
        return side1;
    }

    public double secondSide() {
        return side2;
    }

    public double diagonal() {
        return Math.sqrt(side1 * side1 + side2 * side2);
    }

    public Point center() {
        return barycenter;
    }

    public double perimeter() {
        return 2 * (side1 + side2);
    }

    public double area() {
        return side1 * side2;
    }

    public void translate(final Point newCenter) {
        Point diff = newCenter.subtract(barycenter);
        pointA = pointA.add(diff);
        pointB = pointB.add(diff);
        barycenter = newCenter;
    }

    public void rotate(final double angle) {
        pointA = pointA.rotate(angle);
        pointB = pointB.rotate(angle);
    }

    public void scale(final double coefficient) {
        Point prevBarycenter = barycenter;
        side1 *= Math.abs(coefficient);
        side2 *= Math.abs(coefficient);
        translate(Point.ZERO);
        pointA = pointA.multiply(coefficient);
        pointB = pointB.multiply(coefficient);
        translate(prevBarycenter);
    }
}

class Square extends Rectangle {
    private final double side;

    public Square(Point pointA, Point pointB) {
        super(pointA, pointB, Point.distance(pointA, pointB));
        this.side = firstSide();
    }

    public double side() {
        return side;
    }

    public Circle circumscribedCircle() {
        Point center = new Point((pointA.getX() + pointB.getX()) / 2,
                (pointA.getY() + pointB.getY()) / 2);
        double radius = diagonal() / 2;
        return new Circle(center, radius);
    }

    public Circle inscribedCircle() {
        Point center = new Point((pointA.getX() + pointB.getX()) / 2,
                (pointA.getY() + pointB.getY()) / 2);
        double radius = side() / 2;
        return new Circle(center, radius);
    }
}

class Triangle extends Shape {

    protected Point A, B, C, barycenter;

    public Triangle(final Point A, final Point B, final Point C) {
        this.A = A;
        this.B = B;
        this.C = C;
        barycenter = new Point((A.getX() + B.getX() + C.getX()) / 3, (A.getY() + B.getY() + C.getY()) / 3);
    }

    public List<Point> vertices() {
        return List.of(A, B, C);
    }

    private Point circumscribedCircleCenter() {
        double xAB = A.getX() - B.getX();
        double yAB = A.getY() - B.getY();
        double xBC = B.getX() - C.getX();
        double yBC = B.getY() - C.getY();
        double xCA = C.getX() - A.getX();
        double yCA = C.getY() - A.getY();
        double Z = xAB * yCA - yAB * xCA;
        double Z1 = A.getX() * A.getX() + A.getY() * A.getY();
        double Z2 = B.getX() * B.getX() + B.getY() * B.getY();
        double Z3 = C.getX() * C.getX() + C.getY() * C.getY();
        double ZX = yAB * Z3 + yBC * Z1 + yCA * Z2;
        double ZY = xAB * Z3 + xBC * Z1 + xCA * Z2;
        return new Point(-ZX / 2 / Z, ZY / 2 / Z);
    }

    private double circumscribedCircleRadius() {
        double AB = Point.distance(A, B);
        double AC = Point.distance(A, C);
        double BC = Point.distance(B, C);
        return AB * BC * AC / 4 / area();
    }

    public Circle circumscribedCircle() {
        return new Circle(circumscribedCircleCenter(), circumscribedCircleRadius());
    }

    public Circle inscribedCircle() {
        double side1 = Point.distance(B, C);
        double side2 = Point.distance(A, C);
        double side3 = Point.distance(A, B);
        double sumSides = side1 + side2 + side3;
        double p = perimeter() / 2;

        double radius = Math.sqrt((p - side1) * (p - side2) * (p - side3) / p);
        double x = (side1 * A.getX() + side2 * B.getX() + side3 * C.getX()) / sumSides;
        double y = (side1 * A.getY() + side2 * B.getY() + side3 * C.getY()) / sumSides;

        return new Circle(new Point(x, y), radius);
    }

    public Point orthocenter() {
        double xCB = C.getX() - B.getX();
        double yCB = C.getY() - B.getY();
        double xCA = C.getX() - A.getX();
        double yCA = C.getY() - A.getY();
        double valCB = xCB * A.getX() + yCB * A.getY();
        double valCA = xCA * B.getX() + yCA * B.getY();

        double det = xCB * yCA - xCA * yCB;
        double detX = valCB * yCA - valCA * yCB;
        double detY = xCB * valCA - xCA * valCB;

        return new Point(detX / det, detY / det);
    }

    public Circle ninePointsCircle() {
        Point point = orthocenter().add(circumscribedCircleCenter()).divide(2.0);
        double radius = circumscribedCircleRadius() / 2;
        return new Circle(point, radius);
    }

    public Point center() {
        return barycenter;
    }

    public double perimeter() {
        double sideAB = Point.distance(A, B);
        double sideAC = Point.distance(A, C);
        double sideBC = Point.distance(B, C);
        return sideAB + sideBC + sideAC;
    }

    public double area() {
        return Math.abs((B.getX() - A.getX()) * (C.getY() - A.getY())
                - (C.getX() - A.getX()) * (B.getY() - A.getY())) / 2.0;
    }

    public void translate(final Point newCenter) {
        Point diff = newCenter.subtract(barycenter);
        A = A.add(diff);
        B = B.add(diff);
        C = C.add(diff);
        barycenter = newCenter;
    }

    public void rotate(final double angle) {
        A = A.rotate(angle);
        B = B.rotate(angle);
        C = C.rotate(angle);
    }

    public void scale(final double coefficient) {
        Point prevBarycenter = barycenter;
        translate(Point.ZERO);
        A = A.multiply(coefficient);
        B = B.multiply(coefficient);
        C = C.multiply(coefficient);
        translate(prevBarycenter);
    }
}

