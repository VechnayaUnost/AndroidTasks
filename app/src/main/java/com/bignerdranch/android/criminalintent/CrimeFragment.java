package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_PICTURE = "DialogPicture";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    private static final int REQUEST_PHOTO = 3;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 4;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mCallSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;

    private String mSuspectId;
    private int mPhotoWidth;
    private int mPhotoHeight;

    public static CrimeFragment newInstance(UUID crimeId) {     //TODO: "join args to Fragment"
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public CharSequence formatDate(Date date) {     //TODO: "get format date"
        return DateFormat.format("EEEE, MMM d, yyyy", date);
    }

    public CharSequence formatTime(Date time) {     //TODO: "get format time"
        return DateFormat.format("h:mm a", time);
    }

    public void returnResult() {    //TODO: "return value to activity-host"
        getActivity().setResult(Activity.RESULT_OK, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);     //TODO: "transmission Crime"
        setHasOptionsMenu(true);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                //
            }

            @Override
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mCrime.setTitle(c.toString());
                returnResult();
            }

            @Override
            public void afterTextChanged(Editable c) {
                //
            }
        });

        mDateButton = v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getTime());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                returnResult();
            }
        });

        mReportButton = v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {   //TODO: "send a crime report"
            @Override
            public void onClick(View v) {
                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setChooserTitle(getString(R.string.send_report))
                        .createChooserIntent();
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {      //TODO: "choose a contact"
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) //TODO: "check READ_CONTACTS permission"
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[] {Manifest.permission.READ_CONTACTS},
                            MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                } else {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            }
        });

        final Intent callContact = new Intent(Intent.ACTION_DIAL);
        mCallSuspectButton = v.findViewById(R.id.call_suspect);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {      //TODO: "call a suspect"
            @Override
            public void onClick(View v) {
                Uri number = Uri.parse("tel:" + mCrime.getPhone());
                callContact.setData(number);
                startActivity(callContact);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
            if(mCrime.getPhone() != null) {
                mCallSuspectButton.setText(mCrime.getPhone());
                mCallSuspectButton.setEnabled(true);
            }
        }

        PackageManager packageManager = getActivity().getPackageManager();  //TODO: "protection from the absence of contact applications"
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        mPhotoButton = v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);

        if (canTakePhoto) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }

        mPhotoButton.setOnClickListener(new View.OnClickListener() {    //TODO: "take photo"
            @Override
            public void onClick(View v) {
                requestStoragePermission();
                Dexter.withActivity(getActivity())          //TODO: "CAMERA permission"
                        .withPermission(Manifest.permission.CAMERA)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                startActivityForResult(captureImage, REQUEST_PHOTO);
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                // check for permanent denial of permission
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                       .check();
            }
        });

        mPhotoView = v.findViewById(R.id.crime_photo);
        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();       //TODO: "effective photo upload"
        if (observer.isAlive()) {
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPhotoView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mPhotoWidth = mPhotoView.getMeasuredWidth();
                    mPhotoHeight = mPhotoView.getMeasuredHeight();

                    updatePhotoView();
                }
            });
        }
        mPhotoView.setOnClickListener(new View.OnClickListener() {      //TODO: "output of the enlarged image"
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                PictureDialogFragment dialog = PictureDialogFragment.newInstance(mPhotoFile);
                dialog.show(manager, DIALOG_PICTURE);
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {     //TODO: "populating the menu resource"
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {      //TODO: "reaction to the selection of the menu item"
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {    //TODO: "getting data from the Activity"
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
            returnResult();
        } else if (requestCode == REQUEST_TIME) {
            Date time = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setTime(time);
            updateTime();
            returnResult();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            String suspect = getContactName(data);
            mCrime.setSuspect(suspect);
            mSuspectButton.setText(suspect);

            String phone = getContactPhone();
            mCrime.setPhone(phone);
            if(phone != null) {
                mCallSuspectButton.setEnabled(true);
                mCallSuspectButton.setText(phone);
            } else {
                mCallSuspectButton.setEnabled(false);
                mCallSuspectButton.setText(R.string.call_suspect_text);
            }

            returnResult();
        } else if (requestCode == REQUEST_PHOTO) {
            updatePhotoView();
            returnResult();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
            }
        }
    }

    private void requestStoragePermission() {   //TODO: "read and write external storage permission"
        Dexter.withActivity(getActivity())
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        //
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

    private String getContactName(Intent data) {
        Uri contactUri = data.getData();

        String[] queryFields = new String[] {
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID
        };

        Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
        try {
            if (c.getCount() == 0) {
                return null;
            }
            c.moveToFirst();
            String suspect = c.getString(0);
            mSuspectId = c.getString(1);
            return suspect;
        } finally {
            c.close();
        }
    }

    private String getContactPhone() {
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] queryFields = new String[] {
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String selectionString = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";

        Cursor c = getActivity().getContentResolver().query(phoneUri, queryFields, selectionString, new String[]{mSuspectId}, null);

        try {
            if (c.getCount() == 0) {
                return null;
            }
            c.moveToFirst();
            String phone = c.getString(0);
            return phone;
        } finally {
            c.close();
        }
    }

    private void updateDate() {
        mDateButton.setText(formatDate(mCrime.getDate()).toString());
    }

    private void updateTime() {
        mTimeButton.setText(formatTime(mCrime.getTime()).toString());
    }

    private String getCrimeReport() {   //TODO: "returns a full report"
        String solvedString = null;
        if(mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateString = formatDate(mCrime.getDate()).toString();

        String timeString = formatTime(mCrime.getTime()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, timeString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
            mPhotoView.setEnabled(false);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoWidth, mPhotoHeight);
            mPhotoView.setImageBitmap(bitmap);
            mPhotoView.setEnabled(true);
        }
    }
}
