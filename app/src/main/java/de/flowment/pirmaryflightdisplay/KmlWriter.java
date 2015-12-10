package de.flowment.pirmaryflightdisplay;

import android.location.Location;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by Timo on 10.12.2015.
 */
public class KmlWriter {
    private PrintWriter printWriter;
    private List<Location> locationList;

    public KmlWriter(OutputStream outputStream) {
        this.printWriter = new PrintWriter(outputStream);
    }

    public void pushLocation(Location location) {
        this.locationList.add(location);
    }

    public void writeKml() {
        writeHeader();
        writePlacemark();
        writeFooter();
        this.printWriter.flush();
    }

    private void writeHeader() {
        this.printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        this.printWriter.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">");
    }

    private void writePlacemark() {
        this.printWriter.println("<Placemark>");
        this.printWriter.println("<name>gx:altitudeMode Example</name>");
        this.printWriter.println("<LookAt>");
        this.printWriter.println("<longitude>" + this.locationList.get(0).getLongitude() + "</longitude>");
        this.printWriter.println("<latitude>" + this.locationList.get(0).getLatitude() + "</latitude>");
        this.printWriter.println("<heading>-60</heading>");
        this.printWriter.println("<tilt>70</tilt>");
        this.printWriter.println("<range>6300</range>");
        this.printWriter.println("<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>");
        this.printWriter.println("</LookAt>");
        this.printWriter.println("<LineString>");
        this.printWriter.println("<extrude>1</extrude>");
        this.printWriter.println("<gx:altitudeMode>relativeToSeaFloor</gx:altitudeMode>");
        this.printWriter.println("<coordinates>");
        for (Location location : this.locationList) {
            this.printWriter.println(location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude());
        }
        this.printWriter.println("</coordinates>");
        this.printWriter.println("</LineString>");
        this.printWriter.println("</Placemark>");
    }

    private void writeFooter() {
        this.printWriter.println("</kml>");
    }
}
