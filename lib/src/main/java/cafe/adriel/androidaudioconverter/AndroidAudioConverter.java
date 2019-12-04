package cafe.adriel.androidaudioconverter;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;

public class AndroidAudioConverter {

    private static boolean loaded;

    private Context context;
    private File audioFile;
    private AudioFormat format;
    private IConvertCallback callback;
    private int idFile;

    private static String outFolder;

    private AndroidAudioConverter(Context context){
        this.context = context;
    }

    public static boolean isLoaded(){
        return loaded;
    }

    public static void load(Context context, final ILoadCallback callback){
        try {
            FFmpeg.getInstance(context).loadBinary(new FFmpegLoadBinaryResponseHandler() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onSuccess() {
                            loaded = true;
                            callback.onSuccess();
                        }

                        @Override
                        public void onFailure() {
                            loaded = false;
                            callback.onFailure(new Exception("Failed to loaded FFmpeg lib"));
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
        } catch (Exception e){
            loaded = false;
            callback.onFailure(e);
        }
    }

    public static AndroidAudioConverter with(Context context) {
        return new AndroidAudioConverter(context);
    }

    public AndroidAudioConverter setFile(File originalFile, int idFile) {
        this.audioFile = originalFile;
        this.idFile = idFile;
        return this;
    }

    public AndroidAudioConverter setFormat(AudioFormat format) {
        this.format = format;
        return this;
    }

    public AndroidAudioConverter setCallback(IConvertCallback callback) {
        this.callback = callback;
        return this;
    }

    public AndroidAudioConverter setOutFolder(String pathFolder) {
        outFolder = pathFolder;
        return this;
    }

    public void convert() {
        if(!isLoaded()){
            callback.onFailure(this.idFile, new Exception("FFmpeg not loaded"));
            return;
        }
        if(audioFile == null || !audioFile.exists()){
            callback.onFailure(this.idFile, new IOException("File not exists"));
            return;
        }
        if(!audioFile.canRead()){
            callback.onFailure(this.idFile, new IOException("Can't read the file. Missing permission?"));
            return;
        }

        final File convertedFile = getConvertedFile(audioFile, format);
        final String[] cmd = new String[]{"-y", "-i", audioFile.getPath(), convertedFile.getPath()};

        try {
            FFmpeg.getInstance(context).execute(cmd, new FFmpegExecuteResponseHandler() {
                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onProgress(String message) {

                        }

                        @Override
                        public void onSuccess(String message) {
                            callback.onSuccess(idFile, convertedFile.getPath());
                        }

                        @Override
                        public void onFailure(String message) {
                            callback.onFailure(idFile, new IOException(message));
                        }

                        @Override
                        public void onFinish() {

                        }
                    });
        } catch (Exception e){
            callback.onFailure(this.idFile, e);
        }
    }

    private static File getConvertedFile(File originalFile, AudioFormat format){
        String[] f = originalFile.getPath().split("\\.");
        String filePath;

        if (outFolder == null) {
            filePath = originalFile.getPath().replace(f[f.length - 1], format.getFormat());
            return new File(filePath);
        } else {
            filePath = originalFile.getPath();
            String filename = filePath.substring(filePath.lastIndexOf("/")+1, filePath.length());
            filePath = outFolder + "/"+filename.replace(f[f.length - 1], format.getFormat());
        }

        return new File(filePath);
    }
}