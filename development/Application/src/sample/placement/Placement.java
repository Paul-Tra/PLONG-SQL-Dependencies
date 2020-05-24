package sample.placement;

import sample.Model.Relation;
import sample.Model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Placement {
    /**
     * internal node class
     */
    private class Node{
        protected double x ;
        protected double y ;
        protected double dx ;
        protected double dy ;

        public Node() {
        }

        /**
         * affects random values to x and y
         * @param width bound for x
         * @param height    bound for y
         */
        public void randPos(double width, double height) {
            this.x = new Random().nextDouble() * width;
            this.y = new Random().nextDouble() * height;
        }

        public void setPos(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void setDisp(double dx, double dy) {
            this.dx = dx;
            this.dy = dy;
        }

        /**
         * adds values to dx and dy attributes
         * @param dx
         * @param dy
         */
        public void addDisp(double dx, double dy) {
            this.dx += dx;
            this.dy += dy;
        }
    }

    /**
     * internal edge class
     */
    private class Edge{
        private Node u;
        private Node v;
        public Edge(Node depart, Node arrivee) {
            this.u = depart;
            this.v = arrivee;
        }
    }

    private int iterations;
    private boolean optimum = false;
    private int nb_node;
    private ArrayList<Node> nodes = new ArrayList<>();
    private ArrayList<Edge> edges = new ArrayList<>();
    private HashMap<Integer,String> map = new HashMap<>();
    private double width;
    private double heigth;
    private final int NB_ITERATONS = 1000;
    private static final double IDEAL_COEFFICIENT = 4.6;
    private double temperature; // is the maximum amount of movement allowed for a node

    public Placement(ArrayList<Transaction> transactions, ArrayList<Relation> relations,
                     double width, double height) {
        this.nb_node = transactions.size();
        this.width = width;
        this.heigth = height;
        fillAttributes(transactions, relations);
        fruchtermanReingold();
    }

    /**
     * applies Fruchterman-Reingold force-directed layout algorithm
     * @see Forced_Directed_Gaph.pdf
     */
    public void fruchtermanReingold(){
        temperature = width / 10;
        double cooling = temperature / (NB_ITERATONS + 1);
        double area = width * heigth;
        double k = Math.sqrt(area / nb_node)*IDEAL_COEFFICIENT;
        while (iterations < NB_ITERATONS) {
            manageFRPlacement(cooling, k);
        }

    }

    /**
     * applies a loop turn of Fruchterman-Reingold algorithm
     *
     * @param cooling   value to subtract to the temperature at each loop turn
     * @param k ideal size of edge
     */
    private void manageFRPlacement(double cooling, double k) {
        /* repulsion of each node between them */
        for (Node node : nodes) {
            node.setDisp(0, 0);
            for (Node node1 : nodes) {
                if (!node.equals(node1)) {
                    double dx = node.x - node1.x;
                    double dy = node.y - node1.y;
                    double delta = Math.sqrt((dx * dx) + (dy * dy));
                    if (delta != 0) {
                        double d = repulsiveForce(delta, k) / delta;
                        node.addDisp(dx * d, dy * d);
                    }
                }
            }
        }
        /* calculate attractive forces (only between neighbors) */
        /* attraction due to the links between two nodes*/
        for (Edge e : edges) {
            double dx = e.v.x - e.u.x;
            double dy = e.v.y - e.u.y;
            double delta = Math.sqrt(dx * dx + dy * dy);
            if (delta != 0) {
                double d = attractiveForce(delta, k) / delta;
                double ddx = dx * d;
                double ddy = dy * d;
                e.v.addDisp(-ddx, -ddy);
                e.u.addDisp(+ddx, +ddy);
            }
        }
        optimum = true;
        /* node displacement affectation */
        for (Node v : nodes) {
            double dx = v.dx, dy = v.dy;
            double delta = Math.sqrt((dx * dx) + (dy * dy));
            if (delta != 0) {
                double d = Math.min(delta, temperature) / delta;
                double x = v.x + dx * d, y = v.y + dy * d;
                x = Math.min(width, Math.max(0, x)) - width / 2;
                y = Math.min(heigth, Math.max(0, y)) - heigth / 2;
                v.setPos(Math.min(Math.sqrt((width * width / 4) - (y * y)),
                        Math.max(-Math.sqrt((width * width / 4) - (y * y)), x)) + (width / 2),

                        Math.min(Math.sqrt(Math.abs((heigth * heigth / 4) - (x * x))),
                                Math.max(-Math.sqrt(Math.abs((heigth * heigth / 4) - (x * x))),
                                        y))+ (heigth / 2));
            }
        }
        temperature -= cooling;
        iterations++;
    }

    /**
     * applies repulsion force from d and k
     * @param d
     * @param k
     * @return the corresponding value from parameters with repulsive force applying
     */
    private double repulsiveForce(double d, double k) {
        return (k * k) / d;
    }

    /**
     * applies attraction force from d and k
     * @param d
     * @param k
     * @return the corresponding value from parameters with attractive force applying
     */
    private double attractiveForce(double d, double k) {
        return (d * d) / k;
    }

    /**
     * places the transactions at the same positions as the corresponding nodes
     *
     * @param transactions list of transaction that we want to place
     */
    public void placementTransaction(ArrayList<Transaction> transactions) {
        if (transactions.size() != nodes.size()) {
            System.out.println("there is an issue with th transactions list");
            return;
        }
        for (int i = 0; i < nodes.size(); i++) {
            transactions.get(i).getRectangle().setLayoutX((int) nodes.get(i).x);
            transactions.get(i).getRectangle().setLayoutY((int) nodes.get(i).y);
        }
    }

    /**
     * looks after the filling of the different structures
     *
     * @param transactions
     * @param relations
     */
    private void fillAttributes(ArrayList<Transaction> transactions, ArrayList<Relation> relations){
        initNodes();
        fillMap(transactions);
        initEdges(relations);
    }

    /**
     * fill the map with transaction's id as content from transaction's list
     *
     * @param transactions transaction's list
     */
    private void fillMap(ArrayList<Transaction> transactions) {
        // begin at 1 and not 0 to have a default bad value as 0 for test
        for (int i = 1; i < transactions.size()+1; i++) {
            map.put(i, transactions.get(i-1).getId());
        }
    }

    /**
     * Initialize and put nodes in the node list with random coordinates
     */
    private void initNodes() {
        for (int i = 0; i < nb_node ; i++) {
            Node n = new Node();
            n.randPos(width, heigth);
            nodes.add(n);
        }
    }

    /**
     * Initialize the edges corresponding to the relations
     *
     * @param relations relations that we to initialize under edge's shape
     */
    private void initEdges(ArrayList<Relation> relations){
        for (int i = 0; i < relations.size(); i++) {
            int u = getMapIntFromString(relations.get(i).getSource().getId());
            int v = getMapIntFromString(relations.get(i).getTarget().getId());
            Edge e = new Edge(nodes.get(u), nodes.get(v));
            edges.add(e);
        }
    }

    /**
     * find a key in by the content in the map
     *
     * @param s content that we want to match in the map to find its key
     * @return  the key of the content representing by s
     */
    private int getMapIntFromString(String s) {
        for (int i = 1; i < map.size()  ; i++) {
            if (map.get(i).equals(s)) {
                return i;
            }
        }
        return 0; // default bad value
    }

}
/*
// calculate repulsive forces (from every vertex to every other)
		for (Vertex v : graph.vertexSet()) {
			// reset displacement vector for new calculation
			v.getDisp().set(0, 0);
			for (Vertex u : graph.vertexSet()) {
				if (!v.equals(u)) {
					// normalized difference position vector of v and u
					Vector2d deltaPos = new Vector2d();
					deltaPos.sub(v.getPos(), u.getPos());
					double length = deltaPos.length();
					deltaPos.normalize();

					// displacement depending on repulsive force
					deltaPos.scale(this.forceRepulsive(length, k));
					v.getDisp().add(deltaPos);
				}
			}
		}

		// calculate attractive forces (only between neighbors)
		for (Edge e : graph.edgeSet()) {

			// normalized difference position vector of v and u
			Vector2d deltaPos = new Vector2d();
			deltaPos.sub(e.getV().getPos(), e.getU().getPos());
			double length = deltaPos.length();
			deltaPos.normalize();

			// displacements depending on attractive force
			deltaPos.scale(this.forceAttractive(length, k));

			e.getV().getDisp().sub(deltaPos);
			e.getU().getDisp().add(deltaPos);
		}

		// assume equilibrium
		equilibriumReached = true;

		for (Vertex v : graph.vertexSet()) {

			Vector2d disp = new Vector2d(v.getDisp());
			double length = disp.length();

			// no equilibrium if one vertex has too high net force
			if (length > criterion) {
				equilibriumReached = false;
			}
			// System.out.print((int)length + "; ");
			// limit maximum displacement by temperature t
			disp.normalize();
			disp.scale(Math.min(length, t));
			v.getPos().add(disp);

			// prevent being displaced outside the frame
			v.getPos().x = Math.min(frameWidth, Math.max(0.0, v.getPos().x));
			v.getPos().y = Math.min(frameHeight, Math.max(0.0, v.getPos().y));
		}
		// System.out.println();
		// reduce the temperature as the layout approaches a better
		// configuration but always let vertices move at least 1px
		t = Math.max(t * (1 - coolingRate), 1);

		// System.out.println("t: " + (float) t);

		try {
			Thread.sleep(delay);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		iteration++;
	}
 */