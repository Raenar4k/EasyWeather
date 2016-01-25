package com.raenarapps.easyweather.data;

import android.provider.BaseColumns;

public class WeatherContract {

    public static final class LocationEntry implements BaseColumns {
        public static final String TABLE_NAME = "location";

        //Setting that will be used in OWM query as the location parameter
        public static final String COLUMN_LOCATION_PREF = "location_pref";

        //Coordinates of the location, to be used with maps intent
        public static final String COLUMN_COORD_LAT = "lat";
        public static final String COLUMN_COORD_LONG = "long";

        //City name of the location
        public static final String COLUMN_CITY_NAME = "city_name";
    }

    public static final class WeatherEntry implements BaseColumns {

        public static final String TABLE_NAME = "weather";

        // Column with the foreign key into the location table.
        public static final String COLUMN_LOC_KEY = "location_id";

        // Date, stored as long in milliseconds - unix time
        public static final String COLUMN_DATE = "date";

        // Weather id as returned by API, to identify the icon to be used
        public static final String COLUMN_WEATHER_ID = "weather_id";

        // Short description of the weather, as provided by API
        public static final String COLUMN_SHORT_DESC = "short_desc";

        // Min and max temperatures for the day (stored as floats)
        public static final String COLUMN_MIN_TEMP = "min";
        public static final String COLUMN_MAX_TEMP = "max";

        // Humidity is stored as a float representing percentage
        public static final String COLUMN_HUMIDITY = "humidity";

        // Atmospheric pressure, hPa, stored in float
        public static final String COLUMN_PRESSURE = "pressure";

        // Windspeed is stored as a float representing windspeed  mph
        public static final String COLUMN_WIND_SPEED = "wind";

        // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats
        public static final String COLUMN_DEGREES = "degrees";
    }
}
