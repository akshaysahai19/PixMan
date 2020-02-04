package com.test.pixman;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.snatik.storage.Storage;
import com.test.pixman.databinding.OperationsActivityBinding;
import com.test.pixman.utils.AppConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OperationsActivity extends AppCompatActivity {

    OperationsActivityBinding operationsActivityBinding;
    final static int FLIP_VERTICAL = 1;
    final static int FLIP_HORIZONTAL = 2;
    Bitmap image = null;
    Storage storage;
    File galleryFolder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        operationsActivityBinding = DataBindingUtil.setContentView(this, R.layout.operations_activity);
        loadImage();
        onClicks();

        try {
            createImageGallery();
        } catch (IOException e) {
            e.printStackTrace();
        }
        storage = new Storage(getApplicationContext());
    }

    private void updateImage() {
        operationsActivityBinding.image.setImageBitmap(image);
    }

    private void loadImage() {
        try {
            image = MediaStore.Images.Media.getBitmap(getContentResolver(),
                    Uri.parse(getIntent().getStringExtra(AppConstants.IMAGE_URI)));
            updateImage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flip(int type) {
        Matrix matrix = new Matrix();
        if (type == FLIP_VERTICAL) {
            matrix.preScale(1.0f, -1.0f);
        } else if (type == FLIP_HORIZONTAL) {
            matrix.preScale(-1.0f, 1.0f);
        }

        image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    private void changeOpacity() {
        Canvas canvas = new Canvas(image);
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(42);
        canvas.drawBitmap(image, 0, 0, alphaPaint);
    }

    private void onClicks() {
        operationsActivityBinding.preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateImage();
            }
        });

        operationsActivityBinding.flipH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flip(FLIP_HORIZONTAL);
            }
        });

        operationsActivityBinding.flipV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flip(FLIP_VERTICAL);
            }
        });

        operationsActivityBinding.opacity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                operationsActivityBinding.image.setAlpha(0.5f);
                changeOpacity();
            }
        });

        operationsActivityBinding.addText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                operationsActivityBinding.text.setVisibility(View.VISIBLE);
            }
        });

        operationsActivityBinding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkPermission();
                }
            }
        });

    }

    private void saveImage() {

        File file = null;
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(createImageFile(galleryFolder, false));
            image.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        storage.createFile("pix.png", image);

        Toast.makeText(OperationsActivity.this, "SAVED", Toast.LENGTH_SHORT).show();

        finish();

    }

    private void createImageGallery() throws IOException {
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        galleryFolder = new File(storageDirectory, getResources().getString(R.string.app_name));
        if (!galleryFolder.exists()) {
            boolean wasCreated = galleryFolder.mkdirs();
            if (!wasCreated) {
                Log.e("CapturedImages", "Failed to create directory");
            }
        }
    }

    private File createImageFile(File galleryFolder, boolean temp) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "image_" + timeStamp + "_";
        File file = File.createTempFile(imageFileName, ".png", galleryFolder);
        return file;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermission() {


        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
        };


        int permissionStorageWrite = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );

        int permissionStorageRead = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        if ((permissionStorageWrite == PackageManager.PERMISSION_GRANTED)
                && (permissionStorageRead == PackageManager.PERMISSION_GRANTED)) {

            saveImage();

            return true;
        } else {
            this.requestPermissions(permissions, 1);
            return false;
        }
    }

    /**
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsRequest = new ArrayList<>();
        ArrayList<String> permissionsRejected = new ArrayList<>();


        permissionsRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionsRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean someAccepted = false;
        boolean someRejected = false;
        for (String perms : permissionsRequest) {
            if (hasPermission(perms)) {
                someAccepted = true;
            } else {
                someRejected = true;
                permissionsRejected.add(perms);
            }
        }

        if (permissionsRejected.size() > 0) {
            someRejected = true;
        }

        if (someRejected) {
            Toast.makeText(OperationsActivity.this, "Required App permissions are rejected. Cannot proceed.", Toast.LENGTH_SHORT).show();
        }

        if (someAccepted && !someRejected) {

            saveImage();

        }
    }

    /**
     * @param permission
     * @return
     */
    protected boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return (ContextCompat.checkSelfPermission(OperationsActivity.this, permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    protected boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

}
