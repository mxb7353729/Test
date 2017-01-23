package com.evialab.common;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeometryDemo {
	
	 private GeometryFactory geometryFactory = new GeometryFactory();

	    /**
	     * create a point
	     * @return
	     */
	    public Point createPoint(double x,double y){
	        Coordinate coord = new Coordinate(x,y);
	    	//Coordinate coord = new Coordinate(30,10);
	        Point point = geometryFactory.createPoint(coord);
	        return point;
	    }
	    /**
	     * create a point by WKT
	     * @return
	     * @throws ParseException 
	     */
	    public Point createPointByWKT() throws ParseException{
	        WKTReader reader = new WKTReader(geometryFactory );
	        Point point = (Point) reader.read("POINT (109.013388 32.715519)");
	        return point;
	    }
	    /**
	     * create multiPoint by wkt
	     * @return
	     */
	    public MultiPoint createMulPointByWKT()throws ParseException{
	        WKTReader reader = new WKTReader( geometryFactory );
	        MultiPoint mpoint = (MultiPoint) reader.read("MULTIPOINT(109.013388 32.715519,119.32488 31.435678)");
	        return mpoint;
	    }
	    /**
	     * 
	     * create a line
	     * @return
	     */
	    public LineString createLine(){
	        Coordinate[] coords  = new Coordinate[] {new Coordinate(2, 2), new Coordinate(2, 2)};
	        LineString line = geometryFactory.createLineString(coords);
	        return line;
	    }
	    
	    /**
	     * create a line by WKT
	     * @return
	     * @throws ParseException
	     */
	    public LineString createLineByWKT() throws ParseException{
	        WKTReader reader = new WKTReader( geometryFactory );
	        LineString line = (LineString) reader.read("LINESTRING(0 0, 2 0)");
	        return line;
	    }
	    /**
	     * create multiLine 
	     * @return
	     */
	    public MultiLineString createMLine(){
	        Coordinate[] coords1  = new Coordinate[] {new Coordinate(2, 2), new Coordinate(2, 2)};
	        LineString line1 = geometryFactory.createLineString(coords1);
	        Coordinate[] coords2  = new Coordinate[] {new Coordinate(2, 2), new Coordinate(2, 2)};
	        LineString line2 = geometryFactory.createLineString(coords2);
	        LineString[] lineStrings = new LineString[2];
	        lineStrings[0]= line1;
	        lineStrings[1] = line2;
	        MultiLineString ms = geometryFactory.createMultiLineString(lineStrings);
	        return ms;
	    }
	    
	    /**
	     * create multiLine by WKT
	     * @return
	     * @throws ParseException
	     */
	    public MultiLineString createMLineByWKT()throws ParseException{
	        WKTReader reader = new WKTReader( geometryFactory );
	        MultiLineString line = (MultiLineString) reader.read("MULTILINESTRING((0 0, 2 0),(1 1,2 2))");
	        return line;
	    }
	    /**
	     * create a Circle  创建�?个圆，圆�?(x,y) 半径RADIUS
	     * @param x
	     * @param y
	     * @param RADIUS
	     * @return
	     */
	    public Polygon createCircle(double x, double y, final double RADIUS){
	        final int SIDES = 32;//圆上面的点个�?
	        Coordinate coords[] = new Coordinate[SIDES+1];
	        for( int i = 0; i < SIDES; i++){
	            double angle = ((double) i / (double) SIDES) * Math.PI * 2.0;
	            double dx = Math.cos( angle ) * RADIUS;
	            double dy = Math.sin( angle ) * RADIUS;
	            coords[i] = new Coordinate( (double) x + dx, (double) y + dy );
	        }
	        coords[SIDES] = coords[0];
	        LinearRing ring = geometryFactory.createLinearRing( coords );
	        Polygon polygon = geometryFactory.createPolygon( ring, null );
	        return polygon;
	    }
	    /**
	     * create a polygon
	     * @return
	     */
	    public Polygon createPolygon() throws ParseException{
	    Coordinate[] coords  =
	    		   new Coordinate[] {new Coordinate(116.29,38.95), new Coordinate(115.36,39.95),
	    		                     new Coordinate(116.29,40.93), new Coordinate(117.36,39.95), 
	    		                     new Coordinate(116.32,39.28), new Coordinate(116.29,38.95) };
	    LinearRing ring = geometryFactory.createLinearRing( coords );
	    LinearRing holes[] = null; // use LinearRing[] to represent holes
	    Polygon polygon = geometryFactory.createPolygon(ring, holes );
	    return polygon;
	    }
	 /**
     * create a polygon(多边�?) by WKT
     * @return
     * @throws ParseException
     */
    public Polygon createPolygonByWKT() throws ParseException{
        WKTReader reader = new WKTReader(geometryFactory);
        Polygon polygon = (Polygon)reader.read("POLYGON((20 10, 30 0, 40 10, 30 20, 20 10))");
        return polygon;
    }
    
    /**
     * create multi polygon by wkt
     * @return
     * @throws ParseException
     */
    public MultiPolygon createMulPolygonByWKT() throws ParseException{
        WKTReader reader = new WKTReader(geometryFactory);
        MultiPolygon mpolygon = (MultiPolygon)reader.read("MULTIPOLYGON(((40 10, 30 0, 40 10, 30 20, 40 10),(30 10, 30 0, 40 10, 30 20, 30 10)))");
        return mpolygon;
    }
    /**
     * create GeometryCollection  contain point or multiPoint or line or multiLine or polygon or multiPolygon
     * @return
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {
    	//新建�?个对�?
        GeometryDemo gt = new GeometryDemo();
        //新建�?个点
        Point pt=gt.createPoint(116.29,39.94);
        System.out.println(pt);
        //新建�?个多边形
        Polygon pg=gt.createPolygon();
        System.out.println(pg);
        //判断点是否在多边形内
        boolean bl=pg.contains(pt);
        //输出判断结果
        System.out.println(bl);
    }
}
