package research.mmf.inairgesturerecognizer;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import research.mmf.gesturelib.*;

/***
 * Demo Application
 * Author: Mingming Fan
 * Contact: fmmbupt@gmail.com
 * Contact the author for use of the code
 * */
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView textView_gesture_name;
    private Button bt_start_recognition;

    private Boolean recording_sample_gestures = true;

    private SensorManager mSensorManager;
    private DTWGestureRecognition Recognizer;

    private ArrayList<ArrayList<AccData>> templates = new ArrayList<ArrayList<AccData>>();
    private ArrayList<String> gesture_names = new ArrayList<>();
    private ArrayList<AccData> SensorData =  new ArrayList<AccData>();   //gesture data that is to be recognized
    private int gesture_id = 0;
    private Boolean StoreSensorData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_gesture_name = (TextView)findViewById(R.id.editText_gesture_name);

        bt_start_recognition = (Button)findViewById(R.id.button_record_gesture);

        bt_start_recognition.setOnClickListener(this);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        mSensorManager.registerListener(acc_listener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                mSensorManager.SENSOR_DELAY_FASTEST);

        Recognizer = new DTWGestureRecognition();
    }


    public void onClick(View v)
    {
        if(recording_sample_gestures)
        {
            bt_start_recognition.setText("Click to enter new templates");
        }
        else
        {
            bt_start_recognition.setText("Click to recognize gestures");
        }
        recording_sample_gestures = !recording_sample_gestures;
    }


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
        if(recording_sample_gestures)
        {
            //record the data for templates
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    StoreSensorData = true;
                    templates.add(new ArrayList<AccData>());
                    return false;
                case MotionEvent.ACTION_UP:
                    StoreSensorData = false;
                    String gesture = textView_gesture_name.getText().toString();
                    gesture_names.add(gesture);
                    gesture_id++;
                    Toast.makeText(getApplicationContext(),"Enter the " + gesture+ " template.", Toast.LENGTH_LONG).show();

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
                    int WhichGesture = Recognizer.GestureRecognition(templates, SensorData);
                    String gestureRecognized = gesture_names.get(WhichGesture);
                    Toast.makeText(getApplicationContext(),"It is " + gestureRecognized, Toast.LENGTH_LONG).show();
                    SensorData.clear();
                    return false;
                default:
                    break;
            }
        }

        return super.onTouchEvent(event);
    }
}
