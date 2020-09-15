package net.medinacom.ayana.device;

public interface SnellenBoardListener {
    void onConnecting();
    void onConnected();
    void onConnectingFailed();
    void onDisconnecting();
    void onDisconnected();

    void onBlockOn(int newNum, int prevNum);
    void onBlockOff(int num);
    void onPointerMoved(int position);

    void onCommandError(String message);

    void onPointerOn(int arg1);

    void onPointerOff(int arg1);

    void onCalibSaved();

    void onSwitchMapped(int arg1, int arg2);
}
