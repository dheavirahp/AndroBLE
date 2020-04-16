package com.example.androble;

public class Vehicle {
    private String Image;
    private String plateNo;
    private String bikeType;
    private String bikeMerk;


    public Vehicle(String Image, String plateNo, String bikeType, String bikeMerk) {
        this.Image = Image;
        this.plateNo = plateNo;
        this.bikeType = bikeType;
        this.bikeMerk = bikeMerk;


    }
    public void setImage(String  Image){
        this.Image = Image;
    }

    public void setPlateNo(String plateNo){
        this.plateNo = plateNo;
    }

    public void setBikeType(String bikeType){
        this.bikeType = bikeType;
    }

    public void setBikeMerk(String bikeMerk){
        this.bikeMerk = bikeMerk;
    }

    public String getImage(){
        return this.Image;
    }

    public String getPlateNo(){
        return this.plateNo;
    }

    public String getBikeType(){
        return this.bikeType;
    }

    public String getBikeMerk(){
        return this.bikeMerk;
    }

}
