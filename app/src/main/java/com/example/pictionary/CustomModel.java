package com.example.pictionary;

/**
 * Created by Lucian on 12/8/2015.
 *
 * this is a model (i.e. restricts the instantiation to a single static object available through classes)
 * which contains an interface (i.e. functions that have to be implemented/overriden when class is created)
 *
 */
public class CustomModel {
    public interface OnCustomStateListener {
        void stateChanged();
    }

    private static CustomModel mInstance;
    private OnCustomStateListener mListener;
    private boolean mState;

    private CustomModel() {}

    private float mxCoord;
    private float myCoord;

    public static CustomModel getInstance() {
        if(mInstance == null) {
            mInstance = new CustomModel();
        }
        return mInstance;
    }

    public void setListener(OnCustomStateListener listener) {
        mListener = listener;
    }

    public void changeState(boolean state) {
        if(mListener != null) {
            mState = state;
            notifyStateChange();
        }
    }

    public void changeState(float x, float y)
    {
        if(mListener !=null){
            mxCoord=x;
            myCoord=y;
            notifyStateChange();
        }
    }

    public boolean getState() {
        return mState;
    }

    public float getValueX(){
        return mxCoord;
    }

    public float getValueY(){
        return myCoord;
    }

    private void notifyStateChange() {
        mListener.stateChanged();
    }
}
