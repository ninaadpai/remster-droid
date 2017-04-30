package dev.io.remster;

/**
 * Created by ninaadpai on 4/29/17.
 */

class City {
    String cityName, imgUrl;

    public City(String cityName, String imgUrl) {
        this.cityName = cityName;
        this.imgUrl = imgUrl;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    @Override
    public String toString() {
        return "City{" +
                "cityName='" + cityName + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                '}';
    }
}
