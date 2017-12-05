package com.kuliah.yogy.survey;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kuliah.yogy.survey.app.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final Context context = this;
    Button btnLogout;
    SharedPreferences sharedpreferences;
    String id, username;
    ImageView ivPuas, ivBiasaSaja, ivTidakPuas;
    ProgressDialog pDialog;
    Intent intent;

    int success;
    ConnectivityManager conMgr;

    private String url = Server.URL + "survey.php";

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    String tag_json_obj = "json_obj_req";

    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        btnLogout = (Button) findViewById(R.id.btn_logout);

        ivPuas = (ImageView) findViewById(R.id.iv_gembira);
        ivBiasaSaja = (ImageView) findViewById(R.id.iv_senyum);
        ivTidakPuas = (ImageView) findViewById(R.id.iv_sedih);

        sharedpreferences = getSharedPreferences(Login.my_shared_preferences, Context.MODE_PRIVATE);

        id = getIntent().getStringExtra(TAG_ID);
        username = getIntent().getStringExtra(TAG_USERNAME);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        ivPuas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pilihEkspresi("1");
            }
        });

        ivBiasaSaja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pilihEkspresi("2");
            }
        });

        ivTidakPuas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pilihEkspresi("3");
            }
        });


    }

    private void pilihEkspresi(final String ekspresi) {
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.prompt_saran, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(promptsView);
        final EditText et_sarankritik = (EditText) promptsView
                .findViewById(R.id.et_sarankritik);

        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id_dialog) {
                                String id_ekspresi = ekspresi;
                                String sarankritik = et_sarankritik.getText().toString();
                                String createdby = id;

                                conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                                if (conMgr.getActiveNetworkInfo() != null
                                        && conMgr.getActiveNetworkInfo().isAvailable()
                                        && conMgr.getActiveNetworkInfo().isConnected()) {
                                    inputSurvey(id_ekspresi, sarankritik, createdby);
                                } else {
                                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void inputSurvey(final String id_ekspresi, final String sarankritik, final String createdby) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Loading ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Survey Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {

                        Log.e("Successfully Survey!", jObj.toString());

                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_ekspresi", id_ekspresi);
                params.put("sarankritik", sarankritik);
                params.put("createdby", createdby);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getmInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(Login.session_status, false);
        editor.putString(TAG_ID, null);
        editor.putString(TAG_USERNAME, null);
        editor.commit();

        Intent intent = new Intent(MainActivity.this, Login.class);
        finish();
        startActivity(intent);
    }
}
