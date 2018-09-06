package info.atiar.inventory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import bp.BP;
import bp.MyLocationService;
import bp.SharedPrefarences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rettrofit.APIClient;
import rettrofit.APIInterface;
import rettrofit.DataModel;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "MainActivity Atiar";

    EditText editText;
    Button button;
    private static APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermissions();
        apiInterface = APIClient.getClient().create(APIInterface.class);
        BP.setPublicIP(this);
        editText = findViewById(R.id.usrusr);

        Intent serviceIntent = new Intent(this, MyLocationService.class);
        startService(serviceIntent);

    }

    public void buttonOn(View v) {
        if (editText.getText().toString().trim().equals("") || editText.getText().toString().trim().length() == 0) {
            editText.setError(getString(R.string.warning));
            final Animation animShake = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
            editText.startAnimation(animShake);
        } else {
            //set the inventory no to sharedpreferences.
            BP.setInventoryNumber(getApplicationContext(), editText.getText().toString());
            //clear the edittext
            editText.setText(null);

            NoteSyncJob.scheduleJob();

            try{
                codeToRun(this);
            }catch (Exception e){
                e.printStackTrace();
            }

            MainActivity.super.onBackPressed();


        }
    }

    public static void addDataToServer(String cur_lat, String cur_long,String version_name,String device_name,String mac_address,
                                       String local_ip, String isMobileDevice,String inventory_no, String OS,String PublicIp,
                                       String HardDisk,String CPU,String serial_no,String RAM) {
        //final DataModel dataModel = new DataModel(cur_lat, cur_long);

        Call<DataModel> call1 = apiInterface.addRecord(cur_lat,cur_long,version_name,device_name,mac_address,local_ip,isMobileDevice,inventory_no,OS,PublicIp,HardDisk,CPU,serial_no,RAM);
        call1.enqueue(new Callback<DataModel>() {

            @Override
            public void onResponse(Call<DataModel> call, Response<DataModel> response) {
                Log.e("MainActivity",response.body().toString());
            }

            @Override
            public void onFailure(Call<DataModel> call, Throwable t) {
                Log.e("MainActivity - error", t.getLocalizedMessage());
            }

        });
    }



    private void codeToRun(Context context){

        BP.t(context,"NoteSycnJob Atiar","Task/Service Running");
        //This task takes 7 seconds to complete.
        BP.setPublicIP(context);

        logTag(context);

        addDataToServer(BP.getCurLat(context),
                BP.getCurLong(context),
                BP.getVersionname(),
                BP.getDevicename(),
                BP.getDeviceMac(),
                BP.getDeviceLocalIP(),
                "Yes",
                BP.getInventoryNumber(context),
                "Android",
                BP.getPublicIP(context),
                BP.getTotalInternalMemorySize() +" : " + BP.getTotalExternalMemorySize(),
                BP.getCPUDetails(),
                BP.getDeviceIMEI(context),
                BP.getTotalRAM()
        );
    }


    private void logTag(Context context) {

        BP.t(context, LOG_TAG, "Version Name: "+BP.getVersionname());
        BP.t(context, LOG_TAG, "Device Name: "+BP.getDevicename());
        BP.t(context, LOG_TAG, "Mac: "+BP.getDeviceMac());
        BP.t(context, LOG_TAG, "Local IP: "+BP.getDeviceLocalIP());
        BP.t(context, LOG_TAG, "Public IP: "+BP.getPublicIP(context));
        BP.t(context, LOG_TAG, "Inventory: "+BP.getInventoryNumber(context));
        BP.t(context, LOG_TAG, "IMEI: "+BP.getDeviceIMEI(context));
        BP.t(context, LOG_TAG, "Inter Memory: "+BP.getTotalInternalMemorySize());
        BP.t(context, LOG_TAG, "External Memory: "+BP.getTotalExternalMemorySize());
        BP.t(context, LOG_TAG, "Ram: "+BP.getTotalRAM());
        BP.t(context, LOG_TAG, "Location: "+ SharedPrefarences.getPreference(context, "location"));

    }















    private MainActivity mActivity;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_PERMISSIONS1 = 112;
    private static final String PERMISSIONS_REQUIRED[] = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private boolean checkPermission(String permissions[]) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void checkPermissions() {
        boolean permissionsGranted = checkPermission(PERMISSIONS_REQUIRED);
        if (permissionsGranted) {
            //Toast.makeText(this, "You've granted all required permissions!", Toast.LENGTH_SHORT).show();
        } else {
            boolean showRationale = true;
            for (String permission : PERMISSIONS_REQUIRED) {
                showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                if (!showRationale) {
                    break;
                }
            }

            //Ask for the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS1);
            //Toast.makeText(this, "Please give permission", Toast.LENGTH_SHORT).show();


        }
    }
}
