package com.bignerdranch.android.criminalintent;

import android.annotation.TargetApi;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.IOException;

/**
 * Created by Dino10 on 2015. 8. 11..
 */
public class CrimeCameraFragment extends Fragment {
    private static final String TAG = "CrimeCameraFragment";

    private Camera mCamera;
    private SurfaceView mSurfaceView;

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime_camera, parent, false);

        Button takePictureButton = (Button)v
                .findViewById(R.id.crime_camera_takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mSurfaceView = (SurfaceView)v.findViewById(R.id.crime_camera_surfaceView);
        SurfaceHolder holder = mSurfaceView.getHolder();
        // setType()과 SURFACE_TYPE_PUSH_BUFFERS는 지금은 사용금지된 것들이다,
        // 그러나 허니콤 이전 장치에서 카메라 미리 보기를 하려면 필요하다.
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder) {
                // 이 surface를 미리 보기 영역으로 사용한다는 것을 카메라에게 알려준다.
                try {
                    if (mCamera != null) {
                        mCamera.setPreviewDisplay(holder);
                    }
                } catch (IOException exception) {
                    Log.e(TAG, "Error setting up preview display", exception);
                }
            }

            public void surfaceDestroyed(SurfaceHolder holder) {
                // 더 이상 이 surface를 사용할 수 없으므로 미리 보기를 중단한다.
                if (mCamera != null) {
                    mCamera.stopPreview();
                }
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
                if (mCamera == null) return;

                // surface의 크기가 변경되었으므로 카메라 미리 보기 크기를 변경한다.
                Camera.Parameters parameters = mCamera.getParameters();
                Size s = null;
                parameters.setPreviewSize(s.width, s.height);
                mCamera.setParameters(parameters);
                try {
                    mCamera.startPreview();
                } catch (Exception e) {
                    Log.e(TAG, "Could not start preview", e);
                    mCamera.release();
                    mCamera = null;
                }
            }
        });

        return v;
    }

    @TargetApi(9)
    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mCamera = Camera.open(0);
        } else {
            mCamera = Camera.open();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }
}
