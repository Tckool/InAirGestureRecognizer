package research.mmf.inairgesturerecognizer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import research.mmf.gesturelib.*;

/***
 * Demo Application
 * Author: Mingming Fan
 * Contact: fmmbupt@gmail.com
 * Contact the author for use of the code
 */
public class MainActivity extends Activity implements View.OnClickListener {

    private TextView textView_gesture_name;
    private Button bt_start_recognition;
    private Button save_gestures;
    private Button clear_gestures;
    private Button startButton;
    private Button dataButton;

    private Boolean recording_sample_gestures = false;

    private SensorManager mSensorManager;
    private DTWGestureRecognition Recognizer;

    private ArrayList<ArrayList<AccData>> templates = new ArrayList<ArrayList<AccData>>();
    private ArrayList<String> gesture_names = new ArrayList<String>();
    private ArrayList<AccData> SensorData = new ArrayList<AccData>();   //gesture data that is to be recognized
    private int gesture_id;
    private Boolean StoreSensorData = false;
    private ArrayAdapter arrayAdapter;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private AlertDialog alertDialog;
    private int counter;
    private final String[][] apps = {{"Facebook", "Youtube", "Maps", "Facebook", "Maps", "Youtube"}, {"Maps", "Facebook", "Youtube", "Youtube", "Maps", "Facebook"}};
    private long startTime;
    private String target = "";
    private String dataString;
    private int errors = 0;
    private boolean experimentStarted;
    private int trialCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_gesture_name = (TextView) findViewById(R.id.editText_gesture_name);
        bt_start_recognition = (Button) findViewById(R.id.button_record_gesture);
        save_gestures = (Button) findViewById(R.id.save_gestures);
        clear_gestures = (Button) findViewById(R.id.clear_gestures);
        startButton = (Button) findViewById(R.id.start_button);
        dataButton = (Button) findViewById(R.id.showData);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.registerListener(acc_listener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                mSensorManager.SENSOR_DELAY_FASTEST);

        Recognizer = new DTWGestureRecognition();

        loadStoredData();
        addListeners();

        final ListView listview = (ListView) findViewById(R.id.gesture_list);
        arrayAdapter = new ArrayAdapter(this, R.layout.list_item, R.id.label, gesture_names);
        listview.setAdapter(arrayAdapter);

        alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        // Clear data
//        try {
//            new OutputStreamWriter(openFileOutput("data.txt", MODE_PRIVATE));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Load saved gestures from shared preferences
     */
    private void loadStoredData() {
        sharedPreferences = getSharedPreferences("preferences", 0);
        editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String gestures = sharedPreferences.getString("gestures", null);
        String templates = sharedPreferences.getString("templates", null);
        if (gestures == null) {
            gesture_id = 0;
            return;
        }

        this.gesture_names = gson.fromJson(gestures, new TypeToken<ArrayList<String>>() {
        }.getType());
        this.templates = gson.fromJson(templates, new TypeToken<ArrayList<ArrayList<AccData>>>() {
        }.getType());
        gesture_id = this.gesture_names.size();
    }

