package com.example.nuerosiren;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class SpeechClassifier {
    private static final String TAG = "SpeechClassifier";
    private Interpreter interpreter;
    private int[] inputShape;

    public SpeechClassifier(Context context) throws IOException {
        try {
            interpreter = new Interpreter(loadModelFile(context));
            inputShape = interpreter.getInputTensor(0).shape();
            Log.d(TAG, "Model loaded successfully. Expected input shape: " + Arrays.toString(inputShape));
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize TFLite model: " + e.getMessage());
            throw new IOException("Model initialization failed", e);
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd("dysarthria_cnn_model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    public float classify(float[][][][] inputFeatures) {
        if (interpreter == null) return 0f;
        
        float[][] output = new float[1][1];
        try {
            interpreter.run(inputFeatures, output);
            return output[0][0]; // Risk probability (Single output)
        } catch (Exception e) {
            Log.e(TAG, "Inference failed: " + e.getMessage());
            return 0f;
        }
    }

    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }
}
