package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

public class PictureDialogFragment extends DialogFragment {
    private static final String ARG_PICTURE = "picture";

    private ImageView mPhotoView;

    public static PictureDialogFragment newInstance(File picture) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_PICTURE, picture);
        PictureDialogFragment fragment = new PictureDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {   //TODO: "returns lager image"
        File photoFile = (File) getArguments().getSerializable(ARG_PICTURE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_picture, null);

        Bitmap bitmap = PictureUtils.getScaledBitmap(photoFile.getPath(), getActivity());

        mPhotoView = v.findViewById(R.id.dialog_picture);
        mPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity(), R.style.ThemeOverlay_AppCompat_Dialog)
                .setView(v)
                .create();
    }
}
