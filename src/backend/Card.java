package backend;

class Card {

    public static final int SQUIGGLE = 0;
    public static final int OVAL = 1;
    public static final int DIAMOND = 2;

    public static final int SOLID = 0;
    public static final int STRIPE = 1;
    public static final int OUTLINE = 2;

    public static final int RED = 0;
    public static final int GREEN = 1;
    public static final int PURPLE = 2;

    private int color;
    private int shape;
    private int fill;
    private int num;

    Card(int id) {
        this(id / 27, (id % 27) / 9, (id % 9) / 3, id % 3);
    }

    private Card(int color, int shape, int fill, int num) {
        this.color = color;
        this.shape = shape;
        this.fill = fill;
        this.num = num;
    }

    int getColor() {
        return this.color;
    }

    int getShape() {
        return this.shape;
    }

    int getFill() {
        return this.fill;
    }

    int getNum() {
        return this.num;
    }

}
