package com.sem1hh.crguessgame;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imgCard;
    private TextView tvCardName, tvResult;
    private EditText etGuess;
    private Button btnGuess;

    private List<ClashCard> cardList = new ArrayList<>();
    private ClashCard currentCard;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private long lastUpdate;
    private static final int SHAKE_THRESHOLD = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgCard = findViewById(R.id.imgCard);
        tvCardName = findViewById(R.id.tvCardName);
        tvResult = findViewById(R.id.tvResult);
        etGuess = findViewById(R.id.etGuess);
        btnGuess = findViewById(R.id.btnGuess);

        setupSensors();
        fetchCardsFromApi(); //

        btnGuess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }

    private void fetchCardsFromApi() {
        String GIST_URL = "https://gist.githubusercontent.com/sem1hhh/68fd7f002c33f3802105a72e7bf91e00/raw/53679551eca6430796ab2fa1e37bd9fe02735098/gistfile1.txt";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ClashApi api = retrofit.create(ClashApi.class);

        api.getCards(GIST_URL).enqueue(new Callback<List<ClashCard>>() {
            @Override
            public void onResponse(Call<List<ClashCard>> call, Response<List<ClashCard>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cardList = response.body();
                    showRandomCard();
                } else {
                    tvCardName.setText("Error Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ClashCard>> call, Throwable t) {
                tvCardName.setText("Check Connection.: " + t.getMessage());
            }
        });
    }

    private void showRandomCard() {
        if (cardList.isEmpty()) return;

        Random random = new Random();
        ClashCard nextCard;

        do {
            int index = random.nextInt(cardList.size());
            nextCard = cardList.get(index);
        } while (currentCard != null && nextCard.getName().equals(currentCard.getName()));

        currentCard = nextCard;
        updateUI();
    }
    private void updateUI() {
        tvCardName.setText(currentCard.getName());
        tvResult.setText("");
        etGuess.setText("");

        Glide.with(this)
                .load(currentCard.getIconUrl())
                .into(imgCard);
    }

    private void checkAnswer() {
        String guessText = etGuess.getText().toString();
        if (guessText.isEmpty()) {
            Toast.makeText(this, "Enter Number", Toast.LENGTH_SHORT).show();
            return;
        }

        int guess = Integer.parseInt(guessText);

        if (guess == currentCard.getElixir()) {
            tvResult.setText("TRUE!");
            tvResult.setTextColor(Color.GREEN);
            imgCard.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showRandomCard();
                }
            }, 1000);
        } else {
            tvResult.setText("NO,FALSE!");
            tvResult.setTextColor(Color.RED);
        }
    }

    private void setupSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    Toast.makeText(this, "You Passed Card!", Toast.LENGTH_SHORT).show();
                    showRandomCard();
                }
                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
    }
}