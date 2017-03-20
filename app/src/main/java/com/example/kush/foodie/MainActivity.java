package com.example.kush.foodie;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.input.image.ClarifaiImage;
import okhttp3.OkHttpClient;

public class MainActivity extends AppCompatActivity {

    public Uri imageUrl;
    public Uri videoUrl;


    class test extends AsyncTask<Void, Void, Void>{

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Void doInBackground(Void... params) {
            Log.e("kjsndksdjksadsadsa","hjsadksakjdjgsd878213182387213y21u321hu3iih");
            ClarifaiClient clarifai = new ClarifaiBuilder("JsZUubSQp81Ni1rf9VSlWP__visATvicKk_S9Fp9", "bBf3BcHGtl1nVC2-cnkycsBNlvD7LMuSBZwg0n11")
                    .client(new OkHttpClient()) // OPTIONAL. Allows customization of OkHttp by the user
                    .buildSync();// or use .build() to get a Future<ClarifaiClient>

            // if a Client is registered as a default instance, it will be used
            // automatically, without the user having to keep it around as a field.
            // This can be omitted if you want to manually manage your instance
            //.registerAsDefaultInstance();

            ClarifaiResponse response = clarifai.getDefaultModels().generalModel().predict()
                    .withInputs(
                            ClarifaiInput.forImage(ClarifaiImage.of(imageUrl.toString()))
                    )
                    .executeSync();
            JSONObject responseJSON = null;
            System.out.println(response.rawBody());
            System.out.println(response.rawBody());
            System.out.println(response.rawBody());
            System.out.println(response.rawBody());
            try {
                responseJSON= new JSONObject(response.rawBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject output=null;
            JSONArray data=null;
            try {
                output = responseJSON.getJSONArray("outputs").getJSONObject(0);
                data = output.getJSONObject("data").getJSONArray("concepts");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                for(int i=0;i<data.length();i++){
                    JSONObject obj = data.getJSONObject(i);
                    System.out.println(obj.getString("name")+obj.getString("value"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            System.out.print("test");
            System.out.print("");
            return null;
        }
    }



    private static final int CAMERA_REQUEST = 1888;
    private static final int VIDEO_REQUEST = 2000;
    private static final int AUDIO_REQUEST = 6000;

    private ImageView imageView;
    private VideoView videoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)this.findViewById(R.id.iv);
        videoView = (VideoView)findViewById(R.id.videoView);
        Button photoButton = (Button)findViewById(R.id.b3);

        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

    }

    public void click(View v){

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, VIDEO_REQUEST);
        }

    }

    public void imageupload ( Intent data){

        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bitOut = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100 , bitOut);
        byte[] dataBAOS = bitOut.toByteArray();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imagesRef = storageRef.child("photo");
        UploadTask uploadTask = imagesRef.putBytes(dataBAOS);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Sending failed", Toast.LENGTH_SHORT).show();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageUrl = taskSnapshot.getDownloadUrl();
                Log.e("uri :-",""+imageUrl);
                new test().execute();

            }
        });



    }

    public void promptSpeechToText(View view){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("hi","IN"));
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say Your Complaint");
        try{
            startActivityForResult(i,AUDIO_REQUEST);
        }catch (ActivityNotFoundException a){
            Toast.makeText(this,"Sorry Speech Recognition Not Supported",Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            imageupload(data);
        }
        if(requestCode == VIDEO_REQUEST && resultCode == Activity.RESULT_OK){


            Uri videoUri = data.getData();
            videoView.setVisibility(View.VISIBLE);
            videoView.setVideoURI(videoUri);
            videoView.start();
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference vid = storageReference.child("vid");
            UploadTask uploadTask = vid.putFile(videoUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"sorry",Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    videoUrl = taskSnapshot.getDownloadUrl();
                    Log.e("downString :-",videoUrl.toString());
                    Toast.makeText(getApplicationContext(),"Won",Toast.LENGTH_SHORT).show();
                    new test().execute();
                }
            });


        }
        if(requestCode == AUDIO_REQUEST && resultCode == Activity.RESULT_OK){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            Log.e("final result",result.get(0)); // getting output of string
        }
    }

}
