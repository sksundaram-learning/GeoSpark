/**
 * FILE: ShapefileRDDTest.java
 * PATH: org.datasyslab.geospark.formatMapper.shapefileParser.shapes.ShapefileRDDTest.java
 * Copyright (c) 2015-2017 GeoSpark Development Team
 * All rights reserved.
 */
package org.datasyslab.geospark.formatMapper.shapefileParser.shapes;

import com.vividsolutions.jts.geom.*;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.datasyslab.geospark.formatMapper.shapefileParser.ShapefileRDD;
import org.datasyslab.geospark.formatMapper.shapefileParser.boundary.BoundBox;
import org.datasyslab.geospark.spatialOperator.RangeQuery;
import org.datasyslab.geospark.spatialRDD.LineStringRDD;
import org.datasyslab.geospark.spatialRDD.PointRDD;
import org.datasyslab.geospark.spatialRDD.PolygonRDD;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.junit.*;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ShapefileRDDTest implements Serializable{

    /** The sc. */
    public static JavaSparkContext sc;

    /** The Input location. */
    public static String InputLocation;

    @BeforeClass
    public static void onceExecutedBeforeAll() {
        SparkConf conf = new SparkConf().setAppName("ShapefileRDDTest").setMaster("local[2]").set("spark.executor.cores","2");
        sc = new JavaSparkContext(conf);
        Logger.getLogger("org").setLevel(Level.WARN);
        Logger.getLogger("akka").setLevel(Level.WARN);
        //Hard code to a file in resource folder. But you can replace it later in the try-catch field in your hdfs system.
    }

    /**
     * Test if shapeRDD get correct number of shapes from .shp file
     * @throws IOException
     */
    @Test
    public void testLoadShapeFile() throws IOException {
        // load shape with geotool.shapefile
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/polygon").getPath();
        File file = new File(InputLocation);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        // load shapes with our tool
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc,InputLocation);
        Assert.assertEquals(shapefileRDD.getShapeRDD().collect().size(), collection.size());
        dataStore.dispose();
    }

    /**
     * test if shapeRDD load .shp fie with shape type = Polygon correctly.
     * @throws IOException
     */
    @Test
    public void testLoadShapeFilePolygon() throws IOException{
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/polygon").getPath();
        // load shape with geotool.shapefile
        File file = new File(InputLocation);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        FeatureIterator<SimpleFeature> features = collection.features();
        ArrayList<String> featureTexts = new ArrayList<String>();
        while(features.hasNext()){
            SimpleFeature feature = features.next();
            featureTexts.add(String.valueOf(feature.getDefaultGeometry()));
        }
        final Iterator<String> featureIterator = featureTexts.iterator();
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc,InputLocation);
        PolygonRDD spatialRDD = new PolygonRDD(shapefileRDD.getPolygonRDD());
        try {
			RangeQuery.SpatialRangeQuery(spatialRDD, new Envelope(-180,180,-90,90), false, false).count();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (Geometry geometry : shapefileRDD.getShapeRDD().collect()) {
            Assert.assertEquals(featureIterator.next(), geometry.toText());
        }
        dataStore.dispose();
    }

    /**
     * test if shapeRDD load .shp fie with shape type = PolyLine correctly.
     * @throws IOException
     */
    @Test
    public void testLoadShapeFilePolyLine() throws IOException{
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/polyline").getPath();
        // load shape with geotool.shapefile
        File file = new File(InputLocation);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        FeatureIterator<SimpleFeature> features = collection.features();
        ArrayList<String> featureTexts = new ArrayList<String>();
        while(features.hasNext()){
            SimpleFeature feature = features.next();
            featureTexts.add(String.valueOf(feature.getDefaultGeometry()));
        }
        final Iterator<String> featureIterator = featureTexts.iterator();
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc,InputLocation);
        LineStringRDD spatialRDD = new LineStringRDD(shapefileRDD.getLineStringRDD());
        try {
			RangeQuery.SpatialRangeQuery(spatialRDD, new Envelope(-180,180,-90,90), false, false).count();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (Geometry geometry : shapefileRDD.getShapeRDD().collect()) {
            Assert.assertEquals(featureIterator.next(), geometry.toText());
        }
        dataStore.dispose();
    }

    /**
     * Test if shapeRDD load shape type = MultiPoint correctly.
     * @throws IOException
     */
    @Test
    public void testLoadShapeFileMultiPoint() throws IOException{
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/multipoint").getPath();
        // load shape with geotool.shapefile
        File file = new File(InputLocation);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        FeatureIterator<SimpleFeature> features = collection.features();
        ArrayList<String> featureTexts = new ArrayList<String>();
        while(features.hasNext()){
            SimpleFeature feature = features.next();
            featureTexts.add(String.valueOf(feature.getDefaultGeometry()));
        }
        final Iterator<String> featureIterator = featureTexts.iterator();
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc,InputLocation);
        for (Geometry geometry : shapefileRDD.getShapeRDD().collect()) {
            Assert.assertEquals(featureIterator.next(), geometry.toText());
        }
        dataStore.dispose();
    }

    /**
     * Test if shapeRDD load shape type = Point correctly.
     * @throws IOException
     */
    @Test
    public void testLoadShapeFilePoint() throws IOException{
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/point").getPath();
        // load shape with geotool.shapefile
        File file = new File(InputLocation);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        FeatureIterator<SimpleFeature> features = collection.features();
        ArrayList<String> featureTexts = new ArrayList<String>();
        while(features.hasNext()){
            SimpleFeature feature = features.next();
            featureTexts.add(String.valueOf(feature.getDefaultGeometry()));
        }
        final Iterator<String> featureIterator = featureTexts.iterator();
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc,InputLocation);
        PointRDD spatialRDD = new PointRDD(shapefileRDD.getPointRDD());
        try {
			RangeQuery.SpatialRangeQuery(spatialRDD, new Envelope(-180,180,-90,90), false, false).count();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        for (Geometry geometry : shapefileRDD.getShapeRDD().collect()) {
            Assert.assertEquals(featureIterator.next(), geometry.toText());
        }
        dataStore.dispose();
    }

    /**
     * Test if shapeRDD load .dbf file correctly
     * @throws IOException
     */
    @Test
    public void testLoadDbfFile() throws IOException{
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/dbf").getPath();
        // load shape with geotool.shapefile
        File file = new File(InputLocation);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
                .getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
        FeatureIterator<SimpleFeature> features = collection.features();
        ArrayList<String> featureTexts = new ArrayList<String>();
        while(features.hasNext()){
            SimpleFeature feature = features.next();
            featureTexts.add(String.valueOf(feature.getDefaultGeometry()));
        }
        final Iterator<String> featureIterator = featureTexts.iterator();
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc,InputLocation);
        PolygonRDD spatialRDD = new PolygonRDD(shapefileRDD.getPolygonRDD());
        try {
			RangeQuery.SpatialRangeQuery(spatialRDD, new Envelope(-180,180,-90,90), false, false).count();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assert ((Polygon) spatialRDD.rawSpatialRDD.take(1).get(0)).getUserData().equals("20\t175\t00485050\t0500000US20175\t20175\tSeward\t06\t1655865960\t2777350");
        for (Geometry geometry : shapefileRDD.getShapeRDD().collect()) {
            Assert.assertEquals(featureIterator.next(), geometry.toText());
        }
        dataStore.dispose();
    }

    /**
     * Test if parse the boundary in header correctly
     * @throws IOException
     */
    @Test
    public void testParseBoundary() throws IOException{
        InputLocation = ShapefileRDDTest.class.getClassLoader().getResource("shapefiles/dbf").getPath();
        // load shapefile with geotools's reader
        ShpFiles shpFile = new ShpFiles(InputLocation + "/map.shp");
        GeometryFactory geometryFactory = new GeometryFactory();
        ShapefileReader gtlReader = new ShapefileReader(shpFile, false, true, geometryFactory);
        String gtlbounds =
                gtlReader.getHeader().minX() + ":" +
                gtlReader.getHeader().minY() + ":" +
                gtlReader.getHeader().maxX() + ":" +
                gtlReader.getHeader().maxY();
        // read shapefile by our reader
        ShapefileRDD shapefileRDD = new ShapefileRDD(sc, InputLocation);
        shapefileRDD.count();
        BoundBox bounds = shapefileRDD.getBoundBox(sc, InputLocation);
        String myBounds =
                bounds.getXMin() + ":" +
                bounds.getYMin() + ":" +
                bounds.getXMax() + ":" +
                bounds.getYMax();
        Assert.assertEquals(gtlbounds, myBounds);
        gtlReader.close();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        sc.stop();
    }

}