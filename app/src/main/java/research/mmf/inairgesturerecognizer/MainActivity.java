package research.mmf.inairgesturerecognizer;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import research.mmf.gesturelib.*;

/***
 * Demo Application
 * Author: Mingming Fan
 * Contact: fmmbupt@gmail.com
 * Contact the author for use of the code
 * */
public class MainActivity extends Activity {

    private TextView textView_gesture_name;
    private Button bt_start_recognition;

    private Boolean recording_sample_gestures = true;

    private SensorManager mSensorManager;
    private DTWGestureRecognition Recognizer;

    private ArrayList<ArrayList<AccData>> templates = new ArrayList<ArrayList<AccData>>();
    private ArrayList<String> gesture_names = new ArrayList<String>();
    private ArrayList<AccData> SensorData =  new ArrayList<AccData>();   //gesture data that is to be recognized
    private int gesture_id = 0;
    private Boolean StoreSensorData = false;
    private ArrayAdapter arrayAdapter;

//    private SharedPreferences sharedPreferences = getSharedPreferences("preferences", 0);
//    private SharedPreferences.Editor editor = sharedPreferences.edit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_gesture_name = (TextView)findViewById(R.id.editText_gesture_name);

        bt_start_recognition = (Button)findViewById(R.id.button_record_gesture);

        bt_start_recognition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                View textView = findViewById(R.id.textView);
                View textView3 = findViewById(R.id.textView3);
                TextView title = (TextView)findViewById(R.id.Title);
                View divider = findViewById(R.id.Divider);
                View gestureList = findViewById(R.id.gesture_list);

                if(recording_sample_gestures)
                {
                    bt_start_recognition.setText("Click to recognize gestures");
                    title.setText("Identify Gestures");
                    textView.setVisibility(View.INVISIBLE);
                    textView3.setVisibility(View.INVISIBLE);
                    divider.setVisibility(View.INVISIBLE);
                    gestureList.setVisibility(View.INVISIBLE);
                    textView_gesture_name.setVisibility(View.INVISIBLE);

                }
                else
                {
                    bt_start_recognition.setText("Click to enter new templates");
                    title.setText("Enter Gestures");
                    textView.setVisibility(View.VISIBLE);
                    textView3.setVisibility(View.VISIBLE);
                    divider.setVisibility(View.VISIBLE);
                    gestureList.setVisibility(View.VISIBLE);
                    textView_gesture_name.setVisibility(View.VISIBLE);
                }
                recording_sample_gestures = !recording_sample_gestures;
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.registerListener(acc_listener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                mSensorManager.SENSOR_DELAY_FASTEST);

        Recognizer = new DTWGestureRecognition();

//        System.out.println(sharedPreferences.getStringSet("gestures", null));
//        System.out.println(sharedPreferences.getStringSet("template_1", null));

        final ListView listview = (ListView) findViewById(R.id.gesture_list);
        arrayAdapter = new ArrayAdapter(this, R.layout.list_item, R.id.label, gesture_names);
        listview.setAdapter(arrayAdapter);
    }

//    public void saveGestures() {
//        try {
//            // Remove everything in SharedPreferences
//            editor.clear();
//
//            Set<String> gestures_set = new HashSet<String>();
//            gestures_set.addAll(gesture_names);
//            editor.putStringSet("gestures", gestures_set);
//
//            int counter = 0;
//            for (ArrayList<AccData> template: templates) {
//                System.out.println(template);
//                Set<String> template_set = new HashSet<String>();
//
//                for (AccData accdata: template) {
//                    template_set.add(accdata.asString());
//                }
//                editor.putStringSet("template_" + counter, template_set);
//                counter++;
//            }
//
//            editor.commit();
//        } catch(Exception e) {
//            throw e;
//        }
//    }

    private SensorEventListener acc_listener = new SensorEventListener()
    {
        public void onAccuracyChanged(Sensor sensor,int accuracy)
        {

        }

        public void onSensorChanged(SensorEvent event)
        {
            if(StoreSensorData)
            {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                AccData  data = new AccData(x,y,z);

                Recognizer.Quantization(data);

                if(recording_sample_gestures) {
                    if(templates!=null)
                        templates.get(gesture_id).add(data);
                }
                else
                {
                    SensorData.add(data);
                }
            }
        }
    };


    public boolean onTouchEvent(MotionEvent event)
    {
        System.out.println("on touch event");
        if(recording_sample_gestures)
        {
            //record the data for templates
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    StoreSensorData = true;
                    templates.add(new ArrayList<AccData>());
                    System.out.println("action down");
                    System.out.println(templates.size());
                    return false;
                case MotionEvent.ACTION_UP:
                    StoreSensorData = false;
                    String gesture = textView_gesture_name.getText().toString();
                    gesture_names.add(gesture);
                    gesture_id++;
                    Toast.makeText(getApplicationContext(), "Entered gesture template for " + gesture, Toast.LENGTH_SHORT).show();
                    System.out.println("action up");
                    System.out.println(templates.size());
                    arrayAdapter.notifyDataSetChanged();
                    return false;
                default:
                    break;
            }
        }
        else
        {
            //record the data for recognition
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    StoreSensorData = true;
                    return false;
                case MotionEvent.ACTION_UP:
                    StoreSensorData = false;
                    int WhichGesture = Recognizer.GestureRecognition(new ArrayList<ArrayList<AccData>>(templates), SensorData);
                    String gestureRecognized = gesture_names.get(WhichGesture);
                    Toast.makeText(getApplicationContext(),"It is " + gestureRecognized, Toast.LENGTH_SHORT).show();
                    SensorData.clear();
                    return false;
                default:
                    break;
            }
        }

        return super.onTouchEvent(event);
    }
}