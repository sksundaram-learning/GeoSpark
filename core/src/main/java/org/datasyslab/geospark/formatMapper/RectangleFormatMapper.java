/**
 * FILE: RectangleFormatMapper.java
 * PATH: org.datasyslab.geospark.formatMapper.RectangleFormatMapper.java
 * Copyright (c) 2015-2017 GeoSpark Development Team
 * All rights reserved.
 */
package org.datasyslab.geospark.formatMapper;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.datasyslab.geospark.enums.FileDataSplitter;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.jts2geojson.GeoJSONReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class RectangleFormatMapper.
 */
public class RectangleFormatMapper extends FormatMapper implements FlatMapFunction<Iterator<String>, Polygon>
{

	/**
	 * Instantiates a new rectangle format mapper.
	 *
	 * @param Splitter the splitter
	 * @param carryInputData the carry input data
	 */
	public RectangleFormatMapper(FileDataSplitter Splitter, boolean carryInputData) {
		super(Splitter, carryInputData);
	}

	/**
	 * Instantiates a new rectangle format mapper.
	 *
	 * @param startOffset the start offset
	 * @param endOffset the end offset
	 * @param Splitter the splitter
	 * @param carryInputData the carry input data
	 */
	public RectangleFormatMapper(Integer startOffset, Integer endOffset, FileDataSplitter Splitter,
			boolean carryInputData) {
		super(startOffset, endOffset, Splitter, carryInputData);
	}

