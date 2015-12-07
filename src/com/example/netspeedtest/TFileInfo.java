package com.example.netspeedtest;


public class TFileInfo
{

	private double speed;
	private int finishByte;
	private int totalByte;
	private String fileUrl;
	
	
	public double getSpeed() {
		return speed;
	}
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	public int getFinishByte() {
		return finishByte;
	}
	public void setFinishByte(int hadfinishByte) {
		this.finishByte = hadfinishByte;
	}
	
	public void finishBytePlus(){
		this.finishByte++;
	}
	
	public int getTotalByte() {
		return totalByte;
	}
	public void setTotalByte(int totalByte) {
		this.totalByte = totalByte;
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	
}
