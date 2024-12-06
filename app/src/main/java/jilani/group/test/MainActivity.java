package jilani.group.test;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private int shakeCount = 0;
    private Button playButton;
    private Button playButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = (Button) findViewById(R.id.playButton);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicActivity.class);
                startActivity(intent);
            }
        });

        playButton2 = (Button) findViewById(R.id.playButton2);

        playButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicIntoActivity.class);
                startActivity(intent);
            }
        });

        // Initialiser le gestionnaire de capteurs
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enregistrer le capteur d'accéléromètre
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Désenregistrer le capteur pour économiser de l'énergie
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];  // Accélération sur l'axe X
            float y = event.values[1];  // Accélération sur l'axe Y
            float z = event.values[2];  // Accélération sur l'axe Z

            // Calculer la magnitude de l'accélération pour détecter une secousse
            double acceleration = Math.sqrt(x * x + y * y + z * z);
            long currentTime = System.currentTimeMillis();

            // Si l'accélération est suffisamment élevée, on détecte une secousse
            if (acceleration > 12) { // Le seuil d'accélération dépend de ton test (tu peux ajuster cette valeur)
                if (currentTime - lastShakeTime < 500) { // Si les secousses sont rapprochées (moins de 500 ms)
                    shakeCount++;
                    if (shakeCount == 2) { // Si deux secousses ont été détectées
                        startMusicApp(); // Lancer l'application de musique
                        shakeCount = 0; // Réinitialiser le compteur de secousses
                    }
                } else {
                    shakeCount = 0; // Réinitialiser si la secousse est trop lente
                }
                lastShakeTime = currentTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Gérer les changements de précision si nécessaire
    }

    private void startMusicApp() {
        // Créer un Intent pour ouvrir l'application de musique par défaut
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "audio/*");

        // Vérifier si une application peut gérer cet Intent
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent); // Lancer l'application de musique par défaut
        }
    }
}






/*
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private androidx.gridlayout.widget.GridLayout gridLayout; // Assure-toi de déclarer GridLayout avec 'androidx.gridlayout.widget.GridLayout'
    private TextView[][] textViews = new TextView[5][5]; // Tableau des TextViews
    private int currentRow = 2;  // Position de départ de la case verte (index 2,2)
    private int currentColumn = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser le GridLayout avec 5x5 cellules
        gridLayout = findViewById(R.id.gridLayout);
        gridLayout.setRowCount(5);
        gridLayout.setColumnCount(5);

        // Initialiser la grille de TextViews avec des indices de case
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                TextView textView = new TextView(this);
                textView.setText(String.format("(%d, %d)", row, col)); // Afficher l'index de chaque case
                textView.setTextSize(18);
                textView.setPadding(16, 16, 16, 16);
                textView.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light)); // Couleur bleue par défaut
                gridLayout.addView(textView);
                textViews[row][col] = textView;
            }
        }

        // Initialiser la case verte au centre (2,2)
        textViews[2][2].setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

        // Initialiser le gestionnaire de capteurs
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Enregistrer le capteur d'accéléromètre
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Désenregistrer le capteur pour économiser de l'énergie
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];  // Accélération sur l'axe X
            float y = event.values[1];  // Accélération sur l'axe Y

            // Calculer les deltas pour déplacer la case verte
            int deltaRow = (int) (y / 5);  // Diviser par 5 pour réduire la sensibilité
            int deltaColumn = (int) (x / 5);

            // Calculer la nouvelle position de la case verte
            int newRow = currentRow - deltaRow;
            int newColumn = currentColumn + deltaColumn;

            // Vérifier que la nouvelle position est dans la grille (5x5)
            newRow = Math.max(0, Math.min(4, newRow));
            newColumn = Math.max(0, Math.min(4, newColumn));

            // Si la position change, mettre à jour la case verte
            if (newRow != currentRow || newColumn != currentColumn) {
                // Réinitialiser la couleur de la case précédente
                textViews[currentRow][currentColumn].setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));

                // Mettre à jour la nouvelle case verte
                textViews[newRow][newColumn].setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));

                // Mettre à jour la position actuelle
                currentRow = newRow;
                currentColumn = newColumn;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Gérer les changements de précision si nécessaire
    }
}
*/