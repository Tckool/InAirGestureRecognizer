package research.mmf.inairgesturerecognizer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class    DataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String data = intent.getStringExtra("data");
        String[] dataArray = data.split(":");
        System.out.println(data);
        System.out.println(dataArray);
        final ListView listview = (ListView) findViewById(R.id.data_list);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, dataArray);
        listview.setAdapter(arrayAdapter);

    }
}
