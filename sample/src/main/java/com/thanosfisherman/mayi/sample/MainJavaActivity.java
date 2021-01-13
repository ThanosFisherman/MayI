package com.thanosfisherman.mayi.sample;

import android.Manifest;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thanosfisherman.mayi.MayI;
import com.thanosfisherman.mayi.PermissionBean;
import com.thanosfisherman.mayi.PermissionToken;

import java.util.List;

import io.github.thanosfisherman.blueflow.sample.common.AndroidVersionsKt;

public class MainJavaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonContacts = findViewById(R.id.contacts_permission_button);
        buttonContacts.setOnClickListener(v -> MayI.withActivity(this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .onResult(permissionBean -> {
                    permissionResultSingle(permissionBean);
                })
                .onRationale((permissionBean, permissionToken) -> {
                    permissionRationaleSingle(permissionBean, permissionToken);
                })
                .check());

        Button buttonLocation = findViewById(R.id.location_permission_button);
        buttonLocation.setOnClickListener(v -> MayI.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .onResult(permissionBean -> {
                    permissionResultSingle(permissionBean);
                })
                .onRationale((permissionBean, permissionToken) -> {
                    permissionRationaleSingle(permissionBean, permissionToken);
                })
                .check());

        Button buttonAll = findViewById(R.id.all_permissions_button);
        buttonAll.setOnClickListener(v -> MayI.withActivity(this)
                .withPermissions(createPermissionsArray())
                .onRationale((permissionBeans, permissionToken) -> {
                    permissionRationaleMulti(permissionBeans, permissionToken);
                })
                .onResult(permissionBeans -> {
                    permissionResultMulti(permissionBeans);
                })
                .onErrorListener(e -> {
                    inCaseOfError(e);
                })
                .check());

    }


    private void permissionResultSingle(PermissionBean permission) {
        Toast.makeText(this, "PERMISSION RESULT " + permission, Toast.LENGTH_LONG).show();
    }

    private void permissionRationaleSingle(PermissionBean bean, PermissionToken token) {
        if (bean.getSimpleName().toLowerCase().contains("contacts")) {
            Toast.makeText(this, "Should show rationale for " + bean.getSimpleName() + " permission", Toast.LENGTH_LONG)
                    .show();
            token.skipPermissionRequest();
        } else {
            Toast.makeText(this, "Should show rationale for " + bean.getSimpleName() + " permission", Toast.LENGTH_LONG)
                    .show();
            token.continuePermissionRequest();
        }
    }

    private void permissionResultMulti(List<PermissionBean> permissions) {
        Toast.makeText(this, "MULTI PERMISSION RESULT " + permissions, Toast.LENGTH_LONG)
                .show();
    }

    private void permissionRationaleMulti(List<PermissionBean> permissions, PermissionToken token) {
        Toast.makeText(this, "Rationales for Multiple Permissions " + permissions, Toast.LENGTH_LONG)
                .show();
        token.continuePermissionRequest();
    }

    private void inCaseOfError(Exception e) {
        Toast.makeText(this, "ERROR " + e.toString(), Toast.LENGTH_SHORT).show();
    }

    private String[] createPermissionsArray() {
        final String[] permissions;
        if (AndroidVersionsKt.isAndroidQAndAbove()) {
            permissions = new String[3];
            permissions[0] = Manifest.permission.READ_CONTACTS;
            permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
            permissions[2] = Manifest.permission.ACCESS_BACKGROUND_LOCATION;
        } else {
            permissions = new String[2];
            permissions[0] = Manifest.permission.READ_CONTACTS;
            permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        return permissions;
    }
}