    private SensorEventListener acc_listener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void onSensorChanged(SensorEvent event) {
            if (StoreSensorData) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                AccData data = new AccData(x, y, z);

                Recognizer.Quantization(data);

                if (recording_sample_gestures) {
                    if (templates != null)
                        templates.get(gesture_id).add(data);
                } else {
                    SensorData.add(data);
                }
            }
        }
    };


    public boolean onTouchEvent(MotionEvent event) {
        if (recording_sample_gestures) {
            //record the data for templates
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    StoreSensorData = true;
                    templates.add(new ArrayList<AccData>());
                    return false;
                case MotionEvent.ACTION_UP:
                    StoreSensorData = false;
                    String gesture = textView_gesture_name.getText().toString();
                    gesture_names.add(gesture);
                    gesture_id++;
                    Toast.makeText(getApplicationContext(), "Entered gesture template for " + gesture, Toast.LENGTH_SHORT).show();
                    arrayAdapter.notifyDataSetChanged();
                    return false;
                default:
                    break;
            }
        } else {
            //record the data for recognition
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    StoreSensorData = true;
                    return false;
                case MotionEvent.ACTION_UP:
                    StoreSensorData = false;
                    int WhichGesture = Recognizer.GestureRecognition(new ArrayList<ArrayList<AccData>>(templates), SensorData);
                    String gestureRecognized = gesture_names.get(WhichGesture);

                    Toast.makeText(getApplicationContext(), "It is " + gestureRecognized, Toast.LENGTH_SHORT).show();
                    SensorData.clear();

                    if (gestureRecognized.equals(target)) {
                        foundApp(target);
                    } else {
                        errors++;
                    }

                    return false;
                default:
                    break;
            }
        }

        return super.onTouchEvent(event);
    }

    /**
     * Add event listeners
     */
    private void addListeners() {
        bt_start_recognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!experimentStarted) {
                    View textView = findViewById(R.id.textView);
                    TextView title = (TextView) findViewById(R.id.Title);
                    View divider = findViewById(R.id.Divider);
                    View gestureList = findViewById(R.id.gesture_list);

                    if (recording_sample_gestures) {
                        bt_start_recognition.setText("Click to enter new templates");
                        title.setText("Identify Gestures");
                        textView.setVisibility(View.INVISIBLE);
                        divider.setVisibility(View.INVISIBLE);
                        gestureList.setVisibility(View.INVISIBLE);
                        save_gestures.setVisibility(View.INVISIBLE);
                        clear_gestures.setVisibility(View.INVISIBLE);
                        textView_gesture_name.setVisibility(View.INVISIBLE);
                        dataButton.setVisibility(View.INVISIBLE);
                        startButton.setVisibility(View.VISIBLE);

                    } else {
                        bt_start_recognition.setText("Click to recognize gestures");
                        title.setText("Enter Gestures");
                        textView.setVisibility(View.VISIBLE);
                        divider.setVisibility(View.VISIBLE);
                        gestureList.setVisibility(View.VISIBLE);
                        save_gestures.setVisibility(View.VISIBLE);
                        clear_gestures.setVisibility(View.VISIBLE);
                        textView_gesture_name.setVisibility(View.VISIBLE);
                        dataButton.setVisibility(View.VISIBLE);
                        startButton.setVisibility(View.INVISIBLE);

                    }
                    recording_sample_gestures = !recording_sample_gestures;
                }
            }
        });

        save_gestures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Remove everything in SharedPreferences
                    editor.clear();
                    Gson gson = new Gson();
                    editor.putString("templates", gson.toJson(templates));
                    editor.putString("gestures", gson.toJson(gesture_names));

                    editor.commit();
                    Toast.makeText(getApplicationContext(), "Gestures Saved!", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    throw e;
                }
            }
        });

        clear_gestures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Remove everything in SharedPreferences
                    editor.clear();
                    editor.commit();
                    gesture_names.clear();
                    templates.clear();
                    gesture_id = 0;
                    arrayAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "Deleted all gestures", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    throw e;
                }
            }
        });

        startButton.setOnClickListener(this);

//        dataButton.setOnClickListener();
    }


    private void startTrial(final String target) {
        // delay
        final Handler handler = new Handler();
        errors = 0;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findNext(target);
            }
        }, 5000);
    }

    private void showMessage(String message) {
        alertDialog.setMessage(message);
        alertDialog.show();
    }

    private void findNext(final String app) {
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        target = app;
                        dialog.dismiss();
                        startTime = System.currentTimeMillis();
                    }
                });
        alertDialog.setMessage("Please perform the gesture to open " + app + " once you close this message");
        alertDialog.show();
    }

    private void foundApp(String target) {
        long stopTime = System.currentTimeMillis() - startTime;
        counter++;
        dataString += target.charAt(0) + ": " + stopTime + " E: " + errors + " ";
        if (counter >= apps[trialCounter].length) {
            System.out.println(dataString);
            saveData(dataString + "&&");
            trialCounter++;
            target = "";
            if (trialCounter >= apps.length) {
                showMessage("Experiment completed.");
                experimentStarted = false;
            } else {
                AlertDialog newAlert = new AlertDialog.Builder(MainActivity.this).create();
                newAlert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startExperiment();
                        }
                    });
                newAlert.setMessage("Part 1 complete. Please await instructions from the experimenter");
                newAlert.show();
            }
            return;
        }
        showMessage("Opened " + target);
        target = "";
        startTrial(apps[trialCounter][counter]);
    }

    private void startExperiment() {
        System.out.println("experiment started");
        counter = 0;
        dataString = "";
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    startTrial(apps[trialCounter][counter]);
                }
            });
        alertDialog.setMessage("In Air Gestures Experiment will begin after you close this popup.");
        alertDialog.show();
    }

    @Override
    public void onClick(View view) {
        if (!experimentStarted) {
            experimentStarted = true;
            trialCounter = 0;
            startExperiment();
        }
    }

    private void saveData(String data) {
        //        From http://stackoverflow.com/questions/3572463/what-is-context-on-android
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("data.txt", MODE_APPEND));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readData() {
        String ret = "";
        try {
            InputStream inputStream = openFileInput("data.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("Exception", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("Exception", "Can not read file: " + e.toString());
        }

        return ret;
    }

    public void showData(View view) {
        if (!experimentStarted) {
            Intent intent = new Intent(this, DataActivity.class);
            intent.putExtra("data", readData());
            startActivity(intent);
        }
    }

}