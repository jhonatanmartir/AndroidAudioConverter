package cafe.adriel.androidaudioconverter.callback;

public interface IConvertCallback {
    
    void onSuccess(int id, String convertedFilePath);
    
    void onFailure(int id, Exception error);

}