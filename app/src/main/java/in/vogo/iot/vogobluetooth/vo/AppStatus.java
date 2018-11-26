package in.vogo.iot.vogobluetooth.vo;

public class AppStatus {
    String device;
    String bluetoothConnection;
    String smsStatus;
    String unlockStartTime;
    String unlockLocation;
    String btPairSuccessTime;
    String smsReceivedTime;

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBluetoothConnection() {
        return bluetoothConnection;
    }

    public void setBluetoothConnection(String bluetoothConnection) {
        this.bluetoothConnection = bluetoothConnection;
    }

    public String getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(String smsStatus) {
        this.smsStatus = smsStatus;
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
                ", SMSStatus='" + smsStatus + '\'' +
                ", unlockStartTime='" + unlockStartTime + '\'' +
                ", unlockLocation='" + unlockLocation + '\'' +
                ", btPairSuccessTime='" + btPairSuccessTime + '\'' +
                ", smsReceivedTime='" + smsReceivedTime + '\'' +
                '}';
    }
}
