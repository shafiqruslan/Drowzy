package com.example.drowzy;

    public class LocationContent {

        public String name;
        public Double latitude;
        public Double longitude;
        public Double distance;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public LocationContent(String name, double latitude, double longitude,double distance) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distance = distance;
        }

        public LocationContent(){

        }

    }
