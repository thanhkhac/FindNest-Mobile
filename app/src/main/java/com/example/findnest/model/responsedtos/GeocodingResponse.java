package com.example.findnest.model.responsedtos;

import org.osmdroid.util.GeoPoint;

public class GeocodingResponse
{
    private double lat;
    private double lon;

    public GeoPoint toGeoPoint() {
        return new GeoPoint(lat, lon);
    }
}
