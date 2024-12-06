package jilani.group.test;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MusicActivity extends AppCompatActivity {

    private static final String TAG = "MusicActivity";

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private Button playButton, stopButton, chooseFolderButton, nextButton, prevButton;
    private SeekBar volumeSeekBar;
    private ListView audioListView;

    private List<File> audioFiles = new ArrayList<>();
    private int currentTrackIndex = -1;
    private ArrayAdapter<String> audioListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // Initialisation des éléments de l'interface
        chooseFolderButton = findViewById(R.id.chooseFolderButton);
        playButton = findViewById(R.id.playButton);
        stopButton = findViewById(R.id.stopButton);
        nextButton = findViewById(R.id.nextButton);
        prevButton = findViewById(R.id.prevButton);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        audioListView = findViewById(R.id.audioListView);

        // Désactiver les boutons au départ
        playButton.setEnabled(false);
        stopButton.setEnabled(false);
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);

        // Configurer l'adaptateur pour la liste des audios
        audioListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        audioListView.setAdapter(audioListAdapter);

        // Bouton pour choisir un répertoire
        chooseFolderButton.setOnClickListener(v -> chooseAudioFolder());

        // Bouton Play
        playButton.setOnClickListener(v -> playCurrentTrack());

        // Bouton Stop
        stopButton.setOnClickListener(v -> stopAudio());

        // Boutons Next et Previous
        nextButton.setOnClickListener(v -> playNextTrack());
        prevButton.setOnClickListener(v -> playPreviousTrack());

        // Volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        volumeSeekBar.setMax(maxVolume);
        volumeSeekBar.setProgress(currentVolume);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Lecture depuis la liste
        audioListView.setOnItemClickListener((parent, view, position, id) -> {
            currentTrackIndex = position;
            playCurrentTrack();
        });
    }

    // Choisir un répertoire
    private void chooseAudioFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        chooseFolderLauncher.launch(intent);
    }

    // Gestion du résultat de la sélection de répertoire
    private final ActivityResultLauncher<Intent> chooseFolderLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri treeUri = result.getData().getData();
                    if (treeUri != null) {
                        loadAudioFilesFromFolder(treeUri);
                    }
                }
            });

    // Charger les fichiers audio depuis un répertoire
    private void loadAudioFilesFromFolder(@NonNull Uri folderUri) {
        audioFiles.clear(); // Vider la liste actuelle
        File folder = new File(DocumentsContract.getDocumentId(folderUri).split(":")[1]);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));
            if (files != null) {
                Collections.addAll(audioFiles, files);
                for (File file : files) {
                    audioListAdapter.add(file.getName());
                }
                audioListAdapter.notifyDataSetChanged();
                Toast.makeText(this, "Loaded " + audioFiles.size() + " audio files", Toast.LENGTH_SHORT).show();
            }
        }

        if (!audioFiles.isEmpty()) {
            currentTrackIndex = 0; // Sélectionner la première piste
            playButton.setEnabled(true);
            nextButton.setEnabled(audioFiles.size() > 1);
            prevButton.setEnabled(audioFiles.size() > 1);
        } else {
            Toast.makeText(this, "No audio files found", Toast.LENGTH_SHORT).show();
        }
    }

    // Jouer la piste actuelle
    private void playCurrentTrack() {
        if (currentTrackIndex >= 0 && currentTrackIndex < audioFiles.size()) {
            playAudio(Uri.fromFile(audioFiles.get(currentTrackIndex)));
        }
    }

    // Jouer l'audio
    private void playAudio(@NonNull Uri audioUri) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(this, audioUri);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                playButton.setEnabled(false);
                stopButton.setEnabled(true);
                mediaPlayer.setOnCompletionListener(mp -> {
                    playButton.setEnabled(true);
                    stopButton.setEnabled(false);
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing audio", e);
        }
    }

    // Arrêter l'audio
    private void stopAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        playButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    // Jouer la piste suivante
    private void playNextTrack() {
        if (currentTrackIndex < audioFiles.size() - 1) {
            currentTrackIndex++;
            playCurrentTrack();
        }
    }

    // Jouer la piste précédente
    private void playPreviousTrack() {
        if (currentTrackIndex > 0) {
            currentTrackIndex--;
            playCurrentTrack();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}