	@Override
	public Iterator<Polygon> call(Iterator<String> stringIterator) throws Exception {
		MultiPolygon multiSpatialObjects = null;
		List result= new ArrayList<Polygon>();
		Double x1,x2,y1,y2;
		LinearRing linear;
		while (stringIterator.hasNext()) {
			String line = stringIterator.next();
			try {
				switch (splitter) {
					case CSV:
						lineSplitList = Arrays.asList(line.split(splitter.getDelimiter()));
						x1 = Double.parseDouble(lineSplitList.get(this.startOffset));
						x2 = Double.parseDouble(lineSplitList.get(this.startOffset + 2));
						y1 = Double.parseDouble(lineSplitList.get(this.startOffset + 1));
						y2 = Double.parseDouble(lineSplitList.get(this.startOffset + 3));
						coordinates = new Coordinate[5];
						coordinates[0] = new Coordinate(x1, y1);
						coordinates[1] = new Coordinate(x1, y2);
						coordinates[2] = new Coordinate(x2, y2);
						coordinates[3] = new Coordinate(x2, y1);
						coordinates[4] = coordinates[0];
						linear = fact.createLinearRing(coordinates);
						spatialObject = new Polygon(linear, null, fact);
						if (this.carryInputData) {
							spatialObject.setUserData(line);
						}
						result.add(spatialObject);
						break;
					case TSV:
						lineSplitList = Arrays.asList(line.split(splitter.getDelimiter()));
						x1 = Double.parseDouble(lineSplitList.get(this.startOffset));
						x2 = Double.parseDouble(lineSplitList.get(this.startOffset + 2));
						y1 = Double.parseDouble(lineSplitList.get(this.startOffset + 1));
						y2 = Double.parseDouble(lineSplitList.get(this.startOffset + 3));
						coordinates = new Coordinate[5];
						coordinates[0] = new Coordinate(x1, y1);
						coordinates[1] = new Coordinate(x1, y2);
						coordinates[2] = new Coordinate(x2, y2);
						coordinates[3] = new Coordinate(x2, y1);
						coordinates[4] = coordinates[0];
						linear = fact.createLinearRing(coordinates);
						spatialObject = new Polygon(linear, null, fact);
						if (this.carryInputData) {
							spatialObject.setUserData(line);
						}
						result.add(spatialObject);
						break;
					case GEOJSON:
						GeoJSONReader reader = new GeoJSONReader();
						spatialObject = reader.read(line);
						if (line.contains("Feature")) {
							Feature feature = (Feature) GeoJSONFactory.create(line);
							spatialObject = reader.read(feature.getGeometry());
						} else {
							spatialObject = reader.read(line);
						}
						if (spatialObject instanceof MultiPolygon) {
                	/*
                	 * If this line has a "Multi" type spatial object, GeoSpark separates them to a list of single objects
                	 * and assign original input line to each object.
                	 */
							multiSpatialObjects = (MultiPolygon) spatialObject;
							for (int i = 0; i < multiSpatialObjects.getNumGeometries(); i++) {
								spatialObject = multiSpatialObjects.getGeometryN(i);
								x1 = spatialObject.getEnvelopeInternal().getMinX();
								x2 = spatialObject.getEnvelopeInternal().getMaxX();
								y1 = spatialObject.getEnvelopeInternal().getMinY();
								y2 = spatialObject.getEnvelopeInternal().getMaxY();
								coordinates = new Coordinate[5];
								coordinates[0] = new Coordinate(x1, y1);
								coordinates[1] = new Coordinate(x1, y2);
								coordinates[2] = new Coordinate(x2, y2);
								coordinates[3] = new Coordinate(x2, y1);
								coordinates[4] = coordinates[0];
								linear = fact.createLinearRing(coordinates);
								spatialObject = new Polygon(linear, null, fact);
								if (this.carryInputData) {
									spatialObject.setUserData(line);
								}
								result.add(spatialObject);
							}
						} else {
							x1 = spatialObject.getEnvelopeInternal().getMinX();
							x2 = spatialObject.getEnvelopeInternal().getMaxX();
							y1 = spatialObject.getEnvelopeInternal().getMinY();
							y2 = spatialObject.getEnvelopeInternal().getMaxY();
							coordinates = new Coordinate[5];
							coordinates[0] = new Coordinate(x1, y1);
							coordinates[1] = new Coordinate(x1, y2);
							coordinates[2] = new Coordinate(x2, y2);
							coordinates[3] = new Coordinate(x2, y1);
							coordinates[4] = coordinates[0];
							linear = fact.createLinearRing(coordinates);
							spatialObject = new Polygon(linear, null, fact);
							if (this.carryInputData) {
								spatialObject.setUserData(line);
							}
							result.add(spatialObject);
						}
						break;
					case WKT:
						lineSplitList = Arrays.asList(line.split(splitter.getDelimiter()));
						WKTReader wktreader = new WKTReader();
						spatialObject = wktreader.read(lineSplitList.get(this.startOffset));
						if (spatialObject instanceof MultiPolygon) {
							multiSpatialObjects = (MultiPolygon) spatialObject;
							for (int i = 0; i < multiSpatialObjects.getNumGeometries(); i++) {
                    	/*
                    	 * If this line has a "Multi" type spatial object, GeoSpark separates them to a list of single objects
                    	 * and assign original input line to each object.
                    	 */
								spatialObject = multiSpatialObjects.getGeometryN(i);
								x1 = spatialObject.getEnvelopeInternal().getMinX();
								x2 = spatialObject.getEnvelopeInternal().getMaxX();
								y1 = spatialObject.getEnvelopeInternal().getMinY();
								y2 = spatialObject.getEnvelopeInternal().getMaxY();
								coordinates = new Coordinate[5];
								coordinates[0] = new Coordinate(x1, y1);
								coordinates[1] = new Coordinate(x1, y2);
								coordinates[2] = new Coordinate(x2, y2);
								coordinates[3] = new Coordinate(x2, y1);
								coordinates[4] = coordinates[0];
								linear = fact.createLinearRing(coordinates);
								spatialObject = new Polygon(linear, null, fact);
								if (this.carryInputData) {
									spatialObject.setUserData(line);
								}
								result.add(spatialObject);
							}
						} else {
							x1 = spatialObject.getEnvelopeInternal().getMinX();
							x2 = spatialObject.getEnvelopeInternal().getMaxX();
							y1 = spatialObject.getEnvelopeInternal().getMinY();
							y2 = spatialObject.getEnvelopeInternal().getMaxY();
							coordinates = new Coordinate[5];
							coordinates[0] = new Coordinate(x1, y1);
							coordinates[1] = new Coordinate(x1, y2);
							coordinates[2] = new Coordinate(x2, y2);
							coordinates[3] = new Coordinate(x2, y1);
							coordinates[4] = coordinates[0];
							linear = fact.createLinearRing(coordinates);
							spatialObject = new Polygon(linear, null, fact);
							if (this.carryInputData) {
								spatialObject.setUserData(line);
							}
							result.add(spatialObject);
						}
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result.iterator();
	}
}
