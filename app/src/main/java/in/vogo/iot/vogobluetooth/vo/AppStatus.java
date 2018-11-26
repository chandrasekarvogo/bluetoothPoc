package in.vogo.iot.vogobluetooth.vo;

public class AppStatus {
    String bluetoothConnection;
    String SMSStatus;
    String unlockStartTime;
    String unlockLocation;
    String btPairSuccessTime;
    String smsReceivedTime;

    public String getBluetoothConnection() {
        return bluetoothConnection;
    }

    public void setBluetoothConnection(String bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public String getSMSStatus() {
        return SMSStatus;
    }

    public void setSMSStatus(String SMSStatus) {
        this.SMSStatus = SMSStatus;
    }

    public String getUnlockStartTime() {
        return unlockStartTime;
    }

    public void setUnlockStartTime(String unlockStartTime) {
        this.unlockStartTime = unlockStartTime;
    }

    public String getUnlockLocation() {
        return unlockLocation;
    }

    public void setUnlockLocation(String unlockLocation) {
        this.unlockLocation = unlockLocation;
    }

    public String getBtPairSuccessTime() {
        return btPairSuccessTime;
    }

    public void setBtPairSuccessTime(String btPairSuccessTime) {
        this.btPairSuccessTime = btPairSuccessTime;
    }

    public String getSmsReceivedTime() {
        return smsReceivedTime;
    }

    public void setSmsReceivedTime(String smsReceivedTime) {
        this.smsReceivedTime = smsReceivedTime;
    }

    @Override
    public String toString() {
        return "AppStatus{" +
                "bluetoothConnection='" + bluetoothConnection + '\'' +
                ", SMSStatus='" + SMSStatus + '\'' +
                ", unlockStartTime='" + unlockStartTime + '\'' +
                ", unlockLocation='" + unlockLocation + '\'' +
                ", btPairSuccessTime='" + btPairSuccessTime + '\'' +
                ", smsReceivedTime='" + smsReceivedTime + '\'' +
                '}';
    }
}
